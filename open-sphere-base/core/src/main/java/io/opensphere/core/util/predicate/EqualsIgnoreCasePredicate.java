package io.opensphere.core.util.predicate;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * A predicate that accepts strings that equal one of the strings in a
 * collection, ignoring case.
 */
public class EqualsIgnoreCasePredicate implements Predicate<String>
{
    /** The allowed strings. */
    private final Collection<? extends String> myStrings;

    /**
     * Construct a predicate that accepts strings that equal any of the given
     * strings.
     *
     * @param strings The allowed strings.
     */
    public EqualsIgnoreCasePredicate(Collection<? extends String> strings)
    {
        Utilities.checkNull(strings, "suffixes");
        myStrings = New.unmodifiableCollection(strings);
    }

    /**
     * Construct a predicate that accepts strings that equal the given string,
     * ignoring case.
     *
     * @param string The allowed string.
     */
    public EqualsIgnoreCasePredicate(String string)
    {
        this(Collections.singleton(string));
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj != null && getClass() == obj.getClass()
                && myStrings.size() == ((EqualsIgnoreCasePredicate)obj).myStrings.size()
                && myStrings.containsAll(((EqualsIgnoreCasePredicate)obj).myStrings);
    }

    @Override
    public int hashCode()
    {
        return 31 * myStrings.hashCode();
    }

    @Override
    public boolean test(String input)
    {
        if (input == null)
        {
            return false;
        }
        for (String allowed : myStrings)
        {
            if (input.equalsIgnoreCase(allowed))
            {
                return true;
            }
        }
        return false;
    }
}
