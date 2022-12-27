package com.vuxiii.LR.Records;

import com.vuxiii.Visitor.VisitorAcceptor;

public interface Token extends VisitorAcceptor {
    public Term getTerm();
}