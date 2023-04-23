package com.vuxiii.LR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vuxiii.LR.Records.LRRule;
import com.vuxiii.LR.Records.LRState;
import com.vuxiii.LR.Records.NonTerminal;
import com.vuxiii.LR.Records.ParseAccept;
import com.vuxiii.LR.Records.ParseAction;
import com.vuxiii.LR.Records.ParseError;
import com.vuxiii.LR.Records.ParseGoto;
import com.vuxiii.LR.Records.ParseReduce;
import com.vuxiii.LR.Records.ParseShift;
import com.vuxiii.LR.Records.ParserState;
import com.vuxiii.LR.Records.Rule;
import com.vuxiii.LR.Records.Term;
import com.vuxiii.LR.Records.Terminal;
import com.vuxiii.Utils.*;


public class ParseTable implements Serializable {
    public final List<ParserState> states;
    public final Grammar g; 


    private List<Map<Term, ParseAction>> getAction;

    public ParseTable( Grammar g ) {
        states = new ArrayList<>();
        this.g = g;
    }

    /**
     * This method adds the ParserState to the Table
     * @param state The state to add
     */
    public void add( ParserState state ) {
        states.add( state );
    }

    /**
     * This method returns the starting state in the Table
     * @return The starting ParserState
     */
    public ParserState getStartState() {
        return states.get( 0 );
    }

    /**
     * This method compiles the ParsingTable
     */
    public void compile() {
        states.sort( Comparator.comparing( state -> state.current_state.id ));
        getAction = new ArrayList<>();
        
        List<ParserState> states_ = Utils.toList( states );
        // states_.sort( Comparator.comparing( state -> state.current_state.id ));

        for ( ParserState state : states_ ) {
            Map<Term, ParseAction> map = new HashMap<>();
            
            Map<Term, LRState> mapper = state.current_state.move_to_state;
            
            // Find shift and goto actions.
            for ( Term term : mapper.keySet() ) {
                if ( term == Rule.EOR || term == Rule.EOP ) { }
                
                if ( mapper.get( term ) == null ) continue;
                
                int id = mapper.get( term ).id;
                if ( term instanceof NonTerminal )
                    map.put( term, new ParseGoto( id ) );
                else
                    map.put( term, new ParseShift( id ) );    
            }

            // Find reduce actions (9)
            for ( LRRule rule : state.current_state.containedRules ) {
                if ( rule.dot == rule.size() || rule.get_dot_item() instanceof Terminal && ((Terminal)rule.get_dot_item()).is_epsilon ) {
                    
                    for ( Term term : rule.lookahead ) {
                        map.put( term , new ParseReduce( rule.id, rule, g.getReduceFunction( rule.id ) ) );
                    }

                } else if ( rule.get_dot_item().name.equals( "$" ) ) {
                    map.put( rule.get_dot_item(), new ParseAccept( rule.id, rule, g.getReduceFunction( rule.id ) ) );
                } 
            }

            getAction.add( map );
        }

        // for ( int i = 0; i < getAction.size(); ++i ) {
        //     Map<Term, ParseAction> state = getAction.get( i );
        
            // System.out.println( "State: " + i ); 
            // state.forEach( (t, action) -> System.out.println( "\t" + t.toString() + "\t-> " + action.toString() ) ); 
        
        // }

    }

    /**
     * Returns the action to take given the row and term
     * @param stateIndex To row to look in the table
     * @param term The column to look in the table
     * @return The action to take given the input
     */
    public ParseAction getAction( int stateIndex, Term term ) {
        return getAction.get( stateIndex ).getOrDefault( term, new ParseError( stateIndex, getAction.get( stateIndex ).keySet().toString(), term.toString()) );
    }


    /**
     * Returns a string representation of this parsing table
     */
    public String toString() {

        List<Terminal> terms = Utils.toList( g.terms() );
        
        terms = terms.stream().filter( t -> !t.is_epsilon ).collect( Collectors.toList() );

        List<NonTerminal> nterms =  g.nonTerms();

        nterms = nterms.stream().filter( nt -> !nt.is_start ).collect( Collectors.toList() );

        Map<Term, Integer> term_to_index = new HashMap<>();


        TablePrinter tp = new TablePrinter();

        tp.addTitle( "Parse-table" );


        int size = terms.size() + nterms.size() + 1;
        String[] header = new String[ size ];
        header[0] = "State";
        
        for ( int i = 0; i < terms.size(); ++i ) {
            if ( terms.get(i) == Rule.EOP || terms.get(i) == Rule.EOR ) continue;
            int offset = i+1;
            Term t = terms.get( i );
            // System.out.println( t );
            header[offset] = t.name;
            term_to_index.put(t, offset);
        }
        for ( int i = 0; i < nterms.size(); ++i ) {
            if ( nterms.get(i) == Rule.EOP || nterms.get(i) == Rule.EOR ) continue;
            int offset = i+terms.size()+1;
            Term t = nterms.get( i );

            header[offset] = t.name;
            term_to_index.put(t, offset);
        }
        tp.push( header );
        

        
        List<ParserState> states_ = Utils.toList( states );

        for ( ParserState state : states_ ) {
            String[] row = new String[size];
            for ( int i = 0; i < row.length; ++i ) row[i] = "";
            row[0] = "" + state.current_state.id;
            
            Map<Term, LRState> mapper = state.current_state.move_to_state;
            
            // Find shift and goto actions.
            for ( Term term : mapper.keySet() ) {
                // System.out.println( "At " + term );
                if ( term == Rule.EOR || term == Rule.EOP ) { }
                
                if ( mapper.get( term ) == null ) continue;
                
                String mode = "";
                
                if ( term instanceof NonTerminal )
                    mode = "g";
                else
                    mode = "s";
                row[ term_to_index.get(term) ] += mode + mapper.get( term ).id;
            }

            // Find reduce actions (9)
            for ( LRRule rule : state.current_state.containedRules ) {
                if ( rule.dot == rule.size() || rule.get_dot_item() instanceof Terminal && ((Terminal)rule.get_dot_item()).is_epsilon ) {
                    
                    for ( Term term : rule.lookahead ) {
                        row[ term_to_index.get( term ) ] += "r" + rule.id;
                    }

                } else if ( rule.get_dot_item().name.equals( "$" ) ) {
                    row[ term_to_index.get( rule.get_dot_item() ) ] += "a";
                } 
            }


            tp.push( row );
        }

        return tp.compute();
    }

}
