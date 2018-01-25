package io.opensphere.wps;

import java.awt.Color;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.impl.DefaultMapDataElement;
import io.opensphere.mantle.data.element.impl.MDILinkedMetaDataProvider;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPointGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;
import io.opensphere.mantle.data.geom.impl.SimpleMapPointGeometrySupport;
import io.opensphere.wps.config.v1.WpsProcessSource;
import io.opensphere.wps.response.WPSProcessResult;
import io.opensphere.wps.source.WPSFeature;
import io.opensphere.wps.util.WPSConstants;

/**
 * A helper class with which WPS Geometry objects are created for display on a
 * map.
 */
public class WpsGeometryHelper
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(WpsGeometryHelper.class);

    /**
     * A Counter that helps generate IDs for generated geometries.
     */
    private static final AtomicLong ID_COUNTER = new AtomicLong(1000000);

    /**
     * Helper method to create the meta data provider.
     *
     * @param pDataType The data type of the item to create.
     * @param pFeature The feature from which to get information
     * @param pPosition The location for the feature.
     * @return an MDILinkedMetaDataProvider generated from the supplied
     *         parameters.
     */
    public MDILinkedMetaDataProvider createMetaDataProvider(DataTypeInfo pDataType, WPSFeature pFeature, LatLonAlt pPosition)
    {
        MDILinkedMetaDataProvider mdp = new MDILinkedMetaDataProvider(pDataType.getMetaDataInfo());

        // First take care of values that we know should be there.
        mdp.setValue(WPSConstants.KEY, pFeature.getName());
        mdp.setValue(WPSConstants.TIME_FIELD, pFeature.getTimeInstant().toString());
        mdp.setValue(WPSConstants.LAT, Double.valueOf(pPosition.getLatD()));
        mdp.setValue(WPSConstants.LON, Double.valueOf(pPosition.getLonD()));

        // Now add optional properties.
        for (Entry<String, String> entry : pFeature.getProperties().entrySet())
        {
            mdp.setValue(entry.getKey(), entry.getValue());
        }

        return mdp;
    }

    /**
     * Helper method to create a new map data element.
     *
     * @param pGeometrySupport The map geometry support.
     * @param pFeature The WPS feature from which to create the map data
     *            element.
     * @param pPosition The location of the feature in Lat / Lon / Alt
     *            coordiantes.
     * @param pDataType The data type of the feature to create.
     * @param pTimeSpan The timespan to apply to the element.
     * @param pColor The color to apply to the element.
     * @return A newly created map data element.
     */
    public MapDataElement createMapDataElement(MapGeometrySupport pGeometrySupport, WPSFeature pFeature, LatLonAlt pPosition,
            DataTypeInfo pDataType, TimeSpan pTimeSpan, Color pColor)
    {
        MDILinkedMetaDataProvider dataProvider = createMetaDataProvider(pDataType, pFeature, pPosition);

        MapDataElement dataElement = new DefaultMapDataElement(ID_COUNTER.incrementAndGet(), pTimeSpan, pDataType, dataProvider,
                pGeometrySupport);
        dataElement.getVisualizationState().setColor(pColor);
        return dataElement;
    }

    /**
     * Extracts the time span from the supplied feature.
     *
     * @param pFeature the feature from which to extract the timespan.
     * @return a {@link TimeSpan} configured using the supplied
     *         {@link WPSFeature}
     */
    public TimeSpan getTimeSpan(WPSFeature pFeature)
    {
        TimeSpan ts = null;
        if (pFeature.getUpDate() != null && pFeature.getDownDate() != null
                && pFeature.getUpDate().getTime() <= pFeature.getDownDate().getTime())
        {
            ts = TimeSpan.get(pFeature.getUpDate(), pFeature.getDownDate());
        }
        else
        {
            ts = TimeSpan.get(pFeature.getTimeInstant(), pFeature.getTimeInstant());
        }
        return ts;
    }

    /**
     * Creates a single point from the supplied parameters.
     *
     * @param pFeature the feature from which to create a map data element.
     * @param pDataType the data type with which to create the map data element.
     * @param pColor the color to apply to the element.
     * @param pTimeSpan the time span to apply to the element.
     * @param pElements the container into which the generated element(s) will
     *            be placed.
     */
    public void createPoint(WPSFeature pFeature, DataTypeInfo pDataType, Color pColor, TimeSpan pTimeSpan,
            List<MapDataElement> pElements)
    {
        MapGeometrySupport support = new SimpleMapPointGeometrySupport(pFeature.getLocations().get(0));
        support.setColor(pColor, null);
        support.setTimeSpan(pTimeSpan);

        pElements.add(createMapDataElement(support, pFeature, pFeature.getLocations().get(0), pDataType, pTimeSpan, pColor));
    }

    /**
     * Creates a line from the supplied parameters.
     *
     * @param pFeature the feature from which to create a map data element.
     * @param pDataType the data type with which to create the map data element.
     * @param pColor the color to apply to the element.
     * @param pTimeSpan the time span to apply to the element.
     * @param pElements the container into which the generated element(s) will
     *            be placed.
     */
    public void createLine(WPSFeature pFeature, DataTypeInfo pDataType, Color pColor, TimeSpan pTimeSpan,
            List<MapDataElement> pElements)
    {
        // Do a check for the points being the same. If so don't bother creating
        // line segment
        if (pFeature.getLocations().get(0).equals(pFeature.getLocations().get(1)))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Line end points are the same so creating a single point instead.");
            }

            createPoint(pFeature, pDataType, pColor, pTimeSpan, pElements);
        }
        else
        {
            MapGeometrySupport pt1 = new DefaultMapPointGeometrySupport(pFeature.getLocations().get(0));
            pt1.setColor(pColor, null);
            pt1.setTimeSpan(pTimeSpan);

            MapGeometrySupport pt2 = new DefaultMapPointGeometrySupport(pFeature.getLocations().get(1));
            pt2.setColor(pColor, null);
            pt2.setTimeSpan(pTimeSpan);

            MapGeometrySupport lineSegment = new DefaultMapPolylineGeometrySupport(pFeature.getLocations());
            lineSegment.setColor(pColor, null);
            lineSegment.setTimeSpan(pTimeSpan);

            ((DefaultMapPointGeometrySupport)pt1).addChild(lineSegment);
            ((DefaultMapPointGeometrySupport)pt2).addChild(lineSegment);

            pElements.add(createMapDataElement(pt1, pFeature, pFeature.getLocations().get(0), pDataType, pTimeSpan, pColor));
            pElements.add(createMapDataElement(pt2, pFeature, pFeature.getLocations().get(1), pDataType, pTimeSpan, pColor));
        }
    }

    /**
     * Creates a polygon defined by multiple points within the feature.
     *
     * @param pFeature the feature from which to create a map data element.
     * @param pDataType the data type with which to create the map data element.
     * @param pColor the color to apply to the element.
     * @param pTimeSpan the time span to apply to the element.
     * @param pElements the container into which the generated element(s) will
     *            be placed.
     */
    public void createPolygon(WPSFeature pFeature, DataTypeInfo pDataType, Color pColor, TimeSpan pTimeSpan,
            List<MapDataElement> pElements)
    {
        MapGeometrySupport support = new DefaultMapPolygonGeometrySupport(pFeature.getLocations(), null);
        support.setColor(pColor, null);
        support.setTimeSpan(pTimeSpan);

        pElements.add(createMapDataElement(support, pFeature, pFeature.getLocations().get(0), pDataType, pTimeSpan, pColor));
    }

    /**
     * Helper method to create the collection of map data elements for this
     * feature.
     *
     * @param feature The WPS feature.
     * @param dti The data type info.
     * @param color The color.
     * @return The list of MapDataElements.
     */
    public List<MapDataElement> createGeometrySupport(WPSFeature feature, DataTypeInfo dti, Color color)
    {
        List<MapDataElement> elements = New.list();
        TimeSpan ts = getTimeSpan(feature);

        switch (feature.getGeometryType())
        {
            case LINE:
                // Line should always be two points
                if (feature.getLocations() != null && feature.getLocations().size() > 1)
                {
                    createLine(feature, dti, color, ts, elements);
                }
                break;
            case POLYGON:
                if (feature.getLocations().size() > 0)
                {
                    createPolygon(feature, dti, color, ts, elements);
                }
                break;
            case POINT:
            default:
                if (feature.getLocations().size() > 0)
                {
                    createPoint(feature, dti, color, ts, elements);
                }
                break;
        }
        return elements;
    }

    /**
     * Processes the features contained within the supplied result, and
     * constructs {@link MapDataElement}s for each.
     *
     * @param pResult the result to process.
     * @param pSource the WPS source from which the request was received.
     * @param pDataType the data type of each element.
     * @return a List of {@link MapDataElement} instances, constructed from the
     *         supplied result.
     */
    public List<MapDataElement> createMapDataElements(WPSProcessResult pResult, WpsProcessSource pSource, DataTypeInfo pDataType)
    {
        List<MapDataElement> elements = New.list();
        for (WPSFeature feature : pResult.getFeatures())
        {
            Color color;
            if (pSource.isOverrideColor())
            {
                color = pSource.getColor();
            }
            else
            {
                color = feature.getColor();
            }

            elements.addAll(createGeometrySupport(feature, pDataType, color));
        }
        return elements;
    }
}
