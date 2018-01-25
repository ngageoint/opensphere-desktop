package io.opensphere.core.util.predicate;

import java.util.function.Predicate;

/**
 * A predicate that accepts objects that are not null.
 */
public class NonNullPredicate implements Predicate<Object>
{
    @Override
    public boolean test(Object t)
    {
        return t != null;
    }
}
