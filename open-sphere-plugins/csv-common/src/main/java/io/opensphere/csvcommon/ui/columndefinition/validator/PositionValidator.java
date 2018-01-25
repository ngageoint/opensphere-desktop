package io.opensphere.csvcommon.ui.columndefinition.validator;

import java.util.Map;

import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.validator.BaseValidator;
import io.opensphere.importer.config.ColumnType;

/**
 * Validates the position columns and verifies all inputs have been received
 * that are required.
 */
public class PositionValidator extends BaseValidator
{
    /**
     * Constructs a new position validator.
     *
     * @param model The model to validate.
     */
    public PositionValidator(ColumnDefinitionModel model)
    {
        super(model);
    }

    @Override
    public String validate()
    {
        String message = null;

        Map<ColumnType, ColumnDefinitionRow> typesToColumns = mapTypesToColumns();

        if (typesToColumns.containsKey(ColumnType.LAT) || typesToColumns.containsKey(ColumnType.LON))
        {
            ColumnDefinitionRow latColumn = typesToColumns.get(ColumnType.LAT);
            ColumnDefinitionRow lonColumn = typesToColumns.get(ColumnType.LON);

            if (latColumn == null)
            {
                message = ColumnType.LAT + " column is not identified.";
            }
            else if (lonColumn == null)
            {
                message = ColumnType.LON + " column is not identified.";
            }
        }
        else if (!typesToColumns.containsKey(ColumnType.POSITION) && !typesToColumns.containsKey(ColumnType.WKT_GEOMETRY)
                && !typesToColumns.containsKey(ColumnType.MGRS))
        {
            message = "Selecting location columns is recommended.";
        }

        return message;
    }
}
