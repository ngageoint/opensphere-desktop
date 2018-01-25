package io.opensphere.myplaces.renderer;

import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.Transformer;
import io.opensphere.core.util.collections.New;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.OpenListener;
import io.opensphere.myplaces.specific.RenderGroup;
import io.opensphere.myplaces.specific.Renderer;
import io.opensphere.myplaces.specific.factory.RenderFactory;

/**
 * Responsible for rendering all the my places data but controlling individual
 * renderers.
 */
public class MyPlacesRenderer implements OpenListener
{
    /**
     * Gets the renderers for a specific my places data.
     */
    private final RenderFactory myFactory;

    /**
     * Organizes the data to render, and filters out ones that are not visible.
     */
    private final RenderOrganizer myOrganizer;

    /**
     * Contains all my places data.
     */
    private final MyPlacesModel myModel;

    /**
     * Constructs a new renderer.
     *
     * @param toolbox The toolbox.
     * @param model The model.
     */
    public MyPlacesRenderer(Toolbox toolbox, MyPlacesModel model)
    {
        myFactory = new RenderFactory(toolbox, model);
        for (Renderer renderer : myFactory.getAllRenderers())
        {
            renderer.setOpenListener(this);
        }
        myOrganizer = new RenderOrganizer();
        myModel = model;
    }

    /**
     * Gets the list of transformers.
     *
     * @return The list of transformers.
     */
    public List<Transformer> getTransformers()
    {
        List<Transformer> transformers = New.list();

        List<Renderer> renderers = myFactory.getAllRenderers();
        for (Renderer renderer : renderers)
        {
            transformers.add(renderer.getTransformer());
        }

        return transformers;
    }

    @Override
    public void opened(Renderer renderer)
    {
        List<RenderGroup> renderGroups = myOrganizer.getRenderableFeatures(myModel.getMyPlaces(), myFactory.getAllRenderers());
        for (RenderGroup renderGroup : renderGroups)
        {
            if (renderGroup.getVisType().equals(renderer.getRenderType()))
            {
                renderer.render(renderGroup);
            }
        }
    }

    /**
     * Renders all my places.
     */
    public void renderMyPlaces()
    {
        Kml kml = myModel.getMyPlaces();
        List<RenderGroup> renderGroups = myOrganizer.getRenderableFeatures(kml, myFactory.getAllRenderers());
        for (RenderGroup renderGroup : renderGroups)
        {
            Renderer renderer = myFactory.getRenderer(renderGroup);
            if (renderer != null && renderer.canRender())
            {
                renderer.render(renderGroup);
            }
        }
    }
}
