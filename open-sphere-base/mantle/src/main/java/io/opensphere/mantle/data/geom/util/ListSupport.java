package io.opensphere.mantle.data.geom.util;

import java.util.LinkedList;
import java.util.List;

/**
 * This class supports a very simple conversion from String to List or vice
 * versa.  Escape sequences are used to ensure that nothing is lost in the
 * translation in either direction.
 */
public class ListSupport
{
    private char escapeChar = '\\';
    private char delimiter = ';';

    /**
     * Construct a ListSupport with default configuration:  backslash as the
     * escape character and semi-colon as the delimiter.
     */
    public ListSupport()
    {
    }

    /**
     * Construct a ListSupport with the specified escape character and
     * delimiter.
     * @param esc the escape character
     * @param delim the delimiter
     */
    public ListSupport(char esc, char delim)
    {
        escapeChar = esc;
        delimiter = delim;
    }

    /**
     * Ingest a String and convert it to a List.
     * @param s the String representation
     * @return the represented List
     */
    public List<String> parseList(String s)
    {
        List<String> ret = new LinkedList<>();
        if (s == null || s.isEmpty())
        {
            return ret;
        }
        boolean esc = false;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < s.length(); i++)
        {
            char ch = s.charAt(i);
            if (!esc && ch == escapeChar)
            {
                esc = true;
                continue;
            }
            if (esc)
            {
                esc = false;
                if (ch == delimiter)
                {
                    ret.add(buf.toString());
                    buf.setLength(0);
                    continue;
                }
            }
            buf.append(ch);
        }
        ret.add(buf.toString());
        return ret;
    }

    /**
     * Convert a List to its String representation.
     * @param tokens the List (of Strings)
     * @return a String containing tokens from the specified List
     */
    public String writeList(List<String> tokens)
    {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (String tok :  tokens)
        {
            if (!first)
            {
                buf.append(escapeChar);
                buf.append(delimiter);
            }
            first = false;
            for (int i = 0; i < tok.length(); i++)
            {
                char ch = tok.charAt(i);
                if (ch == escapeChar)
                {
                    buf.append(escapeChar);
                }
                buf.append(ch);
            }
        }
        return buf.toString();
    }
}
