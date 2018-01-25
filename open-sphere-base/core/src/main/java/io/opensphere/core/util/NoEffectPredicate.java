package io.opensphere.core.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Predicate that tests if a function returns its input without modification.
 *
 * @param <T> The input/output type of the function.
 */
public class NoEffectPredicate<T> implements Predicate<T>
{
    /** The function under test. */
    private final Function<? super T, ? extends T> myFunction;

    /**
     * Constructor.
     *
     * @param function The function to test.
     */
    public NoEffectPredicate(Function<? super T, ? extends T> function)
    {
        myFunction = function;
    }

    @Override
    public boolean test(T input)
    {
        return Objects.equals(myFunction.apply(input), input);
    }
}
