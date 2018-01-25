package io.opensphere.core.util.predicate;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.function.ToLowerCaseFunction;

/**
 * A predicate that accepts strings that end with one of the suffixes in a
 * collection.
 */
public class EndsWithPredicate implements Predicate<String>
{
    /** If case should be ignored when comparing strings. */
    private final boolean myIgnoreCase;

    /** The allowed suffixes. */
    private final Collection<? extends String> mySuffixes;

    /**
     * Construct a predicate that accepts strings that end with any of the given
     * suffixes, case sensitive.
     *
     * @param suffixes The allowed suffixes.
     */
    public EndsWithPredicate(Collection<? extends String> suffixes)
    {
        this(suffixes, false);
    }

    /**
     * Construct a predicate that accepts strings that end with any of the given
     * suffixes, case sensitive.
     *
     * @param suffixes The allowed suffixes.
     */
    public EndsWithPredicate(String... suffixes)
    {
        this(Arrays.asList(suffixes), false);
    }

    /**
     * Construct a predicate that accepts strings that end with any of the given
     * suffixes.
     *
     * @param suffixes The allowed suffixes.
     * @param ignoreCase If case should be ignored when comparing strings.
     */
    public EndsWithPredicate(Collection<? extends String> suffixes, boolean ignoreCase)
    {
        Utilities.checkNull(suffixes, "suffixes");
        mySuffixes = New.unmodifiableCollection(ignoreCase ? StreamUtilities.map(suffixes, new ToLowerCaseFunction()) : suffixes);
        myIgnoreCase = ignoreCase;
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj != null && getClass() == obj.getClass() && myIgnoreCase == ((EndsWithPredicate)obj).myIgnoreCase
                && mySuffixes.size() == ((EndsWithPredicate)obj).mySuffixes.size()
                && mySuffixes.containsAll(((EndsWithPredicate)obj).mySuffixes);
    }

    @Override
    public int hashCode()
    {
        return 31 + (myIgnoreCase ? 1231 : 1237) + 31 * mySuffixes.hashCode();
    }

    @Override
    public boolean test(String input)
    {
        if (input == null)
        {
            return false;
        }
        String subject = myIgnoreCase ? input.toLowerCase() : input;
        for (String allowed : mySuffixes)
        {
            if (subject.endsWith(allowed))
            {
                return true;
            }
        }
        return false;
    }
}
