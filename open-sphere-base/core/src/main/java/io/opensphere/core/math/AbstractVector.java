package io.opensphere.core.math;

/**
 * Abstract base class for vectors.
 */
public abstract class AbstractVector
{
    /**
     * Calculate the distance between this vector and vec.
     *
     * @param vec the second vector to determine the distance.
     * @return the distance between the two vectors.
     */
    public double distance(AbstractVector vec)
    {
        return Math.sqrt(distanceSquared(vec));
    }

    /**
     * Calculates the distance squared between this vector and vec.
     *
     * @param vec the second vector to determine the distance squared.
     * @return the distance squared between the two vectors.
     */
    public abstract double distanceSquared(AbstractVector vec);

    /**
     * Get the length.
     *
     * @return length
     */
    public double getLength()
    {
        return Math.sqrt(getLengthSquared());
    }

    /**
     * Get length squared.
     *
     * @return length squared
     */
    public abstract double getLengthSquared();
}
