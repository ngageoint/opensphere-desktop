package io.opensphere.csvcommon.ui.columndefinition.ui;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.DefaultValidatorSupport;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.csvcommon.ui.columndefinition.model.ColumnDefinitionModel;

/**
 * Sends any changes to the model's warning messages, to the UI.
 */
public class ColumnDefinitionBinder implements Observer
{
    /**
     * The validator used to communicate validation messages to the UI.
     */
    private final DefaultValidatorSupport myValidator;

    /**
     * The model containing the validation messages to display.
     */
    private final ColumnDefinitionModel myModel;

    /**
     * Constructs a new column definition binder.
     *
     * @param validator The validator.
     * @param model The model.
     */
    public ColumnDefinitionBinder(DefaultValidatorSupport validator, ColumnDefinitionModel model)
    {
        myModel = model;
        myValidator = validator;
        myModel.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (ColumnDefinitionModel.WARNING_MESSAGE_PROPERTY.equals(arg)
                || ColumnDefinitionModel.ERROR_MESSAGE_PROPERTY.equals(arg))
        {
            updateValidationMessages();
        }
    }

    /**
     * Updates the validator messages based on the model's messages.
     */
    private void updateValidationMessages()
    {
        String errorMessage = myModel.getErrorMessage();
        String warningMessage = myModel.getWarningMessage();

        if (StringUtils.isNotEmpty(errorMessage))
        {
            myValidator.setValidationResult(ValidationStatus.ERROR, errorMessage);
        }
        else if (StringUtils.isNotEmpty(warningMessage))
        {
            myValidator.setValidationResult(ValidationStatus.WARNING, warningMessage);
        }
        else
        {
            myValidator.setValidationResult(ValidationStatus.VALID, null);
        }
    }

    /**
     * Removes itself as a listener to the model.
     */
    public void close()
    {
        myModel.deleteObserver(this);
    }
}
