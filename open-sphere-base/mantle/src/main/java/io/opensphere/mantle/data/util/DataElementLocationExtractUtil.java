package io.opensphere.mantle.data.util;

import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.geom.MapGeometrySupport;
import io.opensphere.mantle.data.geom.MapLocationGeometrySupport;

/**
 * The Class DataElementLocationExtractUtil.
 */
public final class DataElementLocationExtractUtil
{
    /**
     * Gets the position.
     *
     * @param isPointData the is point data
     * @param lonName the lon name
     * @param latName the lat name
     * @param altName the alt name
     * @param dPt the d pt
     * @return the position
     */
    public static LatLonAlt getPosition(boolean isPointData, String lonName, String latName, String altName, DataElement dPt)
    {
        double lat = 0.0;
        double lon = 0.0;
        double alt = 0.0;
        double metaAlt = 0.0;

        LatLonAlt lla = null;

        // Try to get it from the geometry support
        if (isPointData && dPt instanceof MapDataElement)
        {
            MapDataElement mde = (MapDataElement)dPt;
            MapGeometrySupport mgs = mde.getMapGeometrySupport();

            if (mgs instanceof MapLocationGeometrySupport)
            {
                lla = ((MapLocationGeometrySupport)mgs).getLocation();
                lat = lla.getLatD();
                lon = lla.getLonD();
                alt = lla.getAltM();
            }
        }

        if (altName != null)
        {
            Object val = dPt.getMetaData() == null ? null : dPt.getMetaData().getValue(altName);
            metaAlt = extractDouble(val, 0.0);
        }

        if (lla == null)
        {
            if (latName != null)
            {
                Object val = dPt.getMetaData() == null ? null : dPt.getMetaData().getValue(latName);
                lat = extractDouble(val, 0.0);
            }

            if (lonName != null)
            {
                Object val = dPt.getMetaData() == null ? null : dPt.getMetaData().getValue(lonName);
                lon = extractDouble(val, 0.0);
            }

            alt = metaAlt;
        }
        else
        {
            if (alt != metaAlt && metaAlt > alt)
            {
                alt = metaAlt;
            }
        }

        return LatLonAlt.createFromDegreesMeters(lat, lon, alt, Altitude.ReferenceLevel.TERRAIN);
    }

    /**
     * Extract double.
     *
     * @param val the val
     * @param errorVal the error val
     * @return the double
     */
    private static double extractDouble(Object val, double errorVal)
    {
        double retValue = errorVal;
        if (val != null)
        {
            if (val instanceof Number)
            {
                retValue = ((Number)val).doubleValue();
            }
            else
            {
                try
                {
                    retValue = Double.parseDouble(val.toString());
                }
                catch (NumberFormatException e)
                {
                    retValue = errorVal;
                }
            }
        }
        return retValue;
    }

    /**
     * Instantiates a new data element location extract util.
     */
    private DataElementLocationExtractUtil()
    {
        // Don't allow instantiation.
    }
}
