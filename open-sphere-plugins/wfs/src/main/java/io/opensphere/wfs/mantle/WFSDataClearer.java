package io.opensphere.wfs.mantle;

import java.util.List;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DataElementCache;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.factory.MapGeometrySupportConverterRegistry;
import io.opensphere.mantle.data.geom.factory.RenderPropertyPool;
import io.opensphere.mantle.data.geom.factory.impl.DefaultRenderPropertyPool;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.data.util.DataElementLookupUtils;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;

/**
 * Responsible for removing WFS features from the globe given the feature layer,
 * the area to remove from, and the time span to remove from.
 */
public class WFSDataClearer
{
    /**
     * The data element cache.
     */
    private final DataElementCache myCache;

    /** The converter registry. */
    private final MapGeometrySupportConverterRegistry myConverterRegistry;

    /**
     * Used to get data elements.
     */
    private final DataElementLookupUtils myElements;

    /**
     * Used to test for geometry intersection.
     */
    private final GeometryFactory myGeometryFactory = new GeometryFactory();

    /**
     * The layer.
     */
    private final DataTypeInfo myLayer;

    /**
     * Used to convert mantle geometry to core geometry.
     */
    private final RenderPropertyPool myPropertyPool;

    /**
     * The region to remove elements for.
     */
    private final QueryRegion myQueryArea;

    /**
     * Used to remove data elements.
     */
    private final DataTypeController myTypeController;

    /**
     * Used to help convert mantle geometries to core geometries.
     */
    private final VisualizationState myVisualizationState = new VisualizationState(true);

    /**
     * Constructs a new data clearer.
     *
     * @param mantleToolbox The mantle toolbox.
     * @param layer The layer to remove elements for.
     * @param queryArea The area to remove elements for.
     */
    public WFSDataClearer(MantleToolbox mantleToolbox, DataTypeInfo layer, QueryRegion queryArea)
    {
        myCache = mantleToolbox.getDataElementCache();
        myElements = mantleToolbox.getDataElementLookupUtils();
        myTypeController = mantleToolbox.getDataTypeController();
        myConverterRegistry = mantleToolbox.getMapGeometrySupportConverterRegistry();
        myLayer = layer;
        myQueryArea = queryArea;
        myPropertyPool = new DefaultRenderPropertyPool(myLayer);
    }

    /**
     * Removes all of the data elements within the {@link QueryRegion}.
     *
     * @throws DataElementLookupException Thrown if there was an issue getting
     *             the {@link DataElement}.
     */
    public void removeAll() throws DataElementLookupException
    {
        List<Long> elementIds = myCache.getElementIdsForTypeAsList(myLayer);
        removeIds(elementIds);
    }

    /**
     * Removes all of the data elements within the specified {@link TimeSpan}.
     *
     * @param span The time span to remove elements for.
     * @throws DataElementLookupException Thrown if there was an issue getting
     *             the {@link DataElement}.
     */
    public void removeFeatures(TimeSpan span) throws DataElementLookupException
    {
        List<Long> idsWithinTime = idsWithinTime(span);
        removeIds(idsWithinTime);
    }

    /**
     * Gets all element ids within idsWithinTime that are contained in the
     * {@link QueryRegion}.
     *
     * @param idsWithinTime The element ids to check to see if contained in
     *            {@link QueryRegion}.
     * @return The element ids that are contained in the {@link QueryRegion}.
     * @throws DataElementLookupException Thrown if there was an issue getting
     *             the {@link DataElement}.
     */
    private List<Long> idsWithinRegion(List<Long> idsWithinTime) throws DataElementLookupException
    {
        List<Long> idsWithinRegion = New.list();

        List<DataElement> elements = myElements.getDataElements(idsWithinTime, myLayer, null, false);
        int index = 0;
        for (DataElement element : elements)
        {
            if (element instanceof MapDataElement)
            {
                MapDataElement mapElement = (MapDataElement)element;
                MapGeometrySupport geometrySupport = mapElement.getMapGeometrySupport();
                Geometry geometry = myConverterRegistry.getConverter(geometrySupport).createGeometry(geometrySupport,
                        mapElement.getId(), myLayer, myVisualizationState, myPropertyPool);

                List<Polygon> polygons = New
                        .list(JTSCoreGeometryUtilities.convertToJTSPolygonsAndSplit(myQueryArea.getGeometries()));
                if (geometry.jtsIntersectionTests(new Geometry.JTSIntersectionTests(true, true, true), polygons,
                        myGeometryFactory))
                {
                    idsWithinRegion.add(idsWithinTime.get(index));
                }
            }
            index++;
        }

        return idsWithinRegion;
    }

    /**
     * Gets the element ids for myLayer who are within the given timespan.
     *
     * @param span The time span to get ids for.
     * @return The list of element ids that are within span.
     */
    private List<Long> idsWithinTime(TimeSpan span)
    {
        List<Long> elementIds = myCache.getElementIdsForTypeAsList(myLayer);
        List<TimeSpan> spans = myCache.getTimeSpans(elementIds);

        List<Long> idsWithinTime = New.list();
        int index = 0;
        for (TimeSpan aSpan : spans)
        {
            if (span.contains(aSpan))
            {
                idsWithinTime.add(elementIds.get(index));
            }

            index++;
        }

        return idsWithinTime;
    }

    /**
     * Removes the elements with the specified ids who live within the query
     * region.
     *
     * @param elementIds The ids to remove.
     * @throws DataElementLookupException Thrown if there was an issue getting
     *             the {@link DataElement}.
     */
    private void removeIds(List<Long> elementIds) throws DataElementLookupException
    {
        List<Long> idsToRemove = idsWithinRegion(elementIds);
        long[] ids = new long[idsToRemove.size()];
        for (int i = 0; i < ids.length; i++)
        {
            ids[i] = idsToRemove.get(i).longValue();
        }

        myTypeController.removeDataElements(myLayer, ids);
    }
}
