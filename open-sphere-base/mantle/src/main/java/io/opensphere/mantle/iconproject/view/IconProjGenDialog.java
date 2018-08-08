package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;

import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.iconproject.panels.GenIconPane;

/** Creates the Icon Generation Window. */
@SuppressWarnings("serial")
public class IconProjGenDialog extends JFXDialog
{
    /** The current IconRegistry. */
    private final IconRegistry myIconRegistry;

    /**
     * Constructor.
     *
     * Packages icon generation into a swing pane for MIST.
     *
     * @param owner the calling window.
     * @param iconRegistry the current icon registry.
     */
    public IconProjGenDialog(Window owner, IconRegistry iconRegistry)
    {
        super(owner, "Generate an Icon");
        myIconRegistry = iconRegistry;
        System.out.println("projgendialog: " + myIconRegistry.getAllAssignedElementIds().toString());

        GenIconPane pane = new GenIconPane();
        setFxNode(pane);
        setMinimumSize(new Dimension(450, 550));
        setLocationRelativeTo(owner);
        // setAcceptEar(() -> saveImage(pane.getFinalImage(),
        // pane.getImageName()));
    }
}
