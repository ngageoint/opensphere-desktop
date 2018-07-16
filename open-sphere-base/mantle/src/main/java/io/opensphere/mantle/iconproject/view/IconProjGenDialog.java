package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconRegistry;

public class IconProjGenDialog extends JFXDialog
{

    private IconRegistry myIconRegistry;

    public IconProjGenDialog(Window owner, IconRegistry iconRegistry)
    {
        super(owner, "Generate an Icon");
        myIconRegistry = iconRegistry;
        System.out.println(myIconRegistry.getAllAssignedElementIds().toString());

        GenIconPane pane = new GenIconPane();
        setFxNode(pane);
        setMinimumSize(new Dimension(450, 550));
        setLocationRelativeTo(owner);
        // setAcceptEar(() -> saveImage(pane.getFinalImage(),
        // pane.getImageName()));
    }
}
