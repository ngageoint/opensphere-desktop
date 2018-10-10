package io.opensphere.core.geometry;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import io.opensphere.core.util.collections.ConcurrentLazyMap;
import io.opensphere.core.util.collections.LazyMap;

/**
 * Helper class that provides common services for {@link ImageProvidingGeometry}
 * s.
 *
 * @param <E> The type provided to the geometry observer.
 */
public class ImageProvidingGeometryHelper<E>
{
    /** The observable. */
    private final E myObservable;

    /**
     * Map of geometry observers to image manager observers. This map is weak
     * and lazy. When the value for an {@link ImageProvidingGeometry.Observer}
     * is requested, a new {@link ImageManager.Observer} is created if it
     * doesn't exist. The observers are associated in the weak map such that if
     * the {@link ImageProvidingGeometry.Observer} is no longer referenced, the
     * reference to the {@link ImageManager.Observer} will also be cleared.
     */
    @SuppressWarnings("rawtypes")
    private final Map<ImageProvidingGeometry.Observer, ImageManager.Observer> myObserverMap;

    /**
     * A factory that creates {@link ImageManager.Observer}s that talk to
     * {@link ImageProvidingGeometry.Observer}s.
     */
    //@formatter:off
    @SuppressWarnings("rawtypes")
    private final LazyMap.Factory<ImageProvidingGeometry.Observer, ImageManager.Observer> myFactory =
        observer -> () -> observer.dataReady(getObservable());
    //@formatter:on

    /**
     * Constructor.
     *
     * @param observable The observable to be passed to the geometry observer.
     */
    @SuppressWarnings("rawtypes")
    public ImageProvidingGeometryHelper(E observable)
    {
        myObserverMap = ConcurrentLazyMap.create(
                Collections.synchronizedMap(new WeakHashMap<ImageProvidingGeometry.Observer, ImageManager.Observer>()),
                ImageProvidingGeometry.Observer.class, myFactory);
        myObservable = observable;
    }

    /**
     * Get an image manager observer associated with a geometry observer.
     *
     * @param observer The geometry observer.
     * @return The image manager observer.
     */
    public ImageManager.Observer getObserver(ImageProvidingGeometry.Observer<E> observer)
    {
        return myObserverMap == null ? null : myObserverMap.get(observer);
    }

    /**
     * Remove an image manager observer associated with a geometry observer.
     *
     * @param observer The geometry observer.
     * @return The image manager observer, or {@code null} if one does not
     *         exist.
     */
    public ImageManager.Observer removeObserver(ImageProvidingGeometry.Observer<E> observer)
    {
        return myObserverMap == null ? null : myObserverMap.remove(observer);
    }

    /**
     * Get the observable for the geometry observer.
     *
     * @return The observable.
     */
    protected E getObservable()
    {
        return myObservable;
    }
}
