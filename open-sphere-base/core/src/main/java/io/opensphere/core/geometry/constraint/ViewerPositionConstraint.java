package io.opensphere.core.geometry.constraint;

import net.jcip.annotations.Immutable;

import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.LatLonAlt;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * Constrains geometry visibility based on viewer position.
 */
@Immutable
public final class ViewerPositionConstraint
{
    /** The maximum viewer altitude. */
    private final Altitude myMaximumAltitude;

    /** The minimum viewer altitude. */
    private final Altitude myMinimumAltitude;

    /**
     * Construct a viewer position constraint.
     *
     * @param minAltitude The minimum viewer altitude.
     * @param maxAltitude The maximum viewer altitude.
     */
    public ViewerPositionConstraint(Altitude minAltitude, Altitude maxAltitude)
    {
        myMinimumAltitude = minAltitude;
        myMaximumAltitude = maxAltitude;
    }

    /**
     * Check this constraint against the input viewer position.
     *
     * @param position The viewer position to check (in meters).
     * @param projection A projection to use to convert to different coordinate
     *            systems.
     * @return <code>true</code> if the geometry should be visible at this
     *         position.
     */
    public boolean check(Vector3d position, Projection projection)
    {
        double viewerDistance = position.getLength();

        // Only get the lat/lon of the viewer position if necessary.
        LatLonAlt lla = null;

        if (myMinimumAltitude != null)
        {
            // Get the minimum constraint as a distance from the origin.
            double minM;
            ReferenceLevel referenceLevelMin = myMinimumAltitude.getReferenceLevel();
            if (referenceLevelMin == Altitude.ReferenceLevel.ORIGIN)
            {
                minM = myMinimumAltitude.getMeters();
            }
            else if (referenceLevelMin == Altitude.ReferenceLevel.ELLIPSOID
                    || referenceLevelMin == Altitude.ReferenceLevel.TERRAIN)
            {
                lla = projection.convertToPosition(position, referenceLevelMin).getLatLonAlt();
                GeographicPosition gp = new GeographicPosition(
                        LatLonAlt.createFromDegreesMeters(lla.getLatD(), lla.getLonD(), myMinimumAltitude));
                minM = projection.convertToModel(gp, Vector3d.ORIGIN).getLength();
            }
            else
            {
                throw new UnexpectedEnumException(referenceLevelMin);
            }

            if (minM > viewerDistance)
            {
                return false;
            }
        }

        if (myMaximumAltitude != null)
        {
            // Get the maximum constraint as a distance from the origin.
            double maxM;
            ReferenceLevel referenceLevelMax = myMaximumAltitude.getReferenceLevel();
            if (referenceLevelMax == Altitude.ReferenceLevel.ORIGIN)
            {
                maxM = myMaximumAltitude.getMeters();
            }
            else if (referenceLevelMax == Altitude.ReferenceLevel.ELLIPSOID
                    || referenceLevelMax == Altitude.ReferenceLevel.TERRAIN)
            {
                if (lla == null)
                {
                    lla = projection.convertToPosition(position, referenceLevelMax).getLatLonAlt();
                }
                GeographicPosition gp = new GeographicPosition(
                        LatLonAlt.createFromDegreesMeters(lla.getLatD(), lla.getLonD(), myMaximumAltitude));
                maxM = projection.convertToModel(gp, Vector3d.ORIGIN).getLength();
            }
            else
            {
                throw new UnexpectedEnumException(referenceLevelMax);
            }

            if (maxM < viewerDistance)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the maximum viewer altitude.
     *
     * @return The maximum viewer altitude.
     */
    public Altitude getMaximumAltitude()
    {
        return myMaximumAltitude;
    }

    /**
     * Get the minimum viewer altitude.
     *
     * @return The minimum viewer altitude.
     */
    public Altitude getMinimumAltitude()
    {
        return myMinimumAltitude;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append(getClass().getSimpleName()).append(" min[").append(myMinimumAltitude).append("] max[").append(myMaximumAltitude)
                .append(']');
        return sb.toString();
    }
}
