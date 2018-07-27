package io.opensphere.core.modulestate;

import java.awt.Color;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.FeatureActionArrayType;
import com.bitsys.fade.mist.state.v4.FiltersType;
import com.bitsys.fade.mist.state.v4.LayerType;
import com.bitsys.fade.mist.state.v4.LayersCollectionType;
import com.bitsys.fade.mist.state.v4.LayersType;
import com.bitsys.fade.mist.state.v4.QueryAreaType;
import com.bitsys.fade.mist.state.v4.QueryAreasType;
import com.bitsys.fade.mist.state.v4.QueryEntriesType;
import com.bitsys.fade.mist.state.v4.QueryEntryType;
import com.bitsys.fade.mist.state.v4.StateType;
import com.bitsys.fade.mist.state.v4.TimeType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.datafilter.DataFilterOperators;
import io.opensphere.core.geometry.PolygonGeometry;
import io.opensphere.core.geometry.renderproperties.DefaultPolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.PolygonRenderProperties;
import io.opensphere.core.geometry.renderproperties.ZOrderRenderProperties;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.core.util.jts.core.JTSCoreGeometryUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import net.opengis.ogc._100t.FilterType;

/** State utilities. */
public final class StateUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StateUtilities.class);

    /**
     * Gets the time property, creating it if necessary.
     *
     * @param state the state object
     * @return the time property
     */
    public static TimeType getTime(StateType state)
    {
        TimeType time = state.getTime();
        if (time == null)
        {
            time = new TimeType();
            state.setTime(time);
        }
        return time;
    }

    /**
     * Gets the filters property, creating it if necessary.
     *
     * @param state the state object
     * @return the filters property
     */
    public static FiltersType getFilters(StateType state)
    {
        FiltersType filters = state.getFilters();
        if (filters == null)
        {
            filters = new FiltersType();
            state.setFilters(filters);
        }
        return filters;
    }

    /**
     * Gets the query entries property, creating it if necessary.
     *
     * @param state the state object
     * @return the query entries property
     */
    public static QueryEntriesType getQueryEntries(StateType state)
    {
        QueryEntriesType queryEntries = state.getQueryEntries();
        if (queryEntries == null)
        {
            queryEntries = new QueryEntriesType();
            state.setQueryEntries(queryEntries);
        }
        return queryEntries;
    }

    /**
     * Gets the query areas property, creating it if necessary.
     *
     * @param state the state object
     * @return the query areas property
     */
    public static QueryAreasType getQueryAreas(StateType state)
    {
        QueryAreasType queryAreas = state.getQueryAreas();
        if (queryAreas == null)
        {
            queryAreas = new QueryAreasType();
            state.setQueryAreas(queryAreas);
        }
        return queryAreas;
    }

    /**
     * Gets the data layers property, creating it if necessary.
     *
     * @param state the state object
     * @return the data layers property
     */
    public static LayersType getDataLayers(StateType state)
    {
        LayersType prop = state.getDataLayers();
        if (prop == null)
        {
            prop = new LayersType();
            prop.setType(LayersCollectionType.DATA);
            state.setDataLayers(prop);
        }
        return prop;
    }

    /**
     * Gets the map layers property, creating it if necessary.
     *
     * @param state the state object
     * @return the map layers property
     */
    public static LayersType getMapLayers(StateType state)
    {
        LayersType prop = state.getMapLayers();
        if (prop == null)
        {
            prop = new LayersType();
            prop.setType(LayersCollectionType.MAP);
            state.setMapLayers(prop);
        }
        return prop;
    }

    /**
     * Gets the local data layers property, creating it if necessary.
     *
     * @param state the state object
     * @return the local data layers property
     */
    public static LayersType getLocalData(StateType state)
    {
        LayersType prop = state.getLocalData();
        if (prop == null)
        {
            prop = new LayersType();
            prop.setType(LayersCollectionType.DATA);
            state.setLocalData(prop);
        }
        return prop;
    }

    /**
     * Gets the local feature action array property, creating it if necessary.
     *
     * @param state the state object
     * @return the local feature action array property
     */
    public static FeatureActionArrayType getFeatureActions(StateType state)
    {
        FeatureActionArrayType featureActionArray = state.getFeatureActions();
        if (featureActionArray == null)
        {
            featureActionArray = new FeatureActionArrayType();
            state.setFeatureActions(featureActionArray);
        }
        return featureActionArray;
    }

    /**
     * Gets the layers that match the filter.
     *
     * @param layers the layers
     * @param type the layer type to match
     * @return the matching layers
     */
    public static List<LayerType> getLayers(LayersType layers, String type)
    {
        return getLayers(layers, layer -> type.equalsIgnoreCase(layer.getType()));
    }

    /**
     * Gets the layers that match the filter.
     *
     * @param layers1 the layers
     * @param layers2 more layers
     * @param type the layer type to match
     * @return the matching layers
     */
    public static List<LayerType> getLayers(LayersType layers1, LayersType layers2, String type)
    {
        List<LayerType> layers = New.list();
        layers.addAll(getLayers(layers1, type));
        layers.addAll(getLayers(layers2, type));
        return layers;
    }

    /**
     * Gets the layers that match the filter.
     *
     * @param layers the layers
     * @param filter the filter
     * @return the matching layers
     */
    public static List<LayerType> getLayers(LayersType layers, Predicate<? super LayerType> filter)
    {
        return layers != null ? layers.getLayer().stream().filter(filter).collect(Collectors.toList()) : Collections.emptyList();
    }

    /**
     * Converts a JAXB query area to a Core geometry.
     *
     * @param queryArea the JAXB query area
     * @return the Core geometry
     */
    public static PolygonGeometry toCoreGeometry(QueryAreaType queryArea)
    {
        Polygon jtsPolygon = toJtsGeometry(queryArea);
        return toCoreGeometry(jtsPolygon);
    }

    /**
     * Converts a JTS polygon area to a Core geometry.
     *
     * @param jtsPolygon the JTS polygon
     * @return the Core geometry
     */
    public static PolygonGeometry toCoreGeometry(Polygon jtsPolygon)
    {
        PolygonRenderProperties renderProps = new DefaultPolygonRenderProperties(ZOrderRenderProperties.TOP_Z - 1, true, true);
        renderProps.setColor(Color.CYAN);
        renderProps.setWidth(3);
//        Collection<TimeSpan> times = queryArea.getTimes();
//        if (!CollectionUtilities.hasContent(times))
//        {
//            times = Collections.singleton(TimeSpan.TIMELESS);
//            renderProps.setStipple(null);
//        }
//        else
//        {
//            renderProps.setStipple(StippleModelConfig.DOTTED);
//        }
        return JTSCoreGeometryUtilities.convertToPolygonGeometry(jtsPolygon, renderProps);
    }

    /**
     * Converts a JAXB query area to a JTS geometry.
     *
     * @param queryArea the JAXB query area
     * @return the JTS geometry
     */
    public static Polygon toJtsGeometry(QueryAreaType queryArea)
    {
        List<String> vertices = queryArea.getPolygon().getOuterBoundaryIs().getLinearRing().getCoordinates();
        int size = vertices.size();
        Coordinate[] coordinates = new Coordinate[size];
        for (int index = 0; index < size; ++index)
        {
            String vertex = vertices.get(index);
            String[] tokens = vertex.split(",");
            double x = Double.parseDouble(tokens[0]);
            double y = Double.parseDouble(tokens[1]);
            double z = tokens.length > 2 ? Double.parseDouble(tokens[2]) : Coordinate.NULL_ORDINATE;
            coordinates[index] = new Coordinate(x, y, z);
        }
        LinearRing linearRing = JTSUtilities.GEOMETRY_FACTORY.createLinearRing(coordinates);
        Polygon poly = JTSUtilities.GEOMETRY_FACTORY.createPolygon(linearRing, null);
        return poly;
    }

    /**
     * Gets the layer IDs that apply for the given filter.
     *
     * @param filter the filter
     * @param state the state object
     * @return the layer IDs
     */
    public static Collection<String> getLayerIds(FilterType filter, StateType state)
    {
        return state.getQueryEntries() == null ? Collections.emptySet() : state.getQueryEntries().getQueryEntry().stream()
                .filter(e -> filter.getId().equals(e.getFilterId())).map(e -> e.getLayerId()).collect(Collectors.toSet());
    }

    /**
     * Gets the layer IDs that apply for the given area.
     *
     * @param queryArea the query area
     * @param state the state object
     * @return the layer IDs
     */
    public static Collection<String> getLayerIds(QueryAreaType queryArea, StateType state)
    {
        return state.getQueryEntries() == null ? Collections.emptySet()
                : state.getQueryEntries().getQueryEntry().stream()
                        .filter(e -> queryArea.getId().equals(e.getAreaId()) || "*".equals(e.getAreaId()))
                        .map(e -> e.getLayerId()).collect(Collectors.toSet());
    }

    /**
     * Creates a new query entry.
     *
     * @param areaId the area ID
     * @param layerId the layer ID
     * @param filterId the filter ID
     * @param match the match
     * @return the query entry
     */
    public static QueryEntryType newQueryEntry(String areaId, String layerId, String filterId, String match)
    {
        return newQueryEntry(areaId, layerId, filterId, DataFilterOperators.Logical.AND.name().equals(match));
    }

    /**
     * Creates a new query entry.
     *
     * @param areaId the area ID
     * @param layerId the layer ID
     * @param filterId the filter ID
     * @param filterGroup whether the match is AND
     * @return the query entry
     */
    public static QueryEntryType newQueryEntry(String areaId, String layerId, String filterId, boolean filterGroup)
    {
        QueryEntryType queryEntry = new QueryEntryType();
        queryEntry.setAreaId(areaId);
        queryEntry.setLayerId(layerId);
        queryEntry.setFilterId(filterId);
        queryEntry.setIncludeArea(true);
        queryEntry.setFilterGroup(filterGroup);
        return queryEntry;
    }

    /**
     * Gets the loop span from the time object.
     *
     * @param time the time object
     * @return the loop span
     */
    public static TimeSpan getLoopSpan(TimeType time)
    {
        String span = null;
        if (time.getAnimation() != null)
        {
            span = time.getAnimation().getLoop();
        }
        if (StringUtils.isBlank(span))
        {
            span = time.getInterval();
        }
        return parseSpan(span);
    }

    /**
     * Parses a time span string into a time span.
     *
     * @param s the string
     * @return the span, or null if there was a problem
     */
    public static TimeSpan parseSpan(String s)
    {
        TimeSpan span = null;
        if (StringUtils.isNotEmpty(s))
        {
            try
            {
                span = TimeSpan.fromISO8601String(s);
            }
            catch (ParseException e)
            {
                LOGGER.warn(e);
            }
        }
        return span;
    }

    /**
     * Parses a time span string into a time span.
     *
     * @param s the string
     * @return the span, or null if there was a problem
     */
    public static Duration parseDuration(String s)
    {
        Duration duration = null;
        if (StringUtils.isNotEmpty(s))
        {
            try
            {
                duration = Duration.fromISO8601String(s);
            }
            catch (ParseException e)
            {
                LOGGER.warn(e);
            }
        }
        return duration;
    }

    /**
     * Parses a color string into a color.
     *
     * @param s the string
     * @return the color, or null if there was a problem
     */
    public static Color parseColor(String s)
    {
        Color color = null;
        if (StringUtils.isNotEmpty(s))
        {
            try
            {
                color = Color.decode(s);
            }
            catch (NumberFormatException e)
            {
                LOGGER.warn(e);
            }
        }
        return color;
    }

    /**
     * Formats a color to a string.
     *
     * @param color the color
     * @return the formatted string
     */
    public static String formatColor(Color color)
    {
        if (color == null)
        {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("0x").append(format(color.getRed())).append(format(color.getGreen())).append(format(color.getBlue()));
        return sb.toString();
    }

    /**
     * Formats a color component.
     *
     * @param colorComponent the color component
     * @return the formatted string
     */
    private static String format(int colorComponent)
    {
        return String.format("%02X", Integer.valueOf(colorComponent));
    }

    /**
     * Gets the value out of the anys.
     *
     * @param anys the anys
     * @param name the name to lookup
     * @return the value or null
     */
    public static String getValue(Collection<? extends Object> anys, String name)
    {
        String value = null;
        for (Object any : anys)
        {
            if (any instanceof Node)
            {
                Node node = (Node)any;
                if (name.equalsIgnoreCase(node.getLocalName()))
                {
                    value = node.getTextContent();
                    break;
                }
            }
            else if (any instanceof JAXBElement)
            {
                JAXBElement<?> node = (JAXBElement<?>)any;
                if (name.equalsIgnoreCase(node.getName().getLocalPart()))
                {
                    value = StringUtilities.toString(node.getValue());
                    break;
                }
            }
        }
        return value;
    }

    /** Disallow instantiation. */
    private StateUtilities()
    {
    }
}
