package io.opensphere.server.customization;

import io.opensphere.core.model.time.TimeSpan;
import net.opengis.wfs._110.WFSCapabilitiesType;

/**
 * The Server customizations specific to GeoServer servers.
 */
public class GeoServerCustomization extends DefaultCustomization
{
    /* GeoServer likes to have the full time period specified, and it uses
     * inclusive end times, so return the "startTime/stopTime" format and
     * subtract one millisecond from the end time. */
    @Override
    public String getFormattedWMSTime(TimeSpan span)
    {
        return TimeSpan.get(span.getStart(), span.getEnd() - 1L).toISO8601String();
    }

    /* Implemented according to the GeoServer v2.0.2 documentation:
     *
     * -------------------------- Begin GeoServer Doc -------------------------
     *
     * Axis ordering
     *
     * WFS 1.0.0 servers return geographic coordinates in longitude/latitude
     * (x/y) order. This is the most common way to distribute data as well (for
     * example, most shapefiles adopt this order by default).
     *
     * However, the traditional axis order for geographic and cartographic
     * systems is latitude/longitude (y/x), the opposite and WFS 1.1.0
     * specification respects this. This can cause difficulties when switching
     * between servers with different WFS versions, or when upgrading your WFS.
     *
     * To sum up, the defaults are as follows: 1. WFS 1.1.0 request =
     * latitude/longitude 2. WMS 1.0.0 request = longitude/latitude
     *
     * GeoServer, however, in an attempt to minimize confusion and increase
     * interoperability, has adopted the following conventions when specifying
     * projections in the following formats:
     *
     * 1. EPSG:xxxx - longitude/latitude 2.
     * http://www.opengis.net/gml/srs/epsg.xml#xxxx - longitude/latitude 3.
     * urn:x-ogc:def:crs:EPSG:xxxx - latitude/longitude
     *
     * --------------------------- End GeoServer Doc --------------------------
     *
     * Developers note: GeoServer's use of EPSG:xxxx (e.g. EPSG:4326) does not
     * always behave as advertised. Recommend using the fully qualified URN per
     * #3 above. */
    @Override
    public LatLonOrder getLatLonOrder(WFSCapabilitiesType wfsCap)
    {
        // Note: WFS Capabilities is not used.
        LatLonOrder order = LatLonOrder.LONLAT;
        String srsName = getSrsName();

        // OGC standard is to use "urn:ogc" but GeoServer sometimes uses the
        // earlier proposed convention of "urn:x-ogc"
        // URN can also contain a version (e.g. urn:ogc:def:crs:6.9:EPSG:4326
        // specifies version 6.9)
        if (srsName.startsWith("urn:") && srsName.contains("ogc") && srsName.contains("crs"))
        {
            order = LatLonOrder.LATLON;
        }

        return order;
    }

    @Override
    public String getServerType()
    {
        return "GeoServer";
    }

    @Override
    public String getSrsName()
    {
        return "urn:x-ogc:def:crs:EPSG:4326";
    }
}
