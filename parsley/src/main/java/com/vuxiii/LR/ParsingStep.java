package com.vuxiii.LR;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vuxiii.LR.Records.LRRule;
import com.vuxiii.LR.Records.ParseAccept;
import com.vuxiii.LR.Records.ParseAction;
import com.vuxiii.LR.Records.ParseError;
import com.vuxiii.LR.Records.ParseGoto;
import com.vuxiii.LR.Records.ParseReduce;
import com.vuxiii.LR.Records.ParseShift;
import com.vuxiii.LR.Records.ParserState;
import com.vuxiii.LR.Records.ASTToken;
import com.vuxiii.Utils.*;

public class ParsingStep {
    public static int count = 0;
    private int id;

    private final LinkedList<ASTToken> input;

    private final LinkedList<ASTToken> tempInput;

    private final LinkedList<ParserState> stack;

    private final LinkedList<LRRule> reduceStack;

    private final ParseTable table;

    private ASTToken output = null;

    public boolean isFinished = false;

    /**
     * Constructs a single step in the Parsingsteps
     * @param input The list of input tokens to parse
     * @param tempInput The tokens already evaluated
     * @param stack The list of actions taken
     * @param reduceStack The list of reduce that have been used
     * @param table The ParsingTable for the given Grammar
     */
    public ParsingStep( List<ASTToken> input, List<ASTToken> tempInput, List<ParserState> stack, LinkedList<LRRule> reduceStack, ParseTable table ) {
        this.stack = new LinkedList<>();
        this.input = new LinkedList<>();
        this.reduceStack = new LinkedList<>();
        this.tempInput = new LinkedList<>();

        this.table = table;

        this.stack.addAll( stack );
        this.input.addAll( input );
        this.reduceStack.addAll( reduceStack );
        this.tempInput.addAll( tempInput );

        id = count++;
    }

    /**
     * When parsing is done, this method returns the result
     * @return The Token computed from the parsingsteps
     */
    public ASTToken getResult() {
        return inputPeek();
    }

    /**
     * This method takes a step in parsing
     * @return The next ParsingStep
     */
    public ParsingStep step() throws ParserException {
        ParserState currentState = stackPeek();

        ASTToken element = inputPeek();

        if ( Settings.showParsingSteps )
            Utils.log( this );

        ParseAction action = table.getAction( currentState.current_state.id, element.getTerm() );
        if ( Settings.showParsingSteps )
            Utils.log( action );

        if ( action instanceof ParseShift ) {
            if ( Settings.showParsingSteps )
                Utils.log( "In Shift" );
            ParseShift act = (ParseShift) action;
            ParsingStep nextStep = new ParsingStep( input, tempInput, stack, reduceStack, table );

            nextStep.stack.add( currentState.eat( element ) );
            nextStep.tempInput.add( nextStep.inputPop() );

            return nextStep;

        } else if ( action instanceof ParseGoto ) {

            ParseGoto act = (ParseGoto) action;

            ParsingStep nextStep = new ParsingStep( input, tempInput, stack, reduceStack, table );

            nextStep.stack.add( currentState.eat( element ) );
            nextStep.tempInput.add( nextStep.inputPop() ); // ?????

            return nextStep;


        } else if ( action instanceof ParseReduce ) {
            
            ParseReduce act = (ParseReduce) action;

            ParsingStep nextStep = new ParsingStep( input, tempInput, stack, reduceStack, table );
            
            int size = act.rule.size();

            LinkedList<ASTToken> tokenParams = new LinkedList<>();
            for ( int i = 0; i < size; ++i ) {
                nextStep.stackPop();
                tokenParams.addFirst( nextStep.tempInput.removeLast() );
            }
            nextStep.reduceStack.add( act.rule );
            nextStep.output = act.reduce( tokenParams );
            nextStep.input.addFirst( nextStep.output );

            return nextStep;


        } else if ( action instanceof ParseAccept ) {
            
            ParseAccept act = (ParseAccept) action;

            ParsingStep nextStep = new ParsingStep( input, tempInput, stack, reduceStack, table );
            
            nextStep.tempInput.add( nextStep.inputPop() );

            int size = act.rule.size();

            LinkedList<ASTToken> tokenParams = new LinkedList<>();
            for ( int i = 0; i < size; ++i ) {
                nextStep.stackPop();
                tokenParams.addFirst( nextStep.tempInput.removeLast() );
            }
            nextStep.reduceStack.add( act.rule );
            nextStep.output = act.reduce( tokenParams );
            nextStep.input.addFirst( nextStep.output );


            nextStep.isFinished = true; 

            return nextStep; // Output is in "nextStep.output"


        } else if ( action instanceof ParseError ) {
            Utils.log( "We encountered a parse error" );
            Utils.log( action.toString() );

            Utils.log( "Current step of failure" );
            Utils.log( this.toString() );

            throw new ParserException( output, this, action );
        } else {
            Utils.log( "Wtf just happend....." );
            System.exit(-1);
        }

        return null;
    }

    private ASTToken tempInputPop() {
        return tempInput.removeFirst();
    }

    private ASTToken tempInputPeek() {
        return tempInput.peekFirst();
    }

    private ASTToken inputPop() {
        return input.removeFirst();
    }

    private ASTToken inputPeek() {
        return input.peekFirst();
    }

    private ParserState stackPop() {
        return stack.removeLast();
    }

    private ParserState stackPeek() {
        return table.states.get( stack.peekLast().current_state.id );
    }

    public String toString() {

        TablePrinter tp = new TablePrinter();


        tp.addTitle( "ParsingStep: " + id );

        String[] row = new String[] { "Input", "tempInput", "stack", "reduceStack" };
        tp.push( row );

        row = new String[4];

        row[0] = input.toString();
        row[1] = tempInput.toString();
        row[2] = "[";
        for ( ParserState state : stack ) 
            row[2] += state.current_state.id + ", ";
        
        if ( row[2].length() > 1 )
            row[2] = row[2].substring(0, row[2].length() - 2 );
        row[2] += "]";

        if ( reduceStack.size() == 0 )
            row[3] = "[]";
        else {
            row[3] = "[";
            for ( LRRule rule : reduceStack ) 
                row[3] += rule.id + ", ";
            row[3] = row[3].substring(0, row[3].length() - 2 ) + "]";
        }
        tp.push( row );

        return tp.compute();
    }
}
