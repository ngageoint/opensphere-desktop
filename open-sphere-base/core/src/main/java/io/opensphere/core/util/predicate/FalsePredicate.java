package io.opensphere.core.util.predicate;

import java.util.function.Predicate;

/** A {@link Predicate} that always returns {@code false}. */
public class FalsePredicate implements Predicate<Object>
{
    @Override
    public boolean test(Object input)
    {
        return false;
    }
}
