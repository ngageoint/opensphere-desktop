package io.opensphere.filterbuilder.impl;

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
 * Utility class for managing spatial filters.
 */
public class SpatialFilterUtilities
{
    /** Predicate for streaming and filterable data types. */
    private static final Predicate<DataTypeInfo> STREAMING_AND_FILTERABLE = new AndPredicate<>(
            StreamingSupport.IS_STREAMING_ENABLED, DataLayerFilter.DATA_TYPE_FILTERABLE);

    /** The map of geometries to the corresponding type key. */
    private static final Map<PolygonGeometry, Collection<String>> GEOMETRIES_TO_TYPE_KEY_MAP = New.map();

    /** The test to see if a data group is active and contains members. */
    private static final Predicate<DataGroupInfo> DATA_GROUP_FILTER = new Predicate<DataGroupInfo>()
    {
        @Override
        public boolean test(DataGroupInfo dataGroup)
        {
            boolean isGroupActive = dataGroup.activationProperty().isActiveOrActivating();
            return isGroupActive && !dataGroup.findMembers(STREAMING_AND_FILTERABLE, false, true).isEmpty();
        }
    };

    /**
     * Create a new spatial filter.
     *
     * @param toolbox the toolbox
     * @param geometry the geometry to create the spatial filter for
     * @param source the source
     */
    public static void createSpatialFilter(Toolbox toolbox, PolygonGeometry geometry, Object source)
    {
        Collection<? extends DataTypeInfo> dataTypes = getDataTypesFromUser(toolbox, true, null);
        if (CollectionUtilities.hasContent(dataTypes))
        {
            Collection<String> typeKeys = StreamUtilities.map(dataTypes, dataType -> dataType.getTypeKey());

            PolygonGeometry mapGeom = createMapGeometry(geometry, dataTypes);

            // Put the geometry on the map
            toolbox.getGeometryRegistry().addGeometriesForSource(source, Collections.singletonList(mapGeom));

            // Add the filter to the filter registry
            Polygon spatialFilter = JTSCoreGeometryUtilities.convertToJTSPolygon(geometry);
            for (String typeKey : typeKeys)
            {
                addFilterToRegistry(toolbox, typeKey, spatialFilter);
            }

            // Keep track of stuff
            GEOMETRIES_TO_TYPE_KEY_MAP.put(mapGeom, typeKeys);
        }
    }

    /**
     * Opens a dialog to ask the user which data types need a spatial filter.
     *
     * @param toolbox the toolbox
     * @param selectAll whether to select all of the types
     * @param selectedTypeKeys the set of types to select if not all are being selected
     * @return the data types the user selected
     */
    public static Collection<? extends DataTypeInfo> getDataTypesFromUser(Toolbox toolbox, boolean selectAll,
            Collection<String> selectedTypeKeys)
    {
        Collection<? extends DataTypeInfo> dataTypes;

        Collection<DataGroupInfo> results = New.list(1);
        MantleToolboxUtils.getMantleToolbox(toolbox).getDataGroupController().findDataGroupInfo(DATA_GROUP_FILTER, results,
                true);
        if (results.isEmpty())
        {
            final OptionDialog dialog = new OptionDialog(toolbox.getUIRegistry().getMainFrameProvider().get());
            JPanel panel = new JPanel();
            panel.add(new JLabel("Spatial filters require active streaming layers. You can add streaming layers in"));
            LinkButton button = new LinkButton("Add Data");
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    toolbox.getEventManager().publishEvent(new ShowAvailableDataEvent());
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
            SearchAvailableLayersPanel panel = new SearchAvailableLayersPanel(toolbox,
                    new PredicatedAvailableDataDataLayerController(toolbox, DATA_GROUP_FILTER),
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
            OptionDialog dialog = new OptionDialog(toolbox.getUIRegistry().getMainFrameProvider().get(), panel,
                    "Select Layers to Apply Filter");
            dialog.buildAndShow();
            dataTypes = dialog.getSelection() == JOptionPane.OK_OPTION ? panel.getSelectedDataTypes() : null;
        }

        return dataTypes;
    }

    /**
     * Adds a polygonal component to the filter in the registry.
     *
     * @param toolbox the toolbox
     * @param typeKey the type key
     * @param polygon the spatial filter
     */
    public static void addFilterToRegistry(Toolbox toolbox, String typeKey, Polygon polygon)
    {
        toolbox.getDataFilterRegistry().addSpatialLoadFilter(typeKey, addPoly(getFilterGeom(toolbox, typeKey), polygon));
    }

   /**
    * Removes a polygonal component from the filter in the registry.
    *
    * @param typeKey the type key
    * @param polygon the spatial filter
    */
   public static void removeFilterFromRegistry(Toolbox toolbox, String typeKey, Polygon polygon)
   {
       MultiPolygon multiPolygon = delPoly(getFilterGeom(toolbox, typeKey), polygon);
       if (multiPolygon == null)
       {
           toolbox.getDataFilterRegistry().removeSpatialLoadFilter(typeKey);
       }
       else
       {
           toolbox.getDataFilterRegistry().addSpatialLoadFilter(typeKey, multiPolygon);
       }
   }

    /**
     * Add a Polygon component to a MultiPolygon geometry.  If the specified
     * Polygon is already present, then there is no change.  All components are
     * kept in the same form as when inserted; they are never combined (e.g.,
     * by finding the union) or culled (e.g., when one contains another).
     *
     * @param multiPolygon a MultiPolygon, which may be null
     * @param polygon the polygon to be added
     * @return the multipolygon with the polygon added or null
     */
    private static MultiPolygon addPoly(MultiPolygon multiPolygon, Polygon polygon)
    {
        List<Polygon> pList = allPoly(multiPolygon);
        if (!pList.contains(polygon))
        {
            pList.add(polygon);
        }
        return asMultiPoly(pList);
    }

    /**
     * Remove a Polygon component from a MultiPolygon geometry.  If the
     * specified component is not present, then there is no change, even if the
     * Polygon to be removed intersects with other existing components.
     *
     * @param multiPolygon a MultiPolygon, which may be null
     * @param polygon the polygon to be removed
     * @return a MultiPolygon with the polygon removed or null
     */
    private static MultiPolygon delPoly(MultiPolygon multiPolygon, Polygon polygon)
    {
        List<Polygon> pList = allPoly(multiPolygon);
        pList.remove(polygon);
        return asMultiPoly(pList);
    }

    /**
     * Convert a MultiPolygon geometry into an equivalent but much more useable
     * List of Polygons.
     *
     * @param geometry a MultiPolygon, which may be null
     * @return the list of equivalent polygons
     */
    private static List<Polygon> allPoly(MultiPolygon geometry)
    {
        List<Polygon> ret = new LinkedList<>();
        if (geometry == null)
        {
            return ret;
        }
        int n = geometry.getNumGeometries();
        for (int i = 0; i < n; i++)
        {
            ret.add((Polygon) geometry.getGeometryN(i));
        }
        return ret;
    }

    /**
     * Convert a List of Polygons into an equivalent MultiPolygon geometry. If
     * the List is null or empty, then this method returns null.
     *
     * @param polygonList the list of polygons
     * @return the list of equivalent multipolygons
     */
    private static MultiPolygon asMultiPoly(List<Polygon> polygonList)
    {
        if (polygonList == null || polygonList.isEmpty())
        {
            return null;
        }
        return new MultiPolygon(polygonList.toArray(new Polygon[0]), new GeometryFactory());
    }

    /**
     * Creates the map geometry from the geometry.
     *
     * @param geom the geometry
     * @param dataTypes the data types to which this applies
     * @return the map geometry
     */
    private static PolygonGeometry createMapGeometry(PolygonGeometry geom, Collection<? extends DataTypeInfo> dataTypes)
    {
        PolygonRenderProperties props = new DefaultPolygonRenderProperties(ZOrderRenderProperties.TOP_Z, true, true);
        props.setColor(getColor(dataTypes));
        props.setWidth(geom.getRenderProperties().getWidth());
        return geom.derive(props, (Constraints)null);
    }

    /**
     * Gets a color based on the given data types.
     *
     * @param dataTypes the data types
     * @return the color
     */
    private static Color getColor(Collection<? extends DataTypeInfo> dataTypes)
    {
        return dataTypes.size() == 1 ? dataTypes.iterator().next().getBasicVisualizationInfo().getTypeColor()
                : Colors.QUERY_REGION;
    }

    public static Predicate<DataGroupInfo> getFilter()
    {
        return DATA_GROUP_FILTER;
    }

    public static Map<PolygonGeometry, Collection<String>> getGeometryToTypeKeyMap()
    {
        return GEOMETRIES_TO_TYPE_KEY_MAP;
    }

    /**
     * Retrieve a spatial filter Geometry, which ought to be a Polygon or a
     * MultiPolygon, from the filter registry.  If a Geometry is found and its
     * type one of those supported, it is returned as a MultiPolygon (after
     * conversion, if necessary); otherwise, this method returns null.
     *
     * @param toolbox the toolbox
     * @param typeKey the type for which the filter is desired
     * @return a MultiPolygon filter geometry, if found, or null
     */
    private static MultiPolygon getFilterGeom(Toolbox toolbox, String typeKey)
    {
        Geometry g = toolbox.getDataFilterRegistry().getSpatialLoadFilter(typeKey);
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
     * Constructor. Disallow instantiation.
     */
    private SpatialFilterUtilities()
    {
    }
}
