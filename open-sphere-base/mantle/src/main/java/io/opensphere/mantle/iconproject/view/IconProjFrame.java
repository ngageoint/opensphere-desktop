package io.opensphere.mantle.iconproject.view;

import java.awt.Window;

import io.opensphere.core.Toolbox;

public class IconProjFrame
{
    final Toolbox myToolbox;

    public IconProjFrame(Toolbox tb)
    {
        myToolbox = tb;
        Window owner = myToolbox.getUIRegistry().getMainFrameProvider().get();
        IconProjDialog theDialog = new IconProjDialog(owner, tb);
        theDialog.setVisible(true);
    }

    public Toolbox getMyToolbox()
    {
        return myToolbox;
    }
}