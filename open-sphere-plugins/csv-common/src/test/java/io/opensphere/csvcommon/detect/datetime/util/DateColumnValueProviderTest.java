package io.opensphere.csvcommon.detect.datetime.util;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.detect.datetime.util.DateColumnValueProvider;
import io.opensphere.csvcommon.util.CsvTestUtils;

/**
 * Tests the DateColumnValueProvider class.
 *
 */
public class DateColumnValueProviderTest
{
    /**
     * Tests getting a date.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void testGetDate() throws ParseException
    {
        List<List<String>> data = CsvTestUtils.createMultipleTimesData();
        List<String> testRow = data.get(1);

        DateColumn column = new DateColumn();
        column.setPrimaryColumnIndex(0);
        String format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        column.setPrimaryColumnFormat(format);

        DateColumnValueProvider provider = new DateColumnValueProvider();
        Date actualDate = provider.getDate(testRow, column);

        Date expectedDate = new SimpleDateFormat(format).parse(testRow.get(0));

        assertEquals(expectedDate, actualDate);
    }

    /**
     * Tests getting a date that comprises of two columns.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void testGetDateCompositeDate() throws ParseException
    {
        List<List<String>> data = CsvTestUtils.createMultipleTimesData();
        List<String> testRow = data.get(1);

        DateColumn column = new DateColumn();
        column.setPrimaryColumnIndex(8);
        String dateFormat = "MM-dd-yyyy";
        column.setPrimaryColumnFormat(dateFormat);

        column.setSecondaryColumnIndex(9);
        String timeFormat = "HH:mm:ss";
        column.setSecondaryColumnFormat(timeFormat);

        DateColumnValueProvider provider = new DateColumnValueProvider();
        Date actualDate = provider.getDate(testRow, column);

        SimpleDateFormat expectedFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        Date expectedDate = expectedFormat.parse(testRow.get(8) + " " + testRow.get(9));

        assertEquals(expectedDate, actualDate);
    }
}
