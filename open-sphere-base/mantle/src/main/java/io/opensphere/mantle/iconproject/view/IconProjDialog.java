package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;

public class IconProjDialog extends JFXDialog
{
    //private Toolbox myToolbox;
    public IconProjDialog(Window owner,Toolbox tb)
    {
        //super("Intern Icon Manager");
        //myToolbox = tb;
        //Window owner = myToolbox.getUIRegistry().getMainFrameProvider().get();
        super(owner, "Intern Icon Manager");

        setSize(1021, 520);
        setFxNode(new IconProjPanel(tb));
        setMinimumSize(new Dimension(800, 400));
    }

}

