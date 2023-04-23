package com.vuxiii.LR.Records;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// import com.vuxiii.Regex.Token.Token;

public class ParserState implements Serializable {
    public final LRState current_state;
    // public Term accept;

    // public final Map< Term, Function<ParserState, ParserState> > accepter; // Probably change this to a hashMap instead of the function shite.
    private final Map< Term, ParserState > accepter; // Probably change this to a hashMap instead of the function shite.

    public final boolean isError;
    public final String errorMsg;

    public ParserState( String errorMessage ) {
        this.current_state = null;
        // this.accept = null;
        isError = true;
        errorMsg = errorMessage;
        accepter = null;
    }

    // Added to easily add progress in parsing...
    public ParserState( LRState state, Term accept ) {
        current_state = state;
        // this.accept = accept;
        isError = false;
        errorMsg = "";

        accepter = new HashMap<>();
    }

    // public void addMove( Term t, Function<ParserState, ParserState> fun ) {
    //     // if ( current_state.id == 2 ) System.out.println( "ADDEDD " + t );
    //     // accepter.put( t, fun );
    // }

    public void addMove( Term t, ParserState move ) {
        accepter.put( t, move );
    }

    public void addError( Term t, String error ) {
        accepter.put( t, new ParserStateError( error ) );
        // addMove( t, (ParserState oldState) -> new ParserStateError( error ) );
    }


    public ParserState eat( ASTToken t ) {
        // System.out.println( current_state.id );
        // System.out.println( accepter.keySet() );
        // System.out.println( isError );
        return accepter.get( t.getTerm() );
        // return accepter.get( t.getTerm() ).apply( this );
    }


}
