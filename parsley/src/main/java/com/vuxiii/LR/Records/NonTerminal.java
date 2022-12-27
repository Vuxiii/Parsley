package com.vuxiii.LR.Records;

public class NonTerminal extends Term {
    public final boolean is_start;

    public NonTerminal( String n ) {
        super( n );
        is_start = false;
    }
    
    public NonTerminal( String n, boolean isStart ) {
        super( n );
        is_start = isStart;
    }

    // public boolean equals( Object other ) {
    //     if ( other == null ) return false;
    //     if ( !(other instanceof NonTerminal) ) return false;
    //     NonTerminal o = (NonTerminal) other;
    //     if ( !name.equals(o.name) ) return false;

    //     return true;
    // }
}