package io.opensphere.filterbuilder;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.controlpanels.layers.SearchAvailableLayersPanel;
import io.opensphere.controlpanels.layers.activedata.controller.PredicatedAvailableDataDataLayerController;
import io.opensphere.controlpanels.layers.event.ShowAvailableDataEvent;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.core.control.action.context.ContextIdentifiers;
import io.opensphere.core.control.action.context.GeometryContextKey;
import io.opensphere.core.datafilter.impl.DataFilterRegistryAdapter;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.util.Colors;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.core.util.predicate.AndPredicate;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.LinkButton;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.StreamingSupport;
import io.opensphere.mantle.data.filter.DataLayerFilter;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Geometry ContextMenuProvider for Filter Builder (for spatial filters).
 */
@SuppressWarnings("PMD.GodClass")
public class GeometryContextMenuProvider implements ContextMenuProvider<GeometryContextKey>
{
    /** Predicate for streaming and filterable data types. */
    private static final Predicate<DataTypeInfo> STREAMING_AND_FILTERABLE = new AndPredicate<>(
            StreamingSupport.IS_STREAMING_ENABLED, DataLayerFilter.DATA_TYPE_FILTERABLE);

    /**
     * Predicate that determines if a data group should be shown in the layer
     * selector.
     */
    private final Predicate<DataGroupInfo> myDataGroupFilter = new Predicate<DataGroupInfo>()
    {
        @Override
        public boolean test(DataGroupInfo dataGroup)
        {
            boolean isGroupActive = dataGroup.activationProperty().isActiveOrActivating();
            return isGroupActive && !dataGroup.findMembers(STREAMING_AND_FILTERABLE, false, true).isEmpty();
        }
    };

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** Map of geometry to data type keys. */
    private final Map<PolygonGeometry, Collection<String>> myGeomToTypeKeysMap = New.map();

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
                        for (PolygonGeometry geom : New.set(myGeomToTypeKeysMap.keySet()))
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
                JMenuItem saveMI = new JMenuItem("Create spatial filter...");
                saveMI.addActionListener(evt -> createSpatialFilter((PolygonGeometry)key.getGeometry()));
                options.add(saveMI);
            }
            else
            {
                JMenuItem manageLayersMI = new JMenuItem("Manage spatial filter...");
                manageLayersMI.addActionListener(evt -> manageLayers(matchingGeom));
                options.add(manageLayersMI);

                JMenuItem removeMI = new JMenuItem("Remove spatial filter");
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
     * Creates a spatial filter for the given polygon geometry.
     *
     * @param geom the polygon geometry
     */
    private void createSpatialFilter(PolygonGeometry geom)
    {
        Collection<? extends DataTypeInfo> dataTypes = getDataTypesFromUser(true, null);
        if (CollectionUtilities.hasContent(dataTypes))
        {
            Collection<String> typeKeys = StreamUtilities.map(dataTypes, dataType -> dataType.getTypeKey());

            PolygonGeometry mapGeom = createMapGeometry(geom, dataTypes);

            // Put the geometry on the map
            myToolbox.getGeometryRegistry().addGeometriesForSource(this, Collections.singletonList(mapGeom));

            // Add the filter to the filter registry
            Polygon spatialFilter = JTSCoreGeometryUtilities.convertToJTSPolygon(geom);
            for (String typeKey : typeKeys)
            {
                addFilterToRegistry(typeKey, spatialFilter);
            }

            // Keep track of stuff
            myGeomToTypeKeysMap.put(mapGeom, typeKeys);
        }
    }

    /**
     * Brings up a dialog for the user to manage layers for the spatial filter.
     *
     * @param geom the polygon geometry
     */
    private void manageLayers(PolygonGeometry geom)
    {
        Collection<String> initialTypeKeys = myGeomToTypeKeysMap.get(geom);
        if (initialTypeKeys == null)
        {
            initialTypeKeys = Collections.<String>emptyList();
        }

        Collection<? extends DataTypeInfo> dataTypes = getDataTypesFromUser(false, initialTypeKeys);
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
                addFilterToRegistry(typeKey, spatialFilter);
            }

            // Remove the filter from the filter registry for the removed types
            Collection<String> removedKeys = New.list(initialTypeKeys);
            removedKeys.removeAll(newTypeKeys);
            for (String typeKey : removedKeys)
            {
                removeFilterFromRegistry(typeKey, spatialFilter);
            }

            // Keep track of stuff
            myGeomToTypeKeysMap.put(geom, newTypeKeys);
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

        Collection<String> typeKeys = myGeomToTypeKeysMap.remove(geom);

        // Remove the filter to the filter registry
        if (typeKeys != null)
        {
            Polygon spatialFilter = JTSCoreGeometryUtilities.convertToJTSPolygon(geom);
            for (String typeKey : typeKeys)
            {
                removeFilterFromRegistry(typeKey, spatialFilter);
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
        Collection<String> typeKeys = myGeomToTypeKeysMap.get(getGeometry(geom));
        if (typeKeys != null)
        {
            typeKeys.remove(typeKey);
        }
    }

    /**
     * Gets the data types from the user.
     *
     * @param selectAll Whether to select all nodes. If true this overrides the
     *            selectedTypeKeys argument.
     * @param selectedTypeKeys the initially selected data types
     * @return the selected data types, or null if the user didn't complete
     *         selection
     */
    private Collection<? extends DataTypeInfo> getDataTypesFromUser(boolean selectAll, Collection<String> selectedTypeKeys)
    {
        Collection<? extends DataTypeInfo> dataTypes;

        Collection<DataGroupInfo> results = New.list(1);
        MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController().findDataGroupInfo(myDataGroupFilter, results,
                true);
        if (results.isEmpty())
        {
            final OptionDialog dialog = new OptionDialog(myToolbox.getUIRegistry().getMainFrameProvider().get());
            JPanel panel = new JPanel();
            panel.add(new JLabel("Spatial filters require active streaming layers. You can add streaming layers in"));
            LinkButton button = new LinkButton("Add Data");
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    myToolbox.getEventManager().publishEvent(new ShowAvailableDataEvent());
                    dialog.dispose();
                }
            });
            panel.add(button);
            dialog.setTitle("No streaming layers available");
            dialog.setComponent(panel);
            dialog.setButtonLabels(Collections.singletonList(ButtonPanel.OK));
            dialog.buildAndShow();

            dataTypes = null;
        }
        else
        {
            // Create the panel
            SearchAvailableLayersPanel panel = new SearchAvailableLayersPanel(myToolbox,
                    new PredicatedAvailableDataDataLayerController(myToolbox, myDataGroupFilter),
                    TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION, DataTypeInfo.HAS_METADATA_PREDICATE);
            panel.initGuiElements();
            if (selectAll)
            {
                panel.selectAll();
            }
            else
            {
                panel.setSelectedTypeKeys(selectedTypeKeys);
            }

            // Show the dialog
            OptionDialog dialog = new OptionDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), panel,
                    "Select Layers to Apply Filter");
            dialog.buildAndShow();
            dataTypes = dialog.getSelection() == JOptionPane.OK_OPTION ? panel.getSelectedDataTypes() : null;
        }

        return dataTypes;
    }

    /**
     * Creates the map geometry from the geometry.
     * @param geom the geometry
     * @param dataTypes the data types to which this applies
     * @return the map geometry
     */
    private PolygonGeometry createMapGeometry(PolygonGeometry geom, Collection<? extends DataTypeInfo> dataTypes)
    {
        PolygonRenderProperties props = new DefaultPolygonRenderProperties(ZOrderRenderProperties.TOP_Z, true, true);
        props.setColor(getColor(dataTypes));
        props.setWidth(geom.getRenderProperties().getWidth());
        return geom.derive(props, (Constraints)null);
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
        return StreamUtilities.filterOne(myGeomToTypeKeysMap.keySet(), geom -> geom.getVertices().equals(geometry.getVertices()));
    }

    /**
     * Adds a polygonal component to the filter in the registry.
     * @param typeKey the type key
     * @param p the spatial filter
     */
    private void addFilterToRegistry(String typeKey, Polygon p)
    {
        myToolbox.getDataFilterRegistry().addSpatialLoadFilter(typeKey, addPoly(getFilterGeom(typeKey), p));
    }

    /**
     * Removes a polygonal component from the filter in the registry.
     * @param typeKey the type key
     * @param p the spatial filter
     */
    private void removeFilterFromRegistry(String typeKey, Polygon p)
    {
        MultiPolygon g = delPoly(getFilterGeom(typeKey), p);
        if (g == null)
        {
            myToolbox.getDataFilterRegistry().removeSpatialLoadFilter(typeKey);
        }
        else
        {
            myToolbox.getDataFilterRegistry().addSpatialLoadFilter(typeKey, g);
        }
    }

    /**
     * Retrieve a spatial filter Geometry, which ought to be a Polygon or a
     * MultiPolygon, from the filter registry.  If a Geometry is found and its
     * type one of those supported, it is returned as a MultiPolygon (after
     * conversion, if necessary); otherwise, this method returns null.
     * @param typeKey the type for which the filter is desired
     * @return a MultiPolygon filter geometry, if found, or null
     */
    private MultiPolygon getFilterGeom(String typeKey)
    {
        Geometry g = myToolbox.getDataFilterRegistry().getSpatialLoadFilter(typeKey);
        if (g instanceof MultiPolygon)
        {
            return (MultiPolygon)g;
        }
        if (g instanceof Polygon)
        {
            return new MultiPolygon(new Polygon[] {(Polygon)g}, new GeometryFactory());
        }
        return null;
    }

    /**
     * Add a Polygon component to a MultiPolygon geometry.  If the specified
     * Polygon is already present, then there is no change.  All components are
     * kept in the same form as when inserted; they are never combined (e.g.,
     * by finding the union) or culled (e.g., when one contains another).
     * @param g a MultiPolygon, which may be null
     * @param p bla
     * @return bla
     */
    private static MultiPolygon addPoly(MultiPolygon g, Polygon p)
    {
        List<Polygon> pList = allPoly(g);
        if (!pList.contains(p))
        {
            pList.add(p);
        }
        return asMultiPoly(pList);
    }

    /**
     * Remove a Polygon component from a MultiPolygon geometry.  If the
     * specified component is not present, then there is no change, even if the
     * Polygon to be removed intersects with other existing components.
     * @param g a MultiPolygon, which may be null
     * @param p bla
     * @return a MultiPolygon containing at least one component or null
     */
    private static MultiPolygon delPoly(MultiPolygon g, Polygon p)
    {
        List<Polygon> pList = allPoly(g);
        pList.remove(p);
        return asMultiPoly(pList);
    }

    /**
     * Convert a MultiPolygon geometry into an equivalent but much more useable
     * List of Polygons.
     * @param g a MultiPolygon, which may be null
     * @return bla
     */
    private static List<Polygon> allPoly(MultiPolygon g)
    {
        List<Polygon> ret = new LinkedList<>();
        if (g == null)
        {
            return ret;
        }
        int n = g.getNumGeometries();
        for (int i = 0; i < n; i++)
        {
            ret.add((Polygon) g.getGeometryN(i));
        }
        return ret;
    }

    /**
     * Convert a List of Polygons into an equivalent MultiPolygon geometry.  If
     * the List is null or empty, then this method returns null.
     * @param pList bla
     * @return bla
     */
    private static MultiPolygon asMultiPoly(List<Polygon> pList)
    {
        if (pList == null || pList.isEmpty())
        {
            return null;
        }
        return new MultiPolygon(pList.toArray(new Polygon[0]), new GeometryFactory());
    }
}
