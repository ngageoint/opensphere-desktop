package io.opensphere.core.model;

import java.io.Serializable;

import io.opensphere.core.units.length.Length;
import io.opensphere.core.units.length.Meters;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.lang.UnexpectedEnumException;

/**
 * A model for an altitude above a defined reference level.
 */
public final class Altitude implements Serializable
{
    /** Zero altitude with ellipsoid reference. */
    public static final Altitude ZERO_ELLIPSOID = new Altitude(0., ReferenceLevel.ELLIPSOID);

    /** Zero altitude with origin reference. */
    public static final Altitude ZERO_ORIGIN = new Altitude(0., ReferenceLevel.ORIGIN);

    /** Zero altitude with terrain reference. */
    public static final Altitude ZERO_TERRAIN = new Altitude(0., ReferenceLevel.TERRAIN);

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * The altitude in meters, referenced according to {@link #myReferenceLevel}
     * .
     */
    private final double myMeters;

    /** Reference level for the altitude. */
    private final ReferenceLevel myReferenceLevel;

    /**
     * Create using meters.
     *
     * @param altitudeMeters The altitude in meters.
     * @param referenceLevel The reference level.
     * @return The altitude instance.
     */
    public static Altitude createFromMeters(double altitudeMeters, ReferenceLevel referenceLevel)
    {
        return altitudeMeters == 0. ? getZero(referenceLevel) : new Altitude(altitudeMeters, referenceLevel);
    }

    /**
     * Get a zero altitude constant for the given reference level.
     *
     * @param referenceLevel The reference level.
     * @return The constant.
     */
    private static Altitude getZero(ReferenceLevel referenceLevel)
    {
        if (referenceLevel == ReferenceLevel.ELLIPSOID)
        {
            return ZERO_ELLIPSOID;
        }
        else if (referenceLevel == ReferenceLevel.ORIGIN)
        {
            return ZERO_ORIGIN;
        }
        else if (referenceLevel == ReferenceLevel.TERRAIN)
        {
            return ZERO_TERRAIN;
        }
        else
        {
            throw new UnexpectedEnumException(referenceLevel);
        }
    }

    /**
     * Create from a length object.
     *
     * @param magnitude The magnitude of the altitude.
     * @param referenceLevel The reference level for the altitude.
     */
    public Altitude(Length magnitude, ReferenceLevel referenceLevel)
    {
        this(magnitude.inMeters(), referenceLevel);
    }

    /**
     * Private constructor to enforce use of factory methods.
     *
     * @param altitudeMeters The magnitude of the altitude.
     * @param altitudeReference The reference level for the altitude.
     */
    private Altitude(double altitudeMeters, ReferenceLevel altitudeReference)
    {
        myMeters = altitudeMeters;
        myReferenceLevel = altitudeReference;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Altitude other = (Altitude)obj;
        return myReferenceLevel == other.myReferenceLevel && Math.abs(myMeters - other.myMeters) <= MathUtil.DBL_EPSILON;
    }

    /**
     * Get the altitude in kilometers, relative to the level given by
     * {@link #getReferenceLevel()}.
     *
     * @return The altitude in meters.
     */
    public double getKilometers()
    {
        return myMeters / Constants.UNIT_PER_KILO;
    }

    /**
     * Get the altitude, relative to the level given by
     * {@link #getReferenceLevel()}.
     *
     * @return The altitude in meters.
     */
    public Length getMagnitude()
    {
        return new Meters(myMeters);
    }

    /**
     * Get the altitude in meters, relative to the level given by
     * {@link #getReferenceLevel()}.
     *
     * @return The altitude in meters.
     */
    public double getMeters()
    {
        return myMeters;
    }

    /**
     * Get the reference level for the altitude.
     *
     * @return The reference level for the altitude.
     */
    public ReferenceLevel getReferenceLevel()
    {
        return myReferenceLevel;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(myMeters);
        result = prime * result + (int)(temp ^ temp >>> 32);
        result = prime * result + (myReferenceLevel == null ? 0 : myReferenceLevel.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [").append(getMeters()).append("m ").append(myReferenceLevel).append(']');
        return sb.toString();
    }

    /**
     * Types of altitude reference.
     */
    public enum ReferenceLevel
    {
        /** Altitude is relative to the configured ellipsoid. */
        ELLIPSOID,

        /** Altitude is relative to the center of the model. */
        ORIGIN,

        /** Altitude is relative to local elevation. */
        TERRAIN,
    }
}
