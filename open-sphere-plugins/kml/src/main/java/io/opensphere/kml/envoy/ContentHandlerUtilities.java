package io.opensphere.kml.envoy;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * ContentHandler utilities.
 */
public final class ContentHandlerUtilities
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(ContentHandlerUtilities.class);

    /**
     * Assigns the character array to the values in the given String.
     *
     * @param ch The character array
     * @param start The start index
     * @param length The length
     * @param s The String to assign from
     * @param tag The tag
     * @return The new length
     */
    public static int assign(char[] ch, int start, int length, String s, String tag)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(StringUtilities.concat("Replacing <", tag, "> value of '", new String(ch, start, length), "' with '", s,
                    "'"));
        }
        int i = start;
        for (char c : s.toCharArray())
        {
            ch[i++] = c;
        }
        return s.length();
    }

    /**
     * Determines whether the character array contains searchChar.
     *
     * @param ch The character array
     * @param start The start index
     * @param length The length
     * @param searchChar The character for which to search
     * @return Whether the character array contains searchChar
     */
    public static boolean contains(char[] ch, int start, int length, char searchChar)
    {
        boolean contains = false;
        for (int i = start, end = start + length; i < end; i++)
        {
            if (ch[i] == searchChar)
            {
                contains = true;
                break;
            }
        }
        return contains;
    }

    /**
     * Determines whether the character array contains the String.
     *
     * @param ch The character array
     * @param start The start index
     * @param length The length
     * @param s The String for which to search
     * @return Whether the character array contains searchChar
     */
    public static boolean contains(char[] ch, int start, int length, String s)
    {
        return new String(ch, start, length).contains(s);
    }

    /**
     * Determines whether the character array equals the String.
     *
     * @param ch The character array
     * @param start The start index
     * @param length The length
     * @param s The String to with which to compare
     * @return Whether the character array equals the String
     */
    public static boolean equalsString(char[] ch, int start, int length, String s)
    {
        char[] ch2 = s.toCharArray();
        if (ch2.length != length)
        {
            return false;
        }
        for (int i = start, j = 0; j < length; i++, j++)
        {
            if (ch[i] != ch2[j])
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Takes a String containing '+' symbols and adds any integers between them
     * and returns the sum as a String.
     *
     * @param ch The character array
     * @param start The start index
     * @param length The length
     * @return The sum of the values as a String
     */
    public static String sumOfNumbers(char[] ch, int start, int length)
    {
        String[] toks = new String(ch, start, length).split("\\+");
        int sum = 0;
        for (String tok : toks)
        {
            try
            {
                sum += Integer.parseInt(tok);
            }
            catch (NumberFormatException e)
            {
                LOGGER.warn(e.getMessage());
            }
        }
        return String.valueOf(sum);
    }

    /** Private constructor. */
    private ContentHandlerUtilities()
    {
    }
}
