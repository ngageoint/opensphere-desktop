package io.opensphere.csvcommon.detect.datetime.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.opensphere.core.util.DateTimeUtilities;
import io.opensphere.csvcommon.common.datetime.DateColumn;

/**
 * Gets the values from a sample row for a given column.
 */
public class DateColumnValueProvider
{
    /**
     * Gets the date value from the sample row for the specified column.
     *
     * @param row A sample row.
     * @param column The column to get the value for.
     * @return The date for that column within the row.
     * @throws ParseException Thrown if there was issues trying to parse the
     *             date value within the row.
     */
    public Date getDate(List<? extends String> row, DateColumn column) throws ParseException
    {
        StringBuilder dateStringBuilder = new StringBuilder();
        dateStringBuilder.append(row.get(column.getPrimaryColumnIndex()));

        StringBuilder formatStringBuilder = new StringBuilder();
        formatStringBuilder.append(column.getPrimaryColumnFormat());

        if (column.getSecondaryColumnIndex() >= 0)
        {
            dateStringBuilder.append(' ');
            dateStringBuilder.append(row.get(column.getSecondaryColumnIndex()));

            formatStringBuilder.append(' ');
            formatStringBuilder.append(column.getSecondaryColumnFormat());
        }

        String formatString = formatStringBuilder.toString();

        String dateString = dateStringBuilder.toString();
        if (formatString.contains("S"))
        {
            dateString = DateTimeUtilities.fixMillis(dateString);
        }

        SimpleDateFormat format = new SimpleDateFormat(formatString);

        return format.parse(dateString);
    }
}
