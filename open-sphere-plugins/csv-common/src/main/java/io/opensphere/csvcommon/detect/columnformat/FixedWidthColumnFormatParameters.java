package io.opensphere.csvcommon.detect.columnformat;

import io.opensphere.csvcommon.detect.columnformat.ColumnFormatParameters;

/**
 * Column format for a fixed width file.
 */
public class FixedWidthColumnFormatParameters implements ColumnFormatParameters
{
    /** The indices where column divisions occur. */
    private final int[] myColumnDivisions;

    /**
     * Constructor.
     *
     * @param columnDivisions The indices where column divisions occur.
     */
    public FixedWidthColumnFormatParameters(int[] columnDivisions)
    {
        myColumnDivisions = columnDivisions.clone();
    }

    /**
     * Get the indices where column divisions occur.
     *
     * @return The indices where column divisions occur.
     */
    public int[] getColumnDivisions()
    {
        return myColumnDivisions.clone();
    }
}
