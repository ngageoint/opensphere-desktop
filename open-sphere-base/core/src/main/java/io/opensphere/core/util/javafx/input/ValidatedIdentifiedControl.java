package io.opensphere.core.util.javafx.input;

import java.util.function.Supplier;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import jidefx.scene.control.validation.DecorationFreeValidationUtils;
import jidefx.scene.control.validation.ValidationGroup;
import jidefx.scene.control.validation.ValidationMode;
import jidefx.scene.control.validation.Validator;

/**
 * A wrapper around a control, in which a {@link Validator} is associated with the input provider, and also contains a variable
 * name and a title of the input area. This class itself extends {@link Control}, allowing it to be used anywhere an existing
 * control type can be used.
 *
 * @param <CONTROL_TYPE> the the type of control wrapped by the instance.
 */
@SuppressWarnings("PMD.GenericsNaming")
public class ValidatedIdentifiedControl<CONTROL_TYPE extends Control> extends IdentifiedControl<CONTROL_TYPE>
{
    /**
     * The validator associated with the control.
     */
    private Validator myValidator;

    /**
     * The validation group to which the control belongs.
     */
    private ValidationGroup myValidationGroup;

    /**
     * Creates a new empty control, storing the supplied identifier and title.
     *
     * @param pVariableName The name of a variable applied to the input field.
     * @param pDisplayTitle The textual label title of the input field.
     */
    public ValidatedIdentifiedControl(String pVariableName, String pDisplayTitle)
    {
        super(pVariableName, pDisplayTitle);
    }

    /**
     * Creates a new control, wrapping the supplied control, and storing the variable name and title of the control.
     *
     * @param pVariableName The name of a variable applied to the input field.
     * @param pDisplayTitle The textual label title of the input field.
     * @param pControl The control wrapped by the instance.
     */
    public ValidatedIdentifiedControl(String pVariableName, String pDisplayTitle, CONTROL_TYPE pControl)
    {
        super(pVariableName, pDisplayTitle, pControl);
    }

    /**
     * Creates a new control, wrapping the supplied control and accessor function, and storing the variable name and title of the
     * control.
     *
     * @param pVariableName The name of a variable applied to the input field.
     * @param pDisplayTitle The textual label title of the input field.
     * @param pResultAccessorFunction The function used to get results from the control.
     * @param pControl The control wrapped by the instance.
     */
    public ValidatedIdentifiedControl(String pVariableName, String pDisplayTitle, Supplier<String> pResultAccessorFunction,
            CONTROL_TYPE pControl)
    {
        super(pVariableName, pDisplayTitle, pResultAccessorFunction, pControl);
    }

    /**
     * Sets the value of the {@link #myValidator} field.
     *
     * @param pValidator the value to store in the {@link #myValidator} field.
     */
    public void setValidator(Validator pValidator)
    {
        myValidator = pValidator;
        installValidator();
    }

    /**
     * Sets the value of the {@link #myValidator} field.
     *
     * @param pValidator the value to store in the {@link #myValidator} field.
     * @param observableValue the observable value to listen to
     */
    public void setValidator(Validator pValidator, ObservableValue<?> observableValue)
    {
        myValidator = pValidator;
        installValidator(observableValue);
    }

    /**
     * Gets the value of the {@link #myValidator} field.
     *
     * @return the value stored in the {@link #myValidator} field.
     */
    public Validator getValidator()
    {
        return myValidator;
    }

    /**
     * Sets the value of the {@link #myValidationGroup} field.
     *
     * @param pValidationGroup the value to store in the {@link #myValidationGroup} field.
     */
    public void setValidationGroup(ValidationGroup pValidationGroup)
    {
        myValidationGroup = pValidationGroup;
        associateValidationGroup();
    }

    /**
     * Gets the value of the {@link #myValidationGroup} field.
     *
     * @return the value stored in the {@link #myValidationGroup} field.
     */
    public ValidationGroup getValidationGroup()
    {
        return myValidationGroup;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.javafx.input.IdentifiedControl#setControl(javafx.scene.control.Control)
     */
    @Override
    public void setControl(CONTROL_TYPE pControl)
    {
        super.setControl(pControl);
        installValidator();
    }

    /**
     * Associates the control with the internally stored validation group, if it hasn't been associated already.
     */
    protected void associateValidationGroup()
    {
        if (myValidationGroup != null && getControl() != null && !myValidationGroup.containsNode(getControl()))
        {
            myValidationGroup.addValidationNode(getControl());
        }
    }

    /**
     * Installs the validator to the control. No action is taken if either the control or the validator is null. The control is
     * also added to the validation group, if the group has been supplied.
     */
    protected void installValidator()
    {
        if (myValidator != null && getControl() != null)
        {
            DecorationFreeValidationUtils.install(getControl(), myValidator, ValidationMode.ON_FLY);
            associateValidationGroup();
        }
    }

    /**
     * Installs the validator to the control. No action is taken if either the control or the validator is null. The control is
     * also added to the validation group, if the group has been supplied.
     *
     * @param observableValue the observable value to listen to
     */
    protected void installValidator(ObservableValue<?> observableValue)
    {
        if (myValidator != null && getControl() != null && observableValue != null)
        {
            DecorationFreeValidationUtils.install(getControl(), observableValue, myValidator, ValidationMode.ON_FLY);
            associateValidationGroup();
        }
    }
}
