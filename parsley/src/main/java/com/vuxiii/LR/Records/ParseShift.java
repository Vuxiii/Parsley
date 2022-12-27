package com.vuxiii.LR.Records;

public record ParseShift(int id) implements ParseAction {
    // public final int id;

    // public ParseShift( int id ) {
    //     this.id = id;
    // }

    public String toString() {
        return "s" + id;
    }
    
}
