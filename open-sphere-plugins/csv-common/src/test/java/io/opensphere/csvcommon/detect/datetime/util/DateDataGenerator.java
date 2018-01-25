package io.opensphere.csvcommon.detect.datetime.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.csvcommon.util.CsvTestUtils;

/**
 * Generates different combinations of csv date data used for testing.
 */
public final class DateDataGenerator
{
    /**
     * The number of rows to create for test data.
     */
    private static final int ourNumberOfRows = 100;

    /**
     * The number of columns to create for test data.
     */
    private static final int ourNumberOfColumns = 10;

    /**
     * The number of milliseconds to add to each new time value.
     */
    private static final int ourStepSize = 60000;

    /**
     * Generates mock data with three columns representing the times. The up
     * time is represented by a shared day column and an up time column and the
     * down time is represented by a shared day column and a down time column.
     *
     * @param dateFormat The day format.
     * @param timeFormat1 The up time time format.
     * @param timeFormat2 The down time time format.
     * @return The sample data where the DOI column is at index 1, the time up
     *         is at 2, an additional time column 9:26:00 is at 3, and time down
     *         column is at 4.
     * @throws ParseException Bad parse.
     */
    public static List<List<String>> generateDayTimeUpTimeDown(DateFormat dateFormat, DateFormat timeFormat1,
            DateFormat timeFormat2) throws ParseException
    {
        List<List<String>> data = createJunkData(2);

        SimpleDateFormat dateSimpleFormat1 = dateFormat.getFormat();
        SimpleDateFormat timeSimpleFormat1 = timeFormat1.getFormat();
        SimpleDateFormat timeSimpleFormat2 = timeFormat2.getFormat();

        List<String> dateValues1 = createDateValues(dateSimpleFormat1);
        List<String> timeValues1 = createDateValues(timeSimpleFormat1);
        List<String> timeValues2 = createDateValues(timeSimpleFormat2, System.currentTimeMillis() + ourStepSize);

        SimpleDateFormat additionalFormat = timeFormat1.getFormat();
        List<String> additionalTimes = New.list();
        for (String timeValue1 : timeValues1)
        {
            if ("N/A".equals(timeValue1))
            {
                additionalTimes.add("N/A");
            }
            else if (StringUtils.isEmpty(timeValue1))
            {
                additionalTimes.add("");
            }
            else
            {
                Date date = timeSimpleFormat1.parse(timeValue1);
                additionalTimes.add(additionalFormat.format(date));
            }
        }

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues1, 1),
                new Pair<>(timeValues1, 2), new Pair<>(additionalTimes, 3),
                new Pair<>(timeValues2, 4));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with two columns representing an up time and a down
     * time.
     *
     * @param format1 The up time format.
     * @param format2 The down time format.
     * @return The sample data where up time is at index 8 and down time is at
     *         index 9.
     */
    public static List<List<String>> generateDoubleDate(DateFormat format1, DateFormat format2)
    {
        List<List<String>> data = createJunkData(2);

        SimpleDateFormat simpleFormat1 = format1.getFormat();
        SimpleDateFormat simpleFormat2 = format2.getFormat();

        List<String> dateValues1 = createDateValues(simpleFormat1);
        List<String> dateValues2 = createDateValues(simpleFormat2, System.currentTimeMillis() + ourStepSize);

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues1, 8),
                new Pair<>(dateValues2, 9));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with three columns representing the times. The up
     * time is represented by one column and the down time is represented by the
     * two columns.
     *
     * @param format1 The format for the up time.
     * @param dateFormat2 The format for the date.
     * @param timeFormat2 The format for the time.
     * @return The sample data where the up time is at 0 and the end time is at
     *         1, and 2.
     */
    public static List<List<String>> generateDoubleDate(DateFormat format1, DateFormat dateFormat2, DateFormat timeFormat2)
    {
        List<List<String>> data = createJunkData(2);

        SimpleDateFormat dateSimpleFormat1 = format1.getFormat();
        SimpleDateFormat dateSimpleFormat2 = dateFormat2.getFormat();
        SimpleDateFormat timeSimpleFormat2 = timeFormat2.getFormat();

        List<String> dateValues1 = createDateValues(dateSimpleFormat1);
        List<String> dateValues2 = createDateValues(dateSimpleFormat2, System.currentTimeMillis() + ourStepSize);
        List<String> timeValues2 = createDateValues(timeSimpleFormat2, System.currentTimeMillis() + ourStepSize);

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues1, 0),
                new Pair<>(dateValues2, 1), new Pair<>(timeValues2, 2));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with four columns representing the times. The up time
     * is represented by two columns and the down time is represented by two
     * columns.
     *
     * @param dateFormat1 The up time date format.
     * @param timeFormat1 The up time time format.
     * @param dateFormat2 The down time date format.
     * @param timeFormat2 The down time time format.
     * @return The sample data where the up time is at 1 and 4 and the down time
     *         is at 2 and 5.
     */
    public static List<List<String>> generateDoubleDate(DateFormat dateFormat1, DateFormat timeFormat1, DateFormat dateFormat2,
            DateFormat timeFormat2)
    {
        List<List<String>> data = createJunkData(4);

        SimpleDateFormat dateSimpleFormat1 = dateFormat1.getFormat();
        SimpleDateFormat timeSimpleFormat1 = timeFormat1.getFormat();
        SimpleDateFormat dateSimpleFormat2 = dateFormat2.getFormat();
        SimpleDateFormat timeSimpleFormat2 = timeFormat2.getFormat();

        List<String> dateValues1 = createDateValues(dateSimpleFormat1);
        List<String> timeValues1 = createDateValues(timeSimpleFormat1);
        List<String> dateValues2 = createDateValues(dateSimpleFormat2, System.currentTimeMillis() + ourStepSize);
        List<String> timeValues2 = createDateValues(timeSimpleFormat2, System.currentTimeMillis() + ourStepSize);

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues1, 1),
                new Pair<>(dateValues2, 2), new Pair<>(timeValues1, 4),
                new Pair<>(timeValues2, 5));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with a time column and a sparse (20% populate) end
     * time column whose end times, either don't exist, they match the time
     * column or they are later than the time column.
     *
     * @param timeFormat The format to use for time.
     * @param endTimeFormat The format to use for the end time.
     * @return Sample data where the time is at index 4 and the end time is at
     *         index 9.
     */
    public static List<List<String>> generateDownTimeTime(DateFormat timeFormat, DateFormat endTimeFormat)
    {
        List<List<String>> data = createJunkData(2);

        SimpleDateFormat simpleFormat1 = timeFormat.getFormat();
        SimpleDateFormat simpleFormat2 = endTimeFormat.getFormat();

        long now = System.currentTimeMillis();

        List<String> dateValues1 = createDateValues(simpleFormat1, now);
        List<String> dateValues2 = createDateValues(simpleFormat2, now + 100000);

        for (int i = 0; i < dateValues2.size(); i++)
        {
            if (i % 3 != 0)
            {
                dateValues2.remove(i);
                dateValues2.add(i, "N/A");
            }
        }

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues1, 4),
                new Pair<>(dateValues2, 9));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with a time column in the format similar to 201657
     * and a day column similar to yyyyMMdd.
     *
     * @return The mocked data whose date column is at index 8 and time at 9.
     */
    public static List<List<String>> generateHHmmss()
    {
        List<List<String>> data = createJunkData(2);

        SimpleDateFormat dateSimpleFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeSimpleFormat = new SimpleDateFormat("HHmmss");

        List<String> dateValues = createDateValues(dateSimpleFormat);
        List<String> timeValues = createDateValues(timeSimpleFormat);

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues, 8),
                new Pair<>(timeValues, 9));

        return convertToRowsColumns;
    }

    /**
     *
     * Generates mock data with two columns representing an up time and a down
     * time but the down times are presented before the up times.
     *
     * @param format1 The down time format.
     * @param format2 The up time format.
     * @return The sample data where down time is at 4 and up time is at 7.
     */
    public static List<List<String>> generateReverseDoubleDate(DateFormat format1, DateFormat format2)
    {
        List<List<String>> data = createJunkData(2);

        SimpleDateFormat simpleFormat1 = format1.getFormat();
        SimpleDateFormat simpleFormat2 = format2.getFormat();

        List<String> dateValues1 = createDateValues(simpleFormat1, System.currentTimeMillis() + ourStepSize);
        List<String> dateValues2 = createDateValues(simpleFormat2);

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues1, 4),
                new Pair<>(dateValues2, 7));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with three columns representing the times. The up
     * time is represented by two columns and the down time is represented by
     * one column.
     *
     * @param dateFormat1 The format for the date.
     * @param timeFormat1 The format for the time.
     * @param format2 The format for the date/time.
     * @return The sample data where date and time are at index 3 and 4 and the
     *         date/time is at index 7.
     */
    public static List<List<String>> generateReverseDoubleDate(DateFormat dateFormat1, DateFormat timeFormat1, DateFormat format2)
    {
        List<List<String>> data = createJunkData(3);

        SimpleDateFormat dateSimpleFormat1 = dateFormat1.getFormat();
        SimpleDateFormat timeSimpleFormat1 = timeFormat1.getFormat();
        SimpleDateFormat simpleFormat2 = format2.getFormat();

        List<String> dateValues1 = createDateValues(dateSimpleFormat1);
        List<String> timeValues1 = createDateValues(timeSimpleFormat1);
        List<String> dateValues2 = createDateValues(simpleFormat2, System.currentTimeMillis() + ourStepSize);

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues1, 3),
                new Pair<>(timeValues1, 4), new Pair<>(dateValues2, 7));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with a single time value that consists of date
     * columns and time columns.
     *
     * @param dateFormat The date format.
     * @param timeFormat The time format.
     * @return The mock data whose date is column index 0 and time is column 1.
     */
    public static List<List<String>> generateSingleCompoundDate(DateFormat dateFormat, DateFormat timeFormat)
    {
        List<List<String>> data = createJunkData(2);

        SimpleDateFormat dateSimpleFormat = dateFormat.getFormat();
        SimpleDateFormat timeSimpleFormat = timeFormat.getFormat();

        List<String> dateValues = createDateValues(dateSimpleFormat);
        List<String> timeValues = createDateValues(timeSimpleFormat);

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues, 0),
                new Pair<>(timeValues, 1));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with a single time column.
     *
     * @param format The format to put the times in.
     * @return The mocked data whose date column is at index 1.
     */
    public static List<List<String>> generateSingleDate(DateFormat format)
    {
        List<List<String>> data = createJunkData(1);

        SimpleDateFormat simpleFormat = format.getFormat();

        List<String> dateValues = createDateValues(simpleFormat);

        List<List<String>> tableData = convertToRowsColumns(data, new Pair<>(dateValues, 1));

        return tableData;
    }

    /**
     * Generates mock data with a single time values that consists of date
     * columns and time columns but whose time values go into nano seconds.
     *
     * date columns looks something like 2013:04:04::11:14:52.952449889780 and
     * the time column is 0.0.
     *
     * @return The sample data where date is at column index 1 and time is at
     *         column index 2.
     */
    public static List<List<String>> generateSingleDateLotsOfDecimalSeconds()
    {
        List<List<String>> data = createJunkData(2);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd::HH:mm:ss.SSSSSSSSSSSS");

        List<String> dateValues = createDateValues(dateFormat);

        List<String> timeValues = New.list();
        for (int i = 0; i < ourNumberOfRows; i++)
        {
            timeValues.add("0.0");
        }

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues, 1),
                new Pair<>(timeValues, 2));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with a single time column with the format yyyyMMdd.
     *
     * @return The mocked data whose date column is at index 1.
     */
    public static List<List<String>> generateSingleDateyyyyMMdd()
    {
        List<List<String>> data = createJunkData(1);

        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd");

        List<String> dateValues = createDateValues(simpleFormat);

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues, 1));

        return convertToRowsColumns;
    }

    /**
     * Generates mock data with a single time column in the format similar to
     * 2013-01-19 20:16:57.
     *
     * @return The mocked data whose date column is at index 0.
     */
    public static List<List<String>> generateYearMonthDayTime()
    {
        List<List<String>> data = createJunkData(1);

        SimpleDateFormat simpleFormat = new SimpleDateFormat(DateTimeFormats.DATE_TIME_FORMAT);

        List<String> dateValues = createDateValues(simpleFormat);

        List<List<String>> convertToRowsColumns = convertToRowsColumns(data, new Pair<>(dateValues, 0));

        return convertToRowsColumns;
    }

    /**
     * Combines the column to row non date data and the date values into a
     * single rows to column table structure.
     *
     * @param nonDateData The non date data.
     * @param dateValues The date values to combine.
     * @return A mocked up csv table data.
     */
    @SafeVarargs
    private static List<List<String>> convertToRowsColumns(List<List<String>> nonDateData,
            Pair<List<String>, Integer>... dateValues)
    {
        List<List<String>> table = New.list();

        for (int i = 0; i < ourNumberOfRows; i++)
        {
            List<String> row = New.list();
            for (List<String> aColumnOfData : nonDateData)
            {
                row.add(aColumnOfData.get(i));
            }

            for (Pair<List<String>, Integer> aColumnOfDates : dateValues)
            {
                String dateValue = aColumnOfDates.getFirstObject().get(i);
                int columnIndex = aColumnOfDates.getSecondObject().intValue();

                row.add(columnIndex, dateValue);
            }

            table.add(row);
        }

        return table;
    }

    /**
     * Creates date values for the given format.
     *
     * @param simpleFormat The format to create the dates in.
     * @return The list of date values with some empty values or non date
     *         values.
     */
    private static List<String> createDateValues(SimpleDateFormat simpleFormat)
    {
        return createDateValues(simpleFormat, System.currentTimeMillis());
    }

    /**
     * Creates date values for the given format.
     *
     * @param simpleFormat The format to create the dates in.
     * @param startTime The time to start at when creating the dates.
     * @return The list of date values with some empty values or non date
     *         values.
     */
    private static List<String> createDateValues(SimpleDateFormat simpleFormat, long startTime)
    {
        long timeNow = startTime;

        List<String> dates = New.list();
        for (int i = 0; i < ourNumberOfRows; i++)
        {
            if (i % 20 != 0)
            {
                Date aDate = new Date(timeNow);
                dates.add(simpleFormat.format(aDate));
            }
            else
            {
                if (i % 40 == 0)
                {
                    dates.add("N/A");
                }
                else
                {
                    dates.add("");
                }
            }

            timeNow -= ourStepSize;
        }

        return dates;
    }

    /**
     * Creates non date data to mock that of a csv file.
     *
     * @param numberOfDateColumns The number of date columns that will be
     *            created from the calling code.
     * @return Non date data in column to row order.
     */
    private static List<List<String>> createJunkData(int numberOfDateColumns)
    {
        List<List<String>> textValues = New.list();

        for (int i = 0; i < ourNumberOfColumns - numberOfDateColumns; i++)
        {
            textValues.add(CsvTestUtils.createTextValues(ourNumberOfRows, "text " + i));
        }

        return textValues;
    }

    /**
     * Not constructible.
     */
    private DateDataGenerator()
    {
    }
}
