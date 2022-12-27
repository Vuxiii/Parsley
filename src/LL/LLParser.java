package src.LL;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import src.Utils.TablePrinter;

public class LLParser {

    public static void parse( Grammar g ) {

        Set<NonTerminal> nulls = LLParser.compute_null( g );

        System.out.println( "Nulls" );
        nulls.forEach( n -> System.out.println( "\t" + n ));
        
        Map<NonTerminal, Set<Terminal> > firsts = compute_first( g, nulls );

        System.out.println( "Firsts" );
        firsts.forEach( (k, v) -> System.out.println( "\t" + k + ": " + v ));

        Map< NonTerminal, Set<Terminal> > follows = compute_follow( g, nulls, firsts );

        System.out.println( "Follows" );
        follows.forEach( (k, v) -> System.out.println( "\t" + k + ": " + v ));

        System.out.println( LLParser.parsingTable(g, nulls, firsts, follows) );
    }

    public static Set<NonTerminal> compute_null( Grammar g ) {

        Set<NonTerminal> nulls = new HashSet<>();
        int size;
        do {
            size = nulls.size();

            for ( NonTerminal N : g.rules.keySet() ) {
                // if ( nulls.size() == size ) break; // Nothing changed 
                
                List<Rule> rules_for_N = g.get_rule( N );
                for ( Rule rule : rules_for_N ) {
                    // System.out.println( "\nChecking NonTerm " + N + " -> " + rule.rules );
                    int i = 0;

                    // Removes all the first NULLS.
                    while ( i < rule.size() && ((rule.terms.get( i ) instanceof Terminal && ((Terminal) rule.terms.get( i )).is_epsilon)
                            || nulls.contains( rule.terms.get( i ) )) ) {
                        // System.out.println( rule.terms.get( i ) + " is nullable.");
                        ++i;
                    }  
                    
                    if ( i == rule.size() ) {
                        nulls.add( N );
                        // System.out.println( "Added NonTerminal to NULL " + N );
                    }


                    // // Check if current is a NonTerminal
                    // if ( i < rule.size() && rule.terms.get( i ) instanceof NonTerminal ) {
                    //     if ( !rule.terms.get(i).name.equals( N.name ) ) {
                    //         nulls.add( (NonTerminal) rule.terms.get( i ) );
                    //         System.out.println( "Added NonTerminal to NULL " + rule.terms.get( i ) );
                    //     }
                    // }
    
                }
            }

        } while ( size != nulls.size() );
        

        return nulls;
    }

    public static Map< NonTerminal, Set<Terminal> > compute_first( Grammar g, Set<NonTerminal> nulls ) {

        Map< NonTerminal, Set<Terminal> > firsts = new HashMap<>();

        for ( NonTerminal N : g.rules.keySet() ) 
            firsts.put( N, new HashSet<>() );

        int size;
        do {
            size = firsts.values().parallelStream().map( li -> li.size() ).reduce(0, Integer::sum);
            

            // System.out.println( "-".repeat(20) );
            
            // firsts.forEach( (k, v) -> System.out.println( "First(" + k + ") = {" + v + "}" ) );

            // System.out.println( "-".repeat(20) );
            
            for ( NonTerminal N : g.rules.keySet() ) {
                for ( Rule r : g.rules.get( N ) ) {
                    // System.out.println( "\nChecking NonTerm " + N + " -> " + r.terms );
                    int i = 0;
                    Term current = r.terms.get(i);
                    if ( current instanceof Terminal ) {
                        // Ignore epsilons.
                        if ( ((Terminal) current).is_epsilon ) continue;

                        // System.out.println( "First(" + N + ") += {" + current + "}\t\tBy rule 1 (is terminal)");
                        firsts.get( N ).add( (Terminal) current );
                        // firsts.merge( N, Utils.toSet( (Terminal) current ), (n, o) -> { 
                        //     System.out.println( "{" + o + "} -> {" + n + "}" );
                            
                        //     o.addAll( n ); return o; } );
                    } else {
                        boolean addNext = true; //current instanceof NonTerminal && 
                        while( addNext ) { // We know it is a NonTerminal from above.
                            
                            if ( current instanceof NonTerminal ){
                                // System.out.println( "First(" + N + ") += First(" + current + ") = " + firsts.getOrDefault( (NonTerminal) current, Utils.toSet()) + "\tBy rule 2 (Prev were nullable. Current = " + current+ ")");
                                
                                firsts.get( N ).addAll( firsts.get( (NonTerminal) current ) );

                                // firsts.merge( N, firsts.getOrDefault( (NonTerminal) current, Utils.toSet()), (n, o) -> { 
                                    
                                //     System.out.println( "{" + o + "} -> {" + n + "}" );
                                //     o.addAll( n ); return o; } );
                                

                                addNext = nulls.contains( (NonTerminal) current );
                            } else {
                                // System.out.println( "First(" + N + ") += {" + current + "}\t\tBy rule 1 (is terminal)");
                                firsts.get( N ).add( (Terminal) current );
                                // firsts.merge( N, Utils.toSet( (Terminal) current ), (n, o) -> { 
                                //     System.out.println( "{" + o + "} -> {" + n + "}" );
                                //     o.addAll( n ); return o; } );

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

    public static Map< NonTerminal, Set<Terminal> > compute_follow( Grammar g, Set<NonTerminal> nulls, Map<NonTerminal, Set<Terminal> > firsts) {

        Map< NonTerminal, Set<Terminal> > follow = new HashMap<>();

        for ( NonTerminal N : g.rules.keySet() ) 
            follow.put( N, new HashSet<>() );

        int size;
        do {
            size = follow.values().parallelStream().map( li -> li.size() ).reduce(0, Integer::sum);

            for ( NonTerminal N : g.rules.keySet() ) {
                for ( Rule r : g.rules.get( N ) ) {
                    for ( int i = 0; i < r.size(); ++i ) {

                        if ( r.terms.get( i ) instanceof NonTerminal ) {
                            // System.out.println( "At rule: " + N + " -> " + r.rules );
                            NonTerminal current = (NonTerminal) r.terms.get( i );
                            
                            int offset = 1;
                            Term next = i+offset < r.size() ? r.terms.get( i+offset ) : null;
                            if ( i == r.size() - 1 ){ 
                                // Because nothing comes after this current NonTerminal, 
                                // we add First( left hand side ) to Follow( current )
                                // System.out.println( "Follow(" + current + ") += Follow(" + N + ") = " + follow.get( N ) + "\tRule 1" );
                                follow.get( current ).addAll( follow.get( N ) );

                            }else {
                                do {
                                    // Add first of current+1
                                    if ( next instanceof NonTerminal ){
                                        // System.out.println( "Follow(" + current + ") += First(" + next + ") = " + firsts.get( (NonTerminal) next ) + "\tRule 2" );
                                        follow.get( current ).addAll( firsts.get( (NonTerminal) next ) );
                                        
                                        int j = 1;
                                        while ( nulls.contains( (NonTerminal) next ) ) {
                                            next = i+offset+j < r.size() ? r.terms.get( i+offset+j ) : null;
                                            if ( next == null || next instanceof Terminal ) break;
                                            // System.out.println( "Follow(" + current + ") += First(" + next + ") = " + firsts.get( (NonTerminal) next ) + "\tRule 2.5" );
                                            follow.get( current ).addAll( firsts.get( (NonTerminal) next ) );
                                            
                                            j++;                                            
                                        }
                                        if ( next == null ) {
                                            // System.out.println( "Follow(" + current + ") += Follow(" + N + ") = " + follow.get( N ) + "\tRule 2.75" );
                                            follow.get( current ).addAll( follow.get( N ) );
                                        }
                                        // follow.get( current ).addAll( follow.get( (NonTerminal) next ) );
                                    }else{
                                        // System.out.println( "Follow(" + current + ") += {" + next + "}" + "\tRule 3" );
                                        follow.get( current ).add( (Terminal) next );
                                    }
                                    offset++;
                                    next = i+offset < r.size() ? r.terms.get( i+offset ) : null;
                                    if ( next instanceof Terminal || (next instanceof NonTerminal && !nulls.contains( (NonTerminal) next) ) )
                                        next = null;
                                } while ( next != null );
                            }
                            // System.out.println();
                        }
                    }
                }
            }

        } while( size != follow.values().parallelStream().map( li -> li.size() ).reduce(0, Integer::sum) );
        return follow;
    }

    public static String parsingTable( Grammar g, Set<NonTerminal> nulls, Map<NonTerminal, Set<Terminal> > firsts, Map< NonTerminal, Set<Terminal> > follows ) {

        TablePrinter p = new TablePrinter();

        // p.addLast( new String[] {"", "Nullable", "Firsts", "Follow"} );
        
        List<Terminal> terms = new ArrayList<>();
        List<NonTerminal> nonTerms = g.nonTerms();
        terms.addAll( g.terms().parallelStream().filter( term -> { return !term.is_epsilon; } ).collect( Collectors.toList() ) );
        
        // Sort the arrays.
        terms.sort( Comparator.comparing( Term::toString ) );
        nonTerms.sort( Comparator.comparing( Term::toString ) );

        {
            String[] s = new String[terms.size() + 1];
            s[0] = "NT \\ T";
            for ( int i = 0; i < terms.size(); ++i )
                s[i+1] = "'" + terms.get(i).name + "'" ;
            p.addFirst( s );
        }   

        for ( NonTerminal X : nonTerms ) {
            String[] s = new String[terms.size() + 1];
            s[0] = X.name;
            for ( int i = 0; i < terms.size(); ++i ) {
                Terminal T = terms.get( i );
                s[i+1] = "";
                if ( firsts.get( X ).contains( T ) ) {
                    for ( Rule r : g.rules.get( X )) {
                        if ( r.terms.get(0) instanceof Terminal ) {
                            if ( (!((Terminal)r.terms.get(0)).is_epsilon) && r.terms.get(0).name.equals(T.name) )
                                s[i+1] += X.name + "->" + r.terms + " ";
                        } else {
                            // remove the nulls
                            if ( firsts.get((NonTerminal) r.terms.get(0)).contains( T ) )
                                s[i+1] += X.name + "->" + r.terms + " ";
                        }

                    }
                } else {
                    boolean allNull = true;
                    for ( Rule r : g.rules.get( X ) ) {
                        allNull = r.terms.stream().allMatch( t -> { return nulls.contains( t ) || (t instanceof Terminal && ((Terminal) t).is_epsilon ); } );
                        if ( allNull && follows.get(X).contains( T ) ) {
                            s[i+1] += X.name + "->" + r.terms + " ";
                            System.out.println( T + " is in " + X);
                            System.out.println( T + " is in " + follows.get(X) );
                            // break;
                        }
                    }

                }

            }
            p.addLast( s );
        }


        return p.compute();
    }

    public static void getRegexParser() {
        Terminal epsilon = new Terminal();
        

        /*
         epsilon
            : 'epsilon' just means it's ok to match an empty string when no better option is available.

        decimal_digit
            : '0' to '9'

        decimal_number_rest
            : decimal_digit decimal_number_rest
            | epsilon

        decimal_number
            : decimal_digit decimal_number_rest

        hex_digit
            : '0' to '9'
            | 'A' to 'F'
            | 'a' to 'f'

        escaped_hex_character
            | 'x' hex_digit hex_digit

        escaped_ascii_character
            : any character not in 'x' or escaped_regex_class_character

        escaped_regex_class_character
            : 'd'
            | 'D'
            | 's'
            | 'S'
            | 'w'
            | 'W'
            | 'N'  #  Not currently supported: Provide error message in compiler.
            | 'h'  #  Not currently supported: Provide error message in compiler.
            | 'H'  #  Not currently supported: Provide error message in compiler.
            | 'V'  #  Not currently supported: Provide error message in compiler.
            | 'R'  #  Not currently supported: Provide error message in compiler.
            | 'A'  #  Not currently supported: Provide error message in compiler.
            | 'z'  #  Not currently supported: Provide error message in compiler.
            | 'Z'  #  Not currently supported: Provide error message in compiler.
            | 'G'  #  Not currently supported: Provide error message in compiler.
            | 'b'  #  Not currently supported: Provide error message in compiler.
            | 'B'  #  Not currently supported: Provide error message in compiler.

        escaped_character
            : '\' escaped_hex_character
            | '\' escaped_ascii_character
            | '\' escaped_regex_class_character

        character_range
            : character_range_endpoint '-' character_range_endpoint

        #  Any non-escaped character that can appear inside a character class.
        #  Can include '^' because this is picked up directly in the grammar rule
        #  and subsequent '^' characters are treated as literal. '^' is only
        #  special when it's first.  Allow un-escaped '-' since that seems to
        #  be a commonly expected use case.
        class_ascii_character
            : \x00 to ','
            | '.' to '['
            # Avoid '[' and '\'
            | '^' to \xFF

        #  Any non-escaped character that can appear outside a character class.
        non_class_ascii_character
            #  Any character from 0x00 to 0xFF, except
            #  Avoid '^' '$'
            #  Avoid '(' ')' '*' '+'
            #  Avoid '.'
            #  Avoid '?'
            #  Avoid '{' '}'
            #  Avoid '[' '\'
                #  Don't avoid ']' since a lot of regex parsers allow it unescaped
            #  Avoid '|'

        non_class_special_character
            : '.'

        anchor
            : '^'
            | '$'

        non_class_character
            : non_class_ascii_character
            | non_class_special_character
            | escaped_character

        character_range_endpoint
            : class_ascii_character
            | escaped_character

        character_or_range
            : character_range
            | character_range_endpoint


        character_class_rest
            : character_or_range character_class_rest
            | ']'

        character_class
            : '[' '^' character_or_range character_class_rest
            | '[' character_or_range character_class_rest

        explicit_quantifier
            : '{' decimal_number '}'
            | '{' decimal_number ',' '}'
            | '{' ',' decimal_number  '}'
            | '{' decimal_number ',' decimal_number  '}'

        implicit_quantifier
            : '+'
            | '?'
            | '*'

        quantifier_operator
            : implicit_quantifier '?'
            | implicit_quantifier
            | explicit_quantifier '?'
            | explicit_quantifier

        character_or_class
            : character_class
            | non_class_character

        sub_expression
            : '(' regular_expression ')'
            #  Technically supported by doing nothing since back-references are not implemented yet.
            | '(' '?' ':' regular_expression ')'
            #  None of these are currently supported, but they should be parsed to provide good error message:
            | '(' '?' '!' regular_expression ')'
            | '(' '?' '=' regular_expression ')'
            | '(' '?' '<' '!' regular_expression ')'
            | '(' '?' '<' '=' regular_expression ')'
            | '(' '?' '>' regular_expression ')'

        quantified_expression
            : character_or_class quantifier_operator
            | character_or_class
            | sub_expression quantifier_operator
            | sub_expression
            | anchor

        concatenation_expression_rest
            : quantified_expression concatenation_expression_rest
            | epsilon

        concatenation_expression
            : quantified_expression concatenation_expression_rest

        alternation_expression_rest
            : '|' concatenation_expression alternation_expression_rest
            | '|' alternation_expression_rest
            : concatenation_expression alternation_expression_rest
            | epsilon

        alternation_expression
            : '|' alternation_expression_rest
            | concatenation_expression alternation_expression_rest

        regular_expression
            : alternation_expression regular_expression
            | epsilon
         */
    }

    public static void LLSample() {
        // {
        //     Grammar g = new Grammar();
        //     NonTerminal Z = new NonTerminal( "Z" );
        //     NonTerminal Y = new NonTerminal( "Y" );
        //     NonTerminal X = new NonTerminal( "X" );

        //     Terminal d = new Terminal( "d" );
        //     Terminal epsilon = new Terminal();
        //     Terminal c = new Terminal( "c" );
        //     Terminal a = new Terminal( "a" );

        //     g.add_rule( Z, List.of( d ) );
        //     g.add_rule( Z, List.of( X, Y, Z ) );
            
        //     g.add_rule( Y, List.of( epsilon ) );
        //     g.add_rule( Y, List.of( c ) );

        //     g.add_rule( X, List.of( Y ) );
        //     g.add_rule( X, List.of( a ) );

        //     LLparser.parse( g );
        // }

        // {
        //     Grammar g = new Grammar();
        //     NonTerminal S = new NonTerminal( "S" );
        //     NonTerminal E = new NonTerminal( "E" );
        //     NonTerminal E_ = new NonTerminal( "E'" );
        //     NonTerminal T = new NonTerminal( "T" );
        //     NonTerminal T_ = new NonTerminal( "T'" );
        //     NonTerminal F = new NonTerminal( "F" );

        //     Terminal plus = new Terminal( "+" );
        //     Terminal minus = new Terminal( "-" );
        //     Terminal times = new Terminal( "*" );
        //     Terminal div = new Terminal( "/" );
        //     Terminal lparen = new Terminal( "(" );
        //     Terminal rparen = new Terminal( ")" );
        //     Terminal dollar = new Terminal( "$" );
        //     Terminal id = new Terminal( "id" );
        //     Terminal num = new Terminal( "num" );
        //     Terminal epsilon = new Terminal();

        //     g.add_rule( S, List.of( E, dollar ) );

        //     g.add_rule( E, List.of( T, E_ ) );
            
        //     g.add_rule( E_, List.of( plus, T, E_ ) );
        //     g.add_rule( E_, List.of( minus, T, E_ ) );
        //     g.add_rule( E_, List.of( epsilon ) );

        //     g.add_rule( T, List.of( F, T_ ) );
            
        //     g.add_rule( T_, List.of( times, F, T_ ) );
        //     g.add_rule( T_, List.of( div, F, T_ ) );
        //     g.add_rule( T_, List.of( epsilon ) );

        //     g.add_rule( F, List.of( id ) );
        //     g.add_rule( F, List.of( num ) );
        //     g.add_rule( F, List.of( lparen, E, rparen ) );

        //     LLParser.parse( g );
        // }

        // System.out.println( "-".repeat( 10 ) );
        {
            Grammar g = new Grammar();
            NonTerminal SS_ = new NonTerminal( "SS'" );
            NonTerminal S = new NonTerminal( "S" );
            NonTerminal B = new NonTerminal( "B" );
            NonTerminal E = new NonTerminal( "E" );
            NonTerminal X = new NonTerminal( "X" );

            Terminal bslash = new Terminal( "\\" );
            Terminal b = new Terminal( "b" );
            Terminal e = new Terminal( "e" );
            Terminal lcurl = new Terminal( "{" );
            Terminal rcurl = new Terminal( "}" );
            Terminal w = new Terminal( "w" );
            Terminal epsilon = new Terminal();
            Terminal dollar = new Terminal( "$" );


            g.add_rule( SS_, List.of(S, dollar) );

            g.add_rule( S, List.of(epsilon) );
            g.add_rule( S, List.of(X, S) );
            
            g.add_rule( B, List.of(bslash, b, lcurl, w, rcurl) );
            
            g.add_rule( E, List.of(bslash, e, lcurl, w, rcurl) );
            
            g.add_rule( X, List.of(B, S, E) );
            g.add_rule( X, List.of(lcurl, S, rcurl) );
            g.add_rule( X, List.of(w) );
            g.add_rule( X, List.of(b) );
            g.add_rule( X, List.of(e) );
            g.add_rule( X, List.of(bslash, w) );

            LLParser.parse( g );

            
        }

        // System.out.println( "-".repeat( 10 ) );
        // {
        //     Grammar g = new Grammar();
        //     NonTerminal S = new NonTerminal( "S" );
        //     NonTerminal M = new NonTerminal( "M" );
        //     NonTerminal T = new NonTerminal( "T" );

        //     Terminal a = new Terminal( "a" );
        //     Terminal plus = new Terminal("+");
        //     Terminal dollar = new Terminal( "$" );
        //     Terminal epsilon = new Terminal();



        //     g.add_rule( S, List.of(T, M, dollar) );
            
        //     g.add_rule( M, List.of(plus, T, M) );
        //     g.add_rule( M, List.of(epsilon) );
            
        //     g.add_rule( T, List.of(a) );
            
        //     LLparser.parse( g );

            
        // }
    }

}
