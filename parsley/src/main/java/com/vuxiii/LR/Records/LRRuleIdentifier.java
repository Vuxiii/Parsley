package com.vuxiii.LR.Records;

public record LRRuleIdentifier(int id, int dot) {
    public String toString() {
        return "id: " + id + "\ndot: " + dot + "\n";
    }
}
