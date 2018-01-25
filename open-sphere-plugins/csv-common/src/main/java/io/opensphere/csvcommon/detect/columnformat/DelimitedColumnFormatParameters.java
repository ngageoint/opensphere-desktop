package io.opensphere.csvcommon.detect.columnformat;

import java.util.Objects;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;

/**
 * Column format for a delimited file.
 */
public class DelimitedColumnFormatParameters implements ColumnFormatParameters
{
    /** The number of columns. */
    private final int myColumnCount;

    /** The text delimiter. */
    private final Character myTextDelimiter;

    /** The token delimiter. */
    private final Character myTokenDelimiter;

    /**
     * Constructor.
     *
     * @param tokenDelimiter The token delimiter.
     * @param textDelimiter The text delimiter.
     * @param columnCount The number of columns.
     */
    public DelimitedColumnFormatParameters(Character tokenDelimiter, Character textDelimiter, int columnCount)
    {
        myTokenDelimiter = Utilities.checkNull(tokenDelimiter, "tokenDelimiter");
        myTextDelimiter = textDelimiter;
        myColumnCount = columnCount;
        if (columnCount < 1)
        {
            throw new IllegalArgumentException("Column count cannot be < 1");
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        DelimitedColumnFormatParameters other = (DelimitedColumnFormatParameters)obj;
        //@formatter:off
        return myColumnCount == other.myColumnCount
                && Objects.equals(myTextDelimiter, other.myTextDelimiter)
                && Objects.equals(myTokenDelimiter, other.myTokenDelimiter);
        //@formatter:on
    }

    /**
     * Gets the column count.
     *
     * @return The number of columns.
     */
    public int getColumnCount()
    {
        return myColumnCount;
    }

    /**
     * Gets the text delimiter.
     *
     * @return The text delimiter.
     */
    public Character getTextDelimiter()
    {
        return myTextDelimiter;
    }

    /**
     * Gets the token delimiter.
     *
     * @return The token delimiter.
     */
    public Character getTokenDelimiter()
    {
        return myTokenDelimiter;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myColumnCount);
        result = prime * result + HashCodeHelper.getHashCode(myTextDelimiter);
        result = prime * result + HashCodeHelper.getHashCode(myTokenDelimiter);
        return result;
    }
}
