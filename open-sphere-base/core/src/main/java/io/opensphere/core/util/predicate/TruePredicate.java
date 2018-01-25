package io.opensphere.core.util.predicate;

import java.util.function.Predicate;

/** A {@link Predicate} that always returns {@code true}. */
public class TruePredicate implements Predicate<Object>
{
    @Override
    public boolean test(Object input)
    {
        return true;
    }
}
