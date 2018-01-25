package io.opensphere.core.util.function;

import java.util.function.Function;

/**
 * Function that appends a string to the input strings.
 */
public class AppendFunction implements Function<String, String>
{
    /** The string to append. */
    private final String myString;

    /**
     * Constructor.
     *
     * @param string The string to be appended.
     */
    public AppendFunction(String string)
    {
        myString = string;
    }

    @Override
    public String apply(String input)
    {
        return new StringBuilder().append(input).append(myString).toString();
    }
}
