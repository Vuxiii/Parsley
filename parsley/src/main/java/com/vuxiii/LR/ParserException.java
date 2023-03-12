package com.vuxiii.LR;

import com.vuxiii.LR.Records.ASTToken;
import com.vuxiii.LR.Records.ParseAction;

public class ParserException extends Exception {
    public final ASTToken ast;
    public final ParsingStep step_of_failure;
    public final ParseAction action_causing_failure;

    public ParserException(ASTToken output, ParsingStep parsingStep, ParseAction action  ) {
        super();
        ast = output;
        step_of_failure = parsingStep;
        action_causing_failure = action;
    }
}
