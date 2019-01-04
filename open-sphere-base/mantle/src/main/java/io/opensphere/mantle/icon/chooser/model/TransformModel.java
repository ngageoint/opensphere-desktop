package io.opensphere.mantle.icon.chooser.model;

import io.opensphere.core.util.javafx.ConcurrentDoubleProperty;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.DoubleProperty;

/**
 * The model in which the state of the transform operations applied to an icon
 * are maintained.
 */
public class TransformModel
{
    /** The default value for horizontal movement. */
    public static final double DEFAULT_HORIZONTAL_MOVE = 0.0;

    /** The default value for vertical movement. */
    public static final double DEFAULT_VERTICAL_MOVE = 0.0;

    /** The default value for horizontal scaling. */
    public static final double DEFAULT_HORIZONTAL_SCALE = 1.0;

    /** The default value for vertical scaling. */
    public static final double DEFAULT_VERTICAL_SCALE = 1.0;

    /** The default value for rotation. */
    public static final double DEFAULT_ROTATION = 0.0;

    /** The state of the horizontal move transform. */
    private final DoubleProperty myHorizontalMoveProperty;

    /** The state of the vertical move transform. */
    private final DoubleProperty myVerticalMoveProperty;

    /** The state of the horizontal scale transform. */
    private final DoubleProperty myHorizontalScaleProperty;

    /** The state of the vertical scale transform. */
    private final DoubleProperty myVerticalScaleProperty;

    /** The state of the rotation transform. */
    private final DoubleProperty myRotationProperty;

    /** The property reflecting if the model has been changed. */
    private final BooleanExpression myChangedProperty;

    /** Creates a new transform model. */
    public TransformModel()
    {
        myHorizontalMoveProperty = new ConcurrentDoubleProperty(DEFAULT_HORIZONTAL_MOVE);
        myVerticalMoveProperty = new ConcurrentDoubleProperty(DEFAULT_VERTICAL_MOVE);
        myHorizontalScaleProperty = new ConcurrentDoubleProperty(DEFAULT_HORIZONTAL_SCALE);
        myVerticalScaleProperty = new ConcurrentDoubleProperty(DEFAULT_VERTICAL_SCALE);
        myRotationProperty = new ConcurrentDoubleProperty(DEFAULT_ROTATION);

        BooleanBinding moveChanged = Bindings.or(Bindings.notEqual(DEFAULT_HORIZONTAL_MOVE, myHorizontalMoveProperty, 0.0),
                Bindings.notEqual(DEFAULT_VERTICAL_MOVE, myVerticalMoveProperty, 0.0));
        BooleanBinding scaleChanged = Bindings.or(Bindings.notEqual(DEFAULT_HORIZONTAL_SCALE, myHorizontalScaleProperty, 0.0),
                Bindings.notEqual(DEFAULT_VERTICAL_SCALE, myVerticalScaleProperty, 0.0));
        BooleanBinding rotationChanged = Bindings.notEqual(DEFAULT_ROTATION, myRotationProperty, 0.0);

        myChangedProperty = Bindings.or(Bindings.or(moveChanged, scaleChanged), rotationChanged);
    }

    /**
     * Gets the value of the {@link #myChangedProperty} field.
     *
     * @return the value of the myChangedProperty field.
     */
    public BooleanExpression changedProperty()
    {
        return myChangedProperty;
    }

    /**
     * Gets the value of the {@link #myHorizontalMoveProperty} field.
     *
     * @return the value stored in the {@link #myHorizontalMoveProperty} field.
     */
    public DoubleProperty horizontalMoveProperty()
    {
        return myHorizontalMoveProperty;
    }

    /**
     * Gets the value of the {@link #myVerticalMoveProperty} field.
     *
     * @return the value stored in the {@link #myVerticalMoveProperty} field.
     */
    public DoubleProperty verticalMoveProperty()
    {
        return myVerticalMoveProperty;
    }

    /**
     * Gets the value of the {@link #myHorizontalScaleProperty} field.
     *
     * @return the value stored in the {@link #myHorizontalScaleProperty} field.
     */
    public DoubleProperty horizontalScaleProperty()
    {
        return myHorizontalScaleProperty;
    }

    /**
     * Gets the value of the {@link #myVerticalScaleProperty} field.
     *
     * @return the value stored in the {@link #myVerticalScaleProperty} field.
     */
    public DoubleProperty verticalScaleProperty()
    {
        return myVerticalScaleProperty;
    }

    /**
     * Gets the value of the {@link #myRotationProperty} field.
     *
     * @return the value stored in the {@link #myRotationProperty} field.
     */
    public DoubleProperty rotationProperty()
    {
        return myRotationProperty;
    }

    /**
     * Sets the value to all internal properties to the original default value.
     */
    public void resetAllToDefault()
    {
        myHorizontalMoveProperty.set(DEFAULT_HORIZONTAL_MOVE);
        myVerticalMoveProperty.set(DEFAULT_VERTICAL_MOVE);
        myHorizontalScaleProperty.set(DEFAULT_HORIZONTAL_SCALE);
        myVerticalScaleProperty.set(DEFAULT_VERTICAL_SCALE);
        myRotationProperty.set(DEFAULT_ROTATION);
    }
}
