package io.opensphere.wfs.envoy;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;

import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.cache.accessor.TimeSpanAccessor;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.jts.JTSUtilities;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.impl.AbstractLocationGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.impl.DefaultMapPolylineGeometrySupport;

/**
 * Helper class for interacting with the data registry.
 */
public final class WFSDataRegistryHelper
{
    /** Ya. */
    public static final String FILTER_PROP_KEY = "DataFilter";

    /** Property descriptor for data filter. */
    public static final PropertyDescriptor<DataFilter> DATA_FILTER_PROPERTY_DESCRIPTOR = new PropertyDescriptor<DataFilter>(
            FILTER_PROP_KEY, DataFilter.class);

    /** Property descriptor for map data elements. */
    public static final PropertyDescriptor<MapDataElement> MDE_PROPERTY_DESCRIPTOR = new PropertyDescriptor<MapDataElement>(
            "MapDataElement", MapDataElement.class);

    /**
     * Create a data registry accessor that gets geometries from map data
     * elements.
     *
     * @param geometry The bounding geometry for all the elements.
     * @return The accessor.
     */
    public static GeometryAccessor<MapDataElement> createGeometryAccessor(Geometry geometry)
    {
        GeometryAccessor<MapDataElement> geometryAccessor = new GeometryAccessor<MapDataElement>(geometry)
        {
            @Override
            public Geometry access(MapDataElement mde)
            {
                MapGeometrySupport mapGeometrySupport = mde.getMapGeometrySupport();
                // Handle Polygons
                if (mapGeometrySupport instanceof DefaultMapPolygonGeometrySupport)
                {
                    return JTSUtilities.createJTSPolygonFromLatLonAlt(
                            ((DefaultMapPolygonGeometrySupport)mapGeometrySupport).getLocations(),
                            ((DefaultMapPolygonGeometrySupport)mapGeometrySupport).getHoles());
                }
                // Handle Polylines
                else if (mapGeometrySupport instanceof DefaultMapPolylineGeometrySupport)
                {
                    List<LatLonAlt> posList = ((DefaultMapPolylineGeometrySupport)mapGeometrySupport).getLocations();
                    Coordinate[] coords = new Coordinate[posList.size()];
                    for (int i = 0; i < posList.size(); i++)
                    {
                        coords[i] = new Coordinate(posList.get(i).getLonD(), posList.get(i).getLatD());
                    }
                    CoordinateSequence seq = CoordinateArraySequenceFactory.instance().create(coords);
                    return new LineString(seq, getGeometryFactory());
                }
                // TODO: Anything else besides points?
                else
                {
                    AbstractLocationGeometrySupport algs = (AbstractLocationGeometrySupport)mapGeometrySupport;
                    LatLonAlt loc = algs.getLocation();
                    Coordinate[] coord = { new Coordinate(loc.getLonD(), loc.getLatD()) };
                    CoordinateSequence seq = CoordinateArraySequenceFactory.instance().create(coord);
                    return new Point(seq, getGeometryFactory());
                }
            }
        };
        return geometryAccessor;
    }

    /**
     * Create a data registry accessor that gets time spans from map data
     * elements.
     *
     * @param timeSpan The bounding time span for all the elements.
     * @return The accessor.
     */
    public static TimeSpanAccessor<MapDataElement> createTimeSpanAccessor(TimeSpan timeSpan)
    {
        TimeSpanAccessor<MapDataElement> timeSpanAccessor;
        if (timeSpan == TimeSpan.TIMELESS)
        {
            timeSpanAccessor = null;
        }
        else
        {
            timeSpanAccessor = new TimeSpanAccessor<MapDataElement>(timeSpan)
            {
                @Override
                public TimeSpan access(MapDataElement mde)
                {
                    return mde.getTimeSpan();
                }
            };
        }
        return timeSpanAccessor;
    }

    /** Disallow instantiation. */
    private WFSDataRegistryHelper()
    {
    }
}
