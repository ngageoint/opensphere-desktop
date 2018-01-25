package io.opensphere.core.util.predicate;

import java.util.Collection;
import java.util.function.Predicate;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A predicate that accepts objects that are in a collection.
 */
public class InPredicate implements Predicate<Object>
{
    /** The allowed objects. */
    private final Collection<? extends Object> myAllowed;

    /**
     * Constructor.
     *
     * @param allowed The allowed objects.
     */
    public InPredicate(Collection<? extends Object> allowed)
    {
        myAllowed = Utilities.checkNull(New.unmodifiableCollection(allowed), "allowed");
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj != null && getClass() == obj.getClass()
                && myAllowed.size() == ((InPredicate)obj).myAllowed.size() && myAllowed.containsAll(((InPredicate)obj).myAllowed);
    }

    @Override
    public int hashCode()
    {
        return 31 + myAllowed.hashCode();
    }

    @Override
    public boolean test(Object input)
    {
        return myAllowed.contains(input);
    }
}
