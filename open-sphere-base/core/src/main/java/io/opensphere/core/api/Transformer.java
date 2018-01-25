package io.opensphere.core.api;

import java.util.Collection;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.messaging.GenericPublisher;

/**
 * Component that takes models as input and transforms them into
 * {@link Geometry}s.
 */
public interface Transformer extends GenericPublisher<Geometry>
{
    /**
     * Stop publishing geometries.
     */
    void close();

    /**
     * Gets the description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Begin publishing geometries.
     */
    void open();

    /**
     * Publish created geometries.
     *
     * @param adds New geometries.
     * @param removes Removed geometries.
     */
    void publishGeometries(Collection<? extends Geometry> adds, Collection<? extends Geometry> removes);
}
