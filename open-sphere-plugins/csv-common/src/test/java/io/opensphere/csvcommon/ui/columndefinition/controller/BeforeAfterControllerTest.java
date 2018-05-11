package io.opensphere.csvcommon.ui.columndefinition.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.core.model.LatLonAltParser;
import io.opensphere.core.preferences.ListToolPreferences;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.DateTimeFormats;
import io.opensphere.csvcommon.common.Constants;
import io.opensphere.csvcommon.detect.datetime.util.DateDataGenerator;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.util.CsvTestUtils;
import io.opensphere.mantle.util.MantleConstants;

/**
 * Tests the BeforeAfterController class.
 *
 */
@SuppressWarnings("boxing")
public class BeforeAfterControllerTest
{
    /**
     * The name of the ourTestColumnName.
     */
    private static final String ourTestColumnName = "Test Column";

    /**
     * Tests when a format errors out for a given sample data.
     */
    @Test
    public void testUpdateBadFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = createPreferencesRegistry(support);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();

        @SuppressWarnings("unused")
        BeforeAfterController controller = new BeforeAfterController(registry, model);

        ColumnDefinitionRow selectedRow = new ColumnDefinitionRow();
        selectedRow.setColumnId(1);
        selectedRow.setDataType("Date");
        selectedRow.setColumnName(ourTestColumnName);
        selectedRow.setFormat(null);

        model.setSampleData(DateDataGenerator.generateSingleDateyyyyMMdd());
        model.setSelectedDefinition(selectedRow);

        selectedRow.setFormat("MM-dd-yyyy");

        int rowIndex = 0;

        for (List<? extends String> row : model.getSampleData())
        {
            String original = row.get(1);

            assertEquals(original, model.getBeforeAfterTableModel().getValueAt(rowIndex, 0).toString());
            assertEquals(Constants.ERROR_LABEL, model.getBeforeAfterTableModel().getValueAt(rowIndex, 1).toString());
            rowIndex++;
        }

        assertTrue(model.getBeforeAfterTableModel().getColumnName(0).startsWith(ourTestColumnName));
        assertTrue(model.getBeforeAfterTableModel().getColumnName(1).startsWith(ourTestColumnName));

        support.verifyAll();
    }

    /**
     * Tests when a column is selected but it doesn't have a format defined.
     */
    @Test
    public void testUpdateNewColumnNoFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = createPreferencesRegistry(support);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();

        @SuppressWarnings("unused")
        BeforeAfterController controller = new BeforeAfterController(registry, model);

        ColumnDefinitionRow selectedRow = new ColumnDefinitionRow();
        selectedRow.setColumnId(1);
        selectedRow.setDataType("Date");
        selectedRow.setColumnName(ourTestColumnName);
        selectedRow.setFormat(null);

        model.setSampleData(DateDataGenerator.generateSingleDateyyyyMMdd());
        model.setSelectedDefinition(selectedRow);

        int rowIndex = 0;
        for (List<? extends String> row : model.getSampleData())
        {
            assertEquals(row.get(1), model.getBeforeAfterTableModel().getValueAt(rowIndex, 0).toString());
            rowIndex++;
        }

        assertTrue(model.getBeforeAfterTableModel().getColumnName(0).startsWith(ourTestColumnName));
        assertTrue(model.getBeforeAfterTableModel().getColumnName(1).startsWith(ourTestColumnName));

        support.verifyAll();
    }

    /**
     * Tests when a format for the currently selected column has been updated.
     */
    @Test
    public void testUpdateNewFormat()
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = createPreferencesRegistry(support);

        support.replayAll();

        ColumnDefinitionModel model = new ColumnDefinitionModel();

        @SuppressWarnings("unused")
        BeforeAfterController controller = new BeforeAfterController(registry, model);

        ColumnDefinitionRow selectedRow = new ColumnDefinitionRow();
        selectedRow.setColumnId(1);
        selectedRow.setDataType("Date");
        selectedRow.setColumnName(ourTestColumnName);
        selectedRow.setFormat(null);

        model.setSampleData(DateDataGenerator.generateSingleDateyyyyMMdd());
        model.setSelectedDefinition(selectedRow);

        selectedRow.setFormat("yyyyMMdd");

        int rowIndex = 0;
        SimpleDateFormat beforeFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat afterFormat = new SimpleDateFormat(DateTimeFormats.DATE_FORMAT);

        for (List<? extends String> row : model.getSampleData())
        {
            String original = row.get(1);

            String afterValue;

            try
            {
                afterValue = afterFormat.format(beforeFormat.parse(original));
            }
            catch (ParseException e)
            {
                afterValue = Constants.ERROR_LABEL;
            }

            assertEquals(original, model.getBeforeAfterTableModel().getValueAt(rowIndex, 0).toString());
            assertEquals("Assert failed for row index : " + rowIndex, afterValue,
                    model.getBeforeAfterTableModel().getValueAt(rowIndex, 1).toString());
            rowIndex++;
        }

        assertTrue(model.getBeforeAfterTableModel().getColumnName(0).startsWith(ourTestColumnName));
        assertTrue(model.getBeforeAfterTableModel().getColumnName(1).startsWith(ourTestColumnName));

        support.verifyAll();
    }

    /**
     * Tests when a the selected column has been updated to nothing selected.
     */
    @Test
    public void testUpdateNoColumn()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        @SuppressWarnings("unused")
        BeforeAfterController controller = new BeforeAfterController(null, model);

        ColumnDefinitionRow selectedRow = new ColumnDefinitionRow();
        selectedRow.setColumnId(1);
        selectedRow.setColumnName(ourTestColumnName);
        selectedRow.setFormat(null);

        model.setSampleData(DateDataGenerator.generateSingleDateyyyyMMdd());
        model.setSelectedDefinition(selectedRow);
        model.setSelectedDefinition(null);

        assertEquals(0, model.getBeforeAfterTableModel().getRowCount());
        assertFalse(model.getBeforeAfterTableModel().getColumnName(0).startsWith(ourTestColumnName));
        assertFalse(model.getBeforeAfterTableModel().getColumnName(1).startsWith(ourTestColumnName));
    }

    /**
     * Tests when a format errors out for a given sample data.
     */
    @Test
    public void testUpdateNoDataType()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        @SuppressWarnings("unused")
        BeforeAfterController controller = new BeforeAfterController(null, model);

        ColumnDefinitionRow selectedRow = new ColumnDefinitionRow();
        selectedRow.setColumnId(1);
        selectedRow.setDataType(null);
        selectedRow.setColumnName(ourTestColumnName);

        model.setSampleData(DateDataGenerator.generateSingleDateyyyyMMdd());
        model.setSelectedDefinition(selectedRow);

        selectedRow.setFormat("MM-dd-yyyy");

        int rowIndex = 0;

        for (List<? extends String> row : model.getSampleData())
        {
            String original = row.get(1);

            assertEquals(original, model.getBeforeAfterTableModel().getValueAt(rowIndex, 0).toString());
            assertEquals("N/A", model.getBeforeAfterTableModel().getValueAt(rowIndex, 1));
            rowIndex++;
        }

        assertTrue(model.getBeforeAfterTableModel().getColumnName(0).startsWith(ourTestColumnName));
        assertTrue(model.getBeforeAfterTableModel().getColumnName(1).startsWith(ourTestColumnName));
    }

    /**
     * Tests when a format errors out for a given sample data.
     */
    @Test
    public void testUpdateNoFormat()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        @SuppressWarnings("unused")
        BeforeAfterController controller = new BeforeAfterController(null, model);

        ColumnDefinitionRow selectedRow = new ColumnDefinitionRow();
        selectedRow.setColumnId(1);
        selectedRow.setFormat(CoordFormat.DECIMAL.toString());

        model.setSampleData(CsvTestUtils.createBasicData());
        model.setSelectedDefinition(selectedRow);

        selectedRow.setDataType("Latitude");
        selectedRow.setFormat("Decimal");

        int rowIndex = 0;

        for (List<? extends String> row : model.getSampleData())
        {
            String original = row.get(1);

            String expected = null;
            Double lat = LatLonAltParser.parseLat(original, CoordFormat.DECIMAL);
            if (!lat.isNaN())
            {
                expected = lat.toString();
            }
            else
            {
                expected = Constants.ERROR_LABEL;
            }

            assertEquals(original, model.getBeforeAfterTableModel().getValueAt(rowIndex, 0).toString());
            assertEquals(expected, model.getBeforeAfterTableModel().getValueAt(rowIndex, 1));
            rowIndex++;
        }
    }

    /**
     * Creates the preferences registry.
     *
     * @param support The easy mock support object.
     * @return The preferences registry.
     */
    private PreferencesRegistry createPreferencesRegistry(EasyMockSupport support)
    {
        Preferences preferences = support.createMock(Preferences.class);
        preferences.getInt(EasyMock.cmpEq(ListToolPreferences.LIST_TOOL_TIME_PRECISION_DIGITS), EasyMock.eq(0));
        EasyMock.expectLastCall().andReturn(2);
        EasyMock.expectLastCall().atLeastOnce();

        PreferencesRegistry registry = support.createMock(PreferencesRegistry.class);
        registry.getPreferences(EasyMock.eq(ListToolPreferences.class));
        EasyMock.expectLastCall().andReturn(preferences);
        EasyMock.expectLastCall().atLeastOnce();

        Preferences formatPrefs = support.createNiceMock(Preferences.class);
        formatPrefs.getBoolean(EasyMock.isA(String.class), EasyMock.eq(false));
        EasyMock.expectLastCall().andReturn(Boolean.TRUE);
        EasyMock.expectLastCall().atLeastOnce();

        registry.getPreferences(EasyMock.eq(MantleConstants.USER_DATE_FORMAT_CONFIG_FILE_TOPIC));
        EasyMock.expectLastCall().andReturn(formatPrefs);
        EasyMock.expectLastCall().atLeastOnce();

        return registry;
    }
}
