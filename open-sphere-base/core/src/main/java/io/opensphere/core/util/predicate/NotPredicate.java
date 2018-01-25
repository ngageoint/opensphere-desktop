package io.opensphere.core.util.predicate;

import java.util.function.Predicate;

import io.opensphere.core.util.Utilities;

/**
 * A predicate that accepts opposite objects from a wrapped predicate.
 *
 * @param <T> The input for the predicate.
 */
public class NotPredicate<T> implements Predicate<T>
{
    /** The wrapped predicate. */
    private final Predicate<T> myPredicate;

    /**
     * Constructor.
     *
     * @param predicate The predicate to be negated.
     */
    public NotPredicate(Predicate<T> predicate)
    {
        myPredicate = Utilities.checkNull(predicate, "predicate");
    }

    @Override
    public boolean test(T input)
    {
        return !myPredicate.test(input);
    }
}
