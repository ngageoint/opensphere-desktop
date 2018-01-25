package io.opensphere.core.util.predicate;

import java.util.Collection;

/**
 * A predicate that accepts objects that are not in a collection.
 */
public class NotInPredicate extends NotPredicate<Object>
{
    /**
     * Constructor.
     *
     * @param disallowed The disallowed objects.
     */
    public NotInPredicate(Collection<? extends Object> disallowed)
    {
        super(new InPredicate(disallowed));
    }
}
