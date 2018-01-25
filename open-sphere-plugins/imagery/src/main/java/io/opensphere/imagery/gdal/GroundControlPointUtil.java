package io.opensphere.imagery.gdal;

import java.util.List;

import io.opensphere.core.common.georeference.GroundControlPoint;
import io.opensphere.core.model.GeographicBoundingBox;
import io.opensphere.core.model.LatLonAlt;

/**
 * The Class GroundControlPointUtil.
 */
public final class GroundControlPointUtil
{
    /**
     * Return the minimum bounding sector for a list of GCPs.
     *
     * @param listOfGCPs - use these GCPs to find the minimum bounding sector
     * @return the {@link GeographicBoundingBox}
     */
    public static GeographicBoundingBox findSector(List<GroundControlPoint> listOfGCPs)
    {
        double minLat = Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLat = -Double.MAX_VALUE;
        double maxLon = -Double.MAX_VALUE;

        for (final GroundControlPoint gcp : listOfGCPs)
        {
            if (gcp.getLat() > maxLat)
            {
                maxLat = gcp.getLat();
            }
            if (gcp.getLat() < minLat)
            {
                minLat = gcp.getLat();
            }
            if (gcp.getLon() > maxLon)
            {
                maxLon = gcp.getLon();
            }
            if (gcp.getLon() < minLon)
            {
                minLon = gcp.getLon();
            }
        }

        final double delt = 0.0;
        return new GeographicBoundingBox(LatLonAlt.createFromDegrees(minLat - delt, minLon - delt),
                LatLonAlt.createFromDegrees(maxLat + delt, maxLon + delt));
    }

    /**
     * Instantiates a new ground control point util.
     */
    private GroundControlPointUtil()
    {
        // Don't allow instantiation.
    }
}
