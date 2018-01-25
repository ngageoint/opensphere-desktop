package io.opensphere.core.hud.widget.buttons;

import java.util.ArrayList;
import java.util.List;

import io.opensphere.core.util.Utilities;

/**
 * This button group will enforce exactly one activated button at a time.
 */
public class ButtonGroup
{
    /** Buttons which belong to the group. */
    private final List<ToggleButton<?, ?>> myButtons = new ArrayList<>();

    /** Item which is currently selected. */
    private ToggleButton<?, ?> mySelectedItem;

    /**
     * Add a button to the group. If no button is already selected, select it.
     *
     * @param button entry to add.
     */
    public void add(ToggleButton<?, ?> button)
    {
        if (button == null)
        {
            return;
        }

        button.setButtonGroup(this);

        myButtons.add(button);
        if (mySelectedItem == null)
        {
            mySelectedItem = button;
            button.activate();
        }
    }

    /**
     * Handle when a component is clicked.
     *
     * @param clicked the seletedItem to set
     */
    public void componentClicked(ToggleButton<?, ?> clicked)
    {
        if (Utilities.sameInstance(clicked, mySelectedItem))
        {
            return;
        }

        if (mySelectedItem != null)
        {
            mySelectedItem.deactivate();
        }

        mySelectedItem = clicked;
        mySelectedItem.activate();
    }

    /**
     * Get the seletedItem.
     *
     * @return the seletedItem
     */
    public ToggleButton<?, ?> getSeletedItem()
    {
        return mySelectedItem;
    }

    /**
     * Remove a button.
     *
     * @param button button to remove.
     */
    public void removeButton(ToggleButton<?, ?> button)
    {
        myButtons.remove(button);
        if (Utilities.sameInstance(mySelectedItem, button))
        {
            mySelectedItem = null;
            if (!myButtons.isEmpty())
            {
                mySelectedItem = myButtons.get(0);
            }
        }
    }
}
