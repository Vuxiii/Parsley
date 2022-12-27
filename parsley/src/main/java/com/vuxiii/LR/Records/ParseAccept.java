package com.vuxiii.LR.Records;

import java.util.List;
import java.util.function.Function;

// import com.vuxiii.Regex.Token.*;

public class ParseAccept implements ParseAction {
    
    public final int id;

    public final LRRule rule;

    private final Function<List<Token>, Token> reduceFunction;

    public ParseAccept( int id, LRRule rule, Function<List<Token>, Token> reduceFunction ) {
        this.id = id;
        this.rule = rule;
        this.reduceFunction = reduceFunction;
    }

    public Token reduce( List<Token> tokenParams ) {
        return reduceFunction.apply( tokenParams );
    }

    public String toString() {
        return "a";
    }
}
