package io.opensphere.csvcommon.common.datetime;

/**
 * Contains the picked date columns, or null if one could not be determined.
 *
 */
public class DateColumnResults
{
    /**
     * The date column or the up time column.
     */
    private DateColumn myUpTimeColumn;

    /**
     * If multiple dates are defined within the csv, this is the down time or
     * end time column.
     */
    private DateColumn myDownTimeColumn;

    /**
     * If multiple dates are defined within the csv, gets the down time or end
     * time column.
     *
     * @return The down time column or null if there isn't one.
     */
    public DateColumn getDownTimeColumn()
    {
        return myDownTimeColumn;
    }

    /**
     * Gets the date column or the up time column.
     *
     * @return The date column or the up time column.
     */
    public DateColumn getUpTimeColumn()
    {
        return myUpTimeColumn;
    }

    /**
     * Sets the down time or end time column.
     *
     * @param downTimeColumn The down time column or null if there isn't one.
     *
     */
    public void setDownTimeColumn(DateColumn downTimeColumn)
    {
        myDownTimeColumn = downTimeColumn;
    }

    /**
     * Sets the date column or the up time column.
     *
     * @param upTimeColumn The date column or the up time column.
     */
    public void setUpTimeColumn(DateColumn upTimeColumn)
    {
        myUpTimeColumn = upTimeColumn;
    }
}
