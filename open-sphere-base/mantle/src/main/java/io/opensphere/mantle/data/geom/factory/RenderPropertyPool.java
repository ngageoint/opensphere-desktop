package io.opensphere.mantle.data.geom.factory;

import java.util.Collection;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.geometry.renderproperties.RenderProperties;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Interface RenderPropertyPool.
 */
public interface RenderPropertyPool
{
    /**
     * Adds all the unique {@link RenderProperties} owned by the provided
     * collection of {@link Geometry} to the pool. (Greedy)
     *
     * @param geomCollection the collection from which to get RenderProperties
     */
    void addAllFromGeometry(Collection<Geometry> geomCollection);

    /**
     * Adds the unique {@link RenderProperties} owned by the provided collection
     * of {@link Geometry} to the pool.
     *
     * @param geom the {@link Geometry}
     */
    void addFromGeometry(Geometry geom);

    /**
     * Clears all pooled RenderProperties.
     */
    void clearPool();

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    DataTypeInfo getDataType();

    /**
     * Checks the pool to see if an equivalent RenderProperties is already
     * within the pool. If so returns the pool instance if not adds the provided
     * property to the pool and returns the passed in property.
     *
     * @param <T> the generic type
     * @param prop the {@link RenderProperties} to add to the pool.
     * @return the pool instance of the equivalent {@link RenderProperties} or
     *         the now pooled version.
     */
    <T extends RenderProperties> T getPoolInstance(T prop);

    /**
     * Removes the specified property ( or its equivalent ) from the pool if it
     * is in the pool.
     *
     * @param <T> the generic type
     * @param prop the {@link RenderProperties} to remove
     */
    <T extends RenderProperties> void removePoolInstance(T prop);

    /**
     * Gets the number of unique properties in the pool.
     *
     * @return the int number of unique properties.
     */
    int size();

    /**
     * Values.
     *
     * @return the sets the
     */
    Set<RenderProperties> values();
}
