package io.opensphere.core.iconlegend.plugin;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractHUDFrameMenuItemPlugin;
import io.opensphere.core.hud.awt.AbstractInternalFrame;
import io.opensphere.core.iconlegend.ui.IconLegend;

/**
 * The Class IconLegendPlugin.
 */
public class IconLegendPlugin extends AbstractHUDFrameMenuItemPlugin
{
    /** The Icon legend. */
    private IconLegend myIconLegend;

    /**
     * Instantiates a new icon legend plugin.
     */
    public IconLegendPlugin()
    {
        super(IconLegend.TITLE, false, false);
    }

    @Override
    public void initialize(PluginLoaderData data, final Toolbox toolbox)
    {
        super.initialize(data, toolbox);
        myIconLegend = new IconLegend(toolbox);
    }

    @Override
    protected AbstractInternalFrame createInternalFrame(Toolbox toolbox)
    {
        return myIconLegend.getMainFrame();
    }
}
