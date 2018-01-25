package io.opensphere.core.util;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Predicate that matches a regular expression with a String.
 */
public class InversePatternPredicate implements Predicate<String>
{
    /** The string to be matched. */
    private final String myInput;

    /**
     * Constructor.
     *
     * @param input The string to be matched.
     */
    public InversePatternPredicate(String input)
    {
        myInput = Utilities.checkNull(input, "input");
    }

    @Override
    public boolean test(String regex)
    {
        return Pattern.matches(regex, myInput);
    }
}
