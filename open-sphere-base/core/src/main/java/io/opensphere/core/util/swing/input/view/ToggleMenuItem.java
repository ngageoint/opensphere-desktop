package io.opensphere.core.util.swing.input.view;

import javax.swing.JMenuItem;

import io.opensphere.core.util.Service;
import io.opensphere.core.util.swing.input.model.BooleanModel;
import io.opensphere.core.util.swing.input.model.PropertyChangeEvent.Property;
import io.opensphere.core.util.swing.input.model.PropertyChangeListener;

/**
 * A menu item that toggles its text based on the value of a.
 *
 * {@link BooleanModel}.
 */
public class ToggleMenuItem extends JMenuItem implements Service
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private final BooleanModel myBooleanModel;

    /** Listener for changes to the enabled state of the model. */
    private final transient PropertyChangeListener myPropertyChangeListener = e ->
    {
        if (e.getProperty() == Property.ENABLED)
        {
            setEnabled(myBooleanModel.isEnabled());
        }
    };

    /**
     * Constructor.
     *
     * @param booleanModel The model.
     * @param trueText The text to use when the model is {@code true}.
     * @param falseText The text to use when the model is {@code false}.
     */
    public ToggleMenuItem(BooleanModel booleanModel, String trueText, String falseText)
    {
        super(booleanModel.get().booleanValue() ? trueText : falseText);
        myBooleanModel = booleanModel;
        addActionListener(e ->
        {
            booleanModel.toggleValue();
            setText(booleanModel.get().booleanValue() ? trueText : falseText);
        });
        setEnabled(booleanModel.isEnabled());
    }

    /**
     * Constructor that uses the name of the model for the button text.
     *
     * @param booleanModel The boolean model.
     */
    public ToggleMenuItem(BooleanModel booleanModel)
    {
        this(booleanModel, booleanModel.getName(), booleanModel.getName());
    }

    @Override
    public void close()
    {
        myBooleanModel.removePropertyChangeListener(myPropertyChangeListener);
    }

    @Override
    public void open()
    {
        myBooleanModel.addPropertyChangeListener(myPropertyChangeListener);
    }
}
