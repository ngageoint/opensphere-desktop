package io.opensphere.core.pipeline.processor;

import java.util.Collection;
import java.util.Comparator;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.projection.Projection;
import io.opensphere.core.projection.ProjectionChangedEvent;

/**
 * A geometry processor is responsible for doing the coordinate transformations
 * necessary to render a geometry on the screen. The processor typically
 * delegates to a renderer to do the actual GL commands, which allows for
 * different renderers for different render methods.
 *
 * @param <E> The type of geometry handled by this processor.
 */
public interface GeometryProcessor<E extends Geometry> extends GenericSubscriber<Geometry>
{
    /**
     * Cosine of the angle between a bounding ellipsoid's z axis and the view
     * direction when the ellipsoid will be culled from the view. TODO It might
     * be worth considering modifying this value based on the size of the
     * bounding box since smaller boxes will be flatter.
     */
    double ELLIPSOID_CULL_COSINE = .25;

    /** A comparator for comparing geometries based on their rendering order. */
    Comparator<Geometry> RENDER_ORDER_COMPARATOR = (o1, o2) ->
    {
        int r1 = o1.getRenderProperties().getRenderingOrder();
        int r2 = o2.getRenderProperties().getRenderingOrder();
        return r1 < r2 ? -1 : r1 == r2 ? 0 : 1;
    };

    /**
     * Check to see whether all geometries have reached the ready state.
     *
     * @return true if all geometries are ready.
     */
    boolean allGeometriesReady();

    /**
     * Close this processor. Cancel any running jobs.
     */
    void close();

    /**
     * Get the geometries being handled by this processor.
     *
     * @return The geometries.
     */
    Collection<E> getGeometries();

    /**
     * Get the number of geometries being handled by this processor.
     *
     * @return The geometry count.
     */
    int getGeometryCount();

    /**
     * Callback for a projection changed event. Model coordinates must be
     * invalidated in any regions that the projection changed.
     *
     * @param evt The event.
     */
    void handleProjectionChanged(ProjectionChangedEvent evt);

    /**
     * Determine if this processor can handle a particular geometry type.
     *
     * @param type the geometry type
     * @return <code>True</code> if the type can be handled.
     */
    boolean handlesType(Class<? extends E> type);

    /**
     * Determine if this processor is currently handling a particular geometry.
     *
     * @param geo The geometry.
     * @return <code>True</code> if this geometry is owned by this processor.
     */
    boolean hasGeometry(Geometry geo);

    /**
     * Indicates if the processor is closed.
     *
     * @return <code>true</code> if the processor is closed.
     */
    boolean isClosed();

    /**
     * Get if this processor cares about projection changes.
     *
     * @return {@code true} if this processor cares about projection changes.
     */
    boolean sensitiveToProjectionChanges();

    /**
     * Switch to using this projection snapshot.
     *
     * @param projectionSnapshot the projection snapshot to use.
     */
    void switchToProjection(Projection projectionSnapshot);
}
