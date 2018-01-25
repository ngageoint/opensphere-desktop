package io.opensphere.core.util.swing.binding;

import java.awt.EventQueue;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * JTextField that provides some basic JavaFX-like binding.
 */
public class TextField extends JTextField implements AutoCloseable
{
    /** The unique identifier used for serialization. */
    private static final long serialVersionUID = -270748317014619271L;

    /** The property used to track the state of the UI internally. */
    private final transient StringProperty myValue = new SimpleStringProperty(this, "value");

    /** The event source. */
    private EventSource myEventSource;

    /** The external property. */
    private transient StringProperty myExternalProperty;

    /**
     * Creates a new text field with a blank value.
     */
    public TextField()
    {
        super();

        myValue.set("");

        myValue.addListener(this::valueChanged);
        getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                /* intentionally blank */
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                /* intentionally blank */
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                uiChanged(e);
            }
        });
    }

    /**
     * Creates a new text field bound to the supplied property.
     *
     * @param externalProperty the external property to which the field will be
     *            bound.
     * @param toolTipText the tool tip text to apply to the field.
     */
    public TextField(StringProperty externalProperty, String toolTipText)
    {
        super(externalProperty.get());
        setToolTipText(toolTipText);

        myExternalProperty = externalProperty;
        myValue.bindBidirectional(myExternalProperty);

        myValue.addListener(this::valueChanged);
        getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void removeUpdate(DocumentEvent e)
            {
                /* intentionally blank */
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                /* intentionally blank */
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                uiChanged(e);
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception
    {
        if (myExternalProperty != null)
        {
            myValue.unbindBidirectional(myExternalProperty);
        }
    }

    /**
     * Gets the selected property.
     *
     * @return the selected property
     */
    public StringProperty valueProperty()
    {
        return myValue;
    }

    /**
     * Handles a UI change.
     *
     * @param e the event
     */
    private void uiChanged(DocumentEvent e)
    {
        if (myEventSource != EventSource.PROPERTY)
        {
            myEventSource = EventSource.UI;
            myValue.set(getText());
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
    private void valueChanged(ObservableValue<? extends String> observable, String oldValue, String newValue)
    {
        if (myEventSource != EventSource.UI)
        {
            assert EventQueue.isDispatchThread();

            myEventSource = EventSource.PROPERTY;
            setText(newValue);
            myEventSource = null;
        }
    }
}
