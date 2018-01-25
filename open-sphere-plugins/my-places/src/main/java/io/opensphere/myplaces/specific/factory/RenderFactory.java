package io.opensphere.myplaces.specific.factory;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.RenderGroup;
import io.opensphere.myplaces.specific.Renderer;
import io.opensphere.myplaces.specific.points.PointRenderer;
import io.opensphere.myplaces.specific.regions.RegionRenderer;
import io.opensphere.myplaces.specific.tracks.TrackRenderer;

/**
 * Gets the renderer for a specific render group.
 *
 */
public class RenderFactory
{
    /**
     * The renderers.
     */
    private final List<Renderer> myRenderers = New.list();

    /**
     * Constructs a new render factory.
     *
     * @param toolbox The toolbox.
     * @param model The model.
     */
    public RenderFactory(Toolbox toolbox, MyPlacesModel model)
    {
        myRenderers.add(new PointRenderer(toolbox, model));
        myRenderers.add(new RegionRenderer(toolbox, model));
        myRenderers.add(new TrackRenderer(toolbox, model,
                TypeControllerFactory.getInstance().getController(MapVisualizationType.USER_TRACK_ELEMENTS)));
    }

    /**
     * Gets all renderers for my places.
     *
     * @return The list of all renderers.
     */
    public List<Renderer> getAllRenderers()
    {
        return myRenderers;
    }

    /**
     * Gets the renderer that can render the elements within the specified
     * group.
     *
     * @param group The group containing the elements to renderer.
     * @return The renderer.
     */
    public Renderer getRenderer(RenderGroup group)
    {
        Renderer renderer = null;
        for (Renderer aRenderer : myRenderers)
        {
            if (group.getVisType().equals(aRenderer.getRenderType()))
            {
                renderer = aRenderer;
                break;
            }
        }

        return renderer;
    }
}
