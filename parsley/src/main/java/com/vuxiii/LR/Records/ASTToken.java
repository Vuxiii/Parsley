package com.vuxiii.LR.Records;

import com.vuxiii.Visitor.VisitorAcceptor;

public interface ASTToken extends VisitorAcceptor {
    public Term getTerm();
}