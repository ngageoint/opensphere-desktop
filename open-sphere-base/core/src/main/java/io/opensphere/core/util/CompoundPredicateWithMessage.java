package io.opensphere.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Combines multiple {@link PredicateWithMessage}s and returns {@code true} only
 * if all the wrapped predicates return {@code true}. The first predicate to
 * return {@code false} will be used for the message, or if all succeed, then
 * the last predicate will be used for the message.
 *
 * @param <T> The input for the predicate.
 */
public class CompoundPredicateWithMessage<T> implements PredicateWithMessage<T>
{
    /** The message. */
    private volatile String myMessage;

    /** The wrapped predicates. */
    private final Collection<? extends PredicateWithMessage<? super T>> myPredicates;

    /**
     * Constructor.
     *
     * @param predicates The wrapped predicates.
     */
    public CompoundPredicateWithMessage(Collection<? extends PredicateWithMessage<? super T>> predicates)
    {
        myPredicates = Collections.unmodifiableCollection(
                new ArrayList<PredicateWithMessage<? super T>>(Utilities.checkNull(predicates, "predicates")));
    }

    @Override
    public String getMessage()
    {
        return myMessage;
    }

    @Override
    public boolean test(T input)
    {
        for (PredicateWithMessage<? super T> predicate : myPredicates)
        {
            if (predicate.test(input))
            {
                myMessage = predicate.getMessage();
            }
            else
            {
                myMessage = predicate.getMessage();
                return false;
            }
        }
        return true;
    }
}
