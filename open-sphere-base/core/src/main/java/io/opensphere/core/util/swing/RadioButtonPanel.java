package io.opensphere.core.util.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.event.EventListenerList;

/**
 * Radio button panel.
 *
 * @param <T> The type of the options
 */
public class RadioButtonPanel<T> extends GridBagPanel implements ActionListener
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The button map. */
    private final Map<T, AbstractButton> myButtonMap = new HashMap<>();

    /** A list of event listeners for this component. */
    private final EventListenerList myListenerList = new EventListenerList();

    /**
     * Constructor.
     *
     * @param options The options
     */
    public RadioButtonPanel(Collection<? extends T> options)
    {
        this(options, null);
    }

    /**
     * Constructor.
     *
     * @param options The options
     * @param selection The selection
     */
    public RadioButtonPanel(Collection<? extends T> options, T selection)
    {
        this(options, selection, null);
    }

    /**
     * Constructor.
     *
     * @param options The options
     * @param selection The selection
     * @param buttonProvider The button provider
     */
    public RadioButtonPanel(Collection<? extends T> options, T selection, Function<T, AbstractButton> buttonProvider)
    {
        Function<T, AbstractButton> theButtonProvider = buttonProvider;
        if (theButtonProvider == null)
        {
            theButtonProvider = new Function<T, AbstractButton>()
            {
                @Override
                public AbstractButton apply(T option)
                {
                    return new JRadioButton(option.toString());
                }
            };
        }

        ButtonGroup group = new ButtonGroup();
        boolean isFirst = true;
        for (T option : options)
        {
            AbstractButton button = theButtonProvider.apply(option);
            if (option.equals(selection))
            {
                button.setSelected(true);
            }
            button.addActionListener(this);
            setInsets(0, button instanceof JRadioButton || isFirst ? 0 : 4, 0, 0).add(button);
            group.add(button);
            myButtonMap.put(option, button);
            isFirst = false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        for (ActionListener listener : myListenerList.getListeners(ActionListener.class))
        {
            listener.actionPerformed(e);
        }
    }

    /**
     * Adds an <code>ActionListener</code> to the panel.
     *
     * @param l the <code>ActionListener</code> to be added
     */
    public void addActionListener(ActionListener l)
    {
        myListenerList.add(ActionListener.class, l);
    }

    /**
     * Gets the selection.
     *
     * @return The selection
     */
    public T getSelection()
    {
        T selection = null;
        for (Map.Entry<T, AbstractButton> entry : myButtonMap.entrySet())
        {
            if (entry.getValue().isSelected())
            {
                selection = entry.getKey();
                break;
            }
        }
        return selection;
    }

    /**
     * Removes an <code>ActionListener</code> from the panel.
     *
     * @param l the <code>ActionListener</code> to be removed
     */
    public void removeActionListener(ActionListener l)
    {
        myListenerList.remove(ActionListener.class, l);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        for (Component component : getComponents())
        {
            component.setEnabled(enabled);
        }
    }

    /**
     * Sets the selection.
     *
     * @param selection The selection
     */
    public void setSelection(T selection)
    {
        AbstractButton button = myButtonMap.get(selection);
        if (button != null)
        {
            button.setSelected(true);
        }
    }
}
