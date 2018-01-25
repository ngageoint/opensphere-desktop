package io.opensphere.core.util;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Predicate that matches a string with a regular expression.
 */
public class PatternPredicate implements Predicate<String>
{
    /** The pattern. */
    private final Pattern myPattern;

    /**
     * Constructor.
     *
     * @param pattern The pattern to be matched.
     */
    public PatternPredicate(Pattern pattern)
    {
        myPattern = Utilities.checkNull(pattern, "pattern");
    }

    /**
     * Constructor.
     *
     * @param pattern The pattern to be matched.
     */
    public PatternPredicate(String pattern)
    {
        this(Pattern.compile(pattern));
    }

    @Override
    public boolean test(String t)
    {
        return myPattern.matcher(t).matches();
    }
}
