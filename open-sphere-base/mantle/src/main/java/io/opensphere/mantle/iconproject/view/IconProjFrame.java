package io.opensphere.mantle.iconproject.view;

import java.awt.Window;

import io.opensphere.core.Toolbox;

/** UI Controls for Icon Manager. */
public class IconProjFrame
{
    /** the Toolbox used locally. */
    final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * Creates the Icon Manager UI.
     *
     * @param tb the imported toolbox from control pannels.
     */
    public IconProjFrame(Toolbox tb)
    {
        myToolbox = tb;
        Window owner = myToolbox.getUIRegistry().getMainFrameProvider().get();
        IconProjDialog theDialog = new IconProjDialog(owner, myToolbox);
        theDialog.setVisible(true);
    }
}
