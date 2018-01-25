package io.opensphere.core.common.util;

import org.apache.commons.lang.StringUtils;

/**
 * A set of utility methods for use with Strings. Extends the Apache Commons
 * {@link StringUtils} class to prevent unneeded import statements.
 */
public class EnhancedStringUtils extends StringUtils
{
    /**
     * Tests to determine if the supplied String is quoted. A string is
     * considered quoted if the first character is either a single or a double
     * quote, and the last character is the same as the first character.
     *
     * @param pString the string to test.
     * @return true if the supplied string's first character is the same as its
     *         last character, and those characters are either both a double
     *         quote, or both a single quote.
     */
    public static boolean isQuoted(String pString)
    {
        boolean returnValue;
        returnValue = pString.startsWith("\"") && pString.endsWith("\"") || pString.startsWith("'") && pString.endsWith("'");
        return returnValue;
    }

    /**
     * Removes the quotes surrounding the supplied string, if present. Both
     * double and single quotes will be removed. If the string is quoted
     * multiple times (e.g.: "'foo'"), only the outer-most set of quotes will be
     * removed. If the supplied string is not quoted, no action is taken and the
     * supplied string is returned unmodified.
     *
     * @param pString the string from which to remove the quotes.
     * @return the supplied string, minus any surrounding double or single
     *         quotes (if present).
     */
    public static String unquote(String pString)
    {
        String returnValue = pString;
        if (pString.startsWith("\"") && pString.endsWith("\""))
        {
            returnValue = substringBetween(pString, "\"");
        }
        else if (pString.startsWith("'") && pString.endsWith("'"))
        {
            returnValue = substringBetween(pString, "'");
        }

        return returnValue;
    }
}
