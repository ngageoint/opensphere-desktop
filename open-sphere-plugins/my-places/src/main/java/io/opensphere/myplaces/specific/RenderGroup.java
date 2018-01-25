package io.opensphere.myplaces.specific;

import java.util.ArrayList;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * Contains a list of kml placemarks in render order.
 *
 *
 */
public class RenderGroup
{
    /**
     * The features to render.
     */
    private final List<Placemark> myFeaturesToRender = new ArrayList<>();

    /**
     * The features that are hidden.
     */
    private final List<Placemark> myHiddenFeatures = new ArrayList<>();

    /**
     * Map visualization type.
     */
    private final MapVisualizationType myVisType;

    /**
     * Constructs a new render group.
     *
     * @param visType The visualization type of this group.
     */
    public RenderGroup(MapVisualizationType visType)
    {
        myVisType = visType;
    }

    /**
     * Gets the features to render.
     *
     * @return The list of features to render.
     */
    public List<Placemark> getFeaturesToRender()
    {
        return myFeaturesToRender;
    }

    /**
     * Gets the feature to hide.
     *
     * @return The list of features to hide.
     */
    public List<Placemark> getHiddenFeatures()
    {
        return myHiddenFeatures;
    }

    /**
     * Gets the visualization type of the group.
     *
     * @return The visualization type.
     */
    public MapVisualizationType getVisType()
    {
        return myVisType;
    }
}
