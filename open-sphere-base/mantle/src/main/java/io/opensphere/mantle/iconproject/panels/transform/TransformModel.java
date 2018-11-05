package io.opensphere.mantle.iconproject.panels.transform;

import io.opensphere.core.util.javafx.ConcurrentDoubleProperty;
import javafx.beans.property.DoubleProperty;

/**
 * The model in which the state of the transform operations applied to an icon
 * are maintained.
 */
public class TransformModel
{
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

    /**
     *
     */
    public TransformModel()
    {
        myHorizontalMoveProperty = new ConcurrentDoubleProperty();
        myVerticalMoveProperty = new ConcurrentDoubleProperty();
        myHorizontalScaleProperty = new ConcurrentDoubleProperty();
        myVerticalScaleProperty = new ConcurrentDoubleProperty();
        myRotationProperty = new ConcurrentDoubleProperty();
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
}
