package io.opensphere.core.projection.impl;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.AbstractRectangularProjection;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.Utilities;

/**
 * A Miller projection.
 */
public class MillerProjection extends AbstractRectangularProjection
{
    /** The height of the model in model coordinates. */
    private static final double MODEL_HEIGHT = 2.;

    /** The width of the model in model coordinates. */
    private static final double MODEL_WIDTH = 4.;

    @Override
    public Vector3d convertToModel(GeographicPosition lla, Vector3d modelCenter)
    {
        Utilities.checkNull(modelCenter, "modelCenter");
        // Constants in the Miller equation.
        final double a = .625;
        final double b = .4;
        double x = lla.getLatLonAlt().getLonD() / Constants.QUARTER_CIRCLE_DEGREES;
        double latR = Math.toRadians(lla.getLatLonAlt().getLatD());
        double y = a * Math.log(Math.tan(MathUtil.QUARTER_PI + b * latR));

        return new Vector3d(x - modelCenter.getX(), y - modelCenter.getY(), 0.);
    }

    @Override
    public GeographicPosition convertToPosition(Vector3d inPos, ReferenceLevel altReference)
    {
        // Constants in the Miller equation. */
        final double a = 1.6;
        final double b = 1.25;
        double lat = Math.toDegrees(b * Math.atan(Math.sinh(a * inPos.getY())));
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
        return "Miller";
    }

    @Override
    public Vector3d getNormalAtPosition(GeographicPosition inPos)
    {
        return new Vector3d(0., 0., 1.);
    }
}
