package com.vuxiii.LR.Records;

import java.io.Serializable;

public record LRRuleIdentifier(int id, int dot) implements Serializable {
    public String toString() {
        return "id: " + id + "\ndot: " + dot + "\n";
    }
}
