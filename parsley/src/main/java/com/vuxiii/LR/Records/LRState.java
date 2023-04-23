package com.vuxiii.LR.Records;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vuxiii.Utils.*;


public class LRState implements Serializable {
    public static int count = 0;
    

    public final int id;
    public Map<NonTerminal, List<LRRule> > rules = new HashMap<>();
    public Set<LRRule> containedRules = new HashSet<>();
    public Set<LRRuleIdentifier> containedIDRules = new HashSet<>();
    // List<Integer> dot = new ArrayList<>();

    public Map<Term, LRState> move_to_state = new HashMap<>();

    public LRState() {
        id = count++;
    }



    public void add( NonTerminal X, LRRule r) {
        containedRules.add( r );
        containedIDRules.add( new LRRuleIdentifier( r.id, r.dot ) );
        containedRules.add( r );
        move_to_state.put( r.get_term(r.dot), null );
        if ( rules.containsKey( X ) )
            rules.get( X ).add( r );
        else
            rules.put( X, Utils.toList( r ) );
    }
    public List<LRRule> get_rule( NonTerminal N ) { return rules.get( N ); }

    // int get_dot( int i ) { return dot.get( i ); }

    int size() { return rules.size(); }

    public List<Term> getMoves() {
        return Utils.toList( move_to_state.keySet() );
    }

    public String toString() {
        String s = "";

        TablePrinter tp = new TablePrinter();

        tp.addTitle( "State: " + id );
        tp.push( new String[] { "Rules", "Lookahead", "Goto State" } );

        List<String[]> out = new ArrayList<>();

        for ( NonTerminal X : rules.keySet() ) {
            for ( LRRule r : rules.get( X ) ) {
                s += X + " -> ";
                for ( int i = 0; i < r.size(); ++i ) {
                    Term t = r.terms.get( i );
    
                    if ( r.dot == i ) s += ".";
                    s += t;

                } 
                if ( r.dot == r.size() ) s += ".";
                LRState to = move_to_state.get( r.get_dot_item() );
                // tp.push( new String[] { s, r.lookahead.toString(), to == null ? "None" : r.get_dot_item() + ": " + to.id  } );
                // s += "  \t:-> " + r.lookahead + "\n";
                out.add( new String[] {r.id + "", s, r.lookahead.toString(), to == null ? "None" : r.get_dot_item() + ": " + to.id  } );
                s = "";
                
            }    
        }

        out.sort( Comparator.comparing( ss -> Integer.parseInt( ss[0] ) ) );
        // Remove the id field, we dont want to print that.
        out.stream().map( ss -> {         
            String[] o = new String[ss.length - 1];
            for ( int i = 0; i < o.length; ++i )
                o[i] = ss[i+1]; 
            return o;
        }).forEach( ss -> tp.push( ss ) );

        return tp.compute();
    }
}
