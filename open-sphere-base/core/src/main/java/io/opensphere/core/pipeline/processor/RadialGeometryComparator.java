package io.opensphere.core.pipeline.processor;

import java.util.Comparator;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;

/**
 * Comparator that orders geometries such that their distances from a center
 * point increase.
 */
public class RadialGeometryComparator implements Comparator<Geometry>
{
    /** The point around which to order the geometries. */
    private final Vector3d myCenterPoint;

    /**
     * A projection to use to determine the model coordinates of the geometries.
     */
    private final Projection myProjection;

    /**
     * Constructor.
     *
     * @param centerPoint The point around which to order the geometries.
     * @param projection The projection to use to determine the model
     *            coordinates of the geometries.
     */
    public RadialGeometryComparator(Vector3d centerPoint, Projection projection)
    {
        myCenterPoint = centerPoint;
        myProjection = projection;
    }

    @Override
    @SuppressFBWarnings({ "CO_COMPARETO_INCORRECT_FLOATING", "For efficiency's sake, ignore NaN and -0.0" })
    public int compare(Geometry o1, Geometry o2)
    {
        // Convert to sea-level if necessary to avoid computing terrain
        // intersections.
        GeographicPosition pos1 = (GeographicPosition)o1.getReferencePoint();
        LatLonAlt lla1 = pos1.getLatLonAlt();
        if (lla1.getAltitudeReference() == Altitude.ReferenceLevel.TERRAIN)
        {
            pos1 = new GeographicPosition(
                    LatLonAlt.createFromDegrees(lla1.getLatD(), lla1.getLonD(), Altitude.ReferenceLevel.ELLIPSOID));
        }
        Vector3d model1 = myProjection.convertToModel(pos1, Vector3d.ORIGIN);

        GeographicPosition pos2 = (GeographicPosition)o2.getReferencePoint();
        LatLonAlt lla2 = pos2.getLatLonAlt();
        if (lla2.getAltitudeReference() == Altitude.ReferenceLevel.TERRAIN)
        {
            pos2 = new GeographicPosition(
                    LatLonAlt.createFromDegrees(lla2.getLatD(), lla2.getLonD(), Altitude.ReferenceLevel.ELLIPSOID));
        }
        Vector3d model2 = myProjection.convertToModel(pos2, Vector3d.ORIGIN);

        double dist1 = model1.subtract(myCenterPoint).getLength();
        double dist2 = model2.subtract(myCenterPoint).getLength();
        return dist1 < dist2 ? -1 : dist1 == dist2 ? 0 : 1;
    }
}
