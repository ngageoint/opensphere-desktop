package io.opensphere.core.control.keybinding;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractHUDFrameMenuItemPlugin;

/**
 * The Class KeyMapPlugin.
 */
public class KeyMapPlugin extends AbstractHUDFrameMenuItemPlugin
{
    /**
     * Instantiates a new key map plugin.
     */
    public KeyMapPlugin()
    {
        super(KeyMapFrame.TITLE, false, false);
    }

    @Override
    protected NewKeyMapFrame createInternalFrame(Toolbox toolbox)
    {
        NewKeyMapFrame keyMapOptions = new NewKeyMapFrame(toolbox);
        keyMapOptions.setVisible(false);
        keyMapOptions.setLocation(200, 100);
        return keyMapOptions;
    }
}
