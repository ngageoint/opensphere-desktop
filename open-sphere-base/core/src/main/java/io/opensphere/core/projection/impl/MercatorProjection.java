package io.opensphere.core.projection.impl;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.model.TesseraList;
import io.opensphere.core.projection.AbstractRectangularProjection;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.Utilities;

/**
 * A Mercator projection.
 */
public class MercatorProjection extends AbstractRectangularProjection
{
    /**
     * Because the poles are at infinity in this projection, a limit has to be
     * set on the value of y.
     */
    private static final double MAX_VALUE = 100.;

    /** The height of the model in model coordinates. */
    private static final double MODEL_HEIGHT = 2.;

    /** The width of the model in model coordinates. */
    private static final double MODEL_WIDTH = 4.;

    @Override
    public TesseraList<GeographicProjectedTesseraVertex> convertQuadToModel(GeographicPosition vert1, GeographicPosition vert2,
            GeographicPosition vert3, GeographicPosition vert4, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        return quadTessellate(vert1, vert2, vert3, vert4, modelCenter);
    }

    @Override
    public Vector3d convertToModel(GeographicPosition lla, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        double x = lla.getLatLonAlt().getLonD() / Constants.QUARTER_CIRCLE_DEGREES;
        double latR = Math.toRadians(lla.getLatLonAlt().getLatD());
        double y = Math.log(Math.tan(latR) + 1. / Math.cos(latR)) / 2.;
        if (y > MAX_VALUE)
        {
            y = MAX_VALUE;
        }
        if (y < -MAX_VALUE)
        {
            y = -MAX_VALUE;
        }

        return new Vector3d(x - modelCenter.getX(), y - modelCenter.getY(), 0.);
    }

    @Override
    public GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference)
    {
        double lat = Math.toDegrees(Math.atan(Math.sinh(inPos.getY() * 2.)));
        double lon = inPos.getX() * Constants.QUARTER_CIRCLE_DEGREES;
        return new GeographicPosition(LatLonAlt.createFromDegreesKm(lat, lon, 0., altReference));
    }

    @Override
    public double getModelHeight()
    {
        return MODEL_HEIGHT;
    }

    @Override
    public double getModelWidth()
    {
        return MODEL_WIDTH;
    }

    @Override
    public String getName()
    {
        return "Mercator";
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        return new Vector3d(0., 0., 1.);
    }
}
