package io.opensphere.core.util.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.opensphere.core.util.Utilities;

/**
 * A tokenizer that uses a regular expression to select the tokens from a
 * string.
 */
public class PatternStringTokenizer implements StringTokenizer
{
    /** The compiled pattern. */
    private final Pattern myPattern;

    /**
     * Helper method to create a tokenizer for a string for which the division
     * indices are known. This works much the same as creating it from the
     * widths, except that all of the remainder of the string which follows the
     * last division is included in the last token.
     *
     * @param divisions The width of each token.
     * @return The newly create tokenizer.
     */
    public static PatternStringTokenizer createFromDivisions(int[] divisions)
    {
        return new PatternStringTokenizer(generatePatternFromColumnDivisions(divisions));
    }

    /**
     * Helper method to create a tokenizer for a string which has fixed width
     * tokens.
     *
     * @param widths The width of each token.
     * @return The newly create tokenizer.
     */
    public static PatternStringTokenizer createFromWidths(int[] widths)
    {
        return new PatternStringTokenizer(generatePatternFromColumnWidths(widths));
    }

    /**
     * Generate a regex pattern for tokenizing a string with fixed widths.
     *
     * @param divisions The indices at which the divisions occur.
     * @return The pattern.
     */
    public static String generatePatternFromColumnDivisions(final int[] divisions)
    {
        StringBuilder sb = new StringBuilder();
        int previous = 0;
        for (int index : divisions)
        {
            sb.append("(.{").append(index - previous).append("})");
            previous = index;
        }
        sb.append("(.*?)");
        return sb.toString();
    }

    /**
     * Generate a regex pattern for tokenizing a string with fixed widths.
     *
     * @param widths The widths of the tokens.
     * @return The pattern.
     */
    public static String generatePatternFromColumnWidths(final int[] widths)
    {
        StringBuilder sb = new StringBuilder();
        for (int width : widths)
        {
            sb.append("(.{").append(width).append("})");
        }
        return sb.toString();
    }

    /**
     * Constructor.
     *
     * @param pattern A regular expression with capture groups for each token in
     *            the string.
     */
    public PatternStringTokenizer(Pattern pattern)
    {
        myPattern = Utilities.checkNull(pattern, "pattern");
    }

    /**
     * Constructor.
     *
     * @param pattern A string to be compiled into a {@link Pattern}.
     */
    public PatternStringTokenizer(String pattern)
    {
        myPattern = Pattern.compile(Utilities.checkNull(pattern, "pattern"));
    }

    @Override
    public List<String> tokenize(String line)
    {
        Matcher matcher = myPattern.matcher(line);
        if (matcher.matches())
        {
            List<String> cells = new ArrayList<>(matcher.groupCount());
            for (int index = 1; index <= matcher.groupCount(); ++index)
            {
                cells.add(matcher.group(index));
            }
            return cells;
        }
        else
        {
            String[] cells = new String[matcher.groupCount()];
            Arrays.fill(cells, StringUtilities.EMPTY);
            return Arrays.asList(cells);
        }
    }
}
