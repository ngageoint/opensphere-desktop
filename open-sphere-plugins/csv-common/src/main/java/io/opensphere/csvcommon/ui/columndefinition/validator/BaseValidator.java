package io.opensphere.csvcommon.ui.columndefinition.validator;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.collections.New;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.importer.config.ColumnType;

/**
 * Contains base functionality shared by the validators.
 *
 */
public abstract class BaseValidator
{
    /**
     * The column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * Constructs the base validator.
     *
     * @param model The model to validate.
     */
    public BaseValidator(ColumnDefinitionModel model)
    {
        myModel = model;
    }

    /**
     * Maps all special columns to their column types.
     *
     * @return The map of column types to columns.
     */
    protected Map<ColumnType, ColumnDefinitionRow> mapTypesToColumns()
    {
        Map<ColumnType, ColumnDefinitionRow> columnTypes = New.map();

        for (int i = 0; i < myModel.getDefinitionTableModel().getRowCount(); i++)
        {
            ColumnDefinitionRow row = myModel.getDefinitionTableModel().getRow(i);
            String dataType = row.getDataType();

            if (row.isImport() && StringUtils.isNotEmpty(dataType))
            {
                columnTypes.put(ColumnType.fromString(dataType), row);
            }
        }

        return columnTypes;
    }

    /**
     * Validates the model.
     *
     * @return A validation message if the model is not valid, or null if the
     *         model is valid.
     */
    public abstract String validate();
}
