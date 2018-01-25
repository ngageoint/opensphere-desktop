package io.opensphere.csvcommon.detect.datetime.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.ClasspathPreferencesPersistenceManager;
import io.opensphere.core.preferences.InternalPreferencesIF;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.datetime.ConfigurationProvider;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.detect.ValueWithConfidence;
import io.opensphere.csvcommon.detect.datetime.algorithm.DateTimeFinder;
import io.opensphere.csvcommon.detect.datetime.util.DateDataGenerator;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Tests the DateTimeFinder class.
 *
 */
@SuppressWarnings("PMD.GodClass")
public class DateTimeFinderTestFunctional
{
    /**
     * Tests the ability to detect a datetime column and a time column.
     */
    @Test
    public void testDateTimeAndDownTime()
    {
        DateFormatsConfig configuration = getDateFormats();

        DateFormat dateFormat = new DateFormat();
        dateFormat.setSdf("yyyy-M-d HH:mm:ss");

        DateFormat timeFormat = new DateFormat();
        timeFormat.setSdf("'z'HHmmss.SS");

        EasyMockSupport support = new EasyMockSupport();

        List<List<String>> data = DateDataGenerator.generateSingleCompoundDate(dateFormat, timeFormat);

        ConfigurationProvider provider = createConfigurationProvider(support, configuration);
        CellSampler sampler = createSampler(support, data);

        support.replayAll();

        DateTimeFinder finder = new DateTimeFinder(provider);

        ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

        assertTrue(dateFormat.getSdf().equals(value.getValue().getUpTimeColumn().getPrimaryColumnFormat())
                || "yyyy-MM-dd HH:mm:ss".equals(value.getValue().getUpTimeColumn().getPrimaryColumnFormat()));
        assertEquals(0, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertNull(value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
        assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

        assertEquals(Type.TIME, value.getValue().getDownTimeColumn().getDateColumnType());

        assertEquals(timeFormat.getSdf(), value.getValue().getDownTimeColumn().getPrimaryColumnFormat());
        assertEquals(1, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn().getSecondaryColumnFormat());
        assertEquals(-1, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());

        support.verifyAll();
    }

    /**
     * Tests one day column and two time columns that share the same day column.
     *
     * @throws ParseException Bad parse.
     */
    @Test
    public void testDayTimeUpTimeDown() throws ParseException
    {
        DateFormatsConfig configuration = getDateFormats();

        List<DateFormat> dateFormats1 = getFormats(configuration, Type.DATE);
        List<DateFormat> timeFormats1 = getFormats(configuration, Type.TIME);
        List<DateFormat> timeFormats2 = getFormats(configuration, Type.TIME);

        for (DateFormat dateFormat1 : dateFormats1)
        {
            for (DateFormat timeFormat1 : timeFormats1)
            {
                for (DateFormat timeFormat2 : timeFormats2)
                {
                    EasyMockSupport support = new EasyMockSupport();

                    List<List<String>> data = DateDataGenerator.generateDayTimeUpTimeDown(dateFormat1, timeFormat1, timeFormat2);

                    ConfigurationProvider provider = createConfigurationProvider(support, configuration);
                    CellSampler sampler = createSampler(support, data);

                    support.replayAll();

                    DateTimeFinder finder = new DateTimeFinder(provider);

                    ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

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

                    support.verifyAll();
                }
            }
        }
    }

    /**
     * Tests all known formats for two time columns.
     */
    @Test
    public void testDoubleDate()
    {
        DateFormatsConfig configuration = getDateFormats();

        List<DateFormat> dateFormats1 = getFormats(configuration, Type.TIMESTAMP);
        List<DateFormat> dateFormats2 = getFormats(configuration, Type.TIMESTAMP);

        for (DateFormat dateFormat1 : dateFormats1)
        {
            for (DateFormat dateFormat2 : dateFormats2)
            {
                EasyMockSupport support = new EasyMockSupport();

                List<List<String>> data = DateDataGenerator.generateDoubleDate(dateFormat1, dateFormat2);

                ConfigurationProvider provider = createConfigurationProvider(support, configuration);
                CellSampler sampler = createSampler(support, data);

                support.replayAll();

                DateTimeFinder finder = new DateTimeFinder(provider);

                ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

                if (dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
                {
                    assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                    assertEquals(8, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

                    assertNotNull("Failed for format " + dateFormat2.getSdf(), value.getValue().getDownTimeColumn());
                    assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

                    assertEquals(9, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
                }
                else if (!dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
                {
                    assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                    assertEquals(9, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                    assertNull(value.getValue().getDownTimeColumn());
                }
                else if (!dateFormat2.getSdf().contains("y") && dateFormat1.getSdf().contains("y"))
                {
                    assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                    assertEquals(8, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                    assertNull(value.getValue().getDownTimeColumn());
                }
                else
                {
                    assertNull(value.getValue().getUpTimeColumn());
                }

                support.verifyAll();
            }
        }
    }

    /**
     * Tests two time columns with a single up time and a composite down time.
     */
    @Test
    public void testDoubleDate3()
    {
        DateFormatsConfig configuration = getDateFormats();

        List<DateFormat> dateFormats1 = getFormats(configuration, Type.TIMESTAMP);
        List<DateFormat> dateFormats2 = getFormats(configuration, Type.DATE);
        List<DateFormat> timeFormats2 = getFormats(configuration, Type.TIME);
        for (DateFormat dateFormat1 : dateFormats1)
        {
            for (DateFormat dateFormat2 : dateFormats2)
            {
                for (DateFormat timeFormat2 : timeFormats2)
                {
                    EasyMockSupport support = new EasyMockSupport();

                    List<List<String>> data = DateDataGenerator.generateDoubleDate(dateFormat1, dateFormat2, timeFormat2);

                    ConfigurationProvider provider = createConfigurationProvider(support, configuration);
                    CellSampler sampler = createSampler(support, data);

                    support.replayAll();

                    DateTimeFinder finder = new DateTimeFinder(provider);

                    ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

                    if (dateFormat1.getSdf().contains("y"))
                    {
                        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                        assertEquals(0, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                        assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

                        assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

                        assertEquals(1, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
                        assertEquals(2, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
                    }
                    else
                    {
                        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                        assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                        assertEquals(2, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                    }

                    support.verifyAll();
                }
            }
        }
    }

    /**
     * Tests two time columns with two times each of which are represented by
     * two individual date and time columns.
     */
    @Test
    public void testDoubleDate4()
    {
        DateFormatsConfig configuration = getDateFormats();

        List<DateFormat> dateFormats1 = getFormats(configuration, Type.DATE);
        List<DateFormat> timeFormats1 = getFormats(configuration, Type.TIME);
        List<DateFormat> dateFormats2 = getFormats(configuration, Type.DATE);
        List<DateFormat> timeFormats2 = getFormats(configuration, Type.TIME);

        for (DateFormat dateFormat1 : dateFormats1)
        {
            for (DateFormat timeFormat1 : timeFormats1)
            {
                for (DateFormat dateFormat2 : dateFormats2)
                {
                    for (DateFormat timeFormat2 : timeFormats2)
                    {
                        EasyMockSupport support = new EasyMockSupport();

                        List<List<String>> data = DateDataGenerator.generateDoubleDate(dateFormat1, timeFormat1, dateFormat2,
                                timeFormat2);

                        ConfigurationProvider provider = createConfigurationProvider(support, configuration);
                        CellSampler sampler = createSampler(support, data);

                        support.replayAll();

                        DateTimeFinder finder = new DateTimeFinder(provider);

                        ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

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

                        support.verifyAll();
                    }
                }
            }
        }
    }

    /**
     * Tests the case where mock data with a time column and a sparse (20%
     * populate) end time column whose end times, either don't exist, they match
     * the time column or they are later than the time column.
     */
    @Test
    public void testDownTimeTime()
    {
        DateFormatsConfig configuration = getDateFormats();

        List<DateFormat> dateFormats1 = getFormats(configuration, Type.TIMESTAMP);
        List<DateFormat> dateFormats2 = getFormats(configuration, Type.TIMESTAMP);

        for (DateFormat dateFormat1 : dateFormats1)
        {
            for (DateFormat dateFormat2 : dateFormats2)
            {
                EasyMockSupport support = new EasyMockSupport();

                List<List<String>> data = DateDataGenerator.generateDownTimeTime(dateFormat1, dateFormat2);

                ConfigurationProvider provider = createConfigurationProvider(support, configuration);
                CellSampler sampler = createSampler(support, data);

                support.replayAll();

                DateTimeFinder finder = new DateTimeFinder(provider);

                ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

                if (dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
                {
                    assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                    assertEquals(4, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

                    assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

                    assertEquals(9, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
                }
                else if (!dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
                {
                    assertNull(value.getValue().getUpTimeColumn());
                }
                else if (!dateFormat2.getSdf().contains("y") && dateFormat1.getSdf().contains("y"))
                {
                    assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                    assertEquals(4, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                    assertNull(value.getValue().getDownTimeColumn());
                }
                else
                {
                    assertNull(value.getValue().getUpTimeColumn());
                }

                support.verifyAll();
            }
        }
    }

    /**
     * Tests excluding a column based on its name.
     */
    @Test
    public void testExcludeColumn()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormatsConfig configuration = getDateFormats();

        List<List<String>> data = DateDataGenerator.generateSingleDateLotsOfDecimalSeconds();

        ConfigurationProvider provider = createConfigurationProvider(support, configuration);
        CellSampler sampler = createSamplerWithHeaderCells(support, data);

        support.replayAll();

        DateTimeFinder finder = new DateTimeFinder(provider);

        ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

        assertNull(value.getValue().getUpTimeColumn());

        support.verifyAll();
    }

    /**
     * Tests the HHmmss format.
     */
    @Test
    public void testHHmmss()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormatsConfig configuration = getDateFormats();

        List<List<String>> data = DateDataGenerator.generateHHmmss();

        ConfigurationProvider provider = createConfigurationProvider(support, configuration);
        CellSampler sampler = createSampler(support, data);

        support.replayAll();

        DateTimeFinder finder = new DateTimeFinder(provider);

        ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals("yyyyMMdd", value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(8, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals("HHmmss", value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
        assertEquals(9, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());

        support.verifyAll();
    }

    /**
     * Tests two time columns with end time first.
     */
    @Test
    public void testReverseDoubleDate2()
    {
        DateFormatsConfig configuration = getDateFormats();

        List<DateFormat> dateFormats1 = getFormats(configuration, Type.TIMESTAMP);
        List<DateFormat> dateFormats2 = getFormats(configuration, Type.TIMESTAMP);

        for (DateFormat dateFormat1 : dateFormats1)
        {
            for (DateFormat dateFormat2 : dateFormats2)
            {
                EasyMockSupport support = new EasyMockSupport();

                List<List<String>> data = DateDataGenerator.generateReverseDoubleDate(dateFormat1, dateFormat2);

                ConfigurationProvider provider = createConfigurationProvider(support, configuration);
                CellSampler sampler = createSampler(support, data);

                support.replayAll();

                DateTimeFinder finder = new DateTimeFinder(provider);

                ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

                if (dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
                {
                    assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

                    assertEquals(4, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());

                    assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                    assertEquals(7, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                }
                else if (!dateFormat1.getSdf().contains("y") && dateFormat2.getSdf().contains("y"))
                {
                    assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                    assertEquals(7, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                    assertNull(value.getValue().getDownTimeColumn());
                }
                else if (!dateFormat2.getSdf().contains("y") && dateFormat1.getSdf().contains("y"))
                {
                    assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                    assertEquals(4, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                    assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                    assertNull(value.getValue().getDownTimeColumn());
                }
                else
                {
                    assertNull(value.getValue().getUpTimeColumn());
                }

                support.verifyAll();
            }
        }
    }

    /**
     * Tests two time columns with a composite up time and a single column for
     * down time.
     */
    @Test
    public void testReverseDoubleDate3()
    {
        DateFormatsConfig configuration = getDateFormats();

        List<DateFormat> dateFormats1 = getFormats(configuration, Type.DATE);
        List<DateFormat> timeFormats1 = getFormats(configuration, Type.TIME);
        List<DateFormat> dateFormats2 = getFormats(configuration, Type.TIMESTAMP);

        for (DateFormat dateFormat1 : dateFormats1)
        {
            for (DateFormat timeFormat1 : timeFormats1)
            {
                for (DateFormat dateFormat2 : dateFormats2)
                {
                    EasyMockSupport support = new EasyMockSupport();

                    List<List<String>> data = DateDataGenerator.generateReverseDoubleDate(dateFormat1, timeFormat1, dateFormat2);

                    ConfigurationProvider provider = createConfigurationProvider(support, configuration);
                    CellSampler sampler = createSampler(support, data);

                    support.replayAll();

                    DateTimeFinder finder = new DateTimeFinder(provider);

                    ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

                    if (dateFormat2.getSdf().contains("y"))
                    {
                        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                        assertEquals(3, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                        assertEquals(timeFormat1.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
                        assertEquals(4, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());

                        assertEquals(Type.TIMESTAMP, value.getValue().getDownTimeColumn().getDateColumnType());

                        assertEquals(7, value.getValue().getDownTimeColumn().getPrimaryColumnIndex());
                        assertEquals(-1, value.getValue().getDownTimeColumn().getSecondaryColumnIndex());
                    }
                    else
                    {
                        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());

                        assertEquals(3, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                        assertEquals(timeFormat1.getSdf(), value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
                        assertEquals(4, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                    }

                    support.verifyAll();
                }
            }
        }
    }

    /**
     * Tests a single date consisting of two columns.
     */
    @Test
    public void testSingleCompoundDate()
    {
        DateFormatsConfig configuration = getDateFormats();

        List<DateFormat> dateFormats = getFormats(configuration, Type.DATE);
        List<DateFormat> timeFormats = getFormats(configuration, Type.TIME);

        for (DateFormat dateFormat : dateFormats)
        {
            for (DateFormat timeFormat : timeFormats)
            {
                EasyMockSupport support = new EasyMockSupport();

                List<List<String>> data = DateDataGenerator.generateSingleCompoundDate(dateFormat, timeFormat);

                ConfigurationProvider provider = createConfigurationProvider(support, configuration);
                CellSampler sampler = createSampler(support, data);

                support.replayAll();

                DateTimeFinder finder = new DateTimeFinder(provider);

                ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

                assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
                assertEquals(0, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                assertEquals(1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                assertNull(value.getValue().getDownTimeColumn());

                support.verifyAll();
            }
        }
    }

    /**
     * Tests all known formats for a single date column.
     */
    @Test
    public void testSingleDate()
    {
        DateFormatsConfig configuration = getDateFormats();

        List<DateFormat> formats = getFormats(configuration, Type.DATE);

        for (DateFormat format : formats)
        {
            EasyMockSupport support = new EasyMockSupport();

            List<List<String>> data = DateDataGenerator.generateSingleDate(format);

            ConfigurationProvider provider = createConfigurationProvider(support, configuration);
            CellSampler sampler = createSampler(support, data);

            support.replayAll();

            DateTimeFinder finder = new DateTimeFinder(provider);

            ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

            if (format.getSdf().contains("y") && format.getType() == Type.TIMESTAMP || format.getType() == Type.DATE)
            {
                assertNotNull("Failed on format " + format.getSdf(), value.getValue().getUpTimeColumn());
                assertEquals(format.getType(), value.getValue().getUpTimeColumn().getDateColumnType());
                assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                assertNull(value.getValue().getDownTimeColumn());
            }
            else
            {
                assertNull(value.getValue().getUpTimeColumn());
            }

            support.verifyAll();
        }
    }

    /**
     * Tests all known formats for a single date column whose format matches an
     * empty pattern.
     */
    @Test
    public void testSingleDateEmptyPattern()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormatsConfig configuration = new DateFormatsConfig();

        DateFormat emptyFormat = new DateFormat();
        emptyFormat.setType(Type.TIMESTAMP);
        emptyFormat.setSdf("yyyyMMdd HHmmss");

        configuration.getFormats().add(emptyFormat);

        for (DateFormat format : configuration.getFormats())
        {
            List<List<String>> data = DateDataGenerator.generateSingleDate(format);

            ConfigurationProvider provider = createConfigurationProvider(support, configuration);
            CellSampler sampler = createSampler(support, data);

            support.replayAll();

            DateTimeFinder finder = new DateTimeFinder(provider);

            ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

            if (format.getSdf().contains("y") && format.getType() == Type.TIMESTAMP || format.getType() == Type.DATE)
            {
                assertNotNull("Failed on format " + format.getSdf(), value.getValue().getUpTimeColumn());
                assertEquals(format.getType(), value.getValue().getUpTimeColumn().getDateColumnType());
                assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
                assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
                assertNull(value.getValue().getDownTimeColumn());
            }
            else
            {
                assertNull(value.getValue().getUpTimeColumn());
            }

            support.verifyAll();
        }
    }

    /**
     * Tests a Single date with a very long decimal seconds string.
     */
    @Test
    public void testSingleDateLotsOfDecimalSeconds()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormatsConfig configuration = getDateFormats();

        List<List<String>> data = DateDataGenerator.generateSingleDateLotsOfDecimalSeconds();

        ConfigurationProvider provider = createConfigurationProvider(support, configuration);
        CellSampler sampler = createSampler(support, data);

        support.replayAll();

        DateTimeFinder finder = new DateTimeFinder(provider);

        ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals("yyyy:M:d::HH:mm:ss.SSS", value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());

        support.verifyAll();
    }

    /**
     * Tests the yyyyMMdd format.
     */
    @Test
    public void testSingleDateyyyyMMdd()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormatsConfig configuration = getDateFormats();

        List<List<String>> data = DateDataGenerator.generateSingleDateyyyyMMdd();

        ConfigurationProvider provider = createConfigurationProvider(support, configuration);
        CellSampler sampler = createSampler(support, data);

        support.replayAll();

        DateTimeFinder finder = new DateTimeFinder(provider);

        ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

        assertEquals(Type.DATE, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals("yyyyMMdd", value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(1, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());

        support.verifyAll();
    }

    /**
     * Tests the yyyy-MM-dd HH:mm:ss format.
     */
    @Test
    public void testYearMonthDayTime()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormatsConfig configuration = getDateFormats();

        support.replayAll();

        List<List<String>> data = DateDataGenerator.generateYearMonthDayTime();

        ConfigurationProvider provider = createConfigurationProvider(support, configuration);
        CellSampler sampler = createSampler(support, data);

        support.replayAll();

        DateTimeFinder finder = new DateTimeFinder(provider);

        ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals(0, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals(-1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());

        support.verifyAll();
    }

    /**
     * Tests the zHHmmss.SS format.
     */
    @Test
    public void testZ()
    {
        EasyMockSupport support = new EasyMockSupport();

        DateFormatsConfig configuration = getDateFormats();

        support.replayAll();

        DateFormat format = new DateFormat();
        format.setSdf("'z'HHmmss.SS");
        format.setType(Type.TIME);

        DateFormat dateFormat = new DateFormat();
        dateFormat.setSdf("yyyyMMdd");
        dateFormat.setType(Type.DATE);

        List<List<String>> data = DateDataGenerator.generateSingleCompoundDate(dateFormat, format);

        ConfigurationProvider provider = createConfigurationProvider(support, configuration);
        CellSampler sampler = createSampler(support, data);

        support.replayAll();

        DateTimeFinder finder = new DateTimeFinder(provider);

        ValueWithConfidence<DateColumnResults> value = finder.findDates(sampler);

        assertEquals(Type.TIMESTAMP, value.getValue().getUpTimeColumn().getDateColumnType());
        assertEquals("yyyyMMdd", value.getValue().getUpTimeColumn().getPrimaryColumnFormat());
        assertEquals(0, value.getValue().getUpTimeColumn().getPrimaryColumnIndex());
        assertEquals("'z'HHmmss.SS", value.getValue().getUpTimeColumn().getSecondaryColumnFormat());
        assertEquals(1, value.getValue().getUpTimeColumn().getSecondaryColumnIndex());
        assertNull(value.getValue().getDownTimeColumn());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked configuration provider.
     *
     * @param support The easy mock support object.
     * @param config The configuration to return.
     * @return the configuration provider.
     */
    private ConfigurationProvider createConfigurationProvider(EasyMockSupport support, DateFormatsConfig config)
    {
        ConfigurationProvider provider = support.createMock(ConfigurationProvider.class);
        provider.getDateFormats();
        EasyMock.expectLastCall().andReturn(config);
        EasyMock.expectLastCall().atLeastOnce();
        provider.getExcludeColumns();
        EasyMock.expectLastCall().andReturn(New.list("mod"));
        EasyMock.expectLastCall().atLeastOnce();

        return provider;
    }

    /**
     * The cell sampler.
     *
     * @param support The easy mock support.
     * @param data The data for the cell sampler to return.
     * @return The cell sampler.
     */
    private CellSampler createSampler(EasyMockSupport support, List<? extends List<? extends String>> data)
    {
        CellSampler sampler = support.createMock(CellSampler.class);

        sampler.getBeginningSampleCells();
        EasyMock.expectLastCall().andReturn(data);

        sampler.getHeaderCells();
        EasyMock.expectLastCall().andReturn(New.list());

        return sampler;
    }

    /**
     * The cell sampler.
     *
     * @param support The easy mock support.
     * @param data The data for the cell sampler to return.
     * @return The cell sampler.
     */
    private CellSampler createSamplerWithHeaderCells(EasyMockSupport support, List<? extends List<? extends String>> data)
    {
        CellSampler sampler = support.createMock(CellSampler.class);

        sampler.getBeginningSampleCells();
        EasyMock.expectLastCall().andReturn(data);

        List<String> columnNames = New.list();
        if (!data.isEmpty())
        {
            List<? extends String> row = data.get(0);
            for (String cell : row)
            {
                if (cell.contains("N/A") || cell.contains("-"))
                {
                    columnNames.add("Last Modified");
                }
                else
                {
                    columnNames.add("column Name");
                }
            }
        }

        sampler.getHeaderCells();
        EasyMock.expectLastCall().andReturn(columnNames);

        return sampler;
    }

    /**
     * Gets the date formats.
     *
     * @return The list of known configured date formats.
     */
    private DateFormatsConfig getDateFormats()
    {
        ClasspathPreferencesPersistenceManager manager = new ClasspathPreferencesPersistenceManager();
        InternalPreferencesIF preferences = manager.load(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC, null, false);

        DateFormatsConfig config = preferences.getJAXBObject(DateFormatsConfig.class, "DateFormatConfig", null);

        List<DateFormat> formatsToRemove = New.list();
        for (DateFormat format : config.getFormats())
        {
            String sdf = format.getSdf();

            if (DateTimeFormats.DATE_FORMAT.equals(sdf) || "yyyy/MM/dd".equals(sdf) || "MM/dd/yyyy".equals(sdf))
            {
                formatsToRemove.add(format);
            }
        }

        config.getFormats().removeAll(formatsToRemove);

        return config;
    }

    /**
     * Gets the list of formats that are the specified format type.
     *
     * @param config The configuration to get the formats from.
     * @param formatType The format type to get.
     * @return The list of formats within the configuration of the specified
     *         type.
     */
    private List<DateFormat> getFormats(DateFormatsConfig config, Type formatType)
    {
        List<DateFormat> formats = New.list();

        for (DateFormat format : config.getFormats())
        {
            if (format.getType() == formatType)
            {
                boolean canAdd = true;
                if (formatType == Type.DATE || formatType == Type.TIMESTAMP)
                {
                    String sdf = format.getSdf();
                    int monthIndex = sdf.indexOf('M');
                    int dayIndex = sdf.indexOf('d');

                    // Remove any MM dd formats just to make testing easier
                    // since MM is the same as M.
                    int monthMonthIndex = sdf.indexOf("MM");
                    int dayDayIndex = sdf.indexOf("dd");

                    if (dayIndex < monthIndex || monthMonthIndex >= 0 || dayDayIndex >= 0)
                    {
                        canAdd = false;
                    }
                }

                if (canAdd)
                {
                    formats.add(format);
                }
            }
        }

        return formats;
    }
}
