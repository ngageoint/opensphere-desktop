package io.opensphere.csvcommon.common.datetime;

import io.opensphere.core.common.configuration.date.DateFormat.Type;

/**
 * Contains information on a date column within the csv data.
 *
 */
public class DateColumn
{
    /**
     * The column type indicating if it represents a Datetime, a date or just a
     * time.
     */
    private Type myDateColumnType;

    /**
     * The primary column index representing the date.
     */
    private int myPrimaryColumnIndex;

    /**
     * If the date values spans a second column, the index of the second column.
     */
    private int mySecondaryColumnIndex = -1;

    /**
     * The format to use for the primary column data.
     */
    private String myPrimaryColumnFormat;

    /**
     * The format to use for the secondary column data.
     */
    private String mySecondaryColumnFormat;

    /**
     * Gets the column type indicating if it represents a Datetime, a date or
     * just a time.
     *
     * @return The column type indicating if it represents a Datetime, a date or
     *         just a time.
     */
    public Type getDateColumnType()
    {
        return myDateColumnType;
    }

    /**
     * Gets the primary column format.
     *
     * @return The primary column format.
     */
    public String getPrimaryColumnFormat()
    {
        return myPrimaryColumnFormat;
    }

    /**
     * Gets the primary column index representing the date.
     *
     * @return The primary column index representing the date.
     */
    public int getPrimaryColumnIndex()
    {
        return myPrimaryColumnIndex;
    }

    /**
     * Gets the secondary column format.
     *
     * @return The secondary column format.
     */
    public String getSecondaryColumnFormat()
    {
        return mySecondaryColumnFormat;
    }

    /**
     * If the date values spans a second column, gets the index of the second
     * column.
     *
     * @return If the date values spans a second column, the index of the second
     *         column otherwise -1;
     */
    public int getSecondaryColumnIndex()
    {
        return mySecondaryColumnIndex;
    }

    /**
     * Sets the column type indicating if it represents a Datetime, a date or
     * just a time.
     *
     * @param dateColumnType The column type indicating if it represents a
     *            Datetime, a date or just a time.
     */
    public void setDateColumnType(Type dateColumnType)
    {
        myDateColumnType = dateColumnType;
    }

    /**
     * Sets the primary column format.
     *
     * @param primaryColumnFormat The primary column format.
     */
    public void setPrimaryColumnFormat(String primaryColumnFormat)
    {
        myPrimaryColumnFormat = primaryColumnFormat;
    }

    /**
     * Sets the primary column index representing the date.
     *
     * @param primaryColumnIndex The primary column index representing the date.
     */
    public void setPrimaryColumnIndex(int primaryColumnIndex)
    {
        myPrimaryColumnIndex = primaryColumnIndex;
    }

    /**
     * Sets the secondary column format.
     *
     * @param secondaryColumnFormat The secondary column format.
     */
    public void setSecondaryColumnFormat(String secondaryColumnFormat)
    {
        mySecondaryColumnFormat = secondaryColumnFormat;
    }

    /**
     * If the date values spans a second column, sets the index of the second
     * column.
     *
     * @param secondaryColumnIndex If the date values spans a second column, the
     *            index of the second column otherwise -1;
     */
    public void setSecondaryColumnIndex(int secondaryColumnIndex)
    {
        mySecondaryColumnIndex = secondaryColumnIndex;
    }
}
