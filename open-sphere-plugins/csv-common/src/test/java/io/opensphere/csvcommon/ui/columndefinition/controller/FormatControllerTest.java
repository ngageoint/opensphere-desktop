package io.opensphere.csvcommon.ui.columndefinition.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormat;
import io.opensphere.core.common.configuration.date.DateFormat.Type;
import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.preferences.ClasspathPreferencesPersistenceManager;
import io.opensphere.core.preferences.InternalPreferencesIF;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.common.Constants;
import io.opensphere.csvcommon.common.datetime.ConfigurationProviderImpl;
import io.opensphere.csvcommon.detect.util.CSVColumnPrefsUtil;
import io.opensphere.csvcommon.ui.columndefinition.model.BeforeAfterRow;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Tests the FormatController class.
 *
 */
public class FormatControllerTest
{
    /**
     * Tests that the selected format changes when the data type changes.
     */
    @SuppressWarnings("unused")
    @Test
    public void testDataTypeChanged()
    {
        DateFormatsConfig config = getDateFormats();

        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry preferencesRegistry = createPreferencesRegistry(support, config, null);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();

        String formatString = "format";

        ColumnDefinitionRow row0 = new ColumnDefinitionRow();
        row0.setColumnId(0);
        row0.setColumnName("column0");
        row0.setIsImport(true);

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setColumnId(1);
        row1.setColumnName("column1");
        row1.setIsImport(true);

        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setColumnId(2);
        row2.setColumnName("column2");
        row2.setIsImport(true);
        row2.setDataType(ColumnType.TIMESTAMP.toString());
        row2.setFormat(formatString);

        List<List<String>> sampleData = New.list();
        sampleData.add(New.list("2014-05-21 14:51:10"));

        model.setSampleData(sampleData);

        model.getDefinitionTableModel().addRows(New.list(row0, row1, row2));

        FormatController controller = new FormatController(preferencesRegistry, model);

        model.setSelectedDefinition(row0);
        row0.setDataType(ColumnType.TIMESTAMP.toString());
        row0.setFormat(formatString);

        assertTrue(model.canAddFormats());

        row0.setDataType(null);
        assertNull(row0.getFormat());

        row0.setDataType(ColumnType.LAT.toString());
        assertFalse(model.canAddFormats());

        row0.setDataType(ColumnType.TIME.toString());
        row0.setFormat(formatString);
        row0.setDataType(ColumnType.TIME.toString());

        assertTrue(model.canAddFormats());
        assertEquals(formatString, row0.getFormat());

        row0.setDataType(ColumnType.TIMESTAMP.toString());
        assertTrue(model.canAddFormats());
        assertTrue(row0.getFormat().startsWith("yyyy"));

        row0.setDataType(ColumnType.TIME.toString());
        row0.setFormat(formatString);

        model.setSelectedDefinition(row1);

        assertEquals(formatString, row0.getFormat());
        assertNull(row1.getFormat());

        model.setSelectedDefinition(row2);

        assertEquals(formatString, row2.getFormat());

        support.verifyAll();
    }

    /**
     * Tests the save current format function.
     */
    @Test
    public void testSaveCurrentFormat()
    {
        String newFormat = "y";

        DateFormatsConfig config = getDateFormats();

        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry preferencesRegistry = createPreferencesRegistry(support, config, "yyyy");

        support.replayAll();

        BeforeAfterRow successRow = new BeforeAfterRow();
        successRow.setAfterValue("05/20/2014");

        BeforeAfterRow failRow1 = new BeforeAfterRow();
        failRow1.setAfterValue("N/A");

        BeforeAfterRow failRow2 = new BeforeAfterRow();
        failRow2.setAfterValue(Constants.ERROR_LABEL);

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        ColumnDefinitionRow selectedColumn = new ColumnDefinitionRow();
        selectedColumn.setDataType(ColumnType.DATE.toString());
        selectedColumn.setFormat(newFormat);

        @SuppressWarnings("unused")
        FormatController controller = new FormatController(preferencesRegistry, model);
        model.setSelectedDefinition(selectedColumn);

        model.getBeforeAfterTableModel().addRows(New.list(failRow1));
        model.getBeforeAfterTableModel().clear();

        newFormat = "yy";
        selectedColumn.setFormat(newFormat);

        model.getBeforeAfterTableModel().addRows(New.list(failRow2));
        model.getBeforeAfterTableModel().clear();

        newFormat = "yyy";
        selectedColumn.setFormat(newFormat);

        model.getAvailableFormats().add(newFormat);

        model.getBeforeAfterTableModel().addRows(New.list(successRow));

        model.getAvailableFormats().clear();

        newFormat = "yyyy";
        selectedColumn.setFormat(newFormat);

        model.getBeforeAfterTableModel().addRows(New.list(successRow));

        support.verifyAll();
    }

    /**
     * Tests when data types are changed and verifies the formats are populated
     * appropriately.
     */
    @Test
    public void testUpdate()
    {
        DateFormatsConfig config = getDateFormats();

        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry preferencesRegistry = createPreferencesRegistry(support, config, null);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        model.setSampleData(New.<List<String>>list());
        ColumnDefinitionRow selectedColumn = new ColumnDefinitionRow();
        selectedColumn.setDataType(ColumnType.DATE.toString());
        model.getDefinitionTableModel().addRows(New.list(selectedColumn));

        @SuppressWarnings("unused")
        FormatController controller = new FormatController(preferencesRegistry, model);
        model.setSelectedDefinition(selectedColumn);

        List<String> actualAvailableFormats = model.getAvailableFormats();
        Set<String> actuals = New.set(actualAvailableFormats);

        assertEquals(actuals.size(), actualAvailableFormats.size());
        assertTrue(model.getDefinitionTableModel().isCellEditable(0, 3));

        Set<String> expected = New.set();
        for (DateFormat format : config.getFormats())
        {
            if (format.getType() == Type.DATE)
            {
                expected.add(format.getSdf());
            }
        }

        assertEquals(expected.size(), actuals.size());

        for (String anExpected : expected)
        {
            assertTrue(actuals.contains(anExpected));
        }

        selectedColumn.setDataType(ColumnType.TIMESTAMP.toString());

        actualAvailableFormats = model.getAvailableFormats();
        actuals = New.set(actualAvailableFormats);

        assertEquals(actuals.size(), actualAvailableFormats.size());

        expected = New.set();
        for (DateFormat format : config.getFormats())
        {
            if (format.getType() == Type.TIMESTAMP)
            {
                expected.add(format.getSdf());
            }
        }

        assertEquals(expected.size(), actuals.size());

        for (String anExpected : expected)
        {
            assertTrue(actuals.contains(anExpected));
        }

        support.verifyAll();
    }

    /**
     * Tests when no data type is selected.
     */
    @Test
    public void testUpdateNoDataType()
    {
        DateFormatsConfig config = getDateFormats();

        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry preferencesRegistry = createPreferencesRegistry(support, config, null);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        ColumnDefinitionRow selectedColumn = new ColumnDefinitionRow();
        selectedColumn.setDataType(ColumnType.TIME.toString());

        @SuppressWarnings("unused")
        FormatController controller = new FormatController(preferencesRegistry, model);
        model.setSelectedDefinition(selectedColumn);

        List<String> actualAvailableFormats = model.getAvailableFormats();
        Set<String> actuals = New.set(actualAvailableFormats);

        assertEquals(actuals.size(), actualAvailableFormats.size());

        Set<String> expected = New.set();
        for (DateFormat format : config.getFormats())
        {
            if (format.getType() == Type.TIME)
            {
                expected.add(format.getSdf());
            }
        }

        assertEquals(expected.size(), actuals.size());

        for (String anExpected : expected)
        {
            assertTrue(actuals.contains(anExpected));
        }

        selectedColumn = new ColumnDefinitionRow();
        model.setSelectedDefinition(selectedColumn);

        actualAvailableFormats = model.getAvailableFormats();

        assertEquals(0, actualAvailableFormats.size());

        support.verifyAll();
    }

    /**
     * Tests when a column is unselected.
     */
    @Test
    public void testUpdateNoSelectedColumn()
    {
        DateFormatsConfig config = getDateFormats();

        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry preferencesRegistry = createPreferencesRegistry(support, config, null);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();
        ColumnDefinitionRow selectedColumn = new ColumnDefinitionRow();
        selectedColumn.setDataType(ColumnType.TIME.toString());

        @SuppressWarnings("unused")
        FormatController controller = new FormatController(preferencesRegistry, model);
        model.setSelectedDefinition(selectedColumn);

        List<String> actualAvailableFormats = model.getAvailableFormats();
        Set<String> actuals = New.set(actualAvailableFormats);

        assertEquals(actuals.size(), actualAvailableFormats.size());

        Set<String> expected = New.set();
        for (DateFormat format : config.getFormats())
        {
            if (format.getType() == Type.TIME)
            {
                expected.add(format.getSdf());
            }
        }

        assertEquals(expected.size(), actuals.size());

        for (String anExpected : expected)
        {
            assertTrue(actuals.contains(anExpected));
        }

        model.setSelectedDefinition(null);

        actualAvailableFormats = model.getAvailableFormats();

        assertEquals(0, actualAvailableFormats.size());

        support.verifyAll();
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support The easy mock support object.
     * @param config The config to return.
     * @param newFormat A new format if save format should be expected to be
     *            called.
     * @return The easy mocked toolbox.
     */
    @SuppressWarnings("unchecked")
    private PreferencesRegistry createPreferencesRegistry(EasyMockSupport support, DateFormatsConfig config,
            final String newFormat)
    {
        Preferences preferences = support.createMock(Preferences.class);

        if (newFormat != null)
        {
            preferences.putJAXBObject(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY),
                    EasyMock.isA(DateFormatsConfig.class), EasyMock.eq(true), EasyMock.isA(ConfigurationProviderImpl.class));
            EasyMock.expectLastCall().andAnswer(new IAnswer<Object>()
            {
                @Override
                public Object answer()
                {
                    DateFormatsConfig config = (DateFormatsConfig)EasyMock.getCurrentArguments()[1];

                    Set<String> formatsSdfs = New.set();
                    for (DateFormat format : config.getFormats())
                    {
                        formatsSdfs.add(format.getSdf());
                    }

                    assertTrue(formatsSdfs.contains(newFormat));
                    return null;
                }
            });
        }

        preferences.getJAXBObject(EasyMock.eq(DateFormatsConfig.class),
                EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_KEY), EasyMock.isA(DateFormatsConfig.class));
        EasyMock.expectLastCall().andReturn(config);
        EasyMock.expectLastCall().atLeastOnce();
        preferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        EasyMock.expectLastCall().atLeastOnce();

        preferences.getStringList(EasyMock.isA(String.class), (List<String>)EasyMock.isNull());
        EasyMock.expectLastCall().andReturn(New.<String>list());
        EasyMock.expectLastCall().anyTimes();

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.cmpEq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().atLeastOnce();

        registry.getPreferences(EasyMock.eq(CSVColumnPrefsUtil.class));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().anyTimes();

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

        return config;
    }
}
