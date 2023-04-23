package com.vuxiii.LR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.vuxiii.LR.Records.LRRule;
import com.vuxiii.LR.Records.LRState;
import com.vuxiii.LR.Records.NonTerminal;
import com.vuxiii.LR.Records.Rule;
import com.vuxiii.LR.Records.Term;
import com.vuxiii.LR.Records.Terminal;
import com.vuxiii.LR.Records.ASTToken;
import com.vuxiii.Utils.Utils;




public class Grammar implements Serializable {
    
    Map<LRRule, Set<Term>> first_cache = new HashMap<>();
    Set<NonTerminal> nulls_cache = new HashSet<>();

    Map< NonTerminal, List<LRRule> > LRRules;
    // Map<String, Term> terms;
    List<LRState> state_cache;

    Map< Integer, Function<List<ASTToken>, ASTToken> > reduceFunctions;

    public Grammar() {
        LRRules = new HashMap<>();
        // terms = new HashMap<>();
        state_cache = new ArrayList<>();
        reduceFunctions = new HashMap<>();
    }

    public void cache( LRState state ) {
        if ( state != null )
            state_cache.add( state );
    }

    // public int size() {
    //     return terms.keySet().size();
    // }

    public LRState checkCache( List<LRRule> rules ) {
        for ( LRState state : state_cache ) {
            List<LRRule> li = new ArrayList<>();
            li.addAll( state.containedRules );

            if ( li.containsAll( rules ) ) return state;    
        }
        return null;
    }

    public List<NonTerminal> nonTerms() {
        List<NonTerminal> li = new ArrayList<>();

        li.addAll( LRRules.keySet() );

        return li;
    }

    public Set<Terminal> terms() {
        Set<Terminal> li = new HashSet<>();

        for ( NonTerminal N : LRRules.keySet() ) 
            for ( Rule r : LRRules.get(N) ) 
                for ( Term t : r.terms )
                    if ( t instanceof Terminal ) 
                        li.add( (Terminal) t );

        return li;
    }

    private LRRule add_rule( NonTerminal key, List<Term> rule ) {
        LRRule r = new LRRule( key, Utils.toList( rule ), Utils.toSet() );
        LRRules.merge( key, Utils.toList( r ), (o, n) -> { o.addAll( n ); return o; } );
        return r;
    }

    public List<LRRule> get_rule( NonTerminal key ) {
        return LRRules.get( key ).stream().map( r -> r.copy() ).collect( Collectors.toList() );
    }

    private Set<NonTerminal> _get_nulls() { // do this once, return cached.
        if ( !nulls_cache.isEmpty() ) return nulls_cache;

        int size;
        do {
            size = nulls_cache.size();

            for ( NonTerminal N : LRRules.keySet() ) {
                List<LRRule> rules_for_N = get_rule( N );
                for ( LRRule rule : rules_for_N ) {
                    int i = 0;

                    // Removes all the first nulls_cache.
                    while ( i < rule.size() && ((rule.terms.get( i ) instanceof Terminal && ((Terminal) rule.terms.get( i )).is_epsilon)
                            || nulls_cache.contains( rule.terms.get( i ) )) ) {
                        ++i;
                    }  
                    
                    if ( i == rule.size() ) {
                        nulls_cache.add( N );
                    }
                }
            }

        } while ( size != nulls_cache.size() );
        

        return nulls_cache;
    }

    
    private Map< NonTerminal, Set<Terminal> > _get_firsts( Set<NonTerminal> nulls ) {

        Map< NonTerminal, Set<Terminal> > firsts = new HashMap<>();

        for ( NonTerminal N : LRRules.keySet() ) 
            firsts.put( N, new HashSet<>() );

        int size;
        do {
            size = firsts.values().parallelStream().map( li -> li.size() ).reduce(0, Integer::sum);
            

            // System.out.println( "-".repeat(20) );
            
            // firsts.forEach( (k, v) -> System.out.println( "First(" + k + ") = {" + v + "}" ) );

            // System.out.println( "-".repeat(20) );
            
            for ( NonTerminal N : LRRules.keySet() ) {
                for ( Rule r : LRRules.get( N ) ) {

                    int i = 0;
                    Term current = r.terms.get(i);
                    if ( current instanceof Terminal ) {
                        // Ignore epsilons.
                        if ( ((Terminal) current).is_epsilon ) continue;

                        firsts.get( N ).add( (Terminal) current );
                    } else {
                        boolean addNext = true; //current instanceof NonTerminal && 
                        while( addNext ) { // We know it is a NonTerminal from above.
                            
                            if ( current instanceof NonTerminal ){
                                firsts.get( N ).addAll( firsts.get( (NonTerminal) current ) );


                                addNext = nulls.contains( (NonTerminal) current );
                            } else {
                                
                                firsts.get( N ).add( (Terminal) current );
                                

                                addNext = false;
                            }
                            
                            i++;
                            current = i < r.size() ? r.terms.get( i ) : null;
                            addNext = addNext && current != null;
                        }   
                    }
                }
            }
        } while( size != firsts.values().parallelStream().map( li -> li.size() ).reduce(0, Integer::sum) );


        return firsts;

    }


    public Set<Term> get_firsts( LRRule rule ) {
        if ( first_cache.containsKey( rule ) ) return first_cache.get( rule );
        Set<NonTerminal> nulls = _get_nulls();
        Map< NonTerminal, Set<Terminal> > firsts = _get_firsts( nulls );
        Set<Term> ts = new HashSet<>();

        int i = rule.dot + 1;
        
        while( i < rule.size() && nulls.contains( rule.get_term( i ) ) ) {
            // System.out.println( "(1)Adding " + rule.get_term( i ) + " from " + rule.X + " -> " + rule.terms + " dot: " + rule.dot );
            ts.add( rule.get_term( i ) );
            i++;
        }
        // System.out.println( "i is " + i );
        // System.out.println( "rule size is " + rule.size() );
        if ( i < rule.size() ) {
            if ( rule.get_term( i ) instanceof NonTerminal ) {
                // System.out.println( "(2)Adding " + firsts.get( rule.get_term( i ) ) + " from " + rule.X + " -> " + rule.terms  + " dot: " + rule.dot);
                ts.addAll( firsts.get( rule.get_term( i ) ) );
            } else {
                // System.out.println( "(3)Adding " + rule.get_term( i ) + " from " + rule.X + " -> " + rule.terms  + " dot: " + rule.dot);
                ts.add( rule.get_term( i ) );
            }
        } else {
            // System.out.println( "(4)Adding " + rule.lookahead + " from " + rule.X + " -> " + rule.terms + " dot: " + rule.dot);
            // if ( rule.dot == rule.size() )
                // System.out.println( "(5)Adding " + firsts.get( rule.X ) + " from " + rule.X + " -> " + rule.terms  + " dot: " + rule.dot);
            ts.addAll( rule.lookahead );
            if ( rule.dot == rule.size() )
                ts.addAll( firsts.get( rule.X ) );
        }

        return ts;
    }

    public String toString() {
        String s = "";

        List<String[]> rules = new ArrayList<>();
        for ( NonTerminal X : LRRules.keySet() ) {
            for ( LRRule rule : LRRules.get( X ) ) {
                s = "";
                for ( Term t : rule.terms )
                    s += t;
                rules.add( new String[] { "" + rule.id, "] " + X + " -> " + s } );
            }
        }
        
        s = "";
        rules.sort( Comparator.comparing( ss -> Integer.parseInt( ss[0] ) ) );
        for ( String[] ss : rules )
            s += ss[0] + ss[1] + "\n";
        return s;
    }

    private void addReduceFunction( int ruleID, Function<List<ASTToken>, ASTToken> func ) {
        reduceFunctions.put( ruleID, func );
    }

    public Function<List<ASTToken>, ASTToken> getReduceFunction( int ruleID ) {
        return reduceFunctions.get( ruleID );
    }

    public void addRuleWithReduceFunction( NonTerminal key, List<Term> rule, SerializableLambda<List<ASTToken>, ASTToken> func ) {
        addReduceFunction( add_rule(key, rule).id, func);
    }

}
