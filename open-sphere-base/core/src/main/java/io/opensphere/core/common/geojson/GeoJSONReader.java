package io.opensphere.core.common.geojson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import io.opensphere.core.common.geospatial.JTSUtils;

/**
 * Currently supports Point, MultiPoint, LineString, Polygon, GeometryCollection
 * and FeatureCollection. Holes in Polygons are not supported however. This
 * implementation ignores all GeoJSON properties as well. Should I have used a
 * built-in JSON parser instead of paring all the text myself? Yes.
 */
public class GeoJSONReader extends GeoJSON
{

    /**
     * Factory used to make jts objects
     */
    private static GeometryFactory jtsFactory = JTSUtils.createDefaultGeometryFactory();

    /**
     * The <code>Log</code> instance for this class.
     */
    private static final Log LOGGER = LogFactory.getLog(GeoJSONReader.class);

    // optional +|- at beginning
    // non-optional 0 or digit
    // optional decimal point followed by any number of digits
    // or scientific notation number
    private static final String JSON_NUMBER_REGEX = "([+\\-]?" + "(?:0|[1-9]\\d*)" + "(?:\\.\\d*)?" + "(?:[eE][+\\-]?\\d+)?)";

    /**
     *
     * @param pJSON JSON document
     * @return The geometries in the document as jts {@link Geometry}s
     * @throws Exception
     */
    public static List<Geometry> unmarshal(String pJSON) throws IOException
    {
        List<Geometry> geometries = new ArrayList<>();
        // remove all white-space and ignore case for easier pattern matching
        // later
        pJSON = pJSON.replaceAll("\\s", "").toLowerCase();
        unmarshalRecurse(pJSON, geometries);
        return geometries;
    }

    /**
     * Uses recursion to unmarshal hierarchical JSON
     *
     * @param pJSON current JSON text being processing
     * @param pGeometries the resulting data structure that contains the
     *            geometries
     * @throws IOException
     */
    private static void unmarshalRecurse(String pJSON, List<Geometry> pGeometries) throws IOException
    {
        Type JSONType = determineType(pJSON);
        String[] features = new String[0];
        switch (JSONType)
        {
            case FeatureCollection:
                features = getCollection(pJSON, Type.FeatureCollection.getType().toLowerCase(), "features");
                for (int i = 0; i < features.length; i++)
                {
                    unmarshalRecurse("{" + features[i] + "}", pGeometries);
                }
                break;
            case GeometryCollection:
                features = getCollection(pJSON, Type.GeometryCollection.getType().toLowerCase(), "geometries");
                for (int i = 0; i < features.length; i++)
                {
                    unmarshalRecurse("{" + features[i] + "}", pGeometries);
                }
                break;
            case Feature:
                unmarshalRecurse(removeFeatureHeader(pJSON), pGeometries);
                break;
            case Point:
            case LineString:
            case Polygon:
            case MultiPoint:
                pGeometries.add(unmarshalGeometry(pJSON));
                break;
            default:
                throw new IOException("Unknown GeoJSON type.");
        }
    }

    /**
     * No hierarchy here, just a single geometry
     *
     * @param pJSONGeometry the JSON ex: { "type": "Point", "coordinates":
     *            [100.0, 0.0] }
     * @return the jts geometry
     * @throws IOException
     */
    private static Geometry unmarshalGeometry(String pJSONGeometry) throws IOException
    {
        Geometry geometry = null;

        Coordinate[] coordinates = createCoordinatesFromJSONGeometry(pJSONGeometry);
        Type geoType = determineType(pJSONGeometry);
        switch (geoType)
        {
            case Point:
                geometry = jtsFactory.createPoint(coordinates[0]);
                break;
            case MultiPoint:
                geometry = jtsFactory.createMultiPoint(coordinates);
                break;
            case LineString:
                geometry = jtsFactory.createLineString(coordinates);
                break;
            case Polygon:
                geometry = jtsFactory.createPolygon(jtsFactory.createLinearRing(JTSUtils.closeCoordinates(coordinates)), null);
                break;
            default:
                throw new IOException("Unknown GeoJSON type.");
        }
        return geometry;
    }

    /**
     * Get the String coordinates in a JSON geometry
     *
     * @param pJSONGeometry
     * @return
     */
    private static String coordinateStringFromJSONGeometry(String pJSONGeometry)
    {
        final String COOR_PATTERN = "\\{\"type\":\"[a-z]+\",\"coordinates\":\\[([\\[" + JSON_NUMBER_REGEX + ".,\\]]+)\\]\\}.*";
        return pJSONGeometry.replaceAll(COOR_PATTERN, "$1");
    }

    /**
     * Determines the dimensional depth of the coordinates. 1d, 2d... ?
     *
     * @param pJSONGeometry
     * @return
     * @throws IOException
     */
    private static int getGeomertyDimension(String pJSONGeometry) throws IOException
    {
        pJSONGeometry = pJSONGeometry.replaceAll("[^\\[\\]]", "");
        int singlePairCount = 0;
        int maxSinglePairCount = 0;

        // check bracket pairs
        for (int i = 0; i < pJSONGeometry.length(); i++)
        {
            if (pJSONGeometry.charAt(i) == '[')
            {
                singlePairCount++;
                if (singlePairCount > maxSinglePairCount)
                {
                    maxSinglePairCount = singlePairCount;
                }
            }
            else if (pJSONGeometry.charAt(i) == ']')
            {
                singlePairCount--;
            }
            else
            {
                throw new IOException("Not vaild GeoJSON.  Check brackets.");
            }
        }

        if (singlePairCount != 0)
        {
            throw new IOException("Not vaild GeoJSON.  Check brackets.");
        }

        return maxSinglePairCount - 1;
    }

    /**
     * Gets a jts coordinate representation of the JSON
     *
     * @param pJSONGeometry
     * @return
     * @throws IOException
     */
    private static Coordinate[] createCoordinatesFromJSONGeometry(String pJSONGeometry) throws IOException
    {
        List<Double> lonLatList = new ArrayList<>();
        String geoCoordinates = coordinateStringFromJSONGeometry(pJSONGeometry);
        int dimension = getGeomertyDimension(pJSONGeometry);
        recursiveCoordinateParser(geoCoordinates, lonLatList, dimension);

        // copy the array to a primitive data structure
        double[] lonLatArray = new double[lonLatList.size()];
        for (int i = 0; i < lonLatArray.length; i++)
        {
            lonLatArray[i] = lonLatList.get(i);
        }

        return JTSUtils.toCoordinateArray(lonLatArray);
    }

    /**
     * Will parse the coordinates in the coordinate hierarchy and store them
     * along the way
     *
     * @param pCoords
     * @param pLonLatArray
     * @param pDimension
     * @throws IOException
     */
    private static void recursiveCoordinateParser(String pCoords, List<Double> pLonLatArray, int pDimension) throws IOException
    {
        if (pDimension == 0)
        {
            String[] longlat = pCoords.split(",");
            if (longlat.length == 3)
            {
                LOGGER.warn("Only 2 values are supported for a coordinate, ignoring 3rd value.");
            }
            else if (longlat.length != 2)
            {
                throw new IOException("Not vaild GeoJSON.  A coordinate should only have two values!");
            }
            pLonLatArray.add(Double.valueOf(longlat[0]));
            pLonLatArray.add(Double.valueOf(longlat[1]));
        }
        else
        {
            StringBuilder dimRegex = new StringBuilder(",");
            for (int i = pDimension; i > 0; i--)
            {
                dimRegex.insert(0, "\\]");
                dimRegex.append("\\[");
            }
            String[] coords = pCoords.split(dimRegex.toString());
            // remove the first [ and last ]
            coords[0] = coords[0].replaceFirst("\\[", "");
            coords[coords.length - 1] = coords[coords.length - 1].substring(0, coords[coords.length - 1].length() - 1);
            pDimension--;
            for (int i = 0; i < coords.length; i++)
            {
                recursiveCoordinateParser(coords[i], pLonLatArray, pDimension);
            }
        }
    }

    /**
     * Gets the type: of the geometry
     *
     * @param pJSON
     * @return
     * @throws IOException
     */
    private static Type determineType(String pJSON) throws IOException
    {
        final String TYPE_PATTERN = "\\{\"type\":\"([a-z]+)\",.*\\}";
        String typeString = pJSON.replaceAll(TYPE_PATTERN, "$1");
        for (Type t : Type.values())
        {
            if (t.getType().toLowerCase().equals(typeString))
            {
                return t;
            }
        }
        // if we can not find the type, throw an exception.
        throw new IOException("Not vaild GeoJSON.  A coordinate should only have two values!");
    }

    /**
     * Used to get everything in a collection such as the GeometryCollection or
     * FeatureCollection
     *
     * @param pJSON
     * @param pType
     * @param pObject
     * @return
     */
    private static String[] getCollection(String pJSON, String pType, String pObject)
    {
        final String HEADER_PATTERN = "\\{\"type\":\"" + pType + "\",\"" + pObject + "\":\\[(.*)\\]\\}";
        return pJSON.replaceAll(HEADER_PATTERN, "$1").trim().replaceFirst("^\\{", "").replaceFirst("\\}$", "").split("\\},\\{");
    }

    /**
     * Removes some some header information
     *
     * @param pJSON
     * @return
     */
    private static String removeFeatureHeader(String pJSON)
    {
        final String HEADER_PATTERN = "\\{\"type\":\"" + Type.Feature.getType().toLowerCase() + "\",\"geometry\":(.*)\\}";
        return pJSON.replaceAll(HEADER_PATTERN, "$1");
    }
}
