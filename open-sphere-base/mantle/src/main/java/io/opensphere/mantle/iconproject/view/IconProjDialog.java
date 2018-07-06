package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;

public class IconProjDialog extends JFXDialog
{

    public IconProjDialog(Window owner, Toolbox tb)
    {
        super(owner, "Intern Icon Manager");
        setSize(800, 400);
        setFxNode(new IconProjNewView(tb));
        setMinimumSize(new Dimension(800, 400));
    }

}
