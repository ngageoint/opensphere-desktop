package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.util.fx.JFXDialog;

public class IconProjBuilderDialog extends JFXDialog
{
    //private static Window owner = new IconProjFrame().getMyToolbox().getUIRegistry().getMainFrameProvider().get();
    public IconProjBuilderDialog(Window owner) //(Window owner, IconRegistry iconRegistry, IconChooserPanel chooserPanel  later
    {
        super(owner, "Build an Icon");
        setSize(550, 700);
        setFxNode(new IconProjPanel());
        setMinimumSize(new Dimension(450, 600));

        //myIconRegistry = MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry();
        //myChooserPanel = new IconChooserPanel(tb, true, true, iconPopupMenu, treePopupMenu, buildIcon);

    }


}
