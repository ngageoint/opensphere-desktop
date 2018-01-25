package io.opensphere.csvcommon.ui.columndefinition.validator;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.validator.BaseValidator;
import io.opensphere.importer.config.ColumnType;

/**
 * Validates the date columns and verifies all inputs have been received that
 * are required.
 *
 */
public class DateValidator extends BaseValidator
{
    /**
     * The date column types.
     */
    private static final ColumnType[] ourDateColumnTypes = new ColumnType[] { ColumnType.TIMESTAMP, ColumnType.DATE,
        ColumnType.TIME, ColumnType.DOWN_TIMESTAMP, ColumnType.DOWN_DATE, ColumnType.DOWN_TIME, };

    /**
     * Constructs a date validator.
     *
     * @param model The model to validate.
     */
    public DateValidator(ColumnDefinitionModel model)
    {
        super(model);
    }

    /**
     * Validates the model and verifies if date information has been selected
     * and makes sure a format has been selected for the date columns.
     *
     * @return A message if
     */
    @Override
    public String validate()
    {
        String message = null;

        Map<ColumnType, ColumnDefinitionRow> columnTypes = mapTypesToColumns();

        if (columnTypes.containsKey(ColumnType.TIME) && !columnTypes.containsKey(ColumnType.DATE))
        {
            message = "Need a " + ColumnType.DATE + " column identified for the identified " + ColumnType.TIME + " column.";
        }
        else if (columnTypes.containsKey(ColumnType.DOWN_TIME) && !columnTypes.containsKey(ColumnType.DATE)
                && !columnTypes.containsKey(ColumnType.DOWN_DATE) && !columnTypes.containsKey(ColumnType.TIMESTAMP))
        {
            message = "Need a " + ColumnType.DOWN_DATE + " column identified for the identified " + ColumnType.DOWN_TIME + ".";
        }
        else
        {
            for (ColumnType columType : ourDateColumnTypes)
            {
                ColumnDefinitionRow row = columnTypes.get(columType);
                if (row != null && StringUtils.isEmpty(row.getFormat()))
                {
                    message = "Need a format for column " + row.getColumnName();
                    break;
                }
            }
        }

        return message;
    }
}
