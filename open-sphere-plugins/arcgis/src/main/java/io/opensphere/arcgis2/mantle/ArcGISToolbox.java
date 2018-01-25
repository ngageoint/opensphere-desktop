package io.opensphere.arcgis2.mantle;

import io.opensphere.core.PluginToolbox;

/** The ArcGIS toolbox. */
public final class ArcGISToolbox implements PluginToolbox
{
    /** The mantle controller. */
    private final ArcGISMantleController myMantleController;

    /**
     * Constructor.
     *
     * @param mantleController the mantle controller
     */
    public ArcGISToolbox(ArcGISMantleController mantleController)
    {
        myMantleController = mantleController;
    }

    @Override
    public String getDescription()
    {
        return "The ArcGIS toolbox";
    }

    /**
     * Gets the mantle controller.
     *
     * @return the mantle controller
     */
    public ArcGISMantleController getMantleController()
    {
        return myMantleController;
    }
}
