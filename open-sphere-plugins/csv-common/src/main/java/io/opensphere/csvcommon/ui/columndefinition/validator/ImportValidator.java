package io.opensphere.csvcommon.ui.columndefinition.validator;

import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionTableModel;

/**
 * Verifies that there is at least one column selected for import.
 *
 */
public class ImportValidator
{
    /**
     * The column definition model.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * Constructs a new import validator.
     *
     * @param model The model to validate.
     */
    public ImportValidator(ColumnDefinitionModel model)
    {
        myModel = model;
    }

    /**
     * Verifies that there is at least one column being imported.
     *
     * @return A validation message if there was an error in validation or null
     *         if validation passed.
     */
    public String validate()
    {
        String message = null;

        ColumnDefinitionTableModel tableModel = myModel.getDefinitionTableModel();

        boolean hasImport = false;

        for (int i = 0; i < tableModel.getRowCount(); i++)
        {
            ColumnDefinitionRow column = tableModel.getRow(i);
            if (column.isImport())
            {
                hasImport = true;
                break;
            }
        }

        if (!hasImport)
        {
            message = "Must have at least one column selected for import.";
        }

        return message;
    }
}
