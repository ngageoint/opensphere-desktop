package io.opensphere.csvcommon.detect.location.format;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import io.opensphere.core.mgrs.MGRSConverter;
import io.opensphere.core.model.GeographicPosition;

/**
 * The Class LocationFormatter.
 */
public final class LocationFormatter
{
    /** Not constructible. */
    private LocationFormatter()
    {
    }

    /**
     * Validate wkt geometry.
     *
     * @param wktText the wkt text
     * @return true, if successful
     */
    public static boolean validateWKTGeometry(String wktText)
    {
        WKTReader wktReader = new WKTReader();
        try
        {
            Geometry g = wktReader.read(wktText);
            if (g != null)
            {
                return true;
            }
        }
        catch (com.vividsolutions.jts.io.ParseException e)
        {
            return false;
        }
        return false;
    }

    /**
     * Validate the input field for MGRS.
     *
     * @param coords the coords
     * @return True if the input is a valid MGRS string, false otherwise.
     */
    public static boolean validateMGRS(String coords)
    {
        try
        {
            MGRSConverter converter = new MGRSConverter();
            GeographicPosition geoPos = converter.convertToLatLon(coords);
            if (geoPos == null)
            {
                return false;
            }
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }

        return true;
    }
}
