package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.fx.JFXDialog;

/** Main UI Frame. */
@SuppressWarnings("serial")
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
        setSize(900, 600);

        setFxNode(new IconProjNewView(tb, owner));
        setMinimumSize(new Dimension(800, 600));
    }
}
