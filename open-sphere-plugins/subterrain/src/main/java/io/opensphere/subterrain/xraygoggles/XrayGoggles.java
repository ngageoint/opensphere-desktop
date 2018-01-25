package io.opensphere.subterrain.xraygoggles;

import io.opensphere.core.Toolbox;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;
import io.opensphere.subterrain.xraygoggles.ui.XrayGogglesMenuProvider;
import io.opensphere.subterrain.xraygoggles.ui.XrayWindow;

/**
 * Gives the user the ability to see underneath the globe when turned on.
 */
public class XrayGoggles
{
    /**
     * Exposes the xray goggles menu item to the user.
     */
    private final XrayGogglesMenuProvider myMenuProvider;

    /**
     * The model used by all the xray components.
     */
    private final XrayGogglesModel myModel = new XrayGogglesModel();

    /**
     * The xray viewport window that is shown on the screen.
     */
    private final XrayWindow myTransformer;

    /**
     * Constructs a new xray goggles.
     *
     * @param toolbox The system toolbox.
     */
    public XrayGoggles(Toolbox toolbox)
    {
        myTransformer = new XrayWindow(toolbox.getDataRegistry(), myModel);
        myMenuProvider = new XrayGogglesMenuProvider(toolbox, myModel);
    }

    /**
     * Removes the xray goggles from the system.
     */
    public void close()
    {
        myMenuProvider.close();
    }

    /**
     * Gets the transformer used by the xray goggles.
     *
     * @return The xray window transformer that shows the xray viewport when on.
     */
    public XrayWindow getTransformer()
    {
        return myTransformer;
    }
}
