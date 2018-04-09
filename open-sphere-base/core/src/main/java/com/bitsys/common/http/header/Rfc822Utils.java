package com.bitsys.common.http.header;

/**
 * This class provides helper methods relating to RFC 822.
 */
public final class Rfc822Utils
{
    /**
     * Constructs a <code>Rfc822Utils</code>. This constructor is private to
     * prevent instantiation.
     */
    private Rfc822Utils()
    {
    }

    /**
     * Returns <code>true</code> if the given character falls under the
     * definition of <code>"specials"</code> as defined in RFC 822.
     *
     * @param ch the character to test.
     * @return <code>true</code> if the character is a <code>"specials"</code>
     *         character.
     */
    public static boolean isSpecial(final char ch)
    {
        boolean isSpecial;
        switch (ch)
        {
            case '(':
            case ')':
            case '<':
            case '>':
            case '@':
            case ',':
            case ';':
            case ':':
            case '\\':
            case '"':
            case '.':
            case '[':
            case ']':
                isSpecial = true;
                break;
            default:
                isSpecial = false;
                break;
        }
        return isSpecial;
    }
}
