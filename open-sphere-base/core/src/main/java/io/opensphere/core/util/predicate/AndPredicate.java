package io.opensphere.core.util.predicate;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import io.opensphere.core.util.Utilities;

/**
 * A predicate that performs a logical AND of other predicates.
 *
 * @param <T> The input for the predicate.
 */
public class AndPredicate<T> implements Predicate<T>
{
    /** The wrapped predicates. */
    private final Collection<? extends Predicate<? super T>> myPredicates;

    /**
     * Constructor.
     *
     * @param predicates The predicates to be ANDed.
     */
    public AndPredicate(Collection<? extends Predicate<? super T>> predicates)
    {
        myPredicates = Utilities.checkNull(predicates, "predicates");
    }

    /**
     * Convenience constructor that takes two predicates.
     *
     * @param arg1 The first predicate.
     * @param arg2 The second predicate.
     */
    @SuppressWarnings("unchecked")
    public AndPredicate(Predicate<? super T> arg1, Predicate<? super T> arg2)
    {
        this(Arrays.<Predicate<? super T>>asList(arg1, arg2));
    }

    @Override
    public boolean test(T input)
    {
        for (Predicate<? super T> p : myPredicates)
        {
            if (!p.test(input))
            {
                return false;
            }
        }
        return true;
    }
}
