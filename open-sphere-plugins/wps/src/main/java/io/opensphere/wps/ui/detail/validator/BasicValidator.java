package io.opensphere.wps.ui.detail.validator;

import jidefx.scene.control.validation.ValidationEvent;
import jidefx.scene.control.validation.ValidationObject;
import jidefx.scene.control.validation.Validator;

/**
 * A validator that accepts a single {@link ValueExaminer} to look at a given value.
 */
public class BasicValidator implements Validator
{
    /**
     * The examiner used to determine if the value is valid.
     */
    private final ValueExaminer myValueExaminer;

    /**
     * Creates a new validator, using the supplied value examiner to determine if the field's value is valid.
     *
     * @param pValueExaminer the examiner used to determine if the value is valid.
     */
    public BasicValidator(ValueExaminer pValueExaminer)
    {
        myValueExaminer = pValueExaminer;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.util.Callback#call(java.lang.Object)
     */
    @Override
    public ValidationEvent call(ValidationObject pParam)
    {
        ValidationEvent returnValue;
        if (pParam.getNewValue() == null || !myValueExaminer.isValid(pParam.getNewValue().toString()))
        {
            returnValue = new ValidationEvent(ValidationEvent.VALIDATION_ERROR, 0, ValidationEvent.FailBehavior.PERSIST,
                    "Field is required.");
        }
        else
        {
            returnValue = ValidationEvent.OK;
        }
        return returnValue;
    }
}
