package io.opensphere.core.util.swing.binding;

import java.awt.EventQueue;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatter;

/**
 * JSpinner that provides some basic JavaFX-like binding.
 */
public class Spinner extends JSpinner implements AutoCloseable
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The value property. */
    private final transient IntegerProperty myValue = new SimpleIntegerProperty(this, "value");

    /** The event source. */
    private EventSource myEventSource;

    /** Optional external property. */
    private final transient IntegerProperty myExternalProperty;

    /**
     * Constructor.
     *
     * @param externalProperty the property to bind to
     * @param toolTipText the tool tip text
     * @param minimum the first number in the sequence
     * @param maximum the last number in the sequence
     * @param stepSize the difference between elements of the sequence
     */
    public Spinner(IntegerProperty externalProperty, String toolTipText, int minimum, int maximum, int stepSize)
    {
        super(new SpinnerNumberModel(externalProperty.get(), minimum, maximum, stepSize));
        setToolTipText(toolTipText);

        // Make typing in the text field actually work
        ((DefaultFormatter)((JSpinner.DefaultEditor)getEditor()).getTextField().getFormatter()).setCommitsOnValidEdit(true);

        myExternalProperty = externalProperty;
        myValue.bindBidirectional(myExternalProperty);

        // Bind the UI to our internal property
        myValue.addListener(this::propertyChanged);
        addChangeListener(this::uiChanged);
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
    public IntegerProperty selectedProperty()
    {
        return myValue;
    }

    /**
     * Handles a UI change.
     *
     * @param e the event
     */
    private void uiChanged(ChangeEvent e)
    {
        if (myEventSource != EventSource.PROPERTY)
        {
            myEventSource = EventSource.UI;
            myValue.set(((Integer)getValue()).intValue());
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
    private void propertyChanged(ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
    {
        if (myEventSource != EventSource.UI)
        {
            assert EventQueue.isDispatchThread();

            myEventSource = EventSource.PROPERTY;
            setValue(newValue);
            myEventSource = null;
        }
    }
}
