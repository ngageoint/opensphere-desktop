package io.opensphere.core.projection.impl;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.AbstractRectangularProjection;
import io.opensphere.core.util.Utilities;

/**
 * Simple cylindrical projection in which longitude and latitude are
 * proportional to X and Y.
 */
public class EquirectangularProjection extends AbstractRectangularProjection
{
    /** Number of degrees in one unit of model coordinate space. */
    private static final int DEGREES_PER_UNIT = 90;

    @Override
    public Vector3d convertToModel(GeographicPosition lla, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        double x = LatLonAlt.normalizeLongitude(lla.getLatLonAlt().getLonD()) / DEGREES_PER_UNIT;
        double y = LatLonAlt.normalizeLatitude(lla.getLatLonAlt().getLatD()) / DEGREES_PER_UNIT;

        return new Vector3d(x - modelCenter.getX(), y - modelCenter.getY(), 0.);
    }

    @Override
    public GeographicPosition convertToPosition(Vector3d vec, ReferenceLevel altReference)
    {
        double lon = vec.getX() * DEGREES_PER_UNIT;
        double lat = vec.getY() * DEGREES_PER_UNIT;
        return new GeographicPosition(LatLonAlt.createFromDegrees(lat, lon, altReference));
    }

    @Override
    public String getName()
    {
        return "Equirectangular";
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        return new Vector3d(0., 0., 1.);
    }
}
