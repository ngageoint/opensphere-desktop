package io.opensphere.csvcommon.ui.columndefinition.validator;

import java.util.Map;

import io.opensphere.core.model.LatLonAlt.CoordFormat;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.validator.BaseValidator;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class PositionErrorValidator.
 */
public class PositionErrorValidator extends BaseValidator
{
    /**
     * Instantiates a new position error validator.
     *
     * @param model the model
     */
    public PositionErrorValidator(ColumnDefinitionModel model)
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
            if (latColumn != null && CoordFormat.UNKNOWN.toString().equals(latColumn.getFormat()))
            {
                message = "Latitude column format is unknown, please select a valid format.";
            }

            if (lonColumn != null && CoordFormat.UNKNOWN.toString().equals(lonColumn.getFormat()))
            {
                message = "Longitude column format is unknown, please select a valid format.";
            }
        }

        return message;
    }
}
