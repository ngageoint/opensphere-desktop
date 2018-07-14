package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;

/** Main UI Frame. */
public class IconProjDialog extends JFXDialog
{
    /**
     * Constructor.
     * 
     * packages anchorpane into swing dialog for MIST
     *
     * @param owner the calling window.
     * @param tb the toolbox for registry items.
     */
    public IconProjDialog(Window owner, Toolbox tb)
    {
        super(owner, "Intern Icon Manager");
        setLocationRelativeTo(owner);
        setSize(800, 400);
        setFxNode(new IconProjNewView(tb));
        setMinimumSize(new Dimension(800, 400));
    }

}
