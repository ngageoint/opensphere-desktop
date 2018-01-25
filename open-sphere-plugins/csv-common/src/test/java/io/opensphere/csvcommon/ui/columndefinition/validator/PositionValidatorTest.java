package io.opensphere.csvcommon.ui.columndefinition.validator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.importer.config.ColumnType;

/**
 * Tests the position validator class.
 *
 */
public class PositionValidatorTest
{
    /**
     * Tests all successful inputs.
     */
    @Test
    public void testValidate()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setFormat("format");
        row1.setIsImport(true);
        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setFormat("format");
        row2.setIsImport(true);

        model.getDefinitionTableModel().addRows(New.list(row1, row2));

        row1.setDataType(ColumnType.LAT.toString());
        row2.setDataType(ColumnType.LON.toString());

        PositionValidator validator = new PositionValidator(model);

        assertNull(validator.validate());

        row1.setDataType(ColumnType.POSITION.toString());
        row2.setDataType(null);

        assertNull(validator.validate());

        row1.setDataType(ColumnType.MGRS.toString());

        assertNull(validator.validate());

        row1.setDataType(ColumnType.WKT_GEOMETRY.toString());

        assertNull(validator.validate());
    }

    /**
     * Tests when columns are missing.
     */
    @Test
    public void testValidateMissingColumns()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setIsImport(true);
        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setIsImport(true);

        model.getDefinitionTableModel().addRows(New.list(row1, row2));

        PositionValidator validator = new PositionValidator(model);

        assertNotNull(validator.validate());
    }

    /**
     * Tests when lat lon or position are missing formats.
     */
    @Test
    public void testValidateMissingFormats()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setIsImport(true);
        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setIsImport(true);

        model.getDefinitionTableModel().addRows(New.list(row1, row2));

        row1.setDataType(ColumnType.LAT.toString());
        row2.setDataType(ColumnType.LON.toString());

        PositionValidator validator = new PositionValidator(model);

        assertNull(validator.validate());

        row1.setDataType(ColumnType.POSITION.toString());
        row2.setDataType(null);

        assertNull(validator.validate());

        row1.setDataType(ColumnType.MGRS.toString());

        assertNull(validator.validate());

        row1.setDataType(ColumnType.WKT_GEOMETRY.toString());

        assertNull(validator.validate());
    }

    /**
     * Tests when a lat or lon column is missing.
     */
    @Test
    public void testValidateMissingLatOrLon()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setFormat("format");
        row1.setIsImport(true);
        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setFormat("format");
        row2.setIsImport(true);

        model.getDefinitionTableModel().addRows(New.list(row1, row2));

        row1.setDataType(ColumnType.LAT.toString());

        PositionValidator validator = new PositionValidator(model);

        assertTrue(validator.validate().contains(ColumnType.LON.toString()));

        row1.setDataType(null);
        row2.setDataType(ColumnType.LON.toString());

        assertTrue(validator.validate().contains(ColumnType.LAT.toString()));
    }
}
