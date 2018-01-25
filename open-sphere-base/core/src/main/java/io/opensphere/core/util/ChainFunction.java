package io.opensphere.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

/**
 * A function that chains other functions together.
 *
 * @param <T> The input/output type of the functions.
 */
public class ChainFunction<T> implements Function<T, T>
{
    /** The wrapped functions. */
    private final Collection<? extends Function<? super T, ? extends T>> myFunctions;

    /**
     * Constructor.
     *
     * @param functions The functions to be chained.
     */
    public ChainFunction(Collection<? extends Function<? super T, ? extends T>> functions)
    {
        myFunctions = Utilities.checkNull(functions, "functions");
    }

    /**
     * Convenience constructor that takes two functions.
     *
     * @param arg1 The first function.
     * @param arg2 The second function.
     */
    public ChainFunction(Function<? super T, ? extends T> arg1, Function<? super T, ? extends T> arg2)
    {
        this(Arrays.<Function<? super T, ? extends T>>asList(arg1, arg2));
    }

    @Override
    public T apply(T input)
    {
        T result = input;
        for (Function<? super T, ? extends T> f : myFunctions)
        {
            result = f.apply(result);
        }
        return result;
    }
}
