package io.opensphere.csvcommon.detect;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.core.util.lang.TextDelimitedStringTokenizer;
import io.opensphere.csvcommon.common.CellSampler;

/**
 * A simple cell sampler that gets its data from a list of strings.
 */
public class ListCellSampler implements CellSampler
{
    /** The list of strings. */
    private final List<String> myBeginLines;

    /** The beginning sample cells. */
    private final List<List<String>> myBeginningSampleCells;

    /** The ending sample cells. */
    private final List<List<String>> myEndingSampleCells;

    /** The list of strings representing the end lines. */
    private final List<String> myEndLines;

    /** The header cells. */
    private final List<String> myHeaderCells;

    /**
     * Constructor.
     *
     * @param beginLines The list containing the lines.
     * @param delimiter The delimiter
     * @param quoteChar The quote character
     */
    public ListCellSampler(List<String> beginLines, Character delimiter, Character quoteChar)
    {
        this(beginLines, Collections.<String>emptyList(), delimiter, quoteChar, false);
    }

    /**
     * Constructor.
     *
     * @param beginLines The list containing the lines.
     * @param endLines The list containing the ending lines.
     * @param delimiter The delimiter
     * @param quoteChar The quote character
     */
    public ListCellSampler(List<String> beginLines, List<String> endLines, Character delimiter, Character quoteChar)
    {
        this(beginLines, endLines, delimiter, quoteChar, false);
    }

    /**
     * Constructor.
     *
     * @param beginLines The list containing the beginning lines.
     * @param endLines The list containing the ending lines.
     * @param delimiter The delimiter
     * @param quoteChar The quote character
     * @param includeHeaderInSample whether to include the header in the sample
     *            data
     */
    public ListCellSampler(List<String> beginLines, List<String> endLines, Character delimiter, Character quoteChar,
            boolean includeHeaderInSample)
    {
        myBeginLines = Collections.unmodifiableList(beginLines);
        myEndLines = Collections.unmodifiableList(endLines);

        String tokenDelimiter = String.valueOf(delimiter.charValue());
        String textDelimiter = quoteChar == null ? "\"" : String.valueOf(quoteChar.charValue());
        final StringTokenizer tokenizer = new TextDelimitedStringTokenizer(tokenDelimiter, textDelimiter);

        // TODO use header/data line values

        List<String> headerCells = includeHeaderInSample || myBeginLines.isEmpty() ? Collections.<String>emptyList()
                : tokenizer.tokenize(myBeginLines.get(0));
        myHeaderCells = Collections.unmodifiableList(headerCells);

        List<List<String>> beginningSampleCells;
        if (myBeginLines.size() > 1)
        {
            List<String> sampleLines = includeHeaderInSample ? myBeginLines : myBeginLines.subList(1, myBeginLines.size());
            beginningSampleCells = StreamUtilities.map(sampleLines, new Function<String, List<String>>()
            {
                @Override
                public List<String> apply(String line)
                {
                    return tokenizer.tokenize(line);
                }
            });
        }
        else
        {
            beginningSampleCells = Collections.emptyList();
        }
        myBeginningSampleCells = Collections.unmodifiableList(beginningSampleCells);

        myEndingSampleCells = Collections.unmodifiableList(StreamUtilities.map(myEndLines, new Function<String, List<String>>()
        {
            @Override
            public List<String> apply(String line)
            {
                return tokenizer.tokenize(line);
            }
        }));
    }

    @Override
    public void close()
    {
    }

    @Override
    public List<? extends List<? extends String>> getBeginningSampleCells()
    {
        return myBeginningSampleCells;
    }

    @Override
    public List<? extends String> getBeginningSampleLines()
    {
        return myBeginLines;
    }

    @Override
    public List<? extends List<? extends String>> getEndingSampleCells()
    {
        return myEndingSampleCells;
    }

    @Override
    public int getEndingSampleLinesIndexOffset()
    {
        return myBeginningSampleCells.size() + 10;
    }

    @Override
    public List<? extends String> getEndingSampleLines()
    {
        return Collections.emptyList();
    }

    @Override
    public List<? extends String> getHeaderCells()
    {
        return myHeaderCells;
    }

    @Override
    public int sampleLineToAbsoluteLine(int line)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int absoluteLineToSampleLine(int line)
    {
        throw new UnsupportedOperationException();
    }
}
