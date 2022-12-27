package com.vuxiii.LR.Records;

public record LRRuleIdentifier(int id, int dot) {
    
    // final int id;
    // final int dot;

    // public LRRuleIdentifier( int i, int d ) {
    //     id = i;
    //     dot = d;
    // }

    public String toString() {
        return "id: " + id + "\ndot: " + dot + "\n";
    }
}
