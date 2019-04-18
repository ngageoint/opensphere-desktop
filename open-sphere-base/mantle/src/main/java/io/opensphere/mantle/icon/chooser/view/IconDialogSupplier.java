package io.opensphere.mantle.icon.chooser.view;

import java.util.function.Supplier;

import javafx.scene.Node;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.icon.chooser.controller.IconCustomizationController;
import io.opensphere.mantle.icon.chooser.model.IconModel;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Constructs all the fx view stuff for the IconDialog on the FX thread.
 */
public class IconDialogSupplier implements Supplier<Node>
{
    /** The controller used for managing customization operations. */
    @SuppressWarnings("unused")
    private IconCustomizationController myCustomizationController;

    /** The model to be shared between all the UI elements. */
    private IconModel myPanelModel;

    /**
     * The system toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param toolbox The system toolbox.
     */
    public IconDialogSupplier(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public Node get()
    {
        myPanelModel = new IconModel(myToolbox);
        myPanelModel.setIconRegistry(MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry());

        IconView iconView = new IconView(myPanelModel);
        myCustomizationController = new IconCustomizationController(myPanelModel, iconView);

        if (myPanelModel.getIconRegistry().getManagerPrefs().getIconWidth() != 0)
        {
            myPanelModel.tileWidthProperty().set(myPanelModel.getIconRegistry().getManagerPrefs().getIconWidth());
        }

        return iconView;
    }

    /**
     * Gets the icon panel model.
     *
     * @return The model.
     */
    public IconModel getModel()
    {
        return myPanelModel;
    }
}
