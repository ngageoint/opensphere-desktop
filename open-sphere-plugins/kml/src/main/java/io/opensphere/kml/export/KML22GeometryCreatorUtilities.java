package io.opensphere.kml.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolygonGeometrySupport;
import io.opensphere.mantle.data.geom.MapPolylineGeometrySupport;
import io.opensphere.mantle.data.util.DataElementLocationExtractUtil;

/**
 * The helper class to KML22GeometryCreator.
 */
public final class KML22GeometryCreatorUtilities
{
    /**
     * Creates a geometry element.
     *
     * @param dataType the data type
     * @param element the data element
     * @return the geometry element
     */
    public static Geometry createGeometry(DataTypeInfo dataType, DataElement element)
    {
        Geometry geomElement = null;

        if (element instanceof MapDataElement)
        {
            MapGeometrySupport geom = ((MapDataElement)element).getMapGeometrySupport();

            if (geom instanceof MapPolylineGeometrySupport)
            {
                List<LatLonAlt> locations = ((MapPolylineGeometrySupport)geom).getLocations();
                Collection<Coordinate> coordinates = createCoordinates(locations);
                LineString lineString = new LineString();
                lineString.getCoordinates().addAll(coordinates);
                geomElement = lineString;
            }
            else if (geom instanceof MapPolygonGeometrySupport)
            {
                List<LatLonAlt> locations = ((MapPolygonGeometrySupport)geom).getLocations();
                Collection<Coordinate> coordinates = createCoordinates(locations);
                Polygon polygon = new Polygon();
                Boundary outerBoundary = new Boundary();
                LinearRing linearRing = new LinearRing();
                linearRing.getCoordinates().addAll(coordinates);
                outerBoundary.setLinearRing(linearRing);
                polygon.setOuterBoundaryIs(outerBoundary);
                geomElement = polygon;
            }
        }

        // Fall back to creating a point the crappy way
        if (geomElement == null)
        {
            String lonName = dataType.getMetaDataInfo().getLongitudeKey();
            String latName = dataType.getMetaDataInfo().getLatitudeKey();
            String altName = dataType.getMetaDataInfo().getAltitudeKey();
            LatLonAlt lla = DataElementLocationExtractUtil.getPosition(true, lonName, latName, altName, element);

            Point point = new Point();
            point.getCoordinates().add(new Coordinate(toCoordinate(lla)));
            geomElement = point;
        }
        return geomElement;
    }

    /**
     * Creates coordinate list from LatLonAlts.
     *
     * @param locations the list of locations.
     * @return the coordinate list.
     */
    private static Collection<Coordinate> createCoordinates(List<LatLonAlt> locations)
    {
        Collection<Coordinate> coordinates = new ArrayList<Coordinate>();
        for (LatLonAlt location : locations)
        {
            coordinates.add(new Coordinate(location.getLonD(), location.getLatD(), location.getAltM()));
        }
        return coordinates;
    }

    /**
     * Converts a location to KML coordinate string.
     *
     * @param l the location
     * @return the coordinate string
     */
    private static String toCoordinate(LatLonAlt l)
    {
        return new StringBuilder().append(l.getLonD()).append(',').append(l.getLatD()).append(',').append(l.getAltM()).toString();
    }

    /**
     * Private constructor.
     */
    private KML22GeometryCreatorUtilities()
    {
    }
}
