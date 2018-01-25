package io.opensphere.csvcommon.ui.columndefinition.validator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;

/**
 * Tests the import validator class.
 *
 */
public class ImportValidatorTest
{
    /**
     * Tests the import validator class.
     */
    @Test
    public void testValidate()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setIsImport(true);

        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setIsImport(true);

        model.getDefinitionTableModel().addRows(New.list(row1, row2));

        ImportValidator validator = new ImportValidator(model);

        assertNull(validator.validate());

        row2.setIsImport(false);

        assertNull(validator.validate());

        row1.setIsImport(false);

        assertNotNull(validator.validate());
    }
}
