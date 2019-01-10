package io.opensphere.csvcommon.detect.controller;

import java.io.FileNotFoundException;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.model.IntegerRange;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.datetime.DateColumn;
import io.opensphere.csvcommon.common.datetime.DateColumnResults;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.detect.ListLineSampler;
import io.opensphere.csvcommon.detect.columnformat.DelimitedColumnFormatParameters;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.csvcommon.util.CsvTestUtils;
import io.opensphere.csvcommon.util.LocationTestUtils;
import io.opensphere.mantle.util.MantleConstants;

/** Test {@link DetectionControllerImpl}. */
@SuppressWarnings("boxing")
public class DetectionControllerImplTest extends EasyMockSupport
{
    /** Test the test. */
    @Test
    public void test()
    {
        Assert.assertTrue(true);
    }

    /**
     * Test
     * {@link DetectionControllerImpl#detectParameters(CSVParseParameters, io.opensphere.csvcommon.common.LineSampler, LineSamplerFactory, String)}
     * .
     *
     * @throws FileNotFoundException bad file.
     */
    @Test
    public void testDetectParameters() throws FileNotFoundException
    {
        final DateFormatsConfig formatsConfig = new DateFormatsConfig();
        final DateFormat dateFormat = new DateFormat(Type.TIMESTAMP, "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2}Z");
        formatsConfig.addFormat(dateFormat);

        final PreferencesRegistry prefsRegistry = createMock(PreferencesRegistry.class);
        final Preferences prefs = createMock(Preferences.class);
        final Preferences oldPrefs = createNiceMock(Preferences.class);
        EasyMock.expect(prefsRegistry.getPreferences(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC)).andReturn(prefs)
                .anyTimes();
        EasyMock.expect(prefsRegistry.getPreferences("DateFormatConfiguration")).andReturn(oldPrefs).anyTimes();
        EasyMock.expect(prefs.getJAXBObject(EasyMock.eq(DateFormatsConfig.class),
                EasyMock.eq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY), EasyMock.isA(DateFormatsConfig.class)))
                .andReturn(formatsConfig).anyTimes();
        EasyMock.expect(prefs.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false))).andReturn(true).anyTimes();

        LocationTestUtils.loadLocationColumnLists(prefs);
        EasyMock.expect(prefsRegistry.getPreferences(CSVColumnPrefsUtil.class)).andReturn(prefs).anyTimes();

        final ListLineSampler listLineSampler = new ListLineSampler(CsvTestUtils.createBasicDelimitedData(", "),
                CsvTestUtils.createBasicDelimitedData(", "), 10);

        final LineSamplerFactory factory = createNiceMock(LineSamplerFactory.class);
        factory.createSampler((char[])EasyMock.isNull());
        EasyMock.expectLastCall().andReturn(listLineSampler);

        EasyMock.expect(prefs.getStringList("COLOR", null)).andReturn(New.list()).times(2);
        replayAll();

        final DetectionControllerImpl controller = new DetectionControllerImpl(prefsRegistry, null);

        final DetectedParameters result = controller.detectParameters(new CSVParseParameters(), listLineSampler, factory, null);

        Assert.assertEquals(new DelimitedColumnFormatParameters(Character.valueOf(','), null, 5),
                result.getColumnFormatParameter().getBestValue());
        Assert.assertNull(result.getCommentParameter().getBestValue());
        Assert.assertEquals(new IntegerRange(0, 210), result.getDataLinesParameter().getBestValue());
        Assert.assertEquals(Integer.valueOf(0), result.getHeaderLineParameter().getBestValue());

        final DateColumnResults dateColumn = result.getDateColumnParameter().getBestValue();
        Assert.assertNull(dateColumn.getDownTimeColumn());
        final DateColumn upColumn = dateColumn.getUpTimeColumn();
        Assert.assertEquals(DateFormat.Type.TIMESTAMP, upColumn.getDateColumnType());
        Assert.assertEquals(0, upColumn.getPrimaryColumnIndex());
        Assert.assertEquals(dateFormat.getSdf(), upColumn.getPrimaryColumnFormat());
    }
}
