package io.opensphere.mantle.data.accessor;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.cache.accessor.GeometryAccessor;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;
import io.opensphere.mantle.data.geom.MapPathGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;

/**
 * The Class MapDataElementGeometryAccessor.
 */
public class MapDataElementGeometryAccessor extends GeometryAccessor<MapDataElement>
{
    /** The LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(MapDataElementGeometryAccessor.class);

    /** The geometry factory. */
    private final GeometryFactory myGeometryFactory;

    /**
     * Creates a {@link Geometry} that represents the simple or complex
     * composite of a {@link MapGeometrySupport}.
     *
     * @param mapGeomSupport - the {@link MapGeometrySupport} to use to create a
     *            {@link Geometry}
     * @param factory - the facory to use to create the Geometry
     * @return the resultant Geometry.
     */
    public static Geometry createGeometry(MapGeometrySupport mapGeomSupport, GeometryFactory factory)
    {
        Geometry result = null;

        // Create the geometry factory if it was not provided.
        GeometryFactory geomFactory = factory;
        if (geomFactory == null)
        {
            geomFactory = new GeometryFactory();
        }

        if (mapGeomSupport.hasChildren())
        {
            List<Geometry> geomList = new LinkedList<>();
            geomList.add(createGeometryFromTopLevelGeometrySupport(mapGeomSupport, geomFactory));
            for (MapGeometrySupport child : mapGeomSupport.getChildren())
            {
                geomList.add(createGeometry(child, geomFactory));
            }
            result = geomFactory.createGeometryCollection(geomList.toArray(new Geometry[geomList.size()]));
            return result;
        }
        else
        {
            result = createGeometryFromTopLevelGeometrySupport(mapGeomSupport, geomFactory);
        }

        return result;
    }

    /**
     * Creates the geometry from top level geometry support.
     *
     * @param mapGeomSupport the mapGeomSupport
     * @param factory the factory
     * @return the geometry
     */
    private static Geometry createGeometryFromTopLevelGeometrySupport(MapGeometrySupport mapGeomSupport, GeometryFactory factory)
    {
        Geometry result = null;
        if (mapGeomSupport instanceof MapLocationGeometrySupport)
        {
            MapLocationGeometrySupport loc = (MapLocationGeometrySupport)mapGeomSupport;
            result = factory.createPoint(
                    new Coordinate(loc.getLocation().getLonD(), loc.getLocation().getLatD(), loc.getLocation().getAltM()));

            // TODO: Need to handle the MapEllipseGeometrySupport case more
            // specially by creating the polygon
            // for the exterior.
        }
        else if (mapGeomSupport instanceof MapPathGeometrySupport)
        {
            MapPathGeometrySupport path = (MapPathGeometrySupport)mapGeomSupport;
            List<Coordinate> cdList = new LinkedList<>();
            for (LatLonAlt lla : path.getLocations())
            {
                cdList.add(new Coordinate(lla.getLonD(), lla.getLatD(), lla.getAltM()));
            }
            if (!cdList.isEmpty())
            {
                if (mapGeomSupport instanceof MapPolygonGeometrySupport)
                {
                    if (cdList.size() < 4)
                    {
                        if (cdList.size() == 1)
                        {
                            result = factory.createPoint(new Coordinate(cdList.get(0)));
                        }
                        else if (cdList.size() == 2)
                        {
                            result = factory.createLineString(cdList.toArray(new Coordinate[cdList.size()]));
                        }
                        else
                        // Assume error and that 3 points should be a polygon
                        {
                            // If the fist and last point are equal this is
                            // probably a malformed polygon, however
                            // when the first and last point are the same there
                            // is really just a line between the
                            // fist and second point.
                            if (cdList.get(0).equals(cdList.get(2)))
                            {
                                // Remove the last point and make a line string.
                                cdList.remove(2);
                                result = factory.createLineString(cdList.toArray(new Coordinate[cdList.size()]));
                            }
                            else
                            {
                                // Assume the polygon is malformed in that the
                                // last point ( which should be the same
                                // as the first, was not closed. Add the final
                                // duplicate point to close the polygon.

                                // Add first point as last to close the polygon.
                                cdList.add(cdList.get(0));
                                result = factory.createPolygon(
                                        factory.createLinearRing(cdList.toArray(new Coordinate[cdList.size()])), null);
                            }
                        }
                    }
                    else
                    {
                        result = factory.createPolygon(factory.createLinearRing(cdList.toArray(new Coordinate[cdList.size()])),
                                null);
                    }
                }
                else if (mapGeomSupport instanceof MapPolylineGeometrySupport)
                {
                    result = factory.createLineString(cdList.toArray(new Coordinate[cdList.size()]));
                }
                else
                {
                    result = factory.createMultiPoint(cdList.toArray(new Coordinate[cdList.size()])).getBoundary();
                    LOGGER.warn("The Map Path Geometry Support Type \"" + mapGeomSupport.getClass().getName()
                            + "\" is not currently supported, defaulting to envelope.");
                }
            }
        }
        else
        {
            throw new UnsupportedOperationException(
                    "The Map Geometry Support Type \"" + mapGeomSupport.getClass().getName() + "\" is not currently supported.");
        }

        return result;
    }

    /**
     * Instantiates a new map data element geometry accessor.
     *
     * @param extent the overall extent.
     */
    public MapDataElementGeometryAccessor(Geometry extent)
    {
        super(extent);
        myGeometryFactory = new GeometryFactory();
    }

    @Override
    public Geometry access(MapDataElement input)
    {
        MapGeometrySupport mapGeomSupport = input.getMapGeometrySupport();
        Geometry result = null;
        if (mapGeomSupport != null)
        {
            result = createGeometry(mapGeomSupport, myGeometryFactory);
        }
        return result;
    }
}
