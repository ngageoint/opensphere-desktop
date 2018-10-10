package io.opensphere.core.util.javafx.input;

import java.util.function.Supplier;

import io.opensphere.core.util.Visitor;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * A wrapper around a control, in which a variable name and a title are included with the instance definition. This class itself
 * extends {@link Control}, allowing it to be used anywhere an existing control type can be used.
 *
 * @param <CONTROL_TYPE> the the type of control wrapped by the instance.
 */
@SuppressWarnings("PMD.GenericsNaming")
public class IdentifiedControl<CONTROL_TYPE extends Control> extends TitledControl
{
    /**
     * The name of a variable applied to the input field.
     */
    private final String myVariableName;

    /**
     * The control wrapped by the instance.
     */
    private CONTROL_TYPE myControl;

    /**
     * The function used to get results from the control.
     */
    private Supplier<String> myResultAccessorFunction;

    /** The optional units. */
    private String myUnits;

    /**
     * Creates a new control, wrapping the supplied control, and storing the variable name and title of the control.
     *
     * @param pVariableName The name of a variable applied to the input field.
     * @param pDisplayTitle The textual label title of the input field.
     */
    public IdentifiedControl(String pVariableName, String pDisplayTitle)
    {
        super(pDisplayTitle);
        myVariableName = pVariableName;
    }

    /**
     * Creates a new control, wrapping the supplied control, and storing the variable name and title of the control.
     *
     * @param pVariableName The name of a variable applied to the input field.
     * @param pDisplayTitle The textual label title of the input field.
     * @param pControl The control wrapped by the instance.
     */
    public IdentifiedControl(String pVariableName, String pDisplayTitle, CONTROL_TYPE pControl)
    {
        super(pDisplayTitle);
        myVariableName = pVariableName;
        myControl = pControl;
    }

    /**
     * Creates a new control, wrapping the supplied control, and storing the variable name and title of the control.
     *
     * @param pVariableName The name of a variable applied to the input field.
     * @param pDisplayTitle The textual label title of the input field.
     * @param pResultAccessorFunction The function used to get results from the control.
     * @param pControl The control wrapped by the instance.
     */
    public IdentifiedControl(String pVariableName, String pDisplayTitle, Supplier<String> pResultAccessorFunction,
            CONTROL_TYPE pControl)
    {
        super(pDisplayTitle);
        myVariableName = pVariableName;
        myResultAccessorFunction = pResultAccessorFunction;
        myControl = pControl;
    }

    /**
     * Sets the value of the {@link #myResultAccessorFunction} field.
     *
     * @param pResultAccessorFunction the value to store in the {@link #myResultAccessorFunction} field.
     */
    public void setResultAccessorFunction(Supplier<String> pResultAccessorFunction)
    {
        myResultAccessorFunction = pResultAccessorFunction;
    }

    /**
     * Gets the value of the {@link #myResultAccessorFunction} field.
     *
     * @return the value stored in the {@link #myResultAccessorFunction} field.
     */
    public Supplier<String> getResultAccessorFunction()
    {
        return myResultAccessorFunction;
    }

    /**
     * Sets the value of the {@link #myControl} field.
     *
     * @param pControl the value to store in the {@link #myControl} field.
     * @throws UnsupportedOperationException if the control has already been set.
     */
    public void setControl(CONTROL_TYPE pControl)
    {
        if (myControl != null)
        {
            throw new UnsupportedOperationException("Unable to change underlying control once it has been configured.");
        }
        myControl = pControl;
    }

    /**
     * Gets the value of the {@link #myVariableName} field.
     *
     * @return the value stored in the {@link #myVariableName} field.
     */
    public String getVariableName()
    {
        return myVariableName;
    }

    /**
     * Gets the value of the {@link #myControl} field.
     *
     * @return the value stored in the {@link #myControl} field.
     */
    public CONTROL_TYPE getControl()
    {
        return myControl;
    }

    /**
     * Sets the units.
     *
     * @param units the units
     */
    public void setUnits(String units)
    {
        myUnits = units;
    }

    /**
     * Gets the units.
     *
     * @return the units
     */
    public String getUnits()
    {
        return myUnits;
    }

    /**
     * {@inheritDoc}
     *
     * @see javafx.scene.control.Control#createDefaultSkin()
     */
    @Override
    protected Skin<?> createDefaultSkin()
    {
        return new SimpleSkin(this, myControl);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Visitable#visit(io.opensphere.core.util.Visitor)
     */
    @Override
    public void visit(Visitor<?> pVisitor)
    {
        if (getResultAccessorFunction() != null)
        {
            pVisitor.setValue(getVariableName(), getResultAccessorFunction().get());
        }
        else if (myControl instanceof VisitableControl)
        {
            ((VisitableControl)myControl).visit(pVisitor);
        }
    }
}
