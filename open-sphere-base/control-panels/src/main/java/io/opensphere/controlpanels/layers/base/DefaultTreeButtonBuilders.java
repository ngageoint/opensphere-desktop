package io.opensphere.controlpanels.layers.base;

import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.tree.CustomTreeTableModelButtonBuilder;
import io.opensphere.mantle.data.filter.FilterButtonTreeTableTableCellRenderer;

/**
 * The Class DefaultTreeButtonBuilders.
 */
public class DefaultTreeButtonBuilders implements DataTreeButtonProvisioner
{
    /** The my builders. */
    private final List<CustomTreeTableModelButtonBuilder> myButtonBuilders;

    /**
     * Instantiates a new default custom tree table model.
     *
     * @param tb the toolbox
     * @param listener the listener
     */
    public DefaultTreeButtonBuilders(Toolbox tb, ActionListener listener)
    {
        myButtonBuilders = New.list();

        CustomTreeTableModelButtonBuilder builder = new CustomTreeTableModelButtonBuilder("Manage filters", FILTER_BUTTON,
                listener, "icon-filter-up.png", "icon-filter-over.png", "icon-filter-down.png", null);
        builder.setUpdater(new FilterButtonTreeTableTableCellRenderer(tb));
        myButtonBuilders.add(builder);

        builder = new CustomTreeTableModelButtonBuilder("Details and settings", GEAR_BUTTON, listener, "icon-cog-up.png",
                "icon-cog-over.png", "icon-cog-down.png", null);
        builder.setUpdater(new SettingsButtonTreeTableTableCellRenderer());
        myButtonBuilders.add(builder);

        tb.getUIRegistry().getIconLegendRegistry().addIconToLegend(IconUtil.getNormalIcon(IconType.COG), "Details and settings",
                "Opens the 'Layer Details' dialog which shows relevant information for each layer, any settings "
                        + "that are unique to the layer, and several controls at the bottom of the panel.");
    }

    @Override
    public List<CustomTreeTableModelButtonBuilder> getButtonBuilders()
    {
        return Collections.unmodifiableList(myButtonBuilders);
    }

    /**
     * Adds a button builder.
     *
     * @param builder the button builder
     */
    protected void addBuilder(CustomTreeTableModelButtonBuilder builder)
    {
        myButtonBuilders.add(builder);
    }

    /**
     * Adds a button builder at the given index.
     *
     * @param index the index
     * @param builder the button builder
     */
    protected void addBuilder(int index, CustomTreeTableModelButtonBuilder builder)
    {
        myButtonBuilders.add(index, builder);
    }
}
