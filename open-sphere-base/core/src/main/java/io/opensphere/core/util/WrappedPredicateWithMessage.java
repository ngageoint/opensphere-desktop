package io.opensphere.core.util;

import java.util.function.Predicate;

/**
 * Abstract implementation of {@linkplain PredicateWithMessage} that takes a
 * {@link Predicate} that it uses for the test.
 *
 * @param <T> The input for the predicate.
 */
public abstract class WrappedPredicateWithMessage<T> implements PredicateWithMessage<T>
{
    /** The wrapped predicate. */
    private final Predicate<? super T> myPredicate;

    /**
     * Constructor.
     *
     * @param predicate The wrapped predicate.
     */
    public WrappedPredicateWithMessage(Predicate<? super T> predicate)
    {
        myPredicate = predicate;
    }

    @Override
    public boolean test(T input)
    {
        return myPredicate.test(input);
    }
}
