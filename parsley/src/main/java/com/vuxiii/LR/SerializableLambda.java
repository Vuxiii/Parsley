package com.vuxiii.LR;

import java.io.Serializable;
import java.util.function.Function;

public interface SerializableLambda<T, R> extends Function<T, R>, Serializable {

}
