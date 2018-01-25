package io.opensphere.core.util.lang;

import java.util.List;

/** Splits a string into tokens. */
@FunctionalInterface
public interface StringTokenizer
{
    /**
     * Tokenize the string.
     *
     * @param input The string which is to be tokenized.
     * @return A list of string tokens.
     */
    List<String> tokenize(String input);
}
