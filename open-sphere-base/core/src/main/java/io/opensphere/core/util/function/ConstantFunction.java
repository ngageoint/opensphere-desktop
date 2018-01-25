package io.opensphere.core.util.function;

import java.util.function.Function;

/**
 * A function that returns a constant value regardless of input.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public class ConstantFunction<T, R> implements Function<T, R>
{
    /** The value. */
    private final R myValue;

    /**
     * Constructor.
     *
     * @param value the constant value
     */
    public ConstantFunction(R value)
    {
        myValue = value;
    }

    @Override
    public R apply(T t)
    {
        return myValue;
    }
}
