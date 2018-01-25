package io.opensphere.csv.ui.columndefinition.validator;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.importer.config.ColumnType;

/**
 * Tests the ColumnDefinitionValidator class.
 *
 */
public class ColumnDefinitionValidatorTest
{
    /**
     * Tests validating when no imports are selected.
     */
    @Test
    public void testNoImport()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        ColumnDefinitionRow row3 = new ColumnDefinitionRow();

        model.getDefinitionTableModel().addRows(New.list(row1, row2, row3));

        @SuppressWarnings("unused")
        ColumnDefinitionValidator validator = new ColumnDefinitionValidator(model);

        model.setSelectedDefinition(row1);

        assertTrue(model.getErrorMessage().contains("import"));

        row1.setIsImport(true);

        assertNull(model.getErrorMessage());
    }

    /**
     * Tests validating positions.
     */
    @Test
    public void testPositions()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setIsImport(true);
        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setIsImport(true);
        ColumnDefinitionRow row3 = new ColumnDefinitionRow();
        row3.setIsImport(true);

        model.getDefinitionTableModel().addRows(New.list(row1, row2, row3));

        @SuppressWarnings("unused")
        ColumnDefinitionValidator validator = new ColumnDefinitionValidator(model);

        model.setSelectedDefinition(row1);

        assertTrue(model.getWarningMessage().contains("location"));

        row1.setDataType(ColumnType.POSITION.toString());
        row1.setFormat("format");

        assertNull(model.getWarningMessage());
    }

    /**
     * Tests validating time no dates.
     */
    @Test
    public void testTimeNoDate()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setIsImport(true);
        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setIsImport(true);
        row2.setDataType(ColumnType.TIME.toString());
        row2.setFormat("format");
        ColumnDefinitionRow row3 = new ColumnDefinitionRow();
        row3.setIsImport(true);

        model.getDefinitionTableModel().addRows(New.list(row1, row2, row3));

        @SuppressWarnings("unused")
        ColumnDefinitionValidator validator = new ColumnDefinitionValidator(model);

        model.setSelectedDefinition(row1);

        assertTrue(model.getErrorMessage().contains("Date"));

        row1.setDataType(ColumnType.DATE.toString());
        row1.setFormat("format");

        assertNull(model.getErrorMessage());
    }

    /**
     * Tests validating time formats.
     */
    @Test
    public void testTimeNoFormat()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setIsImport(true);
        row1.setDataType(ColumnType.TIMESTAMP.toString());
        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setIsImport(true);
        ColumnDefinitionRow row3 = new ColumnDefinitionRow();
        row3.setIsImport(true);

        model.getDefinitionTableModel().addRows(New.list(row1, row2, row3));

        @SuppressWarnings("unused")
        ColumnDefinitionValidator validator = new ColumnDefinitionValidator(model);

        model.setSelectedDefinition(row1);

        assertTrue(model.getErrorMessage().contains("format"));

        row1.setFormat("format");

        assertNull(model.getErrorMessage());
    }
}
