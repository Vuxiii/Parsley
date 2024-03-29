package com.vuxiii;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import com.vuxiii.LR.Grammar;
import com.vuxiii.LR.LRParser;
import com.vuxiii.LR.ParseTable;
import com.vuxiii.LR.Records.NonTerminal;
import com.vuxiii.LR.Records.Terminal;
import com.vuxiii.LR.Records.ASTToken;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    
    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {

        Grammar g = new Grammar();
        NonTerminal n = new NonTerminal("n");
        g.addRuleWithReduceFunction(n, List.of( n ), (ns) -> new Tok() );
        
        ParseTable t = new ParseTable(g);

        LRParser.save( "asd", t );

        t = LRParser.load("asd");

    }

}
