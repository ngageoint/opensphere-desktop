package io.opensphere.core.model;

import java.io.Serializable;
import java.util.List;

import io.opensphere.core.math.Vector2d;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.units.angle.Angle;
import io.opensphere.core.units.angle.DecimalDegrees;
import io.opensphere.core.units.length.Length;

/**
 * A position on the globe.
 */
public class GeographicPosition implements Position, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The position. */
    private final LatLonAlt myPosition;

    /**
     * Find the weighted average center from a list of positions.
     *
     * @param positions The positions.
     * @return The weighted average center position.
     */
    public static GeographicPosition findCentroid(List<GeographicPosition> positions)
    {
        GeographicPosition centerPoint = null;

        if (positions == null)
        {
            return centerPoint;
        }
        if (positions.size() == 1)
        {
            return positions.get(0);
        }

        double lonAverage = 0;
        double latAverage = 0;
        int pointNum = 0;

        for (GeographicPosition geoPos : positions)
        {
            pointNum++;

            double lat = geoPos.getLatLonAlt().getLatD();
            double lon = geoPos.getLatLonAlt().getLonD();

            double lonDiff = Math.abs(lonAverage - lon);

            // If our longitudes differ by more than 180 degrees
            // then we are straddling the 180 meridian and we need
            // to make an adjustment to properly reposition the
            // centroid.
            if (lonDiff > 180.0)
            {
                if (lon < 0)
                {
                    lon += 360.0;
                }
                else
                {
                    lon -= 360.0;
                }
            }

            latAverage = latAverage * (pointNum - 1);
            lonAverage = lonAverage * (pointNum - 1);

            latAverage += lat;
            lonAverage += lon;

            latAverage = latAverage / pointNum;
            lonAverage = lonAverage / pointNum;

            // If our new adjusted average has moved across
            // the 180 meridian, shift it back to the correct
            // -180 to 180 range
            if (lonAverage > 180.0)
            {
                lonAverage = lonAverage - 360.0;
            }
            else if (lonAverage < -180.0)
            {
                lonAverage = lonAverage + 360.0;
            }
        }
        Altitude.ReferenceLevel altRef = positions.get(0).getLatLonAlt().getAltitudeReference();
        centerPoint = new GeographicPosition(LatLonAlt.createFromDegrees(latAverage, lonAverage, altRef));

        return centerPoint;
    }

    /**
     * Construct a geographic position.
     *
     * @param position The position on the globe.
     */
    public GeographicPosition(LatLonAlt position)
    {
        myPosition = position;
    }

    @Override
    public GeographicPosition add(Position pos)
    {
        if (pos instanceof GeographicPosition)
        {
            LatLonAlt lla = ((GeographicPosition)pos).getLatLonAlt();
            return new GeographicPosition(getLatLonAlt().addDegreesMeters(lla.getLatD(), lla.getLonD(), lla.getAltM()));
        }
        return null;
    }

    @Override
    public GeographicPosition add(Vector3d vec)
    {
        return new GeographicPosition(getLatLonAlt().addDegreesMeters(vec.getY(), vec.getX(), vec.getZ()));
    }

    /**
     * Get a vector constructed as [longitude, latitude, altitude] in degrees
     * and meters. Where the altitude is set to zero.
     *
     * @return The vector.
     */
    @Override
    public Vector3d asFlatVector3d()
    {
        return new Vector3d(myPosition.getLonD(), myPosition.getLatD(), 0.);
    }

    @Override
    public Vector2d asVector2d()
    {
        return new Vector2d(myPosition.getLonD(), myPosition.getLatD());
    }

    /**
     * Get a vector constructed as [longitude, latitude, altitude] in degrees
     * and meters.
     *
     * @return The vector.
     */
    @Override
    public Vector3d asVector3d()
    {
        return new Vector3d(myPosition.getLonD(), myPosition.getLatD(), myPosition.getAltM());
    }

    /**
     * Return a geographic position with the same latitude, longitude and
     * altitude, but with the provided reference level. If this position has the
     * provided reference level, this will be returned otherwise a new position
     * will be created.
     *
     * @param reference The desired reference level.
     * @return The geographic position with the provided reference level.
     */
    public GeographicPosition convertReference(ReferenceLevel reference)
    {
        if (myPosition.getAltitudeReference() == reference)
        {
            return this;
        }
        return new GeographicPosition(myPosition.convertReference(reference));
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj
                || obj != null && getClass() == obj.getClass() && myPosition.equals(((GeographicPosition)obj).getLatLonAlt());
    }

    /**
     * Get the altitude.
     *
     * @return The altitude.
     */
    public Altitude getAlt()
    {
        return getLatLonAlt().getAltitude();
    }

    /**
     * Get the geodetic latitude.
     *
     * @return The latitude.
     */
    public DecimalDegrees getLat()
    {
        return new DecimalDegrees(getLatLonAlt().getLatD());
    }

    /**
     * Get the position.
     *
     * @return the position
     */
    public LatLonAlt getLatLonAlt()
    {
        return myPosition;
    }

    /**
     * Get the longitude.
     *
     * @return The longitude.
     */
    public DecimalDegrees getLon()
    {
        return new DecimalDegrees(getLatLonAlt().getLonD());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits((float)myPosition.getAltM());
        result = prime * result + (myPosition.getAltitudeReference() == null ? 0 : myPosition.getAltitudeReference().hashCode());
        result = prime * result + Float.floatToIntBits((float)myPosition.getLatD());
        result = prime * result + Float.floatToIntBits((float)myPosition.getLonD());
        return result;
    }

    @Override
    public GeographicPosition interpolate(Position pos, double fraction)
    {
        return new GeographicPosition(getLatLonAlt().interpolate(((GeographicPosition)pos).getLatLonAlt(), fraction, false));
    }

    /**
     * Interpolate between this position and another position.
     *
     * @param pos The other position.
     * @param fraction The fraction of the distance to the other position.
     * @param longway Flag indicating if the interpolation should be done the
     *            long way around the globe.
     * @return The interpolated point.
     */
    public GeographicPosition interpolate(Position pos, double fraction, boolean longway)
    {
        return new GeographicPosition(getLatLonAlt().interpolate(((GeographicPosition)pos).getLatLonAlt(), fraction, longway));
    }

    @Override
    public Vector3d subtract(Position pos)
    {
        LatLonAlt lla = ((GeographicPosition)pos).getLatLonAlt();
        return new Vector3d(myPosition.getLatD() - lla.getLatD(), myPosition.getLonD() - lla.getLonD(),
                myPosition.getAltM() - lla.getAltM());
    }

    /**
     * Create a display label for this position using the specified units.
     *
     * @param angleUnits The angle units.
     * @param lengthUnits The length units.
     * @return The display string.
     */
    public String toDisplayString(Class<? extends Angle> angleUnits, Class<? extends Length> lengthUnits)
    {
        StringBuilder sb = new StringBuilder();
        if (angleUnits != null)
        {
            Angle lat = Angle.create(angleUnits, getLat());
            Angle lon = Angle.create(angleUnits, getLon());
            sb.append(lat.toShortLabelString(15, 6, 'N', 'S')).append(' ').append(lon.toShortLabelString(15, 6, 'E', 'W'));
        }
        if (lengthUnits != null)
        {
            if (angleUnits != null)
            {
                sb.append(' ');
            }
            Length alt = Length.create(lengthUnits, getAlt().getMagnitude());
            sb.append(alt.toShortLabelString(10, 0));
        }
        return sb.toString();
    }

    /**
     * Produce a simple string version of my data.
     *
     * @return a simple string version of my data.
     */
    public String toSimpleString()
    {
        return myPosition.toSimpleString();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(64);
        sb.append(getClass().getSimpleName()).append(" [").append(getLatLonAlt()).append(']');
        return sb.toString();
    }
}
