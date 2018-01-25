package io.opensphere.csvcommon.detect.datetime.model;

import java.util.Map;

import io.opensphere.core.util.collections.New;

/**
 * Contains information about a potential date/time column. It contains the
 * column index and the map of SuccessfulFormats.
 *
 */
public class PotentialColumn
{
    /**
     * The index of the potential date/time column.
     */
    private int myColumnIndex;

    /**
     * The formats that were successful for at least one row's column value.
     */
    private final Map<String, SuccessfulFormat> myFormats = New.map();

    /**
     * The most successful format.
     */
    private SuccessfulFormat myMostSuccessfulFormat;

    /**
     * Gets the index of the potential date/time column.
     *
     * @return The index of the potential date/time column.
     */
    public int getColumnIndex()
    {
        return myColumnIndex;
    }

    /**
     * Gets the map of formats successful formats.
     *
     * @return The formats that were successful for at least one row's column
     *         value.
     */
    public Map<String, SuccessfulFormat> getFormats()
    {
        return myFormats;
    }

    /**
     * Gets the most successful format.
     *
     * @return The most successful format.
     */
    public SuccessfulFormat getMostSuccessfulFormat()
    {
        return myMostSuccessfulFormat;
    }

    /**
     * Sets the index of the potential date/time column.
     *
     * @param columnIndex The index of the potential date/time column.
     */
    public void setColumnIndex(int columnIndex)
    {
        myColumnIndex = columnIndex;
    }

    /**
     * Sets the most successful format.
     *
     * @param mostSuccessfulFormat The most successful format.
     */
    public void setMostSuccessfulFormat(SuccessfulFormat mostSuccessfulFormat)
    {
        myMostSuccessfulFormat = mostSuccessfulFormat;
    }
}
