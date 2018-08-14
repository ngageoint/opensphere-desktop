package io.opensphere.core.geometry;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.messaging.GenericSubscriber;

/**
 * Facility that keeps track of a set of geometries and provides notifications
 * when geometries are added or removed.
 */
public interface GeometryRegistry extends GenericSubscriber<Geometry>
{
    /**
     * Add geometries to the registry.
     *
     * @param source The source object for the geometries, which can be used to
     *            look up the geometries.
     * @param geometries The geometries to be added.
     */
    void addGeometriesForSource(Object source, Collection<? extends Geometry> geometries);

    /**
     * Add a subscriber to be notified when geometries are added to the
     * registry.
     *
     * @param subscriber The subscriber.
     */
    void addSubscriber(GenericSubscriber<Geometry> subscriber);

    /**
     * Cancel all pending or currently processing image retrievals from
     * {@link ImageProvider}s.
     */
    void cancelAllImageRetrievals();

    /**
     * Get the data retriever executor.
     *
     * @return the dataRetrieverExecutor
     */
    ExecutorService getDataRetrieverExecutor();

    /**
     * Get all the geometries currently in the registry.
     *
     * @return The geometries.
     */
    Collection<Geometry> getGeometries();

    /**
     * Get the geometries that have been added associated with a particular
     * source, that are of a specific concrete type.
     * <p>
     * Objects that are a sub-type of the given type will <b>not</b> be
     * returned.
     *
     * @param <T> The concrete implementation of the {@code Geometry} interface.
     * @param source The source of the geometries.
     * @param type The concrete type of geometry to return.
     * @return The collection of geometries in the registry associated with the
     *         source.
     */
    <T extends Geometry> Collection<T> getGeometriesForSource(Object source, Class<T> type);

    /**
     * Get the rendering capabilities.
     *
     * @return The rendering capabilities.
     */
    RenderingCapabilities getRenderingCapabilities();

    /**
     * Remove the geometries in this registry that are associated with some data
     * models.
     *
     * @param dataModelIds The data model ids.
     * @param removed Optional collection to be populated with the removed
     *            geometries.
     */
    void removeGeometriesForDataModels(long[] dataModelIds, Collection<? super Geometry> removed);

    /**
     * Remove all the geometries currently registered with a particular source.
     *
     * @param source The source the geometries were added with.
     * @return The geometries that were removed.
     */
    Collection<Geometry> removeGeometriesForSource(Object source);

    /**
     * Remove all the geometries currently registered with a particular source
     * that are a particular concrete type. Sub-types will <b>not</b> be
     * removed.
     *
     * @param <T> The concrete type.
     *
     * @param source The source the geometries were added with.
     * @param type The specific type to be removed.
     * @return The geometries that were removed.
     */
    <T extends Geometry> Collection<T> removeGeometriesForSource(Object source, Class<T> type);

    /**
     * Remove some geometries currently registered with a particular source.
     *
     * @param source The source the geometries were added with.
     * @param geometries The geometries to be removed.
     * @return <code>true</code> if the registry changed as a result of this
     *         operation.
     */
    boolean removeGeometriesForSource(Object source, Collection<? extends Geometry> geometries);

    /**
     * Remove a subscriber.
     *
     * @param subscriber A subscriber.
     */
    void removeSubscriber(GenericSubscriber<Geometry> subscriber);

    /**
     * Set the rendering capabilities.
     *
     * @param caps The rendering capabilities.
     */
    void setRenderingCapabilities(RenderingCapabilities caps);

    /**
     * Get the geometries in this registry that are associated with some data
     * models.
     *
     * @param dataModelIds The data model ids.
     * @return The geometries.
     */
    List<Geometry> getGeometriesForDataModels(long[] dataModelIds);

    /**
     * Get the geometries in this registry that are associated with a data
     * model.
     *
     * @param dataModelId The data model id.
     * @return The geometries.
     */
    List<? extends Geometry> getGeometriesForDataModel(long dataModelId);
}
