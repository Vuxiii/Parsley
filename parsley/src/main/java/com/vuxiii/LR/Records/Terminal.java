package com.vuxiii.LR.Records;

public class Terminal extends Term {
    public boolean is_epsilon = false;
    public boolean is_EOP = false;
    /**
     * This constructor makes an Epsilon terminal
     */
    public Terminal() {
        super( "" );
        is_epsilon = true;
    }

    public Terminal( String n ) {
        super( n );
    }

    public Terminal( String n, boolean is_EOP ) {
        super( n );
        this.is_EOP = is_EOP;
    }

    // public boolean equals( Object other ) {
    //     if ( other == null ) return false;
    //     if ( !(other instanceof Terminal) ) return false;
    //     Terminal o = (Terminal) other;
    //     if ( !name.equals(o.name) ) return false;
    //     if ( is_epsilon != o.is_epsilon ) return false;

    //     return true;
    // }

    public String toString() {
        return (is_epsilon ? "€" : name);
    }
}