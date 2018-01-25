package io.opensphere.core.geometry;

import java.util.Comparator;

import io.opensphere.core.util.TimeBudget;

/**
 * Interface for geometries that have associated images.
 *
 * @param <E> The type provided to the geometry observer.
 */
public interface ImageProvidingGeometry<E> extends Geometry, DataRequestingGeometry
{
    /**
     * Add an observer to be notified when this geometry's image is loaded or
     * changes.
     *
     * @param observer The observer.
     */
    void addObserver(Observer<E> observer);

    /**
     * Get the image manager.
     *
     * @return The image manager.
     */
    ImageManager getImageManager();

    /**
     * Remove an observer.
     *
     * @param observer The observer.
     */
    void removeObserver(Observer<E> observer);

    /**
     * Request the image provider for the image data. If the image manager has a
     * cached image, this call has no effect.
     */
    void requestImageData();

    /**
     * Request the image provider for the image data. If the image manager has a
     * cached image, this call has no effect.
     *
     * @param comparator The comparator to use to prioritize requests.
     * @param timeBudget The time budget for retrieving an image in this thread.
     */
    void requestImageData(Comparator<? super E> comparator, TimeBudget timeBudget);

    /**
     * Indicates if this geometry has the potential of sharing the same image as
     * other geometries.
     *
     * @return True if this geometries image can possibly have the same image as
     *         other geometries being rendered. False if the image for this
     *         geometry is unique to only this geometry.
     */
    boolean sharesImage();

    /**
     * Interface for observers to be notified when a new image is available.
     *
     * @param <E> The type of geometry to be passed to the dataReady call.
     */
    @FunctionalInterface
    public interface Observer<E>
    {
        /**
         * Called when a new image is available.
         *
         * @param obj The geometry.
         */
        void dataReady(E obj);
    }
}
