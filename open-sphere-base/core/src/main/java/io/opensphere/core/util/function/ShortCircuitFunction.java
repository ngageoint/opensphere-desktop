package io.opensphere.core.util.function;

import java.util.function.Function;

/**
 * Function that always returns the input.
 *
 * @param <T> The type of the input/result.
 */
public class ShortCircuitFunction<T> implements Function<T, T>
{
    @Override
    public T apply(T t)
    {
        return t;
    }
}
