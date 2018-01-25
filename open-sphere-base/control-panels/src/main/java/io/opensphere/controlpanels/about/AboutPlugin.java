package io.opensphere.controlpanels.about;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractHUDFrameMenuItemPlugin;

/**
 * The plug-in for the about user interface.
 */
public class AboutPlugin extends AbstractHUDFrameMenuItemPlugin
{
    /**
     * Instantiates a new layer manager plugin.
     */
    public AboutPlugin()
    {
        super(About.TITLE, false, false);
    }

    @Override
    protected About createInternalFrame(Toolbox toolbox)
    {
        About options = new About(toolbox);
        options.setVisible(false);
        options.setLocation(200, 100);
        return options;
    }
}
