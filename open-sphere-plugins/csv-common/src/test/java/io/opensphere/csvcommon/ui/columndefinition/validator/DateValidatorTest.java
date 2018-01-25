package io.opensphere.csvcommon.ui.columndefinition.validator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.importer.config.ColumnType;

/**
 * Tests the Date validator class.
 *
 */
public class DateValidatorTest
{
    /**
     * The date time type.
     */
    private static final String ourDateTime = ColumnType.TIMESTAMP.toString();

    /**
     * The test format.
     */
    private static final String ourFormat = "format";

    /**
     * Tests validating when a time is selected but not a date.
     */
    @Test
    public void testValidateTimeNoDate()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row1 = new ColumnDefinitionRow();
        row1.setIsImport(true);
        row1.setFormat(ourFormat);
        ColumnDefinitionRow row2 = new ColumnDefinitionRow();
        row2.setIsImport(true);
        row2.setFormat(ourFormat);
        ColumnDefinitionRow row3 = new ColumnDefinitionRow();
        row3.setIsImport(true);
        row3.setFormat(ourFormat);

        model.getDefinitionTableModel().addRows(New.list(row1, row2, row3));

        DateValidator validator = new DateValidator(model);

        row1.setDataType(null);
        row2.setDataType(ColumnType.TIME.toString());
        row3.setDataType(null);

        assertNotNull(validator.validate());

        row1.setDataType(ColumnType.DATE.toString());
        row2.setDataType(ColumnType.TIME.toString());
        row3.setDataType(null);

        assertNull(validator.validate());

        row1.setDataType(ColumnType.DATE.toString());
        row2.setDataType(ColumnType.TIME.toString());
        row3.setDataType(ColumnType.DOWN_TIME.toString());

        assertNull(validator.validate());

        row1.setDataType(ourDateTime);
        row2.setDataType(null);
        row3.setDataType(ColumnType.DOWN_TIME.toString());

        assertNull(validator.validate());

        row1.setDataType(ourDateTime);
        row2.setDataType(ColumnType.DOWN_DATE.toString());
        row3.setDataType(ColumnType.DOWN_TIME.toString());

        assertNull(validator.validate());
    }

    /**
     * Tests validating when there isn't a format selected.
     */
    @Test
    public void testValidateNoFormat()
    {
        ColumnDefinitionModel model = new ColumnDefinitionModel();

        ColumnDefinitionRow row = new ColumnDefinitionRow();
        row.setIsImport(true);
        ColumnDefinitionRow supportRow = new ColumnDefinitionRow();
        supportRow.setFormat(ourFormat);
        supportRow.setIsImport(true);
        model.getDefinitionTableModel().addRows(New.list(row, supportRow));

        String[] dataTypes = new String[] { ourDateTime, ColumnType.DATE.toString(), ColumnType.TIME.toString(),
            ColumnType.DOWN_TIMESTAMP.toString(), ColumnType.DOWN_DATE.toString(), ColumnType.DOWN_TIME.toString(), };
        String[] supportTypes = new String[] { "", "", ColumnType.DATE.toString(), ourDateTime, ourDateTime,
            ColumnType.DATE.toString(), };

        DateValidator validator = new DateValidator(model);

        int index = 0;
        for (String dataType : dataTypes)
        {
            supportRow.setDataType(supportTypes[index]);

            row.setDataType(dataType);

            assertNotNull(validator.validate());

            row.setFormat(ourFormat);

            assertNull(validator.validate());

            row.setFormat(null);

            index++;
        }
    }
}
