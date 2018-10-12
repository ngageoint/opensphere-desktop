package io.opensphere.core.util.swing.binding;

import java.awt.EventQueue;
import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

/**
 * JCheckBox that provides some basic JavaFX-like binding.
 */
public class CheckBox extends JCheckBox implements AutoCloseable
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The selected property. */
    private final transient BooleanProperty mySelected = new SimpleBooleanProperty(this, "selected");

    /** The event source. */
    private EventSource myEventSource;

    /** Optional external property. */
    private transient BooleanProperty myExternalProperty;

    /**
     * Constructor.
     *
     * @param text the text
     */
    public CheckBox(String text)
    {
        super(text, false);

        mySelected.set(false);

        // Bind the UI to our internal property
        mySelected.addListener(this::propertyChanged);
        addItemListener(this::uiChanged);
    }

    /**
     * Constructor.
     *
     * @param externalProperty the property to bind to
     * @param toolTipText the tool tip text
     */
    public CheckBox(BooleanProperty externalProperty, String toolTipText)
    {
        super(getName(externalProperty), externalProperty.get());
        setToolTipText(toolTipText);

        myExternalProperty = externalProperty;
        mySelected.bindBidirectional(myExternalProperty);

        //        if (externalProperty instanceof ViewProperty)
        //        {
        //            ViewProperty viewProperty = (ViewProperty)externalProperty;
        //
        //            setToolTipText(viewProperty.getViewSupport().getDescription());
        //            viewProperty.getViewSupport().descriptionProperty().addListener(new ChangeListener<String>()
        //            {
        //                @Override
        //                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
        //                {
        //                    setToolTipText(newValue);
        //                }
        //            });
        //        }

        // Bind the UI to our internal property
        mySelected.addListener(this::propertyChanged);
        addItemListener(this::uiChanged);
    }

    @Override
    public void close()
    {
        if (myExternalProperty != null)
        {
            mySelected.unbindBidirectional(myExternalProperty);
        }
    }

    /**
     * Gets the selected property.
     *
     * @return the selected property
     */
    public BooleanProperty selectedProperty()
    {
        return mySelected;
    }

    /**
     * Handles a UI change.
     *
     * @param e the event
     */
    private void uiChanged(ItemEvent e)
    {
        if (myEventSource != EventSource.PROPERTY)
        {
            myEventSource = EventSource.UI;
            mySelected.set(isSelected());
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
    private void propertyChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
    {
        if (myEventSource != EventSource.UI)
        {
            assert EventQueue.isDispatchThread();

            myEventSource = EventSource.PROPERTY;
            setSelected(newValue.booleanValue());
            myEventSource = null;
        }
    }

    /**
     * Gets the name from the property.
     *
     * @param property the property
     * @return the name
     */
    private static String getName(BooleanProperty property)
    {
        //      if (property instanceof ViewProperty)
        //      {
        //          String displayName = ((ViewProperty)property).getViewSupport().getDisplayName();
        //          if (StringUtils.isNotEmpty(displayName))
        //          {
        //              name = displayName;
        //          }
        //      }
        return property.getName();
    }
}
