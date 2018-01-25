package io.opensphere.csv.ui.columndefinition.validator;

import java.util.Observable;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.csvcommon.ui.columndefinition.listener.BaseSelectedColumnObserver;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionRow;
import io.opensphere.csvcommon.ui.columndefinition.validator.DateValidator;
import io.opensphere.csvcommon.ui.columndefinition.validator.ImportValidator;
import io.opensphere.csvcommon.ui.columndefinition.validator.PositionErrorValidator;
import io.opensphere.csvcommon.ui.columndefinition.validator.PositionValidator;

/**
 * Validates the column definitions and verifies that there are not duplicate
 * column names.
 *
 */
public class ColumnDefinitionValidator extends BaseSelectedColumnObserver
{
    /**
     * Validates the date inputs.
     */
    private final DateValidator myDateValidator;

    /**
     * Validates the import inputs.
     */
    private final ImportValidator myImportValidator;

    /**
     * Validates the position format for lat/lon inputs.
     */
    private final PositionErrorValidator myPositionErrorValidator;

    /**
     * Validates the position inputs.
     */
    private final PositionValidator myPositionValidator;

    /**
     * Constructs a new column definition validator.
     *
     * @param model The model to validate.
     */
    public ColumnDefinitionValidator(ColumnDefinitionModel model)
    {
        super(model);
        myDateValidator = new DateValidator(model);
        myImportValidator = new ImportValidator(model);
        myPositionValidator = new PositionValidator(model);
        myPositionErrorValidator = new PositionErrorValidator(model);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        super.update(o, arg);
        if (o instanceof ColumnDefinitionRow || ColumnDefinitionModel.SELECTED_DEFINITION_PROPERTY.equals(arg))
        {
            validate();
        }
    }

    /**
     * Validates the model.
     */
    public void validate()
    {
        validateErrors();
        validateWarnings();
    }

    /**
     * Validates the model for error messages.
     */
    private void validateErrors()
    {
        String message = myDateValidator.validate();
        if (StringUtils.isEmpty(message))
        {
            message = myImportValidator.validate();

            if (StringUtils.isEmpty(message))
            {
                message = myPositionErrorValidator.validate();
            }
        }

        getModel().setErrorMessage(message);
    }

    /**
     * Validates the model for warning messages.
     */
    private void validateWarnings()
    {
        String message = myPositionValidator.validate();
        getModel().setWarningMessage(message);
    }
}
