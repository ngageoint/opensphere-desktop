package io.opensphere.controlpanels.layers.availabledata;

import java.awt.event.ActionListener;

import io.opensphere.controlpanels.layers.base.DefaultTreeButtonBuilders;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.tree.CustomTreeTableModelButtonBuilder;

/**
 * The Class AvailableDataTreeButtonBuilders.
 */
class AvailableDataTreeButtonBuilders extends DefaultTreeButtonBuilders
{
    /**
     * Instantiates a new available custom tree table model.
     *
     * @param tb the toolbox
     * @param listener the listener
     */
    public AvailableDataTreeButtonBuilders(Toolbox tb, ActionListener listener)
    {
        super(tb, listener);

        CustomTreeTableModelButtonBuilder builder = new CustomTreeTableModelButtonBuilder("Refresh", REFRESH_BUTTON, listener,
                "reload.png", "reload_over.png", "reload_press.png", null);
        RefreshTreeCellRenderer refreshRenderer = new RefreshTreeCellRenderer();
        builder.setUpdater(refreshRenderer);
        addBuilder(builder);

        builder = new CustomTreeTableModelButtonBuilder("Remove", REMOVE_BUTTON, listener, "trash2.gif", "trash1.gif",
                "trash3.gif", null);
        RemoveButtonTreeTableTableCellRenderer removeRenderer = new RemoveButtonTreeTableTableCellRenderer();
        builder.setUpdater(removeRenderer);
        addBuilder(builder);
    }
}
