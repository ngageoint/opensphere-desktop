package io.opensphere.myplaces.renderer;

import java.util.List;
import java.util.Map;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.myplaces.specific.RenderGroup;
import io.opensphere.myplaces.specific.Renderer;
import io.opensphere.myplaces.util.ExtendedDataUtils;

/**
 * Groups data for different renderers.
 *
 */
public class RenderOrganizer
{
    /**
     * Gets the features that can be rendered.
     *
     * @param placesKml The kml representing the my places data.
     * @param renderers The available renderers.
     * @return The list of renderable features.
     */
    public List<RenderGroup> getRenderableFeatures(Kml placesKml, List<Renderer> renderers)
    {
        Map<MapVisualizationType, RenderGroup> renderGroups = New.map();

        populateInitially(renderGroups, renderers);

        Feature feature = placesKml.getFeature();
        if (feature instanceof Document)
        {
            Document document = (Document)feature;
            getRenderableFeatures(document.getFeature(), renderGroups);
        }

        return New.list(renderGroups.values());
    }

    /**
     * Gets the features that can be rendered and groups them my render type.
     *
     * @param features The source of features that my be renderable.
     * @param renderGroups Contains the features to render organized by their
     *            render type.
     */
    private void getRenderableFeatures(List<Feature> features, Map<MapVisualizationType, RenderGroup> renderGroups)
    {
        for (Feature feature : features)
        {
            if (feature instanceof Placemark)
            {
                Placemark placemark = (Placemark)feature;

                ExtendedData extendedData = placemark.getExtendedData();
                MapVisualizationType visType = ExtendedDataUtils.getVisualizationType(extendedData);

                if (!renderGroups.containsKey(visType))
                {
                    RenderGroup group = new RenderGroup(visType);
                    renderGroups.put(visType, group);
                }

                if (placemark.isVisibility() != null && placemark.isVisibility().equals(Boolean.TRUE))
                {
                    renderGroups.get(visType).getFeaturesToRender().add(placemark);
                }
                else
                {
                    renderGroups.get(visType).getHiddenFeatures().add(placemark);
                }
            }
            else if (feature instanceof Folder)
            {
                Folder folder = (Folder)feature;
                getRenderableFeatures(folder.getFeature(), renderGroups);
            }
        }
    }

    /**
     * Populates the initial render groups.
     *
     * @param renderGroups The render groups to initialize.
     * @param renderers The available renderers.
     */
    private void populateInitially(Map<MapVisualizationType, RenderGroup> renderGroups, List<Renderer> renderers)
    {
        for (Renderer renderer : renderers)
        {
            renderGroups.put(renderer.getRenderType(), new RenderGroup(renderer.getRenderType()));
        }
    }
}
