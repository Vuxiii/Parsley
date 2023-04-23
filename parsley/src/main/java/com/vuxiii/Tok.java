package com.vuxiii;

import com.vuxiii.LR.Records.Term;

import java.io.Serializable;

import com.vuxiii.LR.Records.ASTToken;
import com.vuxiii.Visitor.VisitorBase;

public class Tok implements ASTToken, Serializable {

    public Tok() {}

    @Override
    public Term getTerm() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void accept(VisitorBase visitor) {
        // TODO Auto-generated method stub
        
    }
    
}
