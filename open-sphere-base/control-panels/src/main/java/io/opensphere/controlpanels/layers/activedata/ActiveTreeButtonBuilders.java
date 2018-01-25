package io.opensphere.controlpanels.layers.activedata;

import java.awt.event.ActionListener;

import io.opensphere.controlpanels.layers.base.DefaultTreeButtonBuilders;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.tree.CustomTreeTableModelButtonBuilder;

/**
 * The Class ActiveTreeButtonBuilders.
 */
class ActiveTreeButtonBuilders extends DefaultTreeButtonBuilders
{
    /**
     * Instantiates a new active custom tree table model.
     *
     * @param tb the toolbox
     * @param listener the listener
     */
    public ActiveTreeButtonBuilders(Toolbox tb, ActionListener listener)
    {
        super(tb, listener);

        CustomTreeTableModelButtonBuilder builder;

        StreamingSupportUpdater streamingSupportUpdater = new StreamingSupportUpdater();

        builder = new CustomTreeTableModelButtonBuilder(StreamingSupportUpdater.PLAYCLOCK_BUTTON_NAME, PLAYCLOCK_BUTTON, listener,
                "playclock-up.png", "playclock-over.png", "playclock-down.png", null);
        builder.setUpdater(streamingSupportUpdater);
        addBuilder(0, builder);

        builder = new CustomTreeTableModelButtonBuilder(StreamingSupportUpdater.PLAY_BUTTON_NAME, PLAY_BUTTON, listener,
                "icon-play-up.png", "icon-play-over.png", "icon-play-down.png", null);
        builder.setUpdater(streamingSupportUpdater);
        addBuilder(1, builder);

        builder = new CustomTreeTableModelButtonBuilder(StreamingSupportUpdater.PAUSE_BUTTON_NAME, PAUSE_BUTTON, listener,
                "icon-pause-up.png", "icon-pause-over.png", "icon-pause-down.png", null);
        builder.setUpdater(streamingSupportUpdater);
        addBuilder(2, builder);

        builder = new CustomTreeTableModelButtonBuilder(StreamingSupportUpdater.STOP_BUTTON_NAME, STOP_BUTTON, listener,
                "icon-stop-up.png", "icon-stop-over.png", "icon-stop-down.png", null);
        builder.setUpdater(streamingSupportUpdater);
        addBuilder(3, builder);

        builder = new CustomTreeTableModelButtonBuilder("Remove Layer", REMOVE_BUTTON, listener, "icon-remove-up-red.png",
                "icon-remove-over-red.png", "icon-remove-down-red.png", null);
        builder.setUpdater(new DeactivateButtonTreeTableTableCellRenderer());
        addBuilder(builder);

        builder = new CustomTreeTableModelButtonBuilder("Delete", DELETE_BUTTON, listener, "trash2.gif", "trash1.gif",
                "trash3.gif", null);
        builder.setUpdater(new DeleteButtonTreeTableTableCellRenderer());
        addBuilder(builder);

        builder = new CustomTreeTableModelButtonBuilder("Open in New Window", POPOUT_BUTTON, listener, "new-tab.png",
                "new-tab-hover.png", "new-tab-pressed.png", null);
        builder.setUpdater(new PopoutButtonTreeTableTableCellRenderer());
        addBuilder(builder);
    }
}
