package io.opensphere.core.util.swing;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/**
 * ButtonGroup that allows nothing to be selected.
 */
public class AllowNoSelectionButtonGroup extends ButtonGroup
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @Override
    public void setSelected(ButtonModel m, boolean b)
    {
        if (b)
        {
            super.setSelected(m, b);
        }
        else
        {
            clearSelection();
        }
    }
}
