package com.vuxiii.LR.Records;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Term implements Serializable {
    public static Term QUESTION = new Term( "?" );
    public static Map<String, Term> terms = new HashMap<>();

    public String name = null;

    public Term() {}
    public Term( String name ) { 
        this.name = name;
        if ( Term.terms == null ) terms = new HashMap<>();
        Term.terms.put( name, this );
    }

    public static Term get( String term ) {
        return terms.get( term );
    }

    public static int size() {
        return (int) terms.values().parallelStream().filter( t -> !(t == Rule.EOP || t == Rule.EOR) ).count();
    }

    // public boolean equals( Object other ) {
    //     if ( other == null ) return false;
    //     if ( !(other instanceof Term) ) return false;
    //     Term o = (Term) other;
    //     if ( !name.equals(o.name) ) return false;

    //     return true;
    // }

    public String toString() {
        return name;
    }
}
