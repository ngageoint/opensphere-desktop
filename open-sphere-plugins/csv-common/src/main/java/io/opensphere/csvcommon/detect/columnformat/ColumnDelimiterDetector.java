package io.opensphere.csvcommon.detect.columnformat;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import gnu.trove.iterator.TCharIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TCharDoubleHashMap;
import gnu.trove.map.hash.TCharIntHashMap;
import gnu.trove.map.hash.TCharObjectHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TCharIntProcedure;
import gnu.trove.set.TCharSet;
import gnu.trove.set.hash.TCharHashSet;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.TextDelimitedStringTokenizer;
import io.opensphere.csvcommon.common.LineSampler;
import io.opensphere.csvcommon.detect.LineDetector;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;

/**
 * Module that identifies the delimiter.
 */
@SuppressWarnings("PMD.GodClass")
public class ColumnDelimiterDetector implements LineDetector<DelimitedColumnFormatParameters>
{
    /**
     * The character used when no matching character for the condition was
     * found.
     */
    private static final char NO_MATCH = '\uE000';

    /**
     * The percentage of changed column counts before we throw away the
     * potential delimiter.
     */
    private static final float ourColumnCountChangeThreshold = .5f;

    /**
     * Gets the column count out of the rows for the given token and text
     * delimiters.
     *
     * @param rows the rows
     * @param token the token delimiter
     * @param text the text delimiter
     * @return the column count
     */
    private static int getColumnCount(Collection<String> rows, Character token, Character text)
    {
        int columnCount = 0;
        TextDelimitedStringTokenizer tokenizer = new TextDelimitedStringTokenizer(token.toString(),
                text == null ? null : text.toString());
        for (String row : rows)
        {
            int count = tokenizer.tokenize(row).size();
            if (count > columnCount)
            {
                columnCount = count;
            }
        }
        return columnCount;
    }

    /**
     * Gets the count of each potential delimiter in each row.
     *
     * @param rows the rows
     * @return the map of character to count by row
     */
    private static TCharObjectHashMap<int[]> getCountMap(List<String> rows)
    {
        TCharObjectHashMap<int[]> countMap = new TCharObjectHashMap<>();
        for (int r = 0; r < rows.size(); ++r)
        {
            String row = rows.get(r);

            // Get the set of potential delimiters
            TCharHashSet potentialDelimiters = new TCharHashSet();
            for (char ch : row.toCharArray())
            {
                if (isTokenDelimiter(ch))
                {
                    potentialDelimiters.add(ch);
                }
            }

            // Get the count of each delimiter in this row
            for (TCharIterator iter = potentialDelimiters.iterator(); iter.hasNext();)
            {
                char ch = iter.next();

                int[] counts = countMap.get(ch);
                if (counts == null)
                {
                    counts = new int[rows.size()];
                    countMap.put(ch, counts);
                }

                counts[r] = StringUtilities.count(ch, row);
            }
        }
        return countMap;
    }

    /**
     * Gets the score of each potential delimiter.
     *
     * @param countMap the count map
     * @param lineAvg The average number of characters per line.
     * @return the map of character to score
     */
    private static TCharDoubleHashMap getScoreMap(TCharObjectHashMap<int[]> countMap, double lineAvg)
    {
        TCharDoubleHashMap scoreMap = new TCharDoubleHashMap();

        // When a delimiter occurs less than this threshold on average its score
        // will be adjusted down.
        final double delimiterMinCountThreshold = 3.;
        // When the average token size based on the line length and delimiter
        // occurrence is more than this value the delimiter's score will be
        // adjusted down.
        final double tokenSizeMaxThreshold = 100.;

        // Calculate the score as the number of rows of the most consistent
        // count times the the most consistent count
        for (char ch : countMap.keys())
        {
            double countAvg = MathUtil.average(countMap.get(ch));
            double dev = MathUtil.standardDeviation(countMap.get(ch));
            double adjustment = dev > 1 ? dev : 1;
            adjustment = countAvg < delimiterMinCountThreshold ? adjustment * 10 : adjustment;
            adjustment = lineAvg / countAvg > tokenSizeMaxThreshold ? adjustment * 3 : adjustment;
            double score = countAvg / adjustment;
            scoreMap.put(ch, score);
        }

        // Convert to confidence
        TDoubleArrayList values = new TDoubleArrayList(scoreMap.values());
        double maxScore = 0.0;
        if (!values.isEmpty())
        {
            maxScore = values.max();
        }
        for (char ch : scoreMap.keys())
        {
            double confidence = scoreMap.get(ch) * getScoreMultiplier(ch, countMap) / maxScore;
            scoreMap.put(ch, confidence);
        }

        return scoreMap;
    }

    /**
     * Gets the score multiplier for the given character.
     *
     * @param ch the character
     * @param countMap The map of counts.
     * @return the score multiplier
     */
    private static double getScoreMultiplier(char ch, TCharObjectHashMap<int[]> countMap)
    {
        final double nonCommaMultiplier = 0.9;
        final double nonHeaderSubtracter = 0.4;
        double multiplier = ch == ',' ? 1.0 : nonCommaMultiplier;

        if (countMap.containsKey(ch))
        {
            int[] counts = countMap.get(ch);

            boolean doesPotentialHeaderRowContainChar = counts == null || counts.length == 0 || counts[0] != 0;

            if (!doesPotentialHeaderRowContainChar)
            {
                multiplier -= nonHeaderSubtracter;
            }
        }

        return multiplier;
    }

    /**
     * Determines if the given character is a potential text delimiter.
     *
     * @param ch the character
     * @return Whether it is a potential text delimiter
     */
    private static boolean isTextDelimiter(char ch)
    {
        return !(Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || ch == '-');
    }

    /**
     * Determines if the given character is a potential text delimiter.
     *
     * @see #isTextDelimiter(char)
     * @param ch the character
     * @return Whether it is a potential text delimiter and is not null
     */
    private static boolean isTextDelimiter(Character ch)
    {
        return ch != null && isTextDelimiter(ch.charValue());
    }

    /**
     * Determines if the given character is a potential token delimiter.
     *
     * @param ch the character
     * @return Whether it is a potential token delimiter
     */
    private static boolean isTokenDelimiter(char ch)
    {
        return !Character.isLetterOrDigit(ch);
    }

    /**
     * {@inheritDoc}
     * <p>
     * In this case the returned value is a combined parameter. The first
     * element is the token delimiter (for example, "," or ":") the second
     * element is the text delimiter (for example, a quote character)
     */
    @Override
    public ValuesWithConfidence<DelimitedColumnFormatParameters> detect(LineSampler sampler)
    {
        List<String> processedRows = preprocessRows(sampler);

        int[] lineLengths = new int[processedRows.size()];
        for (int r = 0; r < processedRows.size(); ++r)
        {
            String row = processedRows.get(r);
            lineLengths[r] = row.length();
        }
        double lineAvg = MathUtil.average(lineLengths);

        TCharObjectHashMap<int[]> countMap = getCountMap(processedRows);
        TCharDoubleHashMap scoreMap = getScoreMap(countMap, lineAvg);

        char firstDelimiter = NO_MATCH;
        float firstConfidence = 0f;
        float secondConfidence = 0f;
        char secondDelimiter = NO_MATCH;
        int columnCount = 0;
        for (char potentialDelimiter : scoreMap.keys())
        {
            float confidence = (float)scoreMap.get(potentialDelimiter);
            if (confidence > firstConfidence)
            {
                secondConfidence = firstConfidence;
                secondDelimiter = firstDelimiter;
                firstDelimiter = potentialDelimiter;
                firstConfidence = confidence;
                columnCount = MathUtil.mode(countMap.get(potentialDelimiter), 0).getValue().intValue() + 1;
            }
            else if (confidence > secondConfidence)
            {
                secondDelimiter = potentialDelimiter;
                secondConfidence = confidence;
            }
        }

        if (determineSwapDelimiters(processedRows, firstDelimiter, secondDelimiter))
        {
            // The first delimiter is probably the text delimiter and the
            // second the token delimiter.
            char holdDelimiter = firstDelimiter;
            firstDelimiter = secondDelimiter;
            secondDelimiter = holdDelimiter;
            float holdConfidence = firstConfidence;
            firstConfidence = secondConfidence;
            secondConfidence = holdConfidence;
        }

        Character token = null;
        Character text = null;
        if (firstDelimiter != NO_MATCH)
        {
            token = Character.valueOf(firstDelimiter);
            secondDelimiter = lookForTextDelimiter(processedRows, firstDelimiter);
            text = secondDelimiter == NO_MATCH ? null : Character.valueOf(secondDelimiter);
            // Calculate a more accurate column count
            columnCount = getColumnCount(processedRows, token, text);
        }
        else
        {
            token = Character.valueOf(',');
            columnCount = getColumnCount(processedRows, token, text);
        }

        ValueWithConfidence<DelimitedColumnFormatParameters> result = new ValueWithConfidence<>();
        result.setValue(new DelimitedColumnFormatParameters(token, text, columnCount));
        result.setConfidence(firstConfidence);
        return new ValuesWithConfidence<DelimitedColumnFormatParameters>(result);
    }

    /**
     * There may be conditions where we detect the text delimiter as the token
     * delimiter when it occurs consistently as in the case where every column
     * is quoted.
     *
     * @param rows The sample rows which are used for detection
     * @param firstDelimiter The current best guess for the token delimiter.
     * @param secondDelimiter The current second best guess for the token
     *            delimiter.
     * @return true when it appears that the second delimiter is actually the
     *         token delimiter and the first delimiter is the text delimiter.
     */
    private boolean determineSwapDelimiters(List<String> rows, char firstDelimiter, char secondDelimiter)
    {
        /* If the first delimiter is a comma, the delimiters are probably
         * already in the correct order. */
        if (firstDelimiter == ',')
        {
            return false;
        }

        // If the second delimiter is a comma it is probably really first.
        char testFirst = firstDelimiter;
        char testSecond = secondDelimiter;
        boolean testSwitched = false;
        if (secondDelimiter == ',')
        {
            testFirst = secondDelimiter;
            testSecond = firstDelimiter;
            testSwitched = true;
        }

        /* When the second delimiter is white space, this will usually indicate
         * that the file has been formatted to be more easily human readable, it
         * is unlikely that the first delimiter is the text delimiter in this
         * case. */
        if (testFirst != NO_MATCH && testSecond != NO_MATCH && !Character.isWhitespace(testSecond))
        {
            // This patter is looking for cases that look like ,"<some stuff>",
            StringBuilder pattern = new StringBuilder();
            // second delimiter or beginning of line followed by the first
            // delimiter
            pattern.append("[\\").append(testSecond).append("^]\\").append(testFirst);
            // followed by at least one character which is not either delimiter
            pattern.append("[^\\").append(testFirst).append('\\').append(testSecond).append("]+?\\");
            // followed by the first delimiter, followed by the second delimiter
            // or the end of line
            pattern.append(testFirst).append("[$\\").append(testSecond).append(']');
            Pattern pat = Pattern.compile(pattern.toString());
            int matches = 0;
            for (String row : rows)
            {
                Matcher mat = pat.matcher(row);
                while (mat.find())
                {
                    ++matches;
                }
            }
            return matches > rows.size() ^ testSwitched;
        }

        return false ^ testSwitched;
    }

    /**
     * Look for the text delimiter, these should occur next to the token
     * delimiter.
     *
     * @param sampleLines The sample lines to search.
     * @param tokenDelimiter The assumed token delimiter.
     * @return Our best guess at the text delimiter.
     */
    private char lookForTextDelimiter(List<? extends String> sampleLines, char tokenDelimiter)
    {
        TCharIntHashMap delimCounts = new TCharIntHashMap();
        List<TCharIntHashMap> delimTokenCounts = New.list();
        TIntList rowsDelimCounts = new TIntArrayList();
        TIntObjectMap<Boolean> rowHasNewlineWithinTextDelim = new TIntObjectHashMap<>();

        int expectedColumnCount = -1;
        int rowIndex = 0;
        for (String line : sampleLines)
        {
            int tokenCount = lookForTextDelimiterInRow(rowHasNewlineWithinTextDelim, rowIndex, line, tokenDelimiter, delimCounts,
                    delimTokenCounts);
            rowsDelimCounts.add(tokenCount);

            boolean rowHasTextDelimiter = !delimTokenCounts.get(delimTokenCounts.size() - 1).isEmpty();
            if (!rowHasTextDelimiter && expectedColumnCount == -1 && tokenCount > 0)
            {
                expectedColumnCount = tokenCount;
            }

            rowIndex++;
        }

        // If any of the text delimiters change the expected column remove it.
        if (expectedColumnCount != -1)
        {
            removeDelimiters(delimCounts, delimTokenCounts, rowsDelimCounts, expectedColumnCount, rowHasNewlineWithinTextDelim);
        }

        MaxFinder procedure = new MaxFinder();
        delimCounts.forEachEntry(procedure);
        return procedure.getMaxOccuringCharacter();
    }

    /**
     * Looks for text delimiters within the row.
     *
     * @param rowHasNewlineWithinTextDelim Indicates if the row has a newline
     *            within a potential text delimiter.
     * @param rowIndex The row index.
     * @param line The row to analyze.
     * @param tokenDelimiter The token delimiter.
     * @param delimCounts The map to had text delimiters and its counts to.
     * @param delimTokenCounts The number of token delimiters within potential
     *            text delimiters for each row.
     * @return The total number of token delimiters in this row.
     */
    private int lookForTextDelimiterInRow(TIntObjectMap<Boolean> rowHasNewlineWithinTextDelim, int rowIndex, String line,
            char tokenDelimiter, TCharIntHashMap delimCounts, List<TCharIntHashMap> delimTokenCounts)
    {
        rowHasNewlineWithinTextDelim.put(rowIndex, Boolean.FALSE);
        Character previousChar = null;
        char currentChar;
        Character nextChar;
        TCharSet currentDelimiter = new TCharHashSet();

        int tokenCount = 0;
        int tokenWithinDelimiterCount = 0;
        int delimiterWithinDelimiterCount = 0;
        TCharIntHashMap rowsDelimTokenCounts = new TCharIntHashMap();
        for (int i = 0; i < line.length(); ++i)
        {
            currentChar = line.charAt(i);
            nextChar = i + 1 < line.length() ? Character.valueOf(line.charAt(i + 1)) : null;

            // Start of a text delimiter
            if (!currentDelimiter.contains(currentChar) && currentChar != tokenDelimiter
                    && (nextChar == null || currentChar != nextChar.charValue()) && isTextDelimiter(currentChar)
                    && (previousChar == null || previousChar.charValue() == tokenDelimiter))
            {
                currentDelimiter.add(currentChar);
                tokenWithinDelimiterCount = 0;
            }
            // End of a text delimiter
            else if (currentDelimiter.contains(currentChar) && (nextChar == null || nextChar.charValue() == tokenDelimiter
                    && (!isTextDelimiter(previousChar) || Character.valueOf('.').compareTo(previousChar) == 0)))
            {
                if (delimiterWithinDelimiterCount == 0)
                {
                    delimiterWithinDelimiterCount = 0;
                    delimCounts.put(currentChar, delimCounts.get(currentChar) + 1);

                    if (!rowsDelimTokenCounts.containsKey(currentChar))
                    {
                        rowsDelimTokenCounts.put(currentChar, 0);
                    }

                    rowsDelimTokenCounts.put(currentChar, rowsDelimTokenCounts.get(currentChar) + tokenWithinDelimiterCount);
                    tokenWithinDelimiterCount = 0;
                }

                currentDelimiter.remove(currentChar);
            }
            else if (!currentDelimiter.isEmpty() && currentChar == tokenDelimiter)
            {
                tokenWithinDelimiterCount++;
            }
            else if (currentDelimiter.contains(currentChar) && nextChar != null && nextChar.charValue() != tokenDelimiter
                    && !isTextDelimiter(nextChar.charValue()) && currentChar != nextChar.charValue() && previousChar != null
                    && currentChar != previousChar.charValue())
            {
                delimiterWithinDelimiterCount++;
            }

            if (currentChar == tokenDelimiter)
            {
                tokenCount++;
            }

            previousChar = Character.valueOf(currentChar);
        }

        if (!currentDelimiter.isEmpty())
        {
            rowHasNewlineWithinTextDelim.put(rowIndex, Boolean.TRUE);
        }

        delimTokenCounts.add(rowsDelimTokenCounts);

        return tokenCount;
    }

    /**
     * Removes the delimiters if the delimiters affect column counts, which
     * means they are not delimiters.
     *
     * @param delimCounts The map to remove delimiters from.
     * @param delimTokenCounts The number of token delimiters in between each
     *            text delimiter for each row.
     * @param rowsDelimCounts The total number of delimiter counts for a given
     *            row.
     * @param expectedColumnCount The number of expected columns.
     * @param rowHasNewlineWithinTextDelim Indicates if there is a newline
     *            within a potential text delimiter. If that is true than the
     *            column count check will be inaccurate.
     */
    private void removeDelimiters(TCharIntHashMap delimCounts, List<TCharIntHashMap> delimTokenCounts, TIntList rowsDelimCounts,
            int expectedColumnCount, TIntObjectMap<Boolean> rowHasNewlineWithinTextDelim)
    {
        for (char delimiter : delimCounts.keys())
        {
            int rowIndex = 0;
            int columnCountChanged = 0;
            for (TCharIntHashMap rowsDelimTokenCounts : delimTokenCounts)
            {
                if (!(Boolean.TRUE.equals(rowHasNewlineWithinTextDelim.get(rowIndex))
                        || Boolean.TRUE.equals(rowHasNewlineWithinTextDelim.get(rowIndex - 1))))
                {
                    int columnCount = rowsDelimCounts.get(rowIndex) - rowsDelimTokenCounts.get(delimiter);
                    if (-1 * (columnCount - expectedColumnCount) > expectedColumnCount / 2)
                    {
                        columnCountChanged++;
                    }
                }

                rowIndex++;
            }

            if ((float)columnCountChanged / delimTokenCounts.size() > ourColumnCountChangeThreshold)
            {
                delimCounts.remove(delimiter);
            }
        }
    }

    /**
     * Perform some conditioning on the rows to make detection easier.
     *
     * @param sampler The line sampler used to sample the data.
     * @return The processed rows.
     */
    private List<String> preprocessRows(LineSampler sampler)
    {
        final WktRemover remover = new WktRemover();

        /* Since we are only using this to identify the delimiter, it is not a
         * problem to compress the white space characters. If a white space
         * character is the delimiter it will most likely still be correctly
         * detected. We also remove text which occurs within matching
         * braces/parentheses in order to reduce the risk of detecting the
         * separator within the bracketed text as the separator for the line. */
        List<String> reducedRows = StreamUtilities.map(sampler.getBeginningSampleLines(), new Function<String, String>()
        {
            @Override
            public String apply(String row)
            {
                String reduced = row.replaceAll(" +", " ");
                reduced = reduced.replaceAll("\t+", "\t");

                reduced = remover.removeWktData(reduced);

                return reduced;
            }
        });

        List<String> processedRows = New.list();
        for (String row : reducedRows)
        {
            if (!StringUtils.isBlank(row))
            {
                processedRows.add(row);
            }
        }

        return processedRows;
    }

    /**
     * A helper class to look for the character which occurs most in the map.
     */
    private static class MaxFinder implements TCharIntProcedure
    {
        /** The number of times the most common character occurs. */
        private int myMaxCount;

        /** The character which occurs most often. */
        private char myMaxOccuringCharacter = NO_MATCH;

        @Override
        public boolean execute(char delimiter, int count)
        {
            if (count > myMaxCount)
            {
                myMaxCount = count;
                myMaxOccuringCharacter = delimiter;
            }
            return true;
        }

        /**
         * Get the maxOccuringCharacter.
         *
         * @return the maxOccuringCharacter
         */
        public char getMaxOccuringCharacter()
        {
            return myMaxOccuringCharacter;
        }
    }
}
