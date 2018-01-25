package io.opensphere.csvcommon.detect.controller;

import java.util.AbstractList;
import java.util.List;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.StringTokenizer;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.LineSampler;

/**
 * This takes the output of a {@link LineSampler} along with parameters that
 * describe how to split the lines into cells, and provides samples of cells.
 */
public final class CellSamplerImpl implements CellSampler
{
    /** The sampling of cells at the beginning of the document. */
    private final List<List<? extends String>> myBeginningSampleCells = new AbstractList<List<? extends String>>()
    {
        @Override
        public List<? extends String> get(int index)
        {
            String line = getBeginningSampleLines().get(index);
            return tokenize(line);
        }

        @Override
        public int size()
        {
            return getBeginningSampleLines().size();
        }
    };

    /** The sampling of lines at the beginning of the document. */
    private final List<? extends String> myBeginningSampleLines;

    /** The sampling of cells at the end of the document. */
    private final List<List<? extends String>> myEndSampleCells = new AbstractList<List<? extends String>>()
    {
        @Override
        public List<? extends String> get(int index)
        {
            String line = getEndingSampleLines().get(index);
            return tokenize(line);
        }

        @Override
        public int size()
        {
            return getEndingSampleLines().size();
        }
    };

    /** The header line number, or -1 if there isn't one. */
    private final int myHeaderLineNumber;

    /** The sampler from which to get lines of text. */
    private final LineSampler myLineSampler;

    /** Tokenizer used to split sample lines into cells. */
    private final StringTokenizer myTokenizer;

    /**
     * Constructor. Either the token delimiter or the fixed-width pattern must
     * be supplied.
     *
     * @param lineSampler The sampler from which to get lines of text.
     * @param tokenizer A tokenizer that can split lines into cells.
     * @param headerLineNumber The line number for the header, or -1 if there
     *            isn't one.
     */
    public CellSamplerImpl(LineSampler lineSampler, StringTokenizer tokenizer, int headerLineNumber)
    {
        myLineSampler = Utilities.checkNull(lineSampler, "lineSampler");
        myTokenizer = Utilities.checkNull(tokenizer, "tokenizer");
        myHeaderLineNumber = headerLineNumber;

        if (hasHeader())
        {
            myBeginningSampleLines = new AbstractList<String>()
            {
                @Override
                public String get(int index)
                {
                    return myLineSampler.getBeginningSampleLines().get(index + getHeaderLineNumber() + 1);
                }

                @Override
                public int size()
                {
                    return myLineSampler.getBeginningSampleLines().size() - getHeaderLineNumber() - 1;
                }
            };
        }
        else
        {
            myBeginningSampleLines = myLineSampler.getBeginningSampleLines();
        }
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
        return myBeginningSampleLines;
    }

    @Override
    public List<? extends List<? extends String>> getEndingSampleCells()
    {
        return myEndSampleCells;
    }

    @Override
    public List<? extends String> getEndingSampleLines()
    {
        return myLineSampler.getEndingSampleLines();
    }

    @Override
    public int getEndingSampleLinesIndexOffset()
    {
        return myLineSampler.getEndingSampleLinesIndexOffset() - getHeaderLineNumber() - 1;
    }

    @Override
    public List<? extends String> getHeaderCells()
    {
        int headerLineNumber = getHeaderLineNumber();
        return headerLineNumber >= 0 ? tokenize(myLineSampler.getBeginningSampleLines().get(headerLineNumber)) : null;
    }

    @Override
    public int sampleLineToAbsoluteLine(int line)
    {
        return hasHeader() ? line + getHeaderLineNumber() + 1 : line;
    }

    @Override
    public int absoluteLineToSampleLine(int line)
    {
        return hasHeader() ? line - getHeaderLineNumber() - 1 : line;
    }

    /**
     * Get the header line number.
     *
     * @return The header line number, or -1 if there isn't one.
     */
    private int getHeaderLineNumber()
    {
        return myHeaderLineNumber;
    }

    /**
     * Get if there's a header line.
     *
     * @return {@code true} if there's a header line.
     */
    private boolean hasHeader()
    {
        return getHeaderLineNumber() > -1;
    }

    /**
     * Split a line into cells.
     *
     * @param line The line.
     * @return The contents of the cells.
     */
    private List<? extends String> tokenize(String line)
    {
        return new CellSampleList(myTokenizer.tokenize(line));
    }

    /**
     * A list implementation that allows calls to {@link List#get(int)} beyond
     * the end of the list and just returns empty strings.
     */
    private static class CellSampleList extends AbstractList<String>
    {
        /** The tokenized cells. */
        private final List<? extends String> myCells;

        /**
         * Constructor.
         *
         * @param cells The tokenized cells.
         */
        public CellSampleList(List<? extends String> cells)
        {
            myCells = cells;
        }

        @Override
        public String get(int index)
        {
            return index < myCells.size() ? myCells.get(index) : StringUtilities.EMPTY;
        }

        @Override
        public int size()
        {
            return myCells.size();
        }
    }
}
