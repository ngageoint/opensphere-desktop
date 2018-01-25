package io.opensphere.csvcommon.detect.datetime.model;

import io.opensphere.core.common.configuration.date.DateFormat;

/**
 * This class contains a DateFormat that worked for a given column, plus the
 * number of times the format was succesful for the column's values in the
 * analyzed rows.
 *
 */
public class SuccessfulFormat
{
    /**
     * The date format whose pattern matched at least one row's column value.
     */
    private DateFormat myFormat;

    /**
     * The number of rows the pattern matched.
     */
    private int myNumberOfSuccesses;

    /**
     * Gets the date format whose pattern matched at least one row's column
     * value.
     *
     * @return The date format whose pattern matched at least one row's column
     *         value.
     */
    public DateFormat getFormat()
    {
        return myFormat;
    }

    /**
     * Gets the number of rows the pattern matched.
     *
     * @return The number of rows the pattern matched.
     */
    public int getNumberOfSuccesses()
    {
        return myNumberOfSuccesses;
    }

    /**
     * Sets the date format whose pattern matched at least one row's column
     * value.
     *
     * @param format The date format whose pattern matched at least one row's
     *            column value.
     */
    public void setFormat(DateFormat format)
    {
        myFormat = format;
    }

    /**
     * Sets the number of rows the pattern matched.
     *
     * @param numberOfSuccesses The number of rows the pattern matched.
     */
    public void setNumberOfSuccesses(int numberOfSuccesses)
    {
        myNumberOfSuccesses = numberOfSuccesses;
    }
}
