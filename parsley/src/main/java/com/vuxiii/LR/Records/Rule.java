package com.vuxiii.LR.Records;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Rule implements Serializable {
    public static Term EOR = new Term( "EndOfRule" );
    public static Term EOP = new Term( "EndOfParse" );
    public List<Term> terms = new ArrayList<>();

    public Rule( List<Term> terms ) {
        this.terms.addAll( terms );
    }

    public Rule() {}

    public Term get_term( int i ) { return i < size() ? terms.get( i ) : Rule.EOR; }

    public int size() {
        return terms.size();
    }
}
