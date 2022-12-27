package com.vuxiii.LR.Records;

public class ParseError implements ParseAction {
    public final int id;
    public final String expected;
    public final String found;

    public ParseError( int id, String expected, String found ) {
        this.id = id;
        this.expected = expected;
        this.found = found;
    }

    public String toString() {
        return "Parser Exception in state " + id + "!\nExpected: " + expected + " but found: " + found;
    }
}
