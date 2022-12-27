package com.vuxiii.LR.Records;

import java.util.List;
import java.util.function.Function;

// import com.vuxiii.Regex.Token.*;

public class ParseAccept implements ParseAction {
    
    public final int id;

    public final LRRule rule;

    private final Function<List<ASTToken>, ASTToken> reduceFunction;

    public ParseAccept( int id, LRRule rule, Function<List<ASTToken>, ASTToken> reduceFunction ) {
        this.id = id;
        this.rule = rule;
        this.reduceFunction = reduceFunction;
    }

    public ASTToken reduce( List<ASTToken> tokenParams ) {
        return reduceFunction.apply( tokenParams );
    }

    public String toString() {
        return "a";
    }
}
