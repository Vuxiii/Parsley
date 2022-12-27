package com.vuxiii.LR.Records;

public record ParseGoto(int id) implements ParseAction {
    // public final int id;

    // public ParseGoto( int id ) {
        // this.id = id;
    // }

    public String toString() {
        return "g" + id;
    }
}
