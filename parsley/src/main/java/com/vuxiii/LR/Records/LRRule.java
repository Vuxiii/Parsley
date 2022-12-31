package com.vuxiii.LR.Records;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vuxiii.Utils.*;

public class LRRule extends Rule {
    public static int count = 0;
    public final int id;
    public final int dot;
    public final NonTerminal X;
    public Set<Term> lookahead = new HashSet<>();

    public LRRule( NonTerminal X, List<Term> terms, Set<Term> lookahead ) {
        super( terms );
        this.lookahead = lookahead;
        id = count++;
        dot = 0;
        this.X = X;
    }

    public LRRule( NonTerminal X, List<Term> terms, Set<Term> lookahead, int dot, int id ) {
        super( terms );
        this.lookahead = lookahead;
        this.id = id;
        this.dot = dot;
        this.X = X;
    }

    public void lock() {
        
        terms = Collections.unmodifiableList( terms );
        lookahead = Collections.unmodifiableSet( lookahead );
        // Collectors.toList()
    }

    public Term get_dot_item() {
        return dot < size() ? terms.get( dot ) : Rule.EOR;
    }

    public boolean equals( Object other ) {
        if ( other == null )                            return false;
        if ( !(other instanceof LRRule) )               return false;
        LRRule o = (LRRule) other;
        if ( dot != o.dot )                             return false;
        if ( lookahead.size() != o.lookahead.size() )   return false;
        if ( !lookahead.containsAll( o.lookahead ) )    return false;
        if ( terms.size() != o.terms.size() )           return false;
        if ( !terms.containsAll( o.terms ) )            return false;


        return true;
    }

    public NonTerminal X() {
        return X;
    }

    public String toString() {
        String s = "";

        s += terms + " :-> " + lookahead;

        return s;
    }

    public LRRule copy() { // What should dot be????? dot or 0

        return new LRRule( X, Utils.toList( terms ) , Utils.toSet( lookahead ), dot, id );

    }

    public Term get_after_dot() {
        return dot +1 < size() ? terms.get( dot+1 ) : null;
    }
}

