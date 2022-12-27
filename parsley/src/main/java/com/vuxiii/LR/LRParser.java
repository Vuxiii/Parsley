package com.vuxiii.LR;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

// import com.vuxiii.Utils.*;
import com.vuxiii.LR.*;
import com.vuxiii.LR.Records.LRRule;
import com.vuxiii.LR.Records.LRRuleIdentifier;
import com.vuxiii.LR.Records.LRState;
import com.vuxiii.LR.Records.NonTerminal;
import com.vuxiii.LR.Records.ParserState;
import com.vuxiii.LR.Records.Rule;
import com.vuxiii.LR.Records.Term;
import com.vuxiii.LR.Records.Terminal;
import com.vuxiii.LR.Records.ASTToken;
import com.vuxiii.Utils.Utils;

public class LRParser{
    

    public static  ParseTable parse( Grammar g, NonTerminal start ) {

        LRState start_state = _computeState( g, start );
        
        _printStates( g, start_state, new HashSet<>() );

        ParseTable table = getParserTable( g, start_state );

        

        
        // ParserState ns = index.eat( Term.get( "x" ) );
        // if ( ns instanceof ParserStateError ) 
        //     System.out.println( ns.errorMsg );
        // else
        //     System.out.println( "\n\n\n\nArrived at\n" + ns.current_state );

        // In state 1

        // Reading x :-> ParserState newState = acc.get( x ).accept( oldState );

        table.compile();

        System.out.println( g );

        System.out.println( table );

        // return start_state;
        return table;

    }

    public static  ParseTable getParserTable( Grammar  g, LRState start ) {
        return _getParserTable( g, new ParserState( start, null ) );
    }

    private static  ParseTable _getParserTable( Grammar  g, ParserState index ) {
        
        ParseTable tbl = new ParseTable( g );        

        List<ParserState> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();

        queue.add( index );

        while ( queue.size() > 0 ) {
            ParserState current = queue.remove( 0 );
            if ( visited.contains( current.current_state.id ) ) continue;
            tbl.add( current );
            LRState state = current.current_state; 
            visited.add( state.id );

            // System.out.println( "Visiting\n" + state );

            // Set up the fails. The actual eatable will overwrite these.
            g.terms().forEach(   t -> current.addError( t, "The Term '" + t + "' Connot be accepted from the given state\n" + state ) );
            g.nonTerms().forEach(t -> current.addError( t, "The Term '" + t + "' Connot be accepted from the given state\n" + state ) );



            for ( Term t : state.move_to_state.keySet() ) {
                if ( t == Rule.EOR || t == Rule.EOP ) continue; // This should remove (1)

                LRState to_state = state.move_to_state.get( t );
                
                if ( to_state == null ) continue; // (1)
    
                ParserState move = new ParserState( to_state, t );
    
                // System.out.println( "To state id: " + to_state.id );
                
                
                // Here we can add whatever code we want. This will be executed when we move from one state to another.
                // current.addMove( t, ( ParserState oldState ) -> { 
                //     // We have access to 
                //     //  * oldState  = fromState
                //     //  * move      = toState 
                //     //  * t         = usedTerm
                    
                //     // System.out.println( "Made move " + t + " from" );
                //     // System.out.println( oldState.current_state );
                //     // System.out.println( "And arrived at" );
                //     // System.out.println( move.current_state );
                //     return move; 
                // } );

                current.addMove( t, move );

                if ( !visited.contains( to_state.id ) ) {
                    // System.out.print( "Visited: [" );
                    // visited.forEach( s -> System.out.print( s + " " ));
                    // System.out.println("] doesn't contain state " + to_state.id);
                    queue.add( move );
                }
            }

        }


        return tbl;
    }

    private static  void _printStates( Grammar  g, LRState state, Set<LRState> visited ) {
        visited.add( state );
        System.out.println( state.toString() + "\n" );

        for ( Term move : state.move_to_state.keySet() ) {
            LRState ste = state.move_to_state.get( move );
            if ( ste != null && !visited.contains( ste ) ) {
                
                _printStates( g, ste, visited );
            }       
        }
    }

    /**
     * Computes the initial start state
     * @param g The grammar
     * @param start The of the CFG
     * @return The start state
     */
    private static  LRState _computeState( Grammar g, NonTerminal start ) {      
        
        LRState ste = new LRState();

        for ( LRRule r : g.get_rule( start ) ) {
            r.lookahead.add( Term.QUESTION );
            ste.add( start, r );
        }

        _computeClosure( ste, g );

        // Cache the resulting state, so it can be used for loops.
        g.cache( ste );

        for ( Term move : ste.getMoves() ) {
            if ( move.equals( Rule.EOR ) ) continue;
            if ( move instanceof Terminal && ((Terminal) move).is_epsilon ) continue;
            LRState ns = _computeState( ste, move, g );
            g.cache( ns );
            ste.move_to_state.put( move, ns );
            
        }
        return ste;
    }

    /**
     * This method computes the closure for the given state.
     * @param state The state in which to compute the closure.
     * @param g The grammar for the language.
     */
    private static void _computeClosure( LRState state, Grammar g ) {
        // System.out.println( "\t\tBefore closure");
        // System.out.println( state );
        state.containedRules.forEach( r -> r.lock() );
        List< NonTerminal > addQueue = new LinkedList<>();
        
        Map<Term, Set<Term> > getLookahead = new HashMap<>();

        g.terms().forEach( t -> getLookahead.put( t, Utils.toSet() ) ); // Fill it with empty lookahead to avoid null.
        g.nonTerms().forEach( t -> getLookahead.put( t, Utils.toSet() ) ); // Fill it with empty lookahead to avoid null.
        addQueue.addAll( state.rules.keySet() );
        
        // add the lookaheads that should be inherited to the rules added from the closure.
        // Map<NonTerminal, LRRule> inheritLookaheads = new HashMap<>();

        // state.rules.forEach( (LRRule rule) -> inheritLookaheads.put( ));
        

        // List<LRRule> rules_to_add = new ArrayList<>();
        // List<NonTerminal> terms_to_add = new ArrayList<>();
        Map<Integer, LRRule> getCreatedState = new HashMap<>();
        Map<NonTerminal, Set<Term>> addedLookaheads = new HashMap<>();
        Map<NonTerminal, Set<LRRule>> addedRules = new HashMap<>();

        // compute the closure
        while ( !addQueue.isEmpty() ) {
            NonTerminal X = addQueue.remove(0);
            List<LRRule> rules = state.get_rule( X );
            int size = rules.size();
            for ( int i = 0; i < size; ++i ) {
                LRRule rule = rules.get( i );
                
                if ( rule.dot >= rule.size() ) continue;
                
                Term t = rule.get_dot_item();
                if ( t instanceof Terminal ) continue;
                // System.out.println( addQueue.size() );
                
                for ( LRRule r : g.get_rule( (NonTerminal) t ) ) {
                    
                    Set<Term> ts = g.get_firsts( rule, state );
                    
                    // We need to check if r.get_dot_item is NonTerminal. 
                    // true -> We need to add whatever this has added to each other NEWLY added rule with the same X

                    if ( !state.containedIDRules.contains( new LRRuleIdentifier( r.id, r.dot ) ) ) { 
                        // System.out.println( "Skipping " + r); continue; 
                        // System.out.println( "(6)Adding " + ts + " to " + r.X + " -> " + r.terms );
                        addedLookaheads.put( r.X, ts );
                        r.lookahead.addAll( ts );
                        state.add( (NonTerminal) t, r );
                        addQueue.add( (NonTerminal) t );
                        getCreatedState.put( state.id, r );

                        addedRules.merge( r.X, Utils.toSet(r), (o, n) -> {o.addAll( n ); return o;} );
                    } else {
                        // System.out.println( "(6)Adding " + ts + " to " + getCreatedState.get( state.id ).X + " -> " + getCreatedState.get( state.id ).terms );
                        getCreatedState.get( state.id ).lookahead.addAll( ts );
                        addedLookaheads.get( getCreatedState.get( state.id ).X ).addAll( ts );
                    }
                }
            }
        }

        addedLookaheads.forEach( (X, laheads) -> addedRules.get( X ).forEach( r -> r.lookahead.addAll( laheads ) ) ); // Might break...


        // System.out.println( "\t\tAfter closure");
        // System.out.println( state );
        state.containedRules.forEach( r -> r.lock());
    }

    /**
     * Computes the rest of the states
     * @param from Which state we move from
     * @param move With what Symbol
     * @param g The grammar we are using
     * @return The state arrived at by traversing with the given "move" from the given "state"
     */
    private static LRState _computeState( LRState from, Term move, Grammar g ) {
        // (1) Collect all the rules where you can make the move.
        // (2) Generate a new state with these new rules
        // (3) and compute the closure. 
        // (4) * Repeat.
        Map<NonTerminal, List<LRRule> > mapper = new HashMap<>();
        
        // (1) Collect the rules where "move" can be made from the previous state.
        for ( NonTerminal X : from.rules.keySet() ) {
            for ( LRRule rule : from.rules.get( X ) ) {
                if ( rule.get_dot_item().name.equals( move.name ) )
                    mapper.merge( X, Utils.toList( rule ), (o, n) -> { o.addAll( n ); return o; } );
            }
        }

        // (2) Generate the new state, and insert all the rules from the original state 
        //      and progress them by one.
        LRState ste = new LRState();
        // System.out.println( "Adding to state: " + ste.id );
        for ( NonTerminal X : mapper.keySet() ) {
            for ( LRRule r : mapper.get( X ) ) {
                // System.out.println( "Mapper: " + r.toString() + "\t\t" + r.lookahead );
                LRRule rule = new LRRule( X, Utils.toList( r.terms ), Utils.toSet( r.lookahead ), r.dot+1, r.id ); // Same rule, but increment dot.
                
                // rule.dot = r.dot+1;
                ste.add( X, rule );
            }
        }

        // (3) Compute the closure for this state.
        _computeClosure( ste, g );

        // Check if this state already has been made. If it has, just return it.
        // Prevents infinite recursion on loops.
        LRState ns = g.checkCache( Utils.toList( ste.containedRules ) );
        
        if ( ns != null ) { LRState.count--; return ns; } // We have already seen this state before. Just return it.
        
        // This state has not been seen before, therefore cache it, and compute the new states from this state.
        g.cache( ste );

        // (4) Generate the new states by making each move available from this newly created state.
        for ( Term new_move : ste.getMoves() ) {
            if ( new_move.equals( Rule.EOR ) ) { /*System.out.println( "Found EOR" );*/ continue; } // End of Rule
            if ( new_move instanceof Terminal && ((Terminal) new_move).is_EOP ) { /*System.out.println( "Found EOP" );*/ continue; }// End of Parse $
            if ( new_move instanceof Terminal && ((Terminal) new_move).is_epsilon ) continue;
            ste.move_to_state.put( new_move, _computeState( ste, new_move, g  ) );
        }

        return ste;
    }

    /**
     * Provides a sample CFG to be parsed
     * 
     */
    public static void LRSample() {
        // {
        //     NonTerminal S = new NonTerminal( "S", true );
        //     NonTerminal E = new NonTerminal( "E" );
        //     NonTerminal T = new NonTerminal( "T" );
    
        //     Terminal dollar = new Terminal( "$" );
        //     dollar.is_EOP = true;
        //     Terminal plus = new Terminal( "+" );
        //     Terminal x = new Terminal( "x" );
    
        //     Grammar g = new Grammar();
    
        //     g.add_rule( S, List.of( E, dollar ) );
            
        //     g.add_rule( E, List.of( T, plus, E ) );
        //     g.add_rule( E, List.of( T ) );
    
        //     g.add_rule( T, List.of( x ) );
    
        //     LRParser.parse( g, S );
        // }

        // {
        //     NonTerminal S_ = new NonTerminal( "S'", true );
        //     NonTerminal S = new NonTerminal( "S" );
    
        //     Terminal dollar = new Terminal( "$" );
        //     dollar.is_EOP = true;
        //     Terminal x = new Terminal( "x" );
        //     Terminal eps = new Terminal();
    
        //     Grammar g = new Grammar();
    
        //     g.add_rule( S_, List.of( S, dollar ) );
            
        //     g.add_rule( S, List.of( S, x ) );
        //     g.add_rule( S, List.of( eps ) ); 
    
        //     LRParser.parse( g, S_ );
        // }

        // {
        //     NonTerminal S_ = new NonTerminal( "S'", true );
        //     NonTerminal S = new NonTerminal( "S" );
    
        //     Terminal dollar = new Terminal( "$" );
        //     dollar.is_EOP = true;
        //     Terminal x = new Terminal( "x" );
        //     Terminal eps = new Terminal();
    
        //     Grammar g = new Grammar();
    
        //     g.add_rule( S_, List.of( S, dollar ) );
            
        //     g.add_rule( S, List.of( x, S ) );
        //     g.add_rule( S, List.of( eps ) ); 
    
        //     LRParser.parse( g, S_ );
        // }

    //     { // Not valid LR!!!!!!!
            NonTerminal S = new NonTerminal( "S", true );
            NonTerminal G = new NonTerminal( "G" );
            NonTerminal P = new NonTerminal( "P" );
            NonTerminal R = new NonTerminal( "R" );
    
            Terminal dollar = new Terminal( "$" );
            dollar.is_EOP = true;
            Terminal id = new Terminal( "id" );
            Terminal colon = new Terminal( ":" );
            Terminal eps = new Terminal();
    
            Grammar g = new Grammar();
    
            // g.add_rule( S, List.of( G, dollar ) );
            
            // g.add_rule( G, List.of( P ) );
            // g.add_rule( G, List.of( P, G ) );
            // g.add_rule( P, List.of( id, colon, R ) );
            // g.add_rule( R, List.of( eps ) ); 
            // g.add_rule( R, List.of( id, R ) ); 
    
            LRParser.parse( g, S );

    //         // +-------------------------------+
    //         // |State: 0                       |
    //         // +-------------------------------+
    //         // |Rules     |Lookahead|Goto State|
    //         // |----------+---------+----------|
    //         // |S -> .G$  |[?]      |G: 11     |
    //         // |----------+---------+----------|
    //         // |G -> .P   |[$]      |P: 1      |
    //         // |----------+---------+----------|
    //         // |G -> .PG  |[$]      |P: 1      |
    //         // |----------+---------+----------|
    //         // |P -> .id:R|[$, G]   |id: 3     |
    //         // +-------------------------------+
    //         // +-------------------------------+
    //         // |State: 1                       |
    //         // +-------------------------------+
    //         // |Rules     |Lookahead|Goto State|
    //         // |----------+---------+----------|
    //         // |G -> P.   |[$]      |None      |
    //         // |----------+---------+----------|
    //         // |G -> .P   |[$]      |P: 1      |
    //         // |----------+---------+----------|
    //         // |G -> P.G  |[$]      |G: 9      |
    //         // |----------+---------+----------|
    //         // |G -> .PG  |[$]      |P: 1      |
    //         // |----------+---------+----------|
    //         // |P -> .id:R|[$, G]   |id: 3     |
    //         // +-------------------------------+
    //         // It produces above states. However, shouldn't the last entry be id instead of G in lookahead
    //         // The rest looks fine, only the lookaheads are wrong. 

    //         // The table it produces is also wrong. It for some reason creates duplicate rows?
    //         // +------------------------+
    //         // |Parse-table             |
    //         // +------------------------+
    //         // |State|$ |: |id|P |R |G  |
    //         // |-----+--+--+--+--+--+---|
    //         // |0    |  |  |s3|g1|  |g11|
    //         // |-----+--+--+--+--+--+---|
    //         // |1    |r1|  |s3|g1|  |g9 |
    //         // |-----+--+--+--+--+--+---|
    //         // |3    |  |s4|  |  |  |   |
    //         // |-----+--+--+--+--+--+---|
    //         // |3    |  |s4|  |  |  |   |
    //         // |-----+--+--+--+--+--+---|
    //         // |4    |  |  |s6|  |g5|   |
    //         // |-----+--+--+--+--+--+---|
    //         // |4    |  |  |s6|  |g5|   |
    //         // |-----+--+--+--+--+--+---|
    //         // |5    |r3|  |  |  |  |r3 |
    //         // |-----+--+--+--+--+--+---|
    //         // |5    |r3|  |  |  |  |r3 |
    //         // |-----+--+--+--+--+--+---|
    //         // |6    |  |  |s6|  |g7|   |
    //         // |-----+--+--+--+--+--+---|
    //         // |6    |  |  |s6|  |g7|   |
    //         // |-----+--+--+--+--+--+---|
    //         // |7    |  |  |  |  |  |   |
    //         // |-----+--+--+--+--+--+---|
    //         // |7    |  |  |  |  |  |   |
    //         // |-----+--+--+--+--+--+---|
    //         // |9    |r2|  |  |  |  |   |
    //         // |-----+--+--+--+--+--+---|
    //         // |11   |a |  |  |  |  |   |
    //         // +------------------------+
        // }

        // {
        //     NonTerminal S = new NonTerminal( "S", true );
        //     NonTerminal E = new NonTerminal( "E" );

        //     Terminal dollar = new Terminal( "$" );
        //     dollar.is_EOP = true;
        //     Terminal plus = new Terminal( "+" );
        //     Terminal id = new Terminal( "id" );
        //     Terminal lparen = new Terminal( "(" );
        //     Terminal rparen = new Terminal( ")" );
            
        //     Grammar g = new Grammar();
    
        //     g.add_rule( S, List.of( E, dollar ) );
            
        //     g.add_rule( E, List.of( id ) );
        //     g.add_rule( E, List.of( id, lparen, E, rparen ) );
        //     g.add_rule( E, List.of( E, plus, id ) );
    
        //     LRParser.parse( g, S );
        // }
    }

    // https://serokell.io/blog/how-to-implement-lr1-parser
    // For each position in the starting set, 
    // if its locus contains a terminal or is empty, 
    // no new positions are added to the state. 
    // If it is a non-terminal, 
    // then all starting positions of that terminal are added to the set, 
    // with their lookahead set changed to the FIRST(Next), 
    // where Next is the point that follows the locus in the position. 
    // If nothing follows the locus, 
    // the the lookahead set becomes FOLLOW(Entity) instead, 
    // where Entity is the output non-terminal of the rule.

        
}
