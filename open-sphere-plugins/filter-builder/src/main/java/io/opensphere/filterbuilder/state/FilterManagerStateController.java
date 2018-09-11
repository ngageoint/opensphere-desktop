package io.opensphere.filterbuilder.state;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.FiltersType;
import com.bitsys.fade.mist.state.v4.QueryAreaType;
import com.bitsys.fade.mist.state.v4.QueryAreasType;
import com.bitsys.fade.mist.state.v4.QueryEntriesType;
import com.bitsys.fade.mist.state.v4.QueryEntryType;
import com.bitsys.fade.mist.state.v4.StateType;
import com.google.common.base.Objects;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterOperators;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.datafilter.DataFilterRegistry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.modulestate.AbstractModuleStateController;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.StateUtilities;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.filterbuilder.controller.FilterBuilderController;
import io.opensphere.filterbuilder.filter.v1.Filter;
import io.opensphere.filterbuilder.impl.FilterReader;
import io.opensphere.filterbuilder.impl.WFS100FilterToDataFilterConverter;
import io.opensphere.filterbuilder.impl.WFS110FilterToDataFilterConverter;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.mdfilter.CustomBinaryLogicOpType;
import io.opensphere.mantle.data.element.mdfilter.CustomFilter;
import io.opensphere.mantle.data.element.mdfilter.CustomFilterType;
import io.opensphere.mantle.data.element.mdfilter.FilterException;
import io.opensphere.mantle.data.element.mdfilter.FilterToWFS100Converter;
import io.opensphere.mantle.data.element.mdfilter.FilterToWFS110Converter;
import io.opensphere.mantle.data.element.mdfilter.OGCFilterGenerator;
import io.opensphere.mantle.data.element.mdfilter.OGCFilterParameters;
import io.opensphere.mantle.data.element.mdfilter.OGCFilters;
import io.opensphere.mantle.plugin.queryregion.QueryRegion;
import io.opensphere.mantle.plugin.queryregion.QueryRegionManager;
import io.opensphere.mantle.util.JTSGMLUtilities;
import net.jcip.annotations.GuardedBy;
import net.opengis.gml._311.AbstractGeometryType;
import net.opengis.kml._220.BoundaryType;
import net.opengis.kml._220.LinearRingType;
import net.opengis.kml._220.PolygonType;
import net.opengis.ogc._110.BinarySpatialOpType;
import net.opengis.ogc._110.FilterType;
import net.opengis.ogc._110.LogicOpsType;
import net.opengis.ogc._110.SpatialOpsType;

/**
 * The Class FilterManagerStateController.
 */
@SuppressWarnings("PMD.GodClass")
public class FilterManagerStateController extends AbstractModuleStateController
{
    /** The spatial area ID prefix. */
    private static final String SPATIAL_PREFIX = "spatial_";

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(FilterManagerStateController.class);

    /** The Active filters. */
    private final List<Filter> myActiveFilters;

    /** The filter controller. */
    private final FilterBuilderController myController;

    /** The Data group controller. */
    private final DataGroupController myDataGroupController;

    /**
     * Used to get the spatial filters.
     */
    private final DataFilterRegistry myFilterRegistry;

    /** Map of state ids to geometries. */
    @GuardedBy("this")
    private final Map<String, Collection<Object>> myStateToFilterMap = New.map();

    /** The query region manager. */
    private final QueryRegionManager myQueryRegionManager;

    /** The geometry registry. */
    private final GeometryRegistry myGeometryRegistry;

    /**
     * Instantiates a new filter manager state manager.
     *
     * @param fbController the fb controller
     * @param dataTypeController the data type controller
     * @param filterRegistry Used to get the spatial filters.
     * @param queryRegionManager The query region manager
     * @param geometryRegistry The geometry registry
     */
    public FilterManagerStateController(FilterBuilderController fbController, DataGroupController dataTypeController,
            DataFilterRegistry filterRegistry, QueryRegionManager queryRegionManager, GeometryRegistry geometryRegistry)
    {
        myController = fbController;
        myDataGroupController = dataTypeController;
        myActiveFilters = New.list();
        myFilterRegistry = filterRegistry;
        myQueryRegionManager = queryRegionManager;
        myGeometryRegistry = geometryRegistry;
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, Node node)
    {
        try
        {
            XPath xpath = StateXML.newXPath();

            Node filtersNode = (Node)xpath.evaluate("/" + ModuleStateController.STATE_QNAME + "/:filters", node,
                    XPathConstants.NODE);
            if (filtersNode != null)
            {
                FilterReader reader = new FilterReader();
                List<CustomFilter> filterList = reader.readFilters(filtersNode);

                if (CollectionUtilities.hasContent(filterList))
                {
                    Collection<Object> filtersForState = New.collection();
                    for (CustomFilter filter : filterList)
                    {
                        Filter dataFilter = null;
                        DataTypeInfo filterDti = null;
                        if (filter instanceof CustomBinaryLogicOpType && !StringUtils.isBlank(filter.getFilterId()))
                        {
                            filterDti = getDataTypeForFilter(id, filter.getUrlKey());
                            if (filterDti == null)
                            {
                                continue;
                            }
                            dataFilter = convertFilter(id, filter, filterDti);
                            myFilterRegistry.registerFilter(id + "!!" + filter.getFilterId(), dataFilter);
                            filtersForState.add(filter);
                        }

                        if (filter.isActive())
                        {
                            if (filterDti == null)
                            {
                                filterDti = getDataTypeForFilter(id, filter.getUrlKey());
                                if (filterDti == null)
                                {
                                    continue;
                                }
                            }

                            if (filter instanceof CustomBinaryLogicOpType)
                            {
                                if (dataFilter == null)
                                {
                                    dataFilter = convertFilter(id, filter, filterDti);
                                }

                                myController.setCombinationOperator(dataFilter.getSource().getTypeKey(),
                                        DataFilterOperators.Logical.valueOf(dataFilter.getMatch().toUpperCase()));
                                myController.addFilter(dataFilter);
                                filtersForState.add(dataFilter);
                            }
                            else if (filter instanceof CustomFilterType)
                            {
                                activateSpatialFilter(filterDti.getTypeKey(), (CustomFilterType)filter, filtersForState);
                            }
                        }
                    }
                    myStateToFilterMap.put(id, filtersForState);
                }
            }
        }
        catch (JAXBException | XPathExpressionException e)
        {
            LOGGER.error("Failed to read filters state: " + e, e);
        }
    }

    @Override
    public void activateState(String id, String description, Collection<? extends String> tags, StateType state)
    {
        if (canActivateState(state))
        {
            Collection<Object> filtersForState = New.collection();

            Map<String, Filter> filterMap = New.map();

            // Activate attribute filters
            for (net.opengis.ogc._100t.FilterType filter : state.getFilters().getFilter())
            {
                Collection<String> layerIds = StateUtilities.getLayerIds(filter, state);
                for (String layerId : layerIds)
                {
                    DataTypeInfo dataType = getDataTypeForFilter(id, layerId);
                    if (dataType != null)
                    {
                        Filter dataFilter = toFilter(filter, id, dataType);
                        filterMap.put(dataFilter.getTypeKey() + "!!" + filter.getId(), dataFilter);

                        if (dataFilter.isActive())
                        {
                            myController.setCombinationOperator(dataFilter.getSource().getTypeKey(),
                                    DataFilterOperators.Logical.valueOf(dataFilter.getMatch().toUpperCase()));
                            myController.addFilter(dataFilter);
                            filtersForState.add(dataFilter);
                        }
                    }
                }
            }

            // Activate spatial filters
            if (state.getQueryAreas() != null && state.getQueryAreas().isSetQueryArea())
            {
                for (QueryAreaType queryArea : state.getQueryAreas().getQueryArea())
                {
                    Collection<String> layerIds = StateUtilities.getLayerIds(queryArea, state);
                    layerIds.removeIf(l -> l != null && l.endsWith("nrt"));
                    if (queryArea.getId() != null && queryArea.getId().startsWith(SPATIAL_PREFIX)
                            || layerIds.stream().anyMatch(l -> l != null && l.endsWith("stream")))
                    {
                        Polygon jtsPolygon = StateUtilities.toJtsGeometry(queryArea);
                        for (String layerId : layerIds)
                        {
                            String stateLayerId = layerId + "!!" + id;
                            myFilterRegistry.addSpatialLoadFilter(stateLayerId, jtsPolygon);
                            filtersForState.add(stateLayerId);
                        }

                        PolygonGeometry corePolygon = StateUtilities.toCoreGeometry(jtsPolygon);
                        myGeometryRegistry.addGeometriesForSource(this, Collections.singletonList(corePolygon));
                        filtersForState.add(corePolygon);
                    }
                }
            }

            /* Register a filter for each area/layer so that
             * QueryRegionStateController can read them and jam them in the
             * query region. */
            List<QueryEntryType> queryEntries = state.getQueryEntries() != null ? state.getQueryEntries().getQueryEntry()
                    : Collections.emptyList();
            Map<String, List<QueryEntryType>> areaToEntries = CollectionUtilities.partition(queryEntries, e -> e.getAreaId());
            for (Map.Entry<String, List<QueryEntryType>> entry : areaToEntries.entrySet())
            {
                String areaId = entry.getKey();
                Map<String, List<QueryEntryType>> layerToEntries = CollectionUtilities.partition(entry.getValue(),
                    e -> e.getLayerId());
                for (Map.Entry<String, List<QueryEntryType>> entry2 : layerToEntries.entrySet())
                {
                    String layerId = entry2.getKey();
                    List<Filter> filters = entry2.getValue().stream().filter(e -> e.getFilterId() != null)
                            .map(e -> filterMap.get(layerId + "!!" + id + "!!" + e.getFilterId())).filter(f -> f != null)
                            .collect(Collectors.toList());
                    Filter combinedFilter = combineFilters(filters);
                    if (combinedFilter != null)
                    {
                        String registryKey = new StringBuilder(state.getTitle()).append("!!").append(areaId).append("!!")
                                .append(layerId).toString();
                        myFilterRegistry.registerFilter(registryKey, combinedFilter);
                        Runnable deregisterer = () -> myFilterRegistry.deregisterFilter(registryKey);
                        filtersForState.add(deregisterer);
                    }
                }
            }

            myStateToFilterMap.put(id, filtersForState);
        }
    }

    @Override
    public boolean canActivateState(Node node)
    {
        try
        {
            return StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME + "/:filters", node,
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
        return state.getFilters() != null && state.getFilters().isSetFilter()
                || state.getQueryAreas() != null && state.getQueryAreas().isSetQueryArea();
    }

    @Override
    public boolean canSaveState()
    {
        return !getActiveFilters().isEmpty() || !myFilterRegistry.getSpatialLoadFilterKeys().isEmpty()
                || !myQueryRegionManager.getQueryRegions().isEmpty();
    }

    @Override
    public void deactivateState(String id, Node node)
    {
        Collection<Object> filters = myStateToFilterMap.remove(id);
        if (filters != null)
        {
            for (Object filter : filters)
            {
                if (filter instanceof Filter)
                {
                    myController.removeFilter((Filter)filter);
                }
                else if (filter instanceof String)
                {
                    myFilterRegistry.removeSpatialLoadFilter(filter.toString());
                }
                else if (filter instanceof CustomFilter)
                {
                    myFilterRegistry.deregisterFilter(id + "!!" + ((CustomFilter)filter).getFilterId());
                }
                else if (filter instanceof io.opensphere.core.geometry.Geometry)
                {
                    myGeometryRegistry.removeGeometriesForSource(this,
                            Collections.singletonList((io.opensphere.core.geometry.Geometry)filter));
                }
                // Not really a filter, but hey it's flexible
                else if (filter instanceof Runnable)
                {
                    ((Runnable)filter).run();
                }
            }
        }
    }

    @Override
    public void deactivateState(String id, StateType state)
    {
        deactivateState(id, (Node)null);
    }

    @Override
    public List<? extends String> getRequiredStateDependencies()
    {
        return CollectionUtilities.listView("Layers");
    }

    @Override
    public boolean isSaveStateByDefault()
    {
        return true;
    }

    @Override
    public void saveState(Node node)
    {
        List<Filter> all = myController.getAllFilters();
        OGCFilters toExport = new OGCFilters();
        for (Filter filter : all)
        {
            if (filter.isActive())
            {
                myActiveFilters.add(filter);
                try
                {
                    JAXBElement<? extends LogicOpsType> jaxbFilter = FilterToWFS110Converter.convert(filter);
                    CustomBinaryLogicOpType logicOps = new CustomBinaryLogicOpType(jaxbFilter);
                    logicOps.setId(Integer.toHexString(filter.hashCode()));
                    logicOps.setTitle(filter.getName());
                    logicOps.setActive(filter.isActive());
                    logicOps.setFromState(true);
                    logicOps.setUrlKey(filter.getSource().getTypeKey());
                    logicOps.setServerName(filter.getServerName());
                    ChoiceModel<Logical> comboOp = myController.getCombinationOperator(filter.getSource().getTypeKey());
                    logicOps.setMatch(comboOp.get().name());
                    toExport.addFilter(logicOps);
                }
                catch (FilterException e)
                {
                    LOGGER.error("Unable to create an OGCFilter", e);
                }
            }
        }

        saveSpatialFilters(toExport);

        try
        {
            JAXBContext context = JAXBContextHelper.getCachedContext(OGCFilters.class, CustomBinaryLogicOpType.class,
                    CustomFilterType.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper()
            {
                @Override
                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix)
                {
                    String prefixSuggestion = suggestion;
                    if ("http://www.opengis.net/ogc".equals(namespaceUri))
                    {
                        prefixSuggestion = "ogc";
                    }
                    else if (!requirePrefix && ModuleStateController.STATE_NAMESPACE.equals(namespaceUri))
                    {
                        prefixSuggestion = "";
                    }
                    return prefixSuggestion;
                }
            });
            marshaller.marshal(toExport, node);
            XMLUtilities.mergeDuplicateElements(node.getOwnerDocument(), "filters");
        }
        catch (JAXBException e)
        {
            LOGGER.error("Failed to marshal filters: " + e, e);
        }
    }

    @Override
    public void saveState(StateType state)
    {
        FiltersType filtersType = StateUtilities.getFilters(state);
        QueryAreasType queryAreasType = StateUtilities.getQueryAreas(state);
        QueryEntriesType queryEntriesType = StateUtilities.getQueryEntries(state);

        // Save attribute filters
        for (Filter filter : getActiveFilters())
        {
            filtersType.getFilter().add(toFilterType(filter));
            QueryEntryType queryEntry = StateUtilities.newQueryEntry(null, filter.getTypeKey(), getId(filter), filter.getMatch());
            queryEntriesType.getQueryEntry().add(queryEntry);
        }

        // Save spatial filters
        for (String spatialKey : myFilterRegistry.getSpatialLoadFilterKeys())
        {
            QueryAreaType queryArea = toQueryArea(spatialKey);
            CollectionUtilities.addIfNotContained(queryAreasType.getQueryArea(), queryArea,
                (o1, o2) -> o1.getId().equals(o2.getId()));
            QueryEntryType queryEntry = StateUtilities.newQueryEntry(queryArea.getId(), spatialKey, null, null);
            queryEntriesType.getQueryEntry().add(queryEntry);
        }

        // Save query region filters
        for (QueryRegion queryRegion : myQueryRegionManager.getQueryRegions())
        {
            String areaId = queryRegion.getId() != null ? queryRegion.getId()
                    : "area_" + Integer.toHexString(queryRegion.hashCode());
            for (Map.Entry<? extends String, ? extends DataFilter> entry : queryRegion.getTypeKeyToFilterMap().entrySet())
            {
                String layerId = entry.getKey();
                DataFilter filter = entry.getValue();

                if (filter != null)
                {
                    List<Filter> layerFilters = myController.getFilters(layerId);
                    for (DataFilterGroup subFilter : filter.getFilterGroup().getGroups())
                    {
                        Filter match = layerFilters.stream().filter(f -> f.getName().equals(subFilter.getName())).findAny()
                                .orElse(null);
                        if (match != null)
                        {
                            QueryEntryType queryEntry = StateUtilities.newQueryEntry(areaId, layerId, getId(match),
                                    match.getMatch());
                            queryEntriesType.getQueryEntry().add(queryEntry);
                        }
                    }
                }
                else
                {
                    QueryEntryType queryEntry = StateUtilities.newQueryEntry(areaId, layerId, null, null);
                    queryEntriesType.getQueryEntry().add(queryEntry);
                }
            }
        }

        // Combine query entries
        combineQueryEntries(queryEntriesType.getQueryEntry());
    }

    /**
     * Gets the active filters.
     *
     * @return the active filters
     */
    private List<Filter> getActiveFilters()
    {
        return myController.getFilters(null).stream().filter(f -> f.isActive()).collect(Collectors.toList());
    }

    /**
     * Combines filters.
     *
     * @param filters the filters
     * @return the combined filter
     */
    private Filter combineFilters(Collection<? extends Filter> filters)
    {
        Filter combined = null;
        if (filters.size() == 1)
        {
            combined = filters.iterator().next();
        }
        else if (!filters.isEmpty())
        {
            Filter firstFilter = filters.iterator().next();

            combined = new Filter(firstFilter.getTypeKey() + " Master Filter", firstFilter.getSource().copy());
            DataFilterOperators.Logical logicOp = DataFilterOperators.Logical.valueOf(firstFilter.getMatch().toUpperCase());
            combined.getFilterGroup().setLogicOperator(logicOp);
            for (Filter filter : filters)
            {
                combined.getFilterGroup().addFilterGroup(filter.getFilterGroup().clone());
            }
            combined.setFilterCount(filters.size());
        }
        return combined;
    }

    /**
     * Converts the spatial filter key to a query area.
     *
     * @param spatialKey the spatial filter key
     * @return the query area
     */
    private QueryAreaType toQueryArea(String spatialKey)
    {
        Geometry spatialFilter = myFilterRegistry.getSpatialLoadFilter(spatialKey);

        QueryAreaType queryArea = new QueryAreaType();
        queryArea.setId(SPATIAL_PREFIX + Integer.toHexString(spatialFilter.hashCode()));
        PolygonType polygon = new PolygonType();
        BoundaryType boundary = new BoundaryType();
        LinearRingType linearRing = new LinearRingType();
        for (Coordinate coord : spatialFilter.getCoordinates())
        {
            linearRing.getCoordinates().add(toString(coord));
        }
        boundary.setLinearRing(linearRing);
        polygon.setOuterBoundaryIs(boundary);
        queryArea.setPolygon(polygon);

        return queryArea;
    }

    /**
     * Creates a KML string for the coordinate.
     *
     * @param coord the coordinate
     * @return the KML string
     */
    private static String toString(Coordinate coord)
    {
        StringBuilder sb = new StringBuilder(32).append(coord.x).append(',').append(coord.y);
//        sb.append(',').append(coord.z);
        return sb.toString();
    }

    /**
     * Converts the filter to a JAXB 1.0.0 filter.
     *
     * @param filter the filter
     * @return the JAXB 1.0.0 filter
     */
    private net.opengis.ogc._100t.FilterType toFilterType(DataFilter filter)
    {
        net.opengis.ogc._100t.FilterType filterType = new net.opengis.ogc._100t.FilterType();
        filterType.setActive(filter.isActive());
        filterType.setTitle(filter.getName());
        filterType.setDescription(filter.getFilterDescription());
        filterType.setId(getId(filter));
        filterType.setFilterType("single");
        ChoiceModel<Logical> comboOp = myController.getCombinationOperator(filter.getTypeKey());
        filterType.setMatch(comboOp.get().name());
        filterType.setType(filter.getTypeKey());
        try
        {
            filterType.setLogicOps(FilterToWFS100Converter.convert(filter));
        }
        catch (FilterException e)
        {
            LOGGER.error("Unable to create an OGCFilter", e);
        }
        return filterType;
    }

    /**
     * Converts the JAXB 1.0.0 filter a filter.
     *
     * @param filterType the JAXB 1.0.0 filter
     * @param stateId the state ID
     * @param dataType the filter's data type
     * @return the filter
     */
    private Filter toFilter(net.opengis.ogc._100t.FilterType filterType, String stateId, DataTypeInfo dataType)
    {
        Filter filter = new WFS100FilterToDataFilterConverter().apply(filterType);
        filter.setName(filter.getName() + " (" + stateId + ")");
        if (dataType != null)
        {
            filter.getSource().setTypeKey(dataType.getTypeKey());
            filter.getSource().setTypeName(dataType.getTypeName());
            filter.getSource().setServerName(dataType.getSourcePrefix());
            filter.getSource().setTypeDisplayName(dataType.getDisplayName());
        }
        return filter;
    }

    /**
     * Combines query entries.
     *
     * @param queryEntries the query entries
     */
    static void combineQueryEntries(List<QueryEntryType> queryEntries)
    {
        for (int i = queryEntries.size() - 1; i >= 0; i--)
        {
            QueryEntryType entry1 = queryEntries.get(i);

            boolean didCombine = false;
            for (int j = i - 1; j >= 0; j--)
            {
                QueryEntryType entry2 = queryEntries.get(j);
                if (canCombine(entry1, entry2))
                {
                    QueryEntryType combined = combine(entry1, entry2);
                    queryEntries.set(j, combined);
                    didCombine = true;
                }
            }

            if (didCombine)
            {
                queryEntries.remove(i);
            }
        }
    }

    /**
     * Determines if two query entries can be combined.
     *
     * @param entry1 a query entry
     * @param entry2 another query entry
     * @return whether they can be combined
     */
    private static boolean canCombine(QueryEntryType entry1, QueryEntryType entry2)
    {
        return Objects.equal(entry1.getLayerId(), entry2.getLayerId())
                && (entry1.getAreaId() == null || entry2.getAreaId() == null
                        || Objects.equal(entry1.getAreaId(), entry2.getAreaId()))
                && (entry1.getFilterId() == null || entry2.getFilterId() == null
                        || Objects.equal(entry1.getFilterId(), entry2.getFilterId()));
    }

    /**
     * Combines two query entries.
     *
     * @param entry1 a query entry
     * @param entry2 another query entry
     * @return the combined entry
     */
    private static QueryEntryType combine(QueryEntryType entry1, QueryEntryType entry2)
    {
        String areaId = entry1.getAreaId() != null ? entry1.getAreaId() : entry2.getAreaId();
        String filterId;
        boolean filterGroup;
        if (entry1.getFilterId() != null)
        {
            filterId = entry1.getFilterId();
            filterGroup = entry1.isFilterGroup();
        }
        else
        {
            filterId = entry2.getFilterId();
            filterGroup = entry2.isFilterGroup();
        }
        return StateUtilities.newQueryEntry(areaId, entry1.getLayerId(), filterId, filterGroup);
    }

    /**
     * Gets the ID for the filter.
     *
     * @param filter the filter
     * @return the ID
     */
    private static String getId(DataFilter filter)
    {
        return filter != null ? Integer.toHexString(filter.hashCode()) : null;
    }

    /**
     * Convert between filter models.
     *
     * @param id The state id.
     * @param filter The input filter.
     * @param filterDti The data type associated with the filter.
     * @return The new filter.
     */
    protected Filter convertFilter(String id, CustomFilter filter, DataTypeInfo filterDti)
    {
        Filter dataFilter = WFS110FilterToDataFilterConverter.noInList().apply((CustomBinaryLogicOpType)filter);
        StringBuilder sb1 = new StringBuilder();
        sb1.append(filter.getUrlKey());
        sb1.append("!!");
        sb1.append(id);
        // Change the type key to contain the state id
        dataFilter.getSource().setTypeKey(sb1.toString());
        dataFilter.getSource().setTypeName(filterDti.getTypeName());
        dataFilter.getSource().setServerName(filterDti.getSourcePrefix());
        dataFilter.getSource().setTypeDisplayName(filterDti.getDisplayName());
        return dataFilter;
    }

    /**
     * Find the data type for a filter.
     *
     * @param stateId The state id.
     * @param layerId The layer id.
     * @return The data type.
     */
    protected DataTypeInfo getDataTypeForFilter(String stateId, String layerId)
    {
        /* Get the data type that corresponds to this filter so that some of the
         * filter parameters can be set from it. */
        DataTypeInfo filterDti = null;
        for (DataGroupInfo dgi : myDataGroupController.getActiveGroups())
        {
            for (DataTypeInfo dti : dgi.getMembers(false))
            {
                if (dti.getTypeKey().startsWith(layerId)
                        && (!dti.getTypeKey().startsWith("http") || dti.getTypeKey().endsWith(stateId)))
                {
                    filterDti = dti;
                    break;
                }
            }
        }
        return filterDti;
    }

    /**
     * Activates a geospatial filter read from the state file.
     *
     * @param typeKey The data type key the filter should be applied for.
     * @param filter The filter from the state file.
     * @param filtersForState The list to add the activated geospatial filter
     *            to, so we can deactivate the filter when state is deactivated.
     */
    private void activateSpatialFilter(String typeKey, CustomFilterType filter, Collection<Object> filtersForState)
    {
        SpatialOpsType spatialType = filter.getSpatialOps().getValue();
        if (spatialType instanceof BinarySpatialOpType)
        {
            BinarySpatialOpType binarySpatial = (BinarySpatialOpType)spatialType;
            AbstractGeometryType geometry = binarySpatial.getGeometry().getValue();
            Geometry jtsGeom = JTSGMLUtilities.convertGeometry(geometry);
            myFilterRegistry.addSpatialLoadFilter(typeKey, jtsGeom);
            filtersForState.add(typeKey);
        }
        else if (spatialType != null)
        {
            LOGGER.warn("Unrecognized spatial type " + spatialType.getClass());
        }
    }

    /**
     * Adds the any spatial filters to the passed in filters object.
     *
     * @param filters The filters object to add spatial filters to.
     */
    private void saveSpatialFilters(OGCFilters filters)
    {
        Set<String> spatialKeys = myFilterRegistry.getSpatialLoadFilterKeys();
        for (String spatialKey : spatialKeys)
        {
            Geometry geometry = myFilterRegistry.getSpatialLoadFilter(spatialKey);

            OGCFilterParameters params = new OGCFilterParameters();
            params.setGeometryTagName("GEOM");
            params.setRegion(geometry);

            FilterType filterType = OGCFilterGenerator.buildQuery(params, spatialKey);

            CustomFilterType custFilter = new CustomFilterType(filterType);
            custFilter.setActive(true);
            custFilter.setUrlKey(spatialKey);
            custFilter.setFilterType("spatial");
            custFilter.setMatch("AND");

            filters.addFilter(custFilter);
        }
    }
}
