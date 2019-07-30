package io.opensphere.core.control.keybinding;

import cern.colt.Arrays;
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
        super(NewKeyMapFrame.TITLE, false, false);
    }

    @Override
    protected NewKeyMapFrame createInternalFrame(Toolbox toolbox)
    {
        NewKeyMapFrame keyMapOptions = new NewKeyMapFrame();
        keyMapOptions.setVisible(false);
        keyMapOptions.setLocation(200, 100);
        return keyMapOptions;
    }
}
