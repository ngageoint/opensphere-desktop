package io.opensphere.core.pipeline.processor;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.Geometry.GeometryOrderKey;
import io.opensphere.core.geometry.LabelGeometry;
import io.opensphere.core.geometry.TileGeometry;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.model.Position;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.Utilities;

/**
 * A key for organizing geometries and processors by geometry type and the time
 * span. This key may be valid for many geometries, so this key maintains its
 * independence from any particular geometry.
 */
public class ProcessorDistributionKey implements Comparable<ProcessorDistributionKey>
{
    /** The constraint key associated with the time span. */
    private Object myConstraintKey;

    /**
     * The geometry specific portion of the key. When nothing else
     * differentiates two distribution keys, this key may be used to separate
     * like geometries.
     */
    private GeometryOrderKey myGeometryOrderKey;

    /** The type of geometry for which this key is valid. */
    private Class<? extends Geometry> myGeometryType;

    /** The position type for the geometries for which this key is valid. */
    private Class<? extends Position> myPositionType;

    /** The time over which this key is valid. */
    private TimeSpan myTimeSpan;

    /** The Z-order for the geometries for which this key is valid. */
    private int myZOrder;

    /**
     * Constructor.
     */
    public ProcessorDistributionKey()
    {
    }

    /**
     * Constructor.
     *
     * @param geom The geometry for which this key is valid.
     * @param constraintKey The constraint key.
     * @param timeSpan The time over which this key is valid.
     */
    public ProcessorDistributionKey(Geometry geom, Object constraintKey, TimeSpan timeSpan)
    {
        myGeometryType = Utilities.checkNull(geom, "geom").getClass();
        myConstraintKey = constraintKey;
        myTimeSpan = Utilities.checkNull(timeSpan, "timeSpan");
        myPositionType = geom.getPositionType();
        myZOrder = geom.getRenderProperties().getZOrder();
        myGeometryOrderKey = geom.getGeometryOrderKey();
    }

    @Override
    public int compareTo(ProcessorDistributionKey key)
    {
        int result;
        result = comparePositionType(key);

        if (result == 0)
        {
            result = compareZOrder(key);
        }

        if (result == 0)
        {
            result = compareTimeSpan(key);
        }

        if (result == 0)
        {
            result = compareGeometryType(key);
        }

        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ProcessorDistributionKey)
        {
            return compareTo((ProcessorDistributionKey)obj) == 0;
        }
        return false;
    }

    /**
     * Get the constraint key.
     *
     * @return The constraint key associated with the processors, or
     *         {@code null} if there is none.
     */
    public Object getConstraintKey()
    {
        return myConstraintKey;
    }

    /**
     * Get the geometry order key.
     *
     * @return the geometry order key
     */
    public GeometryOrderKey getGeometryOrderKey()
    {
        return myGeometryOrderKey;
    }

    /**
     * Get the geometryType.
     *
     * @return the geometryType
     */
    public Class<? extends Geometry> getGeometryType()
    {
        return myGeometryType;
    }

    /**
     * Get the positionType.
     *
     * @return the positionType
     */
    public Class<? extends Position> getPositionType()
    {
        return myPositionType;
    }

    /**
     * Get the timeSpan.
     *
     * @return the timeSpan
     */
    public TimeSpan getTimeSpan()
    {
        return myTimeSpan;
    }

    /**
     * Get the zOrder.
     *
     * @return the zOrder
     */
    public int getZOrder()
    {
        return myZOrder;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myGeometryType.hashCode();
        result = prime * result + (LabelGeometry.class.isAssignableFrom(myGeometryType) ? ScreenPosition.class.hashCode()
                : myPositionType.hashCode());
        result = prime * result + (myConstraintKey == null ? 0 : myConstraintKey.hashCode());
        result = prime * result + myTimeSpan.hashCode();
        result = prime * result + myZOrder;
        return result;
    }

    /**
     * Set the values in the key.
     *
     * @param geom The geometries for which this key is valid.
     * @param constraintKey The constraint key.
     * @param timeSpan The time over which this key is valid.
     */
    public void set(Geometry geom, Object constraintKey, TimeSpan timeSpan)
    {
        myGeometryType = geom.getClass();
        myConstraintKey = constraintKey;
        myTimeSpan = Utilities.checkNull(timeSpan, "timeSpan");
        myPositionType = geom.getPositionType();
        myZOrder = geom.getRenderProperties().getZOrder();
        myGeometryOrderKey = geom.getGeometryOrderKey();
    }

    @Override
    public String toString()
    {
        return "ProcessorDistributionKey [" + myGeometryType.getSimpleName() + ", " + myPositionType.getSimpleName() + ", "
                + myTimeSpan.toString() + ", key=" + myConstraintKey + ", Z=" + myZOrder + "]";
    }

    /**
     * Try to keep geometries of the same type together when possible.
     *
     * @param key The key to compare against myself.
     * @return The comparison result.
     */
    private int compareGeometryType(ProcessorDistributionKey key)
    {
        // If the geometry types are the same, use geometry specific key
        if (myGeometryType == key.getGeometryType())
        {
            if (myGeometryOrderKey != null && key.getGeometryOrderKey() != null)
            {
                return myGeometryOrderKey.compareTo(key.getGeometryOrderKey());
            }
            else
            {
                return 0;
            }
        }

        // Render tile geometries before other things.
        // TODO This needs to be fixed since it shouldn't apply to terrain
        // tiles.
        if (TileGeometry.class.isAssignableFrom(myGeometryType))
        {
            return -1;
        }

        if (TileGeometry.class.isAssignableFrom(key.getGeometryType()))
        {
            return 1;
        }

        // The classes are different and neither one is a TileGeometry.
        // Order by the geometry type hash codes to try to keep objects of the
        // same geometry type together.
        else
        {
            return myGeometryType.hashCode() < key.getGeometryType().hashCode() ? -1 : 1;
        }
    }

    /**
     * Compare the position types. Geographic positions come before everything
     * else.
     *
     * @param key The key to compare against myself.
     * @return The comparison result.
     */
    private int comparePositionType(ProcessorDistributionKey key)
    {
        int result;

        // TODO: this is hacked to get the label geometries to render with
        // the screen position geometries even if they use geographic
        // coordinates, since the label renderer always uses the screen position
        // viewer.
        if (myPositionType == GeographicPosition.class && !LabelGeometry.class.isAssignableFrom(getGeometryType()))
        {
            result = key.getPositionType() == GeographicPosition.class
                    && !LabelGeometry.class.isAssignableFrom(key.getGeometryType()) ? 0 : -1;
        }
        else
        {
            result = key.getPositionType() == GeographicPosition.class
                    && !LabelGeometry.class.isAssignableFrom(key.getGeometryType()) ? 1 : 0;
        }
        return result;
    }

    /**
     * Compare the time spans.
     *
     * @param key The key to compare against myself.
     * @return The comparison result.
     */
    private int compareTimeSpan(ProcessorDistributionKey key)
    {
        int result = 0;
        result = myTimeSpan.compareTo(key.myTimeSpan);

        if (result == 0)
        {
            if (myConstraintKey == null)
            {
                if (key.myConstraintKey != null)
                {
                    result = 1;
                }
            }
            else if (key.myConstraintKey == null)
            {
                result = -1;
            }
            else if (!myConstraintKey.equals(key.myConstraintKey))
            {
                result = myConstraintKey.hashCode() - key.myConstraintKey.hashCode();

                // Just in case the hash codes are coincident.
                if (result == 0)
                {
                    result = System.identityHashCode(myConstraintKey) - System.identityHashCode(key.myConstraintKey);
                }
            }
        }

        return result;
    }

    /**
     * Compare the Z orders.
     *
     * @param key The key to compare against myself.
     * @return The comparison result.
     */
    private int compareZOrder(ProcessorDistributionKey key)
    {
        int z1 = myZOrder;
        int z2 = key.getZOrder();
        return z1 < z2 ? -1 : z1 == z2 ? 0 : 1;
    }
}
