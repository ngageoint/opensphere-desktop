package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.util.fx.JFXDialog;

public class IconProjDialog extends JFXDialog
{

    public IconProjDialog(Window owner)
    {
        super(owner, "Intern Icon Manager");
        setSize(1021, 520);
        setFxNode(new IconProjNewView());
        setMinimumSize(new Dimension(800, 400));
    }
    
}
