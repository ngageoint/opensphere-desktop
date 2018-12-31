package io.opensphere.core.util.javafx.input.tags;

import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

/**
 *
 */
public class TagFieldModel
{
    /**
     * The property in which the color to use for a new tag's background color
     * is maintained.
     */
    private final ObjectProperty<Color> myTagColorProperty = new ConcurrentObjectProperty<>(TagModel.DEFAULT_COLOR);

    /** The editable state of the tag field. */
    private final BooleanProperty myEditableProperty = new ConcurrentBooleanProperty(true);

    /** The tags defined in the field. */
    private final ObservableList<Tag> myTags = FXCollections.observableArrayList();

    /**
     * Gets the value of the {@link #myEditableProperty} field.
     *
     * @return the value stored in the {@link #myEditableProperty} field.
     */
    public BooleanProperty editableProperty()
    {
        return myEditableProperty;
    }

    /**
     * Gets the value of the {@link #myTags} field.
     *
     * @return the value stored in the {@link #myTags} field.
     */
    public ObservableList<Tag> getTags()
    {
        return myTags;
    }

    /**
     * Gets the value of the {@link #myTagColorProperty} field.
     *
     * @return the value stored in the {@link #myTagColorProperty} field.
     */
    public ObjectProperty<Color> tagColorProperty()
    {
        return myTagColorProperty;
    }
}
