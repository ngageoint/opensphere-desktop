package io.opensphere.core.image;

/**
 * A service that provides images and can be observed for new images.
 *
 * @param <T> The type of object to be used to look up the images.
 */
public interface ObservableImageProvider<T> extends ImageProvider<T>
{
    /**
     * Add an observer to be notified when a new image is ready.
     *
     * @param observer The observer.
     */
    void addObserver(Observer observer);

    /**
     * Interface for observers that need to know when new image date is
     * available.
     */
    @FunctionalInterface
    public interface Observer
    {
        /**
         * Called when new image data are available.
         */
        void dataReady();
    }
}
