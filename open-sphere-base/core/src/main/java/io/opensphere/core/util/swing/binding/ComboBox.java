package io.opensphere.core.util.swing.binding;

import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JComboBox;

import io.opensphere.core.util.swing.ListComboBoxModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

/**
 * JComboBox that provides some basic JavaFX-like binding.
 *
 * @param <E> the type of the elements of this combo box
 */
public class ComboBox<E> extends JComboBox<E> implements AutoCloseable
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The value property. */
    private final transient ObjectProperty<E> myValue = new SimpleObjectProperty<>(this, "value");

    /** The event source. */
    private EventSource myEventSource;

    /** External property. */
    private final transient ObjectProperty<E> myExternalProperty;

    /**
     * Constructor.
     *
     * @param externalProperty the property to bind to
     * @param clazz the class of the elements
     */
    public ComboBox(ObjectProperty<E> externalProperty, Class<E> clazz)
    {
        this(externalProperty, Arrays.asList(clazz.getEnumConstants()));
    }

    /**
     * Constructor.
     *
     * @param externalProperty the property to bind to
     * @param elements the elements
     */
    public ComboBox(ObjectProperty<E> externalProperty, Collection<? extends E> elements)
    {
        super(new ListComboBoxModel<>(elements));
        setSelectedItem(externalProperty.get());

        myExternalProperty = externalProperty;
        myValue.bindBidirectional(myExternalProperty);

        // Bind the UI to our internal property
        myValue.addListener(this::propertyChanged);
        addItemListener(this::uiChanged);
    }

    @Override
    public void close()
    {
        if (myExternalProperty != null)
        {
            myValue.unbindBidirectional(myExternalProperty);
        }
    }

    /**
     * Gets the value property.
     *
     * @return the value property
     */
    public ObjectProperty<E> valueProperty()
    {
        return myValue;
    }

    /**
     * Handles a UI change.
     *
     * @param e the event
     */
    @SuppressWarnings("unchecked")
    private void uiChanged(ItemEvent e)
    {
        if (myEventSource != EventSource.PROPERTY)
        {
            myEventSource = EventSource.UI;
            myValue.set((E)getSelectedItem());
            myEventSource = null;
        }
    }

    /**
     * Handles a property change.
     *
     * @param observable the value that changed
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void propertyChanged(ObservableValue<? extends E> observable, E oldValue, E newValue)
    {
        if (myEventSource != EventSource.UI)
        {
            myEventSource = EventSource.PROPERTY;
            setSelectedItem(newValue);
            myEventSource = null;
        }
    }
}
