package io.opensphere.mantle.plugin.queryregion.impl;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.concurrent.GuardedBy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.QueryAreaType;
import com.bitsys.fade.mist.state.v4.QueryAreasType;
import com.bitsys.fade.mist.state.v4.StateType;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import io.opensphere.core.Notify;
import io.opensphere.core.Toolbox;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.export.ExportException;
import io.opensphere.core.export.Exporters;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.constraint.Constraints;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.GeographicPositionArrayList;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.modulestate.AbstractModuleStateController;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.petrifyable.PetrifyableTDoubleArrayList;
import io.opensphere.core.util.function.AppendFunction;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import net.opengis.kml._220.BoundaryType;
import net.opengis.kml._220.LinearRingType;
import net.opengis.kml._220.PolygonType;

/**
 * State controller for query regions. This is responsible for saving and
 * storing query region state.
 */
@SuppressWarnings("PMD.GodClass")
final class QueryRegionStateController extends AbstractModuleStateController
{
    /** The query region area ID prefix. */
    private static final String REGION_PREFIX = "area_";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(QueryRegionStateController.class);

    /**
     * The query region manager.
     */
    private final QueryRegionManager myQueryRegionManager;

    /** Map of state ids to geometries. */
    @GuardedBy("this")
    private final Map<String, Collection<QueryRegion>> myStateToGeometryMap = New.map();

    /** The toolbox. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     *
     * @param queryRegionManager The query region manager.
     * @param toolbox The toolbox.
     */
    QueryRegionStateController(QueryRegionManager queryRegionManager, Toolbox toolbox)
    {
        myQueryRegionManager = queryRegionManager;
        myToolbox = toolbox;
    }

    @Override
    public synchronized void activateState(final String id, String description, Collection<? extends String> tags, Node node)
    {
        try
        {
            XPath xpath = StateXML.newXPath();

            Node queryAreasNode = (Node)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:queryAreas", node,
                    XPathConstants.NODE);
            Node queryEntriesNode = (Node)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:queryEntries", node,
                    XPathConstants.NODE);
            if (queryAreasNode != null)
            {
                Collection<QueryArea> areas = XMLUtilities.readXMLObject(queryAreasNode, QueryAreas.class).getQueryAreas();
                if (CollectionUtilities.hasContent(areas))
                {
                    Map<String, Collection<QueryEntry>> areaIdToQueryEntryMap = getAreaIdToQueryEntryMap(queryEntriesNode);

                    Collection<QueryRegion> queriesForState = New.collection();
                    for (QueryArea area : areas)
                    {
                        if (CollectionUtilities.hasContent(area.getPolygons()))
                        {
                            PolygonRenderProperties renderProperties = new DefaultPolygonRenderProperties(
                                    ZOrderRenderProperties.TOP_Z - 1, true, true);
                            renderProperties.setColor(Color.CYAN);
                            renderProperties.setWidth(3);

                            Collection<TimeSpan> times = area.getTimes();
                            if (!CollectionUtilities.hasContent(times))
                            {
                                times = Collections.singleton(TimeSpan.TIMELESS);
                                renderProperties.setStipple(null);
                            }
                            else
                            {
                                renderProperties.setStipple(StippleModelConfig.DOTTED);
                            }
                            Collection<PolygonGeometry> geometries = New.collection();
                            PolygonGeometry.Builder<GeographicPosition> builder = new PolygonGeometry.Builder<>();
                            for (Polygon poly : area.getPolygons())
                            {
                                List<Coordinate> vertices = poly.getOuterBoundaryIs().getLinearRing().getCoordinates();
                                PetrifyableTDoubleArrayList list = new PetrifyableTDoubleArrayList(vertices.size() * 3);
                                for (int index = 0; index < vertices.size() - 1; ++index)
                                {
                                    list.add(vertices.get(index).getLatitude());
                                    list.add(vertices.get(index).getLongitude());
                                    list.add(vertices.get(index).getAltitude());
                                }
                                builder.setVertices(
                                        GeographicPositionArrayList.createFromDegreesMeters(list, ReferenceLevel.TERRAIN));
                                PolygonGeometry geom = new PolygonGeometry(builder, renderProperties, (Constraints)null);
                                geometries.add(geom);
                            }

                            if (StringUtils.isEmpty(area.getId()) || !areaIdToQueryEntryMap.containsKey(area.getId()))
                            {
                                Map<String, DataFilter> filterMap = New.map();
                                area.getLayers().stream().map(getIdTransform(id)).forEach(v -> filterMap.put(v, null));
                                queriesForState.add(myQueryRegionManager.addQueryRegion(geometries, times, filterMap));
                            }
                            else
                            {
                                processQueryEntries(id, areaIdToQueryEntryMap, queriesForState, area, times, geometries);
                            }
                        }
                    }

                    myStateToGeometryMap.put(id, queriesForState);
                }
            }
        }
        catch (JAXBException | XPathExpressionException e)
        {
            LOGGER.error("Failed to read query areas state: " + e, e);
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
    {
        if (canActivateState(state))
        {
            Collection<QueryRegion> queriesForState = New.collection();
            for (QueryAreaType queryArea : state.getQueryAreas().getQueryArea())
            {
                Collection<String> layerIds = StateUtilities.getLayerIds(queryArea, state);
                if (queryArea.getId() != null && queryArea.getId().startsWith(REGION_PREFIX)
                        && !layerIds.stream().anyMatch(l -> l != null && l.endsWith("stream")))
                {
                    QueryRegion queryRegion = toQueryRegion(queryArea, state);
                    myQueryRegionManager.addQueryRegion(queryRegion);
                    queriesForState.add(queryRegion);
                }
            }

            myStateToGeometryMap.put(id, queriesForState);
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        try
        {
            return StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:queryAreas", node,
                    XPathConstants.NODE) != null;
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
            return false;
        }
    }

    @Override
    public boolean canActivateState(StateType state)
    {
        return state.getQueryAreas() != null && state.getQueryAreas().isSetQueryArea();
    }

    @Override
    public boolean canSaveState()
    {
        return !myQueryRegionManager.getQueryRegions().isEmpty();
    }

    @Override
    public synchronized void deactivateState(String id, Node node)
    {
        Collection<QueryRegion> regions = myStateToGeometryMap.remove(id);
        if (regions != null)
        {
            myQueryRegionManager.removeQueryRegions(regions);
        }
    }

    @Override
    public void deactivateState(String id, StateType state)
    {
        deactivateState(id, (Node)null);
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return true;
    }

    @Override
    public void saveState(Node node)
    {
        QueryAreas queryAreas = new QueryAreas();
        QueryEntries queryEntries = null;
        List<? extends QueryRegion> queryRegions = myQueryRegionManager.getQueryRegions();
        if (!queryRegions.isEmpty())
        {
            for (QueryRegion queryRegion : queryRegions)
            {
                QueryArea queryArea = new QueryArea();
                queryArea.setId(REGION_PREFIX + Integer.toHexString(queryRegion.hashCode()));
                for (PolygonGeometry polygonGeometry : queryRegion.getGeometries())
                {
                    LinearRing linearRing = new LinearRing();
                    for (Position pos : polygonGeometry.getVertices())
                    {
                        LatLonAlt lla = ((GeographicPosition)pos).getLatLonAlt();
                        linearRing.addToCoordinates(lla.getLonD(), lla.getLatD(), lla.getAltM());
                    }
                    LatLonAlt lla = ((GeographicPosition)polygonGeometry.getVertices().get(0)).getLatLonAlt();
                    linearRing.addToCoordinates(lla.getLonD(), lla.getLatD(), lla.getAltM());

                    Polygon polygon = new Polygon();
                    polygon.createAndSetOuterBoundaryIs().setLinearRing(linearRing);

                    queryArea.getPolygons().add(polygon);
                }
                if (queryRegion.getValidTimes().size() != 1 || queryRegion.getValidTimes().iterator().next().isBounded())
                {
                    queryArea.getTimes().addAll(queryRegion.getValidTimes());
                }
                queryArea.setLayers(queryRegion.getTypeKeys());
                queryAreas.getQueryAreas().add(queryArea);

                if (queryRegion.getTypeKeyToFilterMap().values().stream().anyMatch(v -> v != null))
                {
                    if (queryEntries == null)
                    {
                        queryEntries = new QueryEntries();
                    }
                    saveQueryEntries(node, queryEntries, queryRegion, queryArea);
                }
            }

            try
            {
                JAXBContext context = JAXBContextHelper.getCachedContext(QueryAreas.class, QueryEntries.class);
                context.createMarshaller().marshal(queryAreas, node);
                XMLUtilities.mergeDuplicateElements(node.getOwnerDocument(), "queryAreas");

                if (queryEntries != null)
                {
                    context.createMarshaller().marshal(queryEntries, node);
                    XMLUtilities.mergeDuplicateElements(node.getOwnerDocument(), "queryEntries");
                    XMLUtilities.mergeDuplicateElements(node.getOwnerDocument(), "filters");
                }
            }
            catch (JAXBException e)
            {
                LOGGER.error("Failed to marshal query areas: " + e, e);
            }
        }
    }

    @Override
    public void saveState(StateType state)
    {
        List<? extends QueryRegion> queryRegions = myQueryRegionManager.getQueryRegions();
        if (!queryRegions.isEmpty())
        {
            QueryAreasType queryAreasType = StateUtilities.getQueryAreas(state);
            for (QueryRegion queryRegion : queryRegions)
            {
                queryAreasType.getQueryArea().add(toQueryArea(queryRegion));
            }
        }
    }

    /**
     * Converts a mantle query region to a JAXB query area.
     *
     * @param queryRegion the mantle query region
     * @return the JAXB query area
     */
    private QueryAreaType toQueryArea(QueryRegion queryRegion)
    {
        QueryAreaType queryArea = new QueryAreaType();
        String areaId = queryRegion.getId() != null ? queryRegion.getId()
                : REGION_PREFIX + Integer.toHexString(queryRegion.hashCode());
        queryArea.setId(areaId);
        if (!queryRegion.getGeometries().isEmpty())
        {
            if (queryRegion.getGeometries().size() > 1)
            {
                Notify.warn("Some query region geometries were skipped");
            }

            PolygonGeometry polygonGeometry = queryRegion.getGeometries().iterator().next();

            PolygonType polygon = new PolygonType();
            BoundaryType boundary = new BoundaryType();
            LinearRingType linearRing = new LinearRingType();
            for (Position pos : polygonGeometry.getVertices())
            {
                LatLonAlt lla = ((GeographicPosition)pos).getLatLonAlt();
                linearRing.getCoordinates().add(toString(lla));
            }
            boundary.setLinearRing(linearRing);
            polygon.setOuterBoundaryIs(boundary);
            queryArea.setPolygon(polygon);
        }
        // TODO
//        if (queryRegion.getValidTimes().size() != 1 || queryRegion.getValidTimes().iterator().next().isBounded())
//        {
//            queryArea.getTimes().addAll(queryRegion.getValidTimes());
//        }
        return queryArea;
    }

    /**
     * Converts a JAXB query area to a mantle query region.
     *
     * @param queryArea the JAXB query area
     * @param state the state object
     * @return the mantle query region
     */
    private QueryRegion toQueryRegion(QueryAreaType queryArea, StateType state)
    {
        PolygonGeometry geom = StateUtilities.toCoreGeometry(queryArea);

        Collection<TimeSpan> validTimes = Collections.singleton(TimeSpan.TIMELESS);

        Map<String, DataFilter> typeKeyToFilterMap = New.map();
        Collection<String> layerIds = StateUtilities.getLayerIds(queryArea, state);
        for (String layerId : layerIds)
        {
            String registryKey = new StringBuilder(state.getTitle()).append("!!").append(queryArea.getId()).append("!!")
                    .append(layerId).toString();
            DataFilter filter = myToolbox.getDataFilterRegistry().getRegisteredFilter(registryKey);
            if (filter == null)
            {
                registryKey = new StringBuilder(state.getTitle()).append("!!*!!").append(layerId).toString();
                filter = myToolbox.getDataFilterRegistry().getRegisteredFilter(registryKey);
            }
            typeKeyToFilterMap.put(layerId + "!!" + state.getTitle(), filter);
        }

        DefaultQueryRegion queryRegion = new DefaultQueryRegion(Collections.singleton(geom), validTimes, typeKeyToFilterMap);
        queryRegion.setId(queryArea.getId());
        return queryRegion;
    }

    /**
     * Get the map of area ids to query entries.
     *
     * @param queryEntriesNode The DOM node.
     * @return The map.
     * @throws JAXBException If there is an unmarshalling error.
     */
    private Map<String, Collection<QueryEntry>> getAreaIdToQueryEntryMap(Node queryEntriesNode) throws JAXBException
    {
        Map<String, Collection<QueryEntry>> areaIdToQueryEntryMap = LazyMap.create(New.map(), String.class,
                New.collectionFactory());
        if (queryEntriesNode != null)
        {
            for (QueryEntry queryEntry : XMLUtilities.readXMLObject(queryEntriesNode, QueryEntries.class).getQueryEntries())
            {
                areaIdToQueryEntryMap.get(queryEntry.getAreaId()).add(queryEntry);
            }
        }
        return areaIdToQueryEntryMap;
    }

    /**
     * Get a function that appends a state id to a data type key.
     *
     * @param id The state id.
     * @return The function.
     */
    private AppendFunction getIdTransform(final String id)
    {
        return new AppendFunction("!!" + id);
    }

    /**
     * Create the query regions corresponding to query entries in a state file
     * that apply to a particular area.
     *
     * @param id The state id.
     * @param areaIdToQueryEntryMap Map of area ids to query entries.
     * @param queriesForState The result list of query regions.
     * @param area The current query area.
     * @param times The query times.
     * @param geometries The query geometries.
     */
    private void processQueryEntries(final String id, Map<String, Collection<QueryEntry>> areaIdToQueryEntryMap,
            Collection<QueryRegion> queriesForState, QueryArea area, Collection<TimeSpan> times,
            Collection<PolygonGeometry> geometries)
    {
        Collection<Map<String, DataFilter>> filterMaps = New.collection();
        filterMaps.add(New.map());
        for (QueryEntry queryEntry : CollectionUtilities.concat(areaIdToQueryEntryMap.get("*"),
                areaIdToQueryEntryMap.get(area.getId())))
        {
            if (queryEntry.isIncludeArea())
            {
                for (String layerKey : area.getLayers())
                {
                    if ("*".equals(queryEntry.getLayerId()) || layerKey.equals(queryEntry.getLayerId()))
                    {
                        DataFilter loadFilter;
                        if ("*".equals(queryEntry.getFilterId()))
                        {
                            Collection<? extends DataFilter> filters = myToolbox.getDataFilterRegistry()
                                    .searchRegisteredFilters(Pattern.quote(id + "!!") + ".*");
                            loadFilter = null;
                            for (DataFilter filter : filters)
                            {
                                loadFilter = loadFilter == null ? filter : loadFilter.and(filter);
                            }
                        }
                        else if (queryEntry.getFilterId() == null)
                        {
                            loadFilter = null;
                        }
                        else
                        {
                            loadFilter = myToolbox.getDataFilterRegistry()
                                    .getRegisteredFilter(id + "!!" + queryEntry.getFilterId());
                        }

                        String mapKey = getIdTransform(id).apply(layerKey);

                        // Handle key collisions by creating another map.
                        Map<String, DataFilter> filterMap = filterMaps.stream().filter(m -> !m.containsKey(mapKey)).findFirst()
                                .orElse(null);
                        if (filterMap == null)
                        {
                            filterMap = New.map();
                            filterMaps.add(filterMap);
                        }

                        filterMap.put(mapKey, loadFilter);
                    }
                }
            }
        }
        filterMaps.forEach(filterMap -> queriesForState
                .add(myQueryRegionManager.addQueryRegion(CollectionUtilities.deepCopy(geometries), times, filterMap)));
    }

    /**
     * Save query entries.
     *
     * @param node The node.
     * @param queryEntries The query entries.
     * @param queryRegion The query region.
     * @param queryArea The query area.
     */
    private void saveQueryEntries(Node node, QueryEntries queryEntries, QueryRegion queryRegion, QueryArea queryArea)
    {
        Node filtersNode;
        try
        {
            filtersNode = (Node)StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:filters", node,
                    XPathConstants.NODE);
            if (filtersNode == null)
            {
                filtersNode = StateXML.createElement(node.getOwnerDocument(), "filters");
                node.appendChild(filtersNode);
            }
            Set<String> filtersSaved = New.set();
            for (Map.Entry<? extends String, ? extends DataFilter> entry : queryRegion.getTypeKeyToFilterMap().entrySet())
            {
                QueryEntry queryEntry = new QueryEntry();
                queryEntry.setIncludeArea(true);
                queryEntry.setAreaId(queryArea.getId());
                queryEntry.setLayerId(entry.getKey());
                String filterId = entry.getValue() == null ? null : Integer.toHexString(entry.getValue().hashCode());
                queryEntry.setFilterId(filterId);
                queryEntries.getQueryEntries().add(queryEntry);

                if (filterId != null && !filtersSaved.contains(filterId))
                {
                    filtersSaved.add(filterId);
                    Exporters.getExporter(Collections.singleton(entry.getValue()), myToolbox, Node.class).export(filtersNode);
                }
            }
        }
        catch (XPathExpressionException | IOException | ExportException e)
        {
            LOGGER.error("Failed to export filters for query region state: " + e, e);
        }
    }

    /**
     * Creates a KML string for the location.
     *
     * @param loc the location
     * @return the KML string
     */
    private static String toString(LatLonAlt loc)
    {
        StringBuilder sb = new StringBuilder(32).append(loc.getLonD()).append(',').append(loc.getLatD());
//        sb.append(',').append(loc.getAltM());
        return sb.toString();
    }
}
