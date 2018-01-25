package io.opensphere.csvcommon.detect.datetime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.preferences.ClasspathPreferencesPersistenceManager;
import io.opensphere.core.preferences.InternalPreferencesIF;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.detect.ValuesWithConfidence;
import io.opensphere.csvcommon.detect.datetime.DateTimeDetector;
import io.opensphere.csvcommon.detect.datetime.util.DateDataGenerator;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Tests the DateTimeDetector class.
 *
 */
public class DateTimeDetectorTest
{
    /**
     * Tests the DateTimeDetector.
     */
    @Test
    public void testDetect()
    {
        DateFormatsConfig configuration = getDateFormats();

        for (DateFormat format : configuration.getFormats())
        {
            EasyMockSupport support = new EasyMockSupport();

            PreferencesRegistry preferencesRegistry = createPreferencesRegistry(support, configuration);

            List<List<String>> data = DateDataGenerator.generateSingleDate(format);

            CellSampler cellSampler = createCellSampler(support, data);

            support.replayAll();

            DateTimeDetector detector = new DateTimeDetector(preferencesRegistry);

            ValuesWithConfidence<DateColumnResults> value = detector.detect(cellSampler);

            if (format.getSdf().contains("y") && format.getType() == Type.TIMESTAMP || format.getType() == Type.DATE)
            {
                assertNotNull("Failed on format " + format.getSdf(), value.getBestValue().getUpTimeColumn());
                assertEquals(format.getType(), value.getBestValue().getUpTimeColumn().getDateColumnType());
                assertEquals(1, value.getBestValue().getUpTimeColumn().getPrimaryColumnIndex());
                assertEquals(-1, value.getBestValue().getUpTimeColumn().getSecondaryColumnIndex());
                assertNull(value.getBestValue().getDownTimeColumn());
            }
            else
            {
                assertNull(value.getBestValue().getUpTimeColumn());
            }

            support.verifyAll();
        }
    }

    /**
     * Creates an easy mock cell sampler.
     *
     * @param support The easy mock support object.
     * @param data The data to return.
     * @return The cell sampler.
     */
    private CellSampler createCellSampler(EasyMockSupport support, List<List<String>> data)
    {
        CellSampler sampler = support.createMock(CellSampler.class);
        sampler.getBeginningSampleCells();
        EasyMock.expectLastCall().andReturn(data);
        sampler.getHeaderCells();
        EasyMock.expectLastCall().andReturn(New.list("mod"));

        return sampler;
    }

    /**
     * Create an easy mocked preferences registry.
     *
     * @param support The easy mock support object.
     * @param formats The formats to return.
     * @return The system preferences registry.
     */
    @SuppressWarnings("unchecked")
    private PreferencesRegistry createPreferencesRegistry(EasyMockSupport support, DateFormatsConfig formats)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.getJAXBObject(EasyMock.eq(DateFormatsConfig.class), EasyMock.eq("DateFormatConfig"),
                EasyMock.isA(DateFormatsConfig.class));
        EasyMock.expectLastCall().andReturn(formats);
        preferences.getStringList(EasyMock.cmpEq(ColumnType.TIMESTAMP.name() + "_exclude"), (List<String>)EasyMock.isNull());
        EasyMock.expectLastCall().andReturn(New.list("mod"));
        preferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(true);

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().atLeastOnce();
        registry.getPreferences(EasyMock.eq(CSVColumnPrefsUtil.class));
        EasyMock.expectLastCall().andReturn(preferences);

        return registry;
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

        List<DateFormat> formats = New.list();

        for (DateFormat format : config.getFormats())
        {
            if (format.getType() == Type.DATE || format.getType() == Type.TIMESTAMP)
            {
                boolean canAdd = true;

                String sdf = format.getSdf();
                int monthIndex = sdf.indexOf('M');
                int dayIndex = sdf.indexOf('d');

                if (dayIndex < monthIndex)
                {
                    canAdd = false;
                }

                if (canAdd)
                {
                    formats.add(format);
                }
            }
        }

        config.setFormats(formats);

        return config;
    }
}
