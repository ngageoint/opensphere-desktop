package io.opensphere.csvcommon.detect.datetime.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.datetime.model.PotentialColumn;
import io.opensphere.csvcommon.detect.datetime.model.SuccessfulFormat;
import io.opensphere.csvcommon.detect.datetime.util.DateDataGenerator;

/**
 * Tests the DateRater class.
 *
 */
@SuppressWarnings({ "PMD.GodClass", "boxing" })
public class DateRaterTest
{
    /**
     * Our day format.
     */
    private static final String ourDateFormat = "yyyy-M-d";

    /**
     * Our timestamp format.
     */
    private static final String ourTimestampFormat = "yyyy-M-d HH:mm:ss";

    /**
     * Tests one day column and two time columns that share the same day column.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void testDayTimeUpTimeDown() throws ParseException
    {
        DateRater rater = new DateRater();

        PotentialColumn dateColumn1 = new PotentialColumn();
        dateColumn1.setColumnIndex(1);

        PotentialColumn timeColumn1 = new PotentialColumn();
        timeColumn1.setColumnIndex(2);

        PotentialColumn timeColumn2 = new PotentialColumn();
        timeColumn2.setColumnIndex(4);

        PotentialColumn timeColumn3 = new PotentialColumn();
        timeColumn3.setColumnIndex(3);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(dateColumn1.getColumnIndex(), dateColumn1);
        potentials.put(timeColumn1.getColumnIndex(), timeColumn1);
        potentials.put(timeColumn2.getColumnIndex(), timeColumn2);
        potentials.put(timeColumn3.getColumnIndex(), timeColumn3);

        DateFormat dateFormat1 = new DateFormat();
        dateFormat1.setType(Type.DATE);
        dateFormat1.setSdf(ourDateFormat);

        DateFormat timeFormat1 = new DateFormat();
        timeFormat1.setType(Type.TIME);
        timeFormat1.setSdf("'z'HHmmss");

        DateFormat timeFormat2 = new DateFormat();
        timeFormat2.setType(Type.TIME);
        timeFormat2.setSdf("'z'HHmmss");

        List<List<String>> data = DateDataGenerator.generateDayTimeUpTimeDown(dateFormat1, timeFormat1, timeFormat2);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat1);

        dateColumn1.getFormats().clear();
        dateColumn1.getFormats().put(dateFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(timeFormat1);

        timeColumn1.getFormats().clear();
        timeColumn1.getFormats().put(timeFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(timeFormat2);

        timeColumn2.getFormats().clear();
        timeColumn2.getFormats().put(timeFormat2.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(timeFormat1);

        timeColumn3.getFormats().clear();
        timeColumn3.getFormats().put(timeFormat1.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

        assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(timeFormat1.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
        assertEquals(2, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

        assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

        assertEquals(dateFormat1.getSdf(), value.getValue().getDownTimeColumn().getPrimaryColumnFormat());
        assertEquals(1, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
        assertEquals(timeFormat2.getSdf(), value.getValue().getDownTimeColumn().getSecondaryColumnFormat());
        assertEquals(4, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
    }

    /**
     * Tests all known formats for two time columns.
     */
    @Test
    public void testDoubleDate()
    {
        DateRater rater = new DateRater();

        PotentialColumn dateColumn1 = new PotentialColumn();
        dateColumn1.setColumnIndex(8);

        PotentialColumn dateColumn2 = new PotentialColumn();
        dateColumn2.setColumnIndex(9);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(dateColumn1.getColumnIndex(), dateColumn1);
        potentials.put(dateColumn2.getColumnIndex(), dateColumn2);

        DateFormat dateFormat1 = new DateFormat();
        dateFormat1.setSdf(ourTimestampFormat);

        DateFormat dateFormat2 = new DateFormat();
        dateFormat2.setSdf(ourTimestampFormat);

        List<List<String>> data = DateDataGenerator.generateDoubleDate(dateFormat1, dateFormat2);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat1);

        dateColumn1.getFormats().clear();
        dateColumn1.getFormats().put(dateFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat2);

        dateColumn2.getFormats().clear();
        dateColumn2.getFormats().put(dateFormat2.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        if (dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(8, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

            assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

            assertEquals(dateFormat2.getSdf(), value.getValue().getDownTimeColumn().getPrimaryColumnFormat());
            assertEquals(9, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
        }
        else if (!dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat2.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(9, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
            assertNull(value.getValue().getDownTimeColumn());
        }
        else if (!dateFormat2.getSdf().contains("y") && dateFormat1.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(8, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
            assertNull(value.getValue().getDownTimeColumn());
        }
        else
        {
            assertNull(value.getValue().getUpTimeColumn());
        }
    }

    /**
     * Tests two time columns with a single up time and a composite down time.
     */
    @Test
    public void testDoubleDate3()
    {
        DateRater rater = new DateRater();

        PotentialColumn dateColumn1 = new PotentialColumn();
        dateColumn1.setColumnIndex(0);

        PotentialColumn dateColumn2 = new PotentialColumn();
        dateColumn2.setColumnIndex(1);

        PotentialColumn timeColumn2 = new PotentialColumn();
        timeColumn2.setColumnIndex(2);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(dateColumn1.getColumnIndex(), dateColumn1);
        potentials.put(dateColumn2.getColumnIndex(), dateColumn2);
        potentials.put(timeColumn2.getColumnIndex(), timeColumn2);

        DateFormat dateFormat1 = new DateFormat();
        dateFormat1.setSdf(ourTimestampFormat);

        DateFormat dateFormat2 = new DateFormat();
        dateFormat2.setSdf(ourDateFormat);
        dateFormat2.setType(Type.DATE);

        DateFormat timeFormat2 = new DateFormat();
        timeFormat2.setSdf("HH:mm:ss");
        timeFormat2.setType(Type.TIME);

        List<List<String>> data = DateDataGenerator.generateDoubleDate(dateFormat1, dateFormat2, timeFormat2);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat1);

        dateColumn1.getFormats().clear();
        dateColumn1.getFormats().put(dateFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat2);

        dateColumn2.getFormats().clear();
        dateColumn2.getFormats().put(dateFormat2.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(timeFormat2);

        timeColumn2.getFormats().clear();
        timeColumn2.getFormats().put(timeFormat2.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        if (dateFormat1.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(0, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

            assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

            assertEquals(dateFormat2.getSdf(), value.getValue().getDownTimeColumn().getPrimaryColumnFormat());
            assertEquals(1, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
            assertEquals(timeFormat2.getSdf(), value.getValue().getDownTimeColumn().getSecondaryColumnFormat());
            assertEquals(2, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
        }
        else
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat2.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(timeFormat2.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
            assertEquals(2, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        }
    }

    /**
     * Tests two time columns with two times each of which are represented by
     * two individual date and time columns.
     */
    @Test
    public void testDoubleDate4()
    {
        DateRater rater = new DateRater();

        PotentialColumn dateColumn1 = new PotentialColumn();
        dateColumn1.setColumnIndex(1);

        PotentialColumn timeColumn1 = new PotentialColumn();
        timeColumn1.setColumnIndex(4);

        PotentialColumn dateColumn2 = new PotentialColumn();
        dateColumn2.setColumnIndex(2);

        PotentialColumn timeColumn2 = new PotentialColumn();
        timeColumn2.setColumnIndex(5);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(dateColumn1.getColumnIndex(), dateColumn1);
        potentials.put(timeColumn1.getColumnIndex(), timeColumn1);
        potentials.put(dateColumn2.getColumnIndex(), dateColumn2);
        potentials.put(timeColumn2.getColumnIndex(), timeColumn2);

        DateFormat dateFormat1 = new DateFormat(Type.DATE, ourDateFormat);
        DateFormat timeFormat1 = new DateFormat(Type.TIME, "HH:mm:ss");
        DateFormat dateFormat2 = new DateFormat(Type.DATE, ourDateFormat);
        DateFormat timeFormat2 = new DateFormat(Type.TIME, "HH:mm:ss");

        List<List<String>> data = DateDataGenerator.generateDoubleDate(dateFormat1, timeFormat1, dateFormat2, timeFormat2);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat1);

        dateColumn1.getFormats().clear();
        dateColumn1.getFormats().put(dateFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(timeFormat1);

        timeColumn1.getFormats().clear();
        timeColumn1.getFormats().put(timeFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat2);

        dateColumn2.getFormats().clear();
        dateColumn2.getFormats().put(dateFormat2.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(timeFormat2);

        timeColumn2.getFormats().clear();
        timeColumn2.getFormats().put(timeFormat2.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

        assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(timeFormat1.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
        assertEquals(4, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

        assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

        assertEquals(dateFormat2.getSdf(), value.getValue().getDownTimeColumn().getPrimaryColumnFormat());
        assertEquals(2, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
        assertEquals(timeFormat2.getSdf(), value.getValue().getDownTimeColumn().getSecondaryColumnFormat());
        assertEquals(5, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
    }

    /**
     * Tests the case where mock data with a time column and a sparse (20%
     * populate) end time column whose end times, either don't exist, they match
     * the time column or they are later than the time column.
     */
    @Test
    public void testDownTimeTime()
    {
        DateRater rater = new DateRater();

        PotentialColumn dateColumn1 = new PotentialColumn();
        dateColumn1.setColumnIndex(4);

        PotentialColumn dateColumn2 = new PotentialColumn();
        dateColumn2.setColumnIndex(9);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(dateColumn1.getColumnIndex(), dateColumn1);
        potentials.put(dateColumn2.getColumnIndex(), dateColumn2);

        DateFormat dateFormat1 = new DateFormat(Type.TIMESTAMP, ourTimestampFormat);
        DateFormat dateFormat2 = new DateFormat(Type.TIMESTAMP, ourTimestampFormat);

        List<List<String>> data = DateDataGenerator.generateDownTimeTime(dateFormat1, dateFormat2);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat1);

        dateColumn1.getFormats().clear();
        dateColumn1.getFormats().put(dateFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat2);

        dateColumn2.getFormats().clear();
        dateColumn2.getFormats().put(dateFormat2.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        if (dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(4, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

            assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

            assertEquals(dateFormat2.getSdf(), value.getValue().getDownTimeColumn().getPrimaryColumnFormat());
            assertEquals(9, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
        }
        else if (!dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
        {
            assertNull("Fail for format " + dateFormat1.getSdf() + " and " + dateFormat2.getSdf(),
                    value.getValue().getUpTimeColumn());
        }
        else if (!dateFormat2.getSdf().contains("y") && dateFormat1.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(4, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
            assertNull(value.getValue().getDownTimeColumn());
        }
        else
        {
            assertNull(value.getValue().getUpTimeColumn());
        }
    }

    /**
     * Tests the HHmmss format.
     */
    @Test
    public void testHHmmss()
    {
        DateFormat format = new DateFormat();
        format.setSdf("HHmmss");
        format.setType(Type.TIME);

        DateFormat dateFormat = new DateFormat();
        dateFormat.setSdf("yyyyMMdd");
        dateFormat.setType(Type.DATE);

        DateRater rater = new DateRater();

        PotentialColumn column = new PotentialColumn();
        column.setColumnIndex(9);

        PotentialColumn dateColumn = new PotentialColumn();
        dateColumn.setColumnIndex(8);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(column.getColumnIndex(), column);
        potentials.put(dateColumn.getColumnIndex(), dateColumn);

        List<List<String>> data = DateDataGenerator.generateHHmmss();

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(format);

        column.getFormats().clear();
        column.getFormats().put(format.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat);

        dateColumn.getFormats().clear();
        dateColumn.getFormats().put(dateFormat.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals(dateFormat.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(8, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(format.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
        assertEquals(9, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());
    }

    /**
     * Tests two time columns with end time first.
     */
    @Test
    public void testReverseDoubleDate2()
    {
        DateRater rater = new DateRater();

        PotentialColumn dateColumn1 = new PotentialColumn();
        dateColumn1.setColumnIndex(4);

        PotentialColumn dateColumn2 = new PotentialColumn();
        dateColumn2.setColumnIndex(7);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(dateColumn1.getColumnIndex(), dateColumn1);
        potentials.put(dateColumn2.getColumnIndex(), dateColumn2);

        DateFormat dateFormat1 = new DateFormat(Type.TIMESTAMP, ourTimestampFormat);
        DateFormat dateFormat2 = new DateFormat(Type.TIMESTAMP, ourTimestampFormat);

        List<List<String>> data = DateDataGenerator.generateReverseDoubleDate(dateFormat1, dateFormat2);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat1);

        dateColumn1.getFormats().clear();
        dateColumn1.getFormats().put(dateFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat2);

        dateColumn2.getFormats().clear();
        dateColumn2.getFormats().put(dateFormat2.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        if (dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

            assertEquals(dateFormat1.getSdf(), value.getValue().getDownTimeColumn().getPrimaryColumnFormat());
            assertEquals(4, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());

            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat2.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(7, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        }
        else if (!dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat2.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(7, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
            assertNull(value.getValue().getDownTimeColumn());
        }
        else if (!dateFormat2.getSdf().contains("y") && dateFormat1.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(4, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
            assertNull(value.getValue().getDownTimeColumn());
        }
        else
        {
            assertNull(value.getValue().getUpTimeColumn());
        }
    }

    /**
     * Tests two time columns with a composite up time and a single column for
     * down time.
     */
    @Test
    public void testReverseDoubleDate3()
    {
        DateRater rater = new DateRater();

        PotentialColumn dateColumn1 = new PotentialColumn();
        dateColumn1.setColumnIndex(3);

        PotentialColumn timeColumn1 = new PotentialColumn();
        timeColumn1.setColumnIndex(4);

        PotentialColumn dateColumn2 = new PotentialColumn();
        dateColumn2.setColumnIndex(7);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(dateColumn1.getColumnIndex(), dateColumn1);
        potentials.put(timeColumn1.getColumnIndex(), timeColumn1);
        potentials.put(dateColumn2.getColumnIndex(), dateColumn2);

        DateFormat dateFormat1 = new DateFormat(Type.DATE, ourDateFormat);
        DateFormat timeFormat1 = new DateFormat(Type.TIME, "HH:mm:ss");
        DateFormat dateFormat2 = new DateFormat(Type.TIMESTAMP, ourTimestampFormat);

        List<List<String>> data = DateDataGenerator.generateReverseDoubleDate(dateFormat1, timeFormat1, dateFormat2);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat1);

        dateColumn1.getFormats().clear();
        dateColumn1.getFormats().put(dateFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(timeFormat1);

        timeColumn1.getFormats().clear();
        timeColumn1.getFormats().put(timeFormat1.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat2);

        dateColumn2.getFormats().clear();
        dateColumn2.getFormats().put(dateFormat2.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        if (dateFormat2.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(3, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(timeFormat1.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
            assertEquals(4, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

            assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

            assertEquals(dateFormat2.getSdf(), value.getValue().getDownTimeColumn().getPrimaryColumnFormat());
            assertEquals(7, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
        }
        else
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

            assertEquals(dateFormat1.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(3, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(timeFormat1.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
            assertEquals(4, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        }
    }

    /**
     * Tests a single date consisting of two columns.
     */
    @Test
    public void testSingleCompoundDate()
    {
        DateRater rater = new DateRater();

        PotentialColumn dateColumn = new PotentialColumn();
        dateColumn.setColumnIndex(0);

        PotentialColumn timeColumn = new PotentialColumn();
        timeColumn.setColumnIndex(1);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(dateColumn.getColumnIndex(), dateColumn);
        potentials.put(timeColumn.getColumnIndex(), timeColumn);

        DateFormat dateFormat = new DateFormat(Type.DATE, ourDateFormat);
        DateFormat timeFormat = new DateFormat(Type.TIME, "HH:mm:ss");

        List<List<String>> data = DateDataGenerator.generateSingleCompoundDate(dateFormat, timeFormat);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat);

        dateColumn.getFormats().clear();
        dateColumn.getFormats().put(dateFormat.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(timeFormat);

        timeColumn.getFormats().clear();
        timeColumn.getFormats().put(timeFormat.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals(dateFormat.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(0, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(timeFormat.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
        assertEquals(1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());
    }

    /**
     * Tests all known formats for a single date column.
     */
    @Test
    public void testSingleDate()
    {
        DateRater rater = new DateRater();

        PotentialColumn column = new PotentialColumn();
        column.setColumnIndex(1);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(column.getColumnIndex(), column);

        DateFormat format = new DateFormat(Type.TIMESTAMP, ourTimestampFormat);
        List<List<String>> data = DateDataGenerator.generateSingleDate(format);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(format);

        column.getFormats().clear();
        column.getFormats().put(format.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        if (format.getSdf().contains("y"))
        {
            assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
            assertEquals(format.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
            assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
            assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
            assertNull(value.getValue().getDownTimeColumn());
        }
        else
        {
            assertNull(value.getValue().getUpTimeColumn());
        }
    }

    /**
     * Tests a Single date with a very long decimal seconds string.
     */
    @Test
    public void testSingleDateLotsOfDecimalSeconds()
    {
        DateFormat format = new DateFormat();
        format.setSdf("yyyy:MM:d::HH:mm:ss.SSS");
        format.setType(Type.TIMESTAMP);

        DateRater rater = new DateRater();

        PotentialColumn column = new PotentialColumn();
        column.setColumnIndex(1);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(column.getColumnIndex(), column);

        List<List<String>> data = DateDataGenerator.generateSingleDateLotsOfDecimalSeconds();

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(format);

        column.getFormats().clear();
        column.getFormats().put(format.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals(format.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());
    }

    /**
     * Tests the yyyyMMdd format.
     */
    @Test
    public void testSingleDateyyyyMMdd()
    {
        DateFormat format = new DateFormat();
        format.setSdf("yyyyMMdd");
        format.setType(Type.DATE);

        DateRater rater = new DateRater();

        PotentialColumn column = new PotentialColumn();
        column.setColumnIndex(1);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(column.getColumnIndex(), column);

        List<List<String>> data = DateDataGenerator.generateSingleDateyyyyMMdd();

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(format);

        column.getFormats().clear();
        column.getFormats().put(format.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        assertEquals(Type.DATE, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals(format.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());
    }

    /**
     * Tests the yyyy-MM-dd HH:mm:ss format.
     */
    @Test
    public void testYearMonthDayTime()
    {
        DateFormat format = new DateFormat();
        format.setSdf(DateTimeFormats.DATE_TIME_FORMAT);
        format.setType(Type.TIMESTAMP);

        DateRater rater = new DateRater();

        PotentialColumn column = new PotentialColumn();
        column.setColumnIndex(0);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(column.getColumnIndex(), column);

        List<List<String>> data = DateDataGenerator.generateYearMonthDayTime();

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(format);

        column.getFormats().clear();
        column.getFormats().put(format.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals(format.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(0, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());
    }

    /**
     * Tests the zHHmmss.SS format.
     */
    @Test
    public void testZ()
    {
        DateFormat format = new DateFormat();
        format.setSdf("'z'HHmmss.SS");
        format.setType(Type.TIME);

        DateFormat dateFormat = new DateFormat();
        dateFormat.setSdf("yyyyMMdd");
        dateFormat.setType(Type.DATE);

        DateRater rater = new DateRater();

        PotentialColumn column = new PotentialColumn();
        column.setColumnIndex(1);

        PotentialColumn dateColumn = new PotentialColumn();
        dateColumn.setColumnIndex(0);

        Map<Integer, PotentialColumn> potentials = New.map();
        potentials.put(column.getColumnIndex(), column);
        potentials.put(dateColumn.getColumnIndex(), dateColumn);

        List<List<String>> data = DateDataGenerator.generateSingleCompoundDate(dateFormat, format);

        SuccessfulFormat successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(format);

        column.getFormats().clear();
        column.getFormats().put(format.getSdf(), successfulFormat);

        successfulFormat = new SuccessfulFormat();
        successfulFormat.setNumberOfSuccesses(data.size());
        successfulFormat.setFormat(dateFormat);

        dateColumn.getFormats().clear();
        dateColumn.getFormats().put(dateFormat.getSdf(), successfulFormat);

        ValueWithConfidence<DateColumnResults> value = rater.rateAndPick(potentials, data);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals(dateFormat.getSdf(), value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(0, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(format.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
        assertEquals(1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());
    }
}
