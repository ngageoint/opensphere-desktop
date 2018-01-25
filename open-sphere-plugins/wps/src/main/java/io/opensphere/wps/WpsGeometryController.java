package io.opensphere.wps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.controller.event.impl.ActiveDataGroupsChangedEvent;
import io.opensphere.mantle.crust.MiniMantle;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.wps.layer.WpsDataTypeInfo;
import io.opensphere.wps.request.WpsProcessConfiguration;
import io.opensphere.wps.util.WPSConstants;
import io.opensphere.wps.util.WpsUtilities;

/** Manages WPS geometries on the map. */
public class WpsGeometryController implements Service
{
    /** The geometry registry. */
    private final GeometryRegistry myGeometryRegistry;

    /** Connects core geometries to mantle layer. */
    private final MiniMantle myMiniMantle;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public WpsGeometryController(Toolbox toolbox)
    {
        myGeometryRegistry = toolbox.getGeometryRegistry();
        myMiniMantle = new MiniMantle(toolbox.getEventManager());
    }

    @Override
    public void open()
    {
        myMiniMantle.open();
    }

    @Override
    public void close()
    {
        myMiniMantle.close();
    }

    /**
     * Handles layer activation/deactivation.
     *
     * @param pEvent the event
     */
    public void handleActiveDataGroupsChangedEvent(ActiveDataGroupsChangedEvent pEvent)
    {
        if (!pEvent.getActivatedGroups().isEmpty())
        {
            for (WpsDataTypeInfo dataType : getWpsTypes(pEvent.getActivatedGroups()))
            {
                add(dataType.getProcessConfiguration());
            }
        }

        if (!pEvent.getDeactivatedGroups().isEmpty())
        {
            for (WpsDataTypeInfo dataType : getWpsTypes(pEvent.getDeactivatedGroups()))
            {
                remove(dataType.getProcessConfiguration());
            }
        }
    }

    /**
     * Creates and adds any geometries for the given process configuration.
     *
     * @param config the process configuration
     */
    public void add(WpsProcessConfiguration config)
    {
        Collection<PolygonGeometry> geometries = createGeometries(config);
        if (!geometries.isEmpty())
        {
            myGeometryRegistry.addGeometriesForSource(this, geometries);
            myMiniMantle.addGeometries(config.getResultType().getTypeKey(), geometries);
        }
    }

    /**
     * Removes any geometries for the given process configuration.
     *
     * @param config the process configuration
     */
    public void remove(WpsProcessConfiguration config)
    {
        Collection<Geometry> geometries = myMiniMantle.removeGeometries(config.getResultType().getTypeKey());
        if (CollectionUtilities.hasContent(geometries))
        {
            myGeometryRegistry.removeGeometriesForSource(this, geometries);
        }
    }

    /**
     * Gets the WPS data types from the given data groups.
     *
     * @param groups the data groups
     * @return the WPS data types
     */
    private static Collection<WpsDataTypeInfo> getWpsTypes(Collection<? extends DataGroupInfo> groups)
    {
        Predicate<? super DataTypeInfo> isWps = t -> t instanceof WpsDataTypeInfo;
        return groups.stream().filter(g -> g.hasMember(isWps, false)).flatMap(g -> g.findMembers(isWps, false, false).stream())
                .map(t -> (WpsDataTypeInfo)t).collect(Collectors.toList());
    }

    /**
     * Creates the geometries for the process configuration.
     *
     * @param config the process configuration
     * @return the geometries
     */
    private static Collection<PolygonGeometry> createGeometries(WpsProcessConfiguration config)
    {
        Collection<PolygonGeometry> geometries = New.list();

        Map<String, String> inputs = config.getInputs();
        String bboxText = inputs.containsKey(WPSConstants.BBOX) ? inputs.get(WPSConstants.BBOX) : inputs.get("FILTER");

        if (bboxText != null)
        {
            String[] bboxes = bboxText.split(" ");
            for (String bbox : bboxes)
            {
                List<LatLonAlt> locations = WpsUtilities.parseLocations(bbox);
                PolygonGeometry polygon = createPolygon(locations, config.getResultType());
                geometries.add(polygon);
            }
        }

        return geometries;
    }

    /**
     * Creates a Core polygon.
     *
     * @param locations the locations
     * @param dataType the data type
     * @return the polygon geometry
     */
    private static PolygonGeometry createPolygon(Collection<? extends LatLonAlt> locations, DataTypeInfo dataType)
    {
        PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<>();
        builder.setVertices(locations.stream().map(GeographicPosition::new).collect(Collectors.toList()));
        DefaultPolygonRenderProperties renderProperties = new DefaultPolygonRenderProperties(
                dataType.getMapVisualizationInfo().getZOrder(), true, true);
        renderProperties.setColor(dataType.getBasicVisualizationInfo().getTypeColor());
        renderProperties.setWidth(3);
        return new PolygonGeometry(builder, renderProperties, null);
    }
}
