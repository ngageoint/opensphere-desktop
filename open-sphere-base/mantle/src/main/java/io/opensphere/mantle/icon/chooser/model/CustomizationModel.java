package io.opensphere.mantle.icon.chooser.model;

import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import javafx.beans.property.StringProperty;

/** A model in which the customization information is maintained. */
public class CustomizationModel
{
    /** The model in which the transformation information is maintained. */
    private final TransformModel myTransformModel = new TransformModel();

    /** The property in which the user-entered name is maintained. */
    private final StringProperty myNameProperty = new ConcurrentStringProperty();

    /** The property in which the user-entered source is maintained. */
    private final StringProperty mySourceProperty = new ConcurrentStringProperty();

    /** The property in which the user-entered tags are maintained. */
    private final StringProperty myTagsProperty = new ConcurrentStringProperty();

    /**
     * Gets the value of the {@link #myNameProperty} field.
     *
     * @return the value of the myNameProperty field.
     */
    public StringProperty nameProperty()
    {
        return myNameProperty;
    }

    /**
     * Gets the value of the {@link #mySourceProperty} field.
     *
     * @return the value of the mySourceProperty field.
     */
    public StringProperty sourceProperty()
    {
        return mySourceProperty;
    }

    /**
     * Gets the value of the {@link #myTagsProperty} field.
     *
     * @return the value of the myTagsProperty field.
     */
    public StringProperty tagsProperty()
    {
        return myTagsProperty;
    }

    /**
     * Gets the value of the {@link #myTransformModel} field.
     *
     * @return the value of the myTransformModel field.
     */
    public TransformModel getTransformModel()
    {
        return myTransformModel;
    }
}
