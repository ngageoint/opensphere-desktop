package io.opensphere.mantle.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * The Class StringUtils.
 */
public final class StringUtils
{
    /** The Constant SPACE. */
    private static final char SPACE = ' ';

    /** The Constant TAB. */
    private static final char TAB = '\t';

    /**
     * This function will take a source string and explode it based on the
     * string indexes provided in breakIndexes.
     *
     * Note: Duplicate indexes are ignored, and the string is sorted into
     * numeric order when called. So passed in index array may be altered into a
     * different order with this call.
     *
     * Note: if breakIndexes is null or zero length the source string is
     * returned in the String[]. Or if the source string is null.
     *
     * @param source the source stream
     * @param breakIndexes , the indexes on which to break up the string.
     * @param trimParts if true the parts will be trimmed of excess white spaces
     *            using String.trim()
     * @return a String[] containing the parts of the exploded string
     */
    public static String[] explode(String source, int[] breakIndexes, boolean trimParts)
    {
        String[] result = null;
        if (source != null && breakIndexes != null && breakIndexes.length > 0)
        {
            Arrays.sort(breakIndexes);

            List<String> partList = new ArrayList<>();
            int startIndex = 0;
            int curBreakIndex = 0;
            for (int i = 0; i < breakIndexes.length; i++)
            {
                curBreakIndex = breakIndexes[i];
                if (curBreakIndex == startIndex)
                {
                    continue;
                }
                if (curBreakIndex < source.length())
                {
                    partList.add(source.substring(startIndex, curBreakIndex));
                }
                else
                {
                    if (startIndex >= source.length())
                    {
                        continue;
                    }
                    partList.add(source.substring(startIndex));
                }
                startIndex = curBreakIndex;
            }
            if (startIndex <= source.length() - 1)
            {
                partList.add(source.substring(startIndex));
            }
            result = new String[partList.size()];
            for (int i = 0; i < partList.size(); i++)
            {
                result[i] = trimParts ? partList.get(i).trim() : partList.get(i);
            }
        }
        else
        {
            result = new String[1];
            result[0] = source;
        }

        return result;
    }

    /**
     * Parse one CSV record, while avoiding the excessively long function name.
     * @param line the CSV text
     * @return an array of field values
     */
    public static String[] parseCsv(String line)
    {
        return explodeLineOnUnquotedDelimiters(line, ',', "\"");
    }

    /**
     * Given a CSV line entry, tokenizes the line into parts by splitting on
     * commas, but not commas inside quotation sets.
     *
     * @param line - the line to explode
     * @param delimeterChar the delimiter char
     * @param quoteChar the quote char (single character only)
     * @return the String[] of the tokens
     */
    private static String[] explodeLineOnUnquotedDelimiters(String line, char delimeterChar, String quoteChar)
    {
        List<String> valueList = new ArrayList<>();
        int lastIndex = 0;
        boolean inQuote = false;

        for (int i = 0; i < line.length(); i++)
        {
            if (line.charAt(i) == quoteChar.charAt(0))
            {
                Character prevChar = findPreviousNonWhiteSpaceChar(line, i);
                Character nextChar = findNextNonWhiteSpaceChar(line, i);
                if (prevChar == null || prevChar.charValue() == delimeterChar || nextChar == null
                        || nextChar.charValue() == delimeterChar)
                {
                    inQuote = !inQuote;
                }
            }
            if (!inQuote)
            {
                if (line.charAt(i) == delimeterChar)
                {
                    String sub = line.substring(lastIndex, i);

                    sub = trimAndRemoveQuoteChars(quoteChar, sub);

                    valueList.add(sub.trim());
                    lastIndex = i + 1;
                }
                if (i == line.length() - 1)
                {
                    if (lastIndex < line.length())
                    {
                        String sub = line.substring(lastIndex);
                        sub = trimAndRemoveQuoteChars(quoteChar, sub);

                        valueList.add(sub.trim());
                    }
                    else if (line.charAt(line.length() - 1) == delimeterChar)
                    {
                        valueList.add("");
                    }
                }
            }
        }

        return !valueList.isEmpty() ? valueList.toArray(new String[valueList.size()]) : null;
    }

    /**
     * Given a multi-line sample of some fixed field data this function will
     * attempt to guess at where the column breaks are located.
     *
     * It does this by detecting either white space breaks or tab breaks and
     * histograming them by end index. And then thresholding by the provided
     * percentage where only occurrences with the number of detection equals or
     * exceeds the threshold are considered "Passing"
     *
     * This will work better with more data.
     *
     * @param sample the multi-line sample
     * @param thresholdPercentage - the threshold.
     * @return int[] if determined, null if none passed threshold
     */
    public static int[] guessColumnBreaks(String sample, double thresholdPercentage)
    {
        TIntIntMap indexCountMap = new TIntIntHashMap();

        String[] lines = sample.split("\n");
        Pattern p = Pattern.compile("\\s+|\t+");

        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++)
        {
            Matcher m = p.matcher(lines[lineIdx]);

            while (m.find())
            {
                int end = m.end();
                indexCountMap.put(end, indexCountMap.get(end) + 1);
            }
        }

        TIntList passIndexes = new TIntArrayList();
        for (TIntIntIterator iter = indexCountMap.iterator(); iter.hasNext();)
        {
            iter.advance();
            double fraction = (double)iter.value() / (double)lines.length;
            if (fraction >= thresholdPercentage)
            {
                passIndexes.add(iter.key());
            }
        }

        if (passIndexes.isEmpty())
        {
            return null;
        }

        int[] breaks = null;

        passIndexes.sort();
        breaks = new int[passIndexes.size()];
        for (int i = 0; i < passIndexes.size(); i++)
        {
            breaks[i] = passIndexes.get(i);
        }

        return breaks;
    }

    /**
     * Find next non white space ( no tab, no space ) char.
     *
     * @param line the line
     * @param startIndex the start index
     * @return the character
     */
    private static Character findNextNonWhiteSpaceChar(String line, int startIndex)
    {
        Character aChar = null;
        if (startIndex < line.length())
        {
            for (int idx = startIndex + 1; idx < line.length(); idx++)
            {
                char c = line.charAt(idx);
                if (c != TAB && c != SPACE)
                {
                    aChar = Character.valueOf(c);
                    break;
                }
            }
        }
        return aChar;
    }

    /**
     * Find previous non white space ( no tab, no space ) char.
     *
     * @param line the line
     * @param startIndex the start index
     * @return the character
     */
    private static Character findPreviousNonWhiteSpaceChar(String line, int startIndex)
    {
        Character aChar = null;
        if (startIndex != 0)
        {
            for (int idx = startIndex - 1; idx >= 0; idx--)
            {
                char c = line.charAt(idx);
                if (c != TAB && c != SPACE)
                {
                    aChar = Character.valueOf(c);
                    break;
                }
            }
        }
        return aChar;
    }

    /**
     * Trim and remove quote chars.
     *
     * @param quoteChar the quote char
     * @param pSub the sub
     * @return the string
     */
    private static String trimAndRemoveQuoteChars(String quoteChar, String pSub)
    {
        String sub = pSub.trim();

        // Remove lead and trail quotes
        if (sub.startsWith(quoteChar))
        {
            sub = sub.substring(1);
        }

        if (sub.endsWith(quoteChar))
        {
            sub = sub.substring(0, sub.length() - 1);
        }
        return sub;
    }

    /**
     * Disallow instantiation.
     */
    private StringUtils()
    {
    }
}
