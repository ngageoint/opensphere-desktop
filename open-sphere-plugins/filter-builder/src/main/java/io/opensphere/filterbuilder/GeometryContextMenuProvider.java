package io.opensphere.filterbuilder;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.datafilter.impl.DataFilterRegistryAdapter;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.filterbuilder.impl.SpatialFilterUtilities;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Geometry ContextMenuProvider for Filter Builder (for spatial filters).
 */
@SuppressWarnings("PMD.GodClass")
public class GeometryContextMenuProvider implements ContextMenuProvider<GeometryContextKey>
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The spatial filter change listener. */
    private final DataFilterRegistryAdapter mySpatialFilterListener;

    /** The Delete context menu provider. */
    private final ContextMenuProvider<Void> myDeleteMenuProvider;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public GeometryContextMenuProvider(Toolbox toolbox)
    {
        myToolbox = toolbox;

        mySpatialFilterListener = new DataFilterRegistryAdapter()
        {
            @Override
            public void spatialFilterAdded(String typeKey, Geometry filter)
            {
                // Nothing currently adds spatial filters externally but this
                // may need to be implemented in the future
            }

            @Override
            public void spatialFilterRemoved(String typeKey, Geometry filter)
            {
                if (filter instanceof Polygon)
                {
                    removeTypeKey(typeKey, (Polygon)filter);
                }
                else if (filter instanceof MultiPolygon)
                {
                    MultiPolygon multiPolygon = (MultiPolygon)filter;
                    for (int n = 0; n < multiPolygon.getNumGeometries(); ++n)
                    {
                        removeTypeKey(typeKey, (Polygon)multiPolygon.getGeometryN(n));
                    }
                }
            }
        };
        myToolbox.getDataFilterRegistry().addListener(mySpatialFilterListener);

        myDeleteMenuProvider = new ContextMenuProvider<Void>()
        {
            @Override
            public List<JMenuItem> getMenuItems(String contextId, Void key)
            {
                JMenuItem clearSpatialFilters = new JMenuItem("Clear spatial filters");
                clearSpatialFilters.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        Quantify.collectMetric("mist3d.filter.clear-spatial-filters");
                        for (PolygonGeometry geom : New.set(SpatialFilterUtilities.getGeometryToTypeKeyMap().keySet()))
                        {
                            removeSpatialFilter(geom);
                        }
                    }
                });
                return Collections.singletonList(clearSpatialFilters);
            }

            @Override
            public int getPriority()
            {
                return 5;
            }
        };
        myToolbox.getUIRegistry().getContextActionManager().registerContextMenuItemProvider(ContextIdentifiers.DELETE_CONTEXT,
                Void.class, myDeleteMenuProvider);
    }

    @Override
    public List<JMenuItem> getMenuItems(String contextId, final GeometryContextKey key)
    {
        List<JMenuItem> options = New.list(1);
        if ((ContextIdentifiers.GEOMETRY_COMPLETED_CONTEXT.equals(contextId)
                || ContextIdentifiers.GEOMETRY_SELECTION_CONTEXT.equals(contextId))
                && key.getGeometry() instanceof PolygonGeometry)
        {
            final PolygonGeometry matchingGeom = getGeometry((PolygonGeometry)key.getGeometry());
            if (matchingGeom == null)
            {
                JMenuItem saveMI = new JMenuItem("Create spatial filter");
                saveMI.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                saveMI.setIcon(new GenericFontIcon(AwesomeIconSolid.FILTER, Color.WHITE));
                saveMI.addActionListener(evt -> SpatialFilterUtilities.createSpatialFilter(myToolbox,
                        (PolygonGeometry)key.getGeometry(), this));
                options.add(saveMI);
            }
            else
            {
                JMenuItem manageLayersMI = new JMenuItem("Manage spatial filter");
                manageLayersMI.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                manageLayersMI.setIcon(new GenericFontIcon(AwesomeIconSolid.FILTER, Color.WHITE));
                manageLayersMI.addActionListener(evt -> manageLayers(matchingGeom));
                options.add(manageLayersMI);

                JMenuItem removeMI = new JMenuItem("Remove spatial filter");
                removeMI.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                removeMI.setIcon(new GenericFontIcon(AwesomeIconSolid.FILTER, Color.WHITE));
                removeMI.addActionListener(evt -> removeSpatialFilter(matchingGeom));
                options.add(removeMI);
            }
        }
        return options;
    }

    @Override
    public int getPriority()
    {
        return 11022;
    }

    /**
     * Brings up a dialog for the user to manage layers for the spatial filter.
     *
     * @param geom the polygon geometry
     */
    private void manageLayers(PolygonGeometry geom)
    {
        Collection<String> initialTypeKeys = SpatialFilterUtilities.getGeometryToTypeKeyMap().get(geom);
        if (initialTypeKeys == null)
        {
            initialTypeKeys = Collections.<String>emptyList();
        }

        Collection<? extends DataTypeInfo> dataTypes = SpatialFilterUtilities.getDataTypesFromUser(myToolbox, false, initialTypeKeys);
        if (dataTypes != null)
        {
            Collection<String> newTypeKeys = StreamUtilities.map(dataTypes, dataType -> dataType.getTypeKey());

            geom.getRenderProperties().setColor(getColor(dataTypes));

            Polygon spatialFilter = JTSCoreGeometryUtilities.convertToJTSPolygon(geom);

            // Add the filter to the filter registry for the added types
            Collection<String> addedKeys = New.list(newTypeKeys);
            addedKeys.removeAll(initialTypeKeys);
            for (String typeKey : addedKeys)
            {
                SpatialFilterUtilities.addFilterToRegistry(myToolbox, typeKey, spatialFilter);
            }

            // Remove the filter from the filter registry for the removed types
            Collection<String> removedKeys = New.list(initialTypeKeys);
            removedKeys.removeAll(newTypeKeys);
            for (String typeKey : removedKeys)
            {
                SpatialFilterUtilities.removeFilterFromRegistry(myToolbox, typeKey, spatialFilter);
            }

            // Keep track of stuff
            SpatialFilterUtilities.getGeometryToTypeKeyMap().put(geom, newTypeKeys);
        }
    }

    /**
     * Removes the spatial filter for the given polygon geometry.
     *
     * @param geom the polygon geometry
     */
    private void removeSpatialFilter(PolygonGeometry geom)
    {
        // Remove the geometry from the map
        myToolbox.getGeometryRegistry().removeGeometriesForSource(this, Collections.singletonList(geom));

        Collection<String> typeKeys = SpatialFilterUtilities.getGeometryToTypeKeyMap().remove(geom);

        // Remove the filter to the filter registry
        if (typeKeys != null)
        {
            Polygon spatialFilter = JTSCoreGeometryUtilities.convertToJTSPolygon(geom);
            for (String typeKey : typeKeys)
            {
                SpatialFilterUtilities.removeFilterFromRegistry(myToolbox, typeKey, spatialFilter);
            }
        }
    }

    /**
     * Removes the type key from the given geometry in the map.
     *
     * @param typeKey the type key
     * @param spatialFilter the spatial filter
     */
    private void removeTypeKey(String typeKey, Polygon spatialFilter)
    {
        PolygonGeometry geom = JTSCoreGeometryUtilities.convertToPolygonGeometry(spatialFilter,
                new DefaultPolygonRenderProperties(0, true, true));
        Collection<String> typeKeys = SpatialFilterUtilities.getGeometryToTypeKeyMap().get(getGeometry(geom));
        if (typeKeys != null)
        {
            typeKeys.remove(typeKey);
        }
    }

    /**
     * Gets a color based on the given data types.
     * @param dataTypes the data types
     * @return the color
     */
    private Color getColor(Collection<? extends DataTypeInfo> dataTypes)
    {
        return dataTypes.size() == 1 ? dataTypes.iterator().next().getBasicVisualizationInfo().getTypeColor()
                : Colors.QUERY_REGION;
    }

    /**
     * Gets the matching geometry from the internal map (also the one used on
     * the map).
     *
     * @param geometry the geometry to look for
     * @return the matching geometry from the map
     */
    private PolygonGeometry getGeometry(final PolygonGeometry geometry)
    {
        return StreamUtilities.filterOne(SpatialFilterUtilities.getGeometryToTypeKeyMap().keySet(),
                geom -> geom.getVertices().equals(geometry.getVertices()));
    }
}
