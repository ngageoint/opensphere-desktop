package io.opensphere.csv.ui.columndefinition.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.common.configuration.date.DateFormatsConfig;
import io.opensphere.core.preferences.ClasspathPreferencesPersistenceManager;
import io.opensphere.core.preferences.InternalPreferencesIF;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.csv.util.ColumnHeaders;
import io.opensphere.csv.util.CsvTestUtils;
import io.opensphere.csvcommon.common.CellSampler;
import io.opensphere.csvcommon.config.v2.CSVParseParameters;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionTableModel;
import io.opensphere.importer.config.ColumnType;
import io.opensphere.importer.config.SpecialColumn;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Tests the ColumnDefinitionController class.
 *
 */
public class ColumnDefinitionControllerTest
{
    /**
     * Tests that changes made to the loaded model gets saved back to the
     * CSVParseParameters.
     */
    @Test
    public void testApplyChanges()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = createPreferencesRegistry(support);
        CellSampler sampler = createSampler(support);
        ColumnDefinitionModel model = createModel();

        support.replayAll();

        @SuppressWarnings("unused")
        ColumnDefinitionController controller = new ColumnDefinitionController(registry, model, sampler);

        ColumnDefinitionTableModel definitionTable = model.getDefinitionTableModel();

        CSVParseParameters parameters = model.getSelectedParameters();

        ColumnDefinitionRow timeRow = definitionTable.getRow(0);
        model.setSelectedDefinition(timeRow);
        timeRow.setFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        SpecialColumn timeColumn = parameters.getSpecialColumn(ColumnType.TIMESTAMP);
        assertEquals(0, timeColumn.getColumnIndex());
        assertEquals("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timeColumn.getFormat());

        ColumnDefinitionRow latRow = definitionTable.getRow(1);
        model.setSelectedDefinition(latRow);
        latRow.setDataType(null);

        assertEquals(2, parameters.getSpecialColumns().size());

        ColumnDefinitionRow lonRow = definitionTable.getRow(2);
        model.setSelectedDefinition(lonRow);
        lonRow.setColumnName("Longitude");

        List<? extends String> columnNames = parameters.getColumnNames();
        assertEquals("Longitude", columnNames.get(2));

        ColumnDefinitionRow name2 = definitionTable.getRow(4);
        model.setSelectedDefinition(name2);
        name2.setIsImport(true);

        columnNames = parameters.getColumnNames();
        assertEquals(ColumnHeaders.TIME.toString(), columnNames.get(0));
        assertEquals(ColumnHeaders.LAT.toString(), columnNames.get(1));
        assertEquals("Longitude", columnNames.get(2));
        assertEquals(ColumnHeaders.NAME.toString(), columnNames.get(3));
        assertEquals(ColumnHeaders.NAME2.toString(), columnNames.get(4));

        assertEquals(2, parameters.getSpecialColumns().size());

        timeColumn = parameters.getSpecialColumn(ColumnType.TIMESTAMP);
        assertEquals(0, timeColumn.getColumnIndex());
        assertEquals("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timeColumn.getFormat());

        SpecialColumn lonColumn = parameters.getSpecialColumn(ColumnType.LON);
        assertEquals(2, lonColumn.getColumnIndex());
        assertEquals("Decimal", lonColumn.getFormat());

        assertTrue(parameters.getColumnsToIgnore().isEmpty());

        support.verifyAll();
    }

    /**
     * Verifies that all data gets populated appropriately both in the column
     * table and in the before after table, and in the available data types and
     * available formats.
     */
    @Test
    public void testLoadExisting()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = createPreferencesRegistry(support);
        CellSampler sampler = createSampler(support);
        ColumnDefinitionModel model = createModel();

        // Add a bad special column.
        SpecialColumn specialColumn = new SpecialColumn();
        specialColumn.setColumnIndex(3);
        model.getSelectedParameters().getSpecialColumns().add(specialColumn);

        support.replayAll();

        @SuppressWarnings("unused")
        ColumnDefinitionController controller = new ColumnDefinitionController(registry, model, sampler);

        assertFalse(model.getAvailableDataTypes().isEmpty());
        assertFalse(model.getAvailableFormats().isEmpty());
        assertFalse(model.getBeforeAfterTableModel().getRowCount() == 0);

        ColumnDefinitionTableModel definitionTable = model.getDefinitionTableModel();
        assertEquals(5, definitionTable.getRowCount());

        ColumnDefinitionRow timeRow = definitionTable.getRow(0);
        assertTrue(timeRow.isImport());
        assertEquals(0, timeRow.getColumnId());
        assertEquals(ColumnHeaders.TIME.toString(), timeRow.getColumnName());
        assertEquals(ColumnType.TIMESTAMP.toString(), timeRow.getDataType());
        assertEquals("yyyy-MM-dd'T'HH:mm:ss'Z'", timeRow.getFormat());

        ColumnDefinitionRow latRow = definitionTable.getRow(1);
        assertTrue(latRow.isImport());
        assertEquals(1, latRow.getColumnId());
        assertEquals(ColumnHeaders.LAT.toString(), latRow.getColumnName());
        assertEquals(ColumnType.LAT.toString(), latRow.getDataType());
        assertEquals("Decimal", latRow.getFormat());

        ColumnDefinitionRow lonRow = definitionTable.getRow(2);
        assertTrue(lonRow.isImport());
        assertEquals(2, lonRow.getColumnId());
        assertEquals(ColumnHeaders.LON.toString(), lonRow.getColumnName());
        assertEquals(ColumnType.LON.toString(), lonRow.getDataType());
        assertEquals("Decimal", lonRow.getFormat());

        ColumnDefinitionRow nameRow = definitionTable.getRow(3);
        assertTrue(nameRow.isImport());
        assertEquals(3, nameRow.getColumnId());
        assertEquals(ColumnHeaders.NAME.toString(), nameRow.getColumnName());
        assertNull(nameRow.getDataType());
        assertNull(nameRow.getFormat());

        ColumnDefinitionRow name2Row = definitionTable.getRow(4);
        assertTrue(name2Row.isImport());
        assertEquals(4, name2Row.getColumnId());
        assertEquals(ColumnHeaders.NAME2.toString(), name2Row.getColumnName());
        assertNull(name2Row.getDataType());
        assertNull(name2Row.getFormat());

        support.verifyAll();
    }

    /**
     * Creates the prepopulated model.
     *
     * @return The model.
     */
    private ColumnDefinitionModel createModel()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        List<String> columnNames = Arrays.asList(ColumnHeaders.TIME.toString(), ColumnHeaders.LAT.toString(),
                ColumnHeaders.LON.toString(), ColumnHeaders.NAME.toString(), ColumnHeaders.NAME2.toString());

        CSVParseParameters parameters = new CSVParseParameters();
        parameters.setColumnNames(columnNames);

        parameters.setColumnsToIgnore(New.<Integer>list(4));

        SpecialColumn timeColumn = new SpecialColumn();
        timeColumn.setColumnIndex(0);
        timeColumn.setColumnType(ColumnType.TIMESTAMP);
        timeColumn.setFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        SpecialColumn latColumn = new SpecialColumn();
        latColumn.setColumnIndex(1);
        latColumn.setColumnType(ColumnType.LAT);
        latColumn.setFormat("Decimal");

        SpecialColumn lonColumn = new SpecialColumn();
        lonColumn.setColumnIndex(2);
        lonColumn.setColumnType(ColumnType.LON);
        lonColumn.setFormat("Decimal");

        parameters.getSpecialColumns().add(timeColumn);
        parameters.getSpecialColumns().add(latColumn);
        parameters.getSpecialColumns().add(lonColumn);

        model.setSelectedParameters(parameters);

        return model;
    }

    /**
     * Creates an easy mocked toolbox.
     *
     * @param support The easy mock support object.
     * @return The toolbox.
     */
    @SuppressWarnings("unchecked")
    private PreferencesRegistry createPreferencesRegistry(EasyMockSupport support)
    {
        Preferences formatPreferences = support.createMock(Preferences.class);
        formatPreferences.<DateFormatsConfig>getJAXBObject(EasyMock.isA(Class.class), EasyMock.isA(String.class),
                EasyMock.isA(DateFormatsConfig.class));
        EasyMock.expectLastCall().andReturn(getDateFormats());
        EasyMock.expectLastCall().atLeastOnce();
        formatPreferences.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        EasyMock.expectLastCall().atLeastOnce();

        Preferences listToolPreferences = support.createMock(Preferences.class);
        listToolPreferences.getInt(EasyMock.isA(String.class), EasyMock.eq(0));
        EasyMock.expectLastCall().andReturn(Integer.valueOf(0));
        EasyMock.expectLastCall().atLeastOnce();

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.eq(ListToolPreferences.class));
        EasyMock.expectLastCall().andReturn(listToolPreferences);
        EasyMock.expectLastCall().atLeastOnce();

        registry.getPreferences(EasyMock.eq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(formatPreferences);
        EasyMock.expectLastCall().atLeastOnce();

        return registry;
    }

    /**
     * Creates an easy mocked cell sampler.
     *
     * @param support The easy mock support object.
     * @return the cell sampler.
     */
    private CellSampler createSampler(EasyMockSupport support)
    {
        CellSampler sampler = support.createMock(CellSampler.class);
        sampler.getBeginningSampleCells();
        EasyMock.expectLastCall().andReturn(CsvTestUtils.createBasicData());

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

        return config;
    }
}
