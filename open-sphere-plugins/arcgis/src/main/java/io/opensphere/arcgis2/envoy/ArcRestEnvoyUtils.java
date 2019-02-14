package io.opensphere.arcgis2.envoy;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.net.UrlBuilder;

/** ARC REST envoy utilities. */
public final class ArcRestEnvoyUtils
{
    /**
     * Builds the feature query URL.
     *
     * @param url The base URL string to query.
     * @param ids The IDs to query.
     * @return the URL
     * @throws MalformedURLException if there was a problem building the URL
     */
    public static URL buildRequestUrl(String url, long[] ids) throws MalformedURLException
    {
        UrlBuilder builder = new UrlBuilder(url + "/query");

        Map<String, String> parameters = New.map();
        parameters.put("objectIds", toString(ids));
        parameters.put("outfields", "*");
        parameters.put("f", "json");
        builder.setQueryParameters(parameters);

        return builder.toURL();
    }

    /**
     * Builds the feature query URL.
     *
     * @param url The base URL string to query.
     * @param geometry The geometry to query.
     * @param timeSpan The time span to query.
     * @param filter The optional filter to query.
     * @return the URL
     * @throws MalformedURLException if there was a problem building the URL
     */
    public static URL buildIdsUrl(String url, Geometry geometry, TimeSpan timeSpan, DataFilter filter)
        throws MalformedURLException
    {
        UrlBuilder builder = new UrlBuilder(url + "/query");

        Map<String, String> parameters = New.map();

        // Build a JSON representation of the bounding geometry
        String geom = buildPolygonString(geometry);
        if (StringUtils.isNotEmpty(geom))
        {
            parameters.put("geometryType", "esriGeometryPolygon");
            try
            {
                parameters.put("geometry", URLEncoder.encode(geom, "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new MalformedURLException("Error encoding geometry: " + e);
            }
        }

        // Add time query, if applicable
        if (!timeSpan.isTimeless())
        {
            StringBuilder requestTimes = new StringBuilder(64);
            requestTimes
                    .append(timeSpan.isUnboundedStart() ? "null" : Long.toString(convertDateToMillis(timeSpan.getStartDate())));
            requestTimes.append(',');
            requestTimes.append(timeSpan.isUnboundedEnd() ? "null" : Long.toString(convertDateToMillis(timeSpan.getEndDate())));
            parameters.put("time", requestTimes.toString());
        }

        // Add user-defined filter
        if (filter != null)
        {

            try
            {
                parameters.put("where", URLEncoder.encode(filter.getSqlLikeString().replace("*", "%"), "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                throw new MalformedURLException("Error encoding filter: " + e);
            }

        }

        // Request only IDs
        parameters.put("returnIdsOnly", "true");

        // Add the output format
        parameters.put("f", "json");

        builder.setQueryParameters(parameters);

        return builder.toURL();
    }

    /**
     * Converts the IDs into a request string.
     *
     * @param ids the IDs
     * @return the request string
     */
    private static String toString(long[] ids)
    {
        StringBuilder sb = new StringBuilder();
        for (long id : ids)
        {
            sb.append(id).append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Builds a string representation of the bounding geometry.
     *
     * @param region the bounding region to translate
     * @return the ArcGIS-specific string representation of the Geometry
     */
    private static String buildPolygonString(Geometry region)
    {
        String geomString = null;
        if (region instanceof Polygon)
        {
            geomString = buildPolygonString((Polygon)region);
        }
        else if (region instanceof MultiPolygon)
        {
            geomString = buildMultiPolygonString((MultiPolygon)region);
        }
        else
        {
            throw new UnsupportedOperationException(region.getClass() + " is not supported.");
        }

        if (StringUtils.isNotEmpty(geomString))
        {
            if (geomString.length() > 500)
            {
                // if the string is too long, it cannot be used in a GET query,
                // as it will exceed the length allowed by a URI. In these
                // cases, use a bounding box and down-select the results:
                geomString = buildPolygonString(region.getEnvelope());
            }

            StringBuilder buffer = new StringBuilder("{\"rings\":[");
            buffer.append(geomString);
            buffer.append("]}");
            geomString = buffer.toString();
        }
        return geomString;
    }

    /**
     * Builds a JSON-formatted string representing the points in each polygon
     * within the multi-polygon.
     *
     * @param region the region
     * @return the string
     */
    private static String buildMultiPolygonString(MultiPolygon region)
    {
        StringBuilder jsonMultiPolygon = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < region.getNumGeometries(); i++)
        {
            if (region.getGeometryN(i) instanceof Polygon)
            {
                Polygon currentPoly = (Polygon)region.getGeometryN(i);
                if (!first)
                {
                    jsonMultiPolygon.append(',');
                }
                else
                {
                    first = false;
                }
                jsonMultiPolygon.append(buildPolygonString(currentPoly));
            }
        }

        return jsonMultiPolygon.toString();
    }

    /**
     * Builds a JSON-formatted string representing the points in a polygon.
     *
     * @param region the region
     * @return the converted string
     */
    private static String buildPolygonString(Polygon region)
    {
        StringBuilder jsonPolygon = new StringBuilder("[");

        String exteriorString = convertPolygonCoordsToJson(region.getExteriorRing().getCoordinates());
        jsonPolygon.append(exteriorString);

        for (int i = 0; i < region.getNumInteriorRing(); i++)
        {
            String interiorString = convertPolygonCoordsToJson(region.getInteriorRingN(i).getCoordinates());
            jsonPolygon.append(',');
            jsonPolygon.append(interiorString);
        }

        jsonPolygon.append(']');
        return jsonPolygon.toString();
    }

    /**
     * Get the GMT Unix Epoch time in milliseconds.
     *
     * @param date the input {@link Date} to convert.
     * @return milliseconds since 1/1/1970 in GMT
     */
    private static long convertDateToMillis(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.ZONE_OFFSET, 0);
        cal.set(Calendar.DST_OFFSET, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Convert a set of polygon coordinates to a JSON-formatted string.
     *
     * @param poly the list of coordinates in the polygon
     * @return the JSON representation of the polygon's coordinates
     */
    private static String convertPolygonCoordsToJson(Coordinate[] poly)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Coordinate coord : poly)
        {
            if (!first)
            {
                sb.append(',');
            }
            else
            {
                first = false;
            }
            sb.append('[').append(coord.x);
            sb.append(',').append(coord.y).append(']');
        }
        // Make sure polygon is closed.
        if (!poly[0].equals2D(poly[poly.length - 1]))
        {
            sb.append("," + "[").append(poly[0].x);
            sb.append(',').append(poly[0].y).append(']');
        }
        return sb.toString();
    }

    /** Disallow instantiation. */
    private ArcRestEnvoyUtils()
    {
    }
}
