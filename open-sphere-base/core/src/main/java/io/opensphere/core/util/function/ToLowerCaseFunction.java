package io.opensphere.core.util.function;

import java.util.function.Function;

/**
 * Function that converts strings to lower case.
 */
public class ToLowerCaseFunction implements Function<String, String>
{
    @Override
    public String apply(String input)
    {
        return input == null ? null : input.toLowerCase();
    }
}
