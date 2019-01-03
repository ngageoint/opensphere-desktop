package io.opensphere.mantle.icon.chooser.model;

import java.util.UUID;

import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

/**
 * The model defining a single icon set. Note that the icons themselves are not
 * associated with the set, this model only defines the information pertaining
 * to the set itself.
 */
public class IconSet
{
    /** The property in which Editable values are stored. */
    private final BooleanProperty myEditableProperty = new ConcurrentBooleanProperty(false);

    /** The unique identifier of the icon set. */
    private final String myId = UUID.randomUUID().toString();

    /** The property in which Name values are stored. */
    private final StringProperty myNameProperty = new ConcurrentStringProperty();

    /**
     * Gets the property defined in the {@link #myEditableProperty} field.
     *
     * @return the property defined in the {@link #myEditableProperty} field.
     */
    public BooleanProperty editableProperty()
    {
        return myEditableProperty;
    }

    /**
     * Gets the value of the {@link #myId} field.
     *
     * @return the value stored in the {@link #myId} field.
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the value stored in the Name property. Logically equivalent to
     * calling <code>NameProperty().get();</code>.
     *
     * @return the value stored in the {@link #myNameProperty}.
     */
    public String getName()
    {
        return myNameProperty.get();
    }

    /**
     * Gets the value stored in the Editable property. Logically equivalent to
     * calling <code>EditableProperty().get();</code>.
     *
     * @return the value stored in the {@link #myEditableProperty}.
     */
    public Boolean isEditable()
    {
        return myEditableProperty.get();
    }

    /**
     * Gets the property defined in the {@link #myNameProperty} field.
     *
     * @return the property defined in the {@link #myNameProperty} field.
     */
    public StringProperty nameProperty()
    {
        return myNameProperty;
    }

    /**
     * Stores the supplied value in the {@link #myEditableProperty}. Logically
     * equivalent to calling <code>EditableProperty().set(Editable);</code>.
     *
     * @param Editable the value to store in the {@link #myEditableProperty}.
     */
    public void setEditable(Boolean Editable)
    {
        myEditableProperty.set(Editable);
    }

    /**
     * Stores the supplied value in the {@link #myNameProperty}. Logically
     * equivalent to calling <code>NameProperty().set(Name);</code>.
     *
     * @param Name the value to store in the {@link #myNameProperty}.
     */
    public void setName(String Name)
    {
        myNameProperty.set(Name);
    }

}
