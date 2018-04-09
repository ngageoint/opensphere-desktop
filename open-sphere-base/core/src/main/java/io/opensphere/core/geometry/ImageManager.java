package io.opensphere.core.geometry;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.apache.log4j.Logger;

import io.opensphere.core.image.Image;
import io.opensphere.core.image.ImageProvider;
import io.opensphere.core.image.ImmediateImageProvider;
import io.opensphere.core.image.ObservableImageProvider;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.TimeBudget;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ThreadControl;

/**
 * An image manager that handles requesting an image from an
 * {@link ImageProvider}, caching the image, and tracking dirty regions in the
 * image.
 */
@javax.annotation.concurrent.Immutable
@SuppressWarnings("PMD.GodClass")
public class ImageManager
{
    /** Atomic updater for myDirtyRegionHead. */
    private static final AtomicReferenceFieldUpdater<ImageManager, DirtyRegionNode> DIRTY_REGION_UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(ImageManager.class, DirtyRegionNode.class, "myDirtyRegionHead");

    /** Atomic updater for myImageCache. */
    private static final AtomicReferenceFieldUpdater<ImageManager, ImageGroup> IMAGE_CACHE_UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(ImageManager.class, ImageGroup.class, "myImageCache");

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ImageManager.class);

    /** The support for notifying observers on me. */
    private final ChangeSupport<Observer> myChangeSupport = new WeakChangeSupport<>();

    /**
     * The head of the list of regions which have changed in the source image.
     */
    private volatile DirtyRegionNode myDirtyRegionHead;

    /**
     * Future used to track image retrieval. This should be set to {@code null}
     * once the image data is retrieved.
     */
    private transient Future<?> myFuture;

    /**
     * A cached reference to my Image. The geometry's monitor is locked when
     * this is written, but not necessarily when it is read.
     */
    private transient volatile ImageGroup myImageCache;

    /** A key identifying this tile to the image provider. */
    private final Object myImageKey;

    /**
     * The facility that provides my image. This may be {@code null} if the
     * geometry is not drawable.
     */
    private final ImageProvider<Object> myImageProvider;

    /** My observer on the image provider. */
    private final ObservableImageProvider.Observer myImageProviderObserver = this::fetchImageData;

    /** The support for notifying request observers. */
    private final ChangeSupport<RequestObserver> myRequestChangeSupport = new StrongChangeSupport<>();

    /** The current data requester. */
    private volatile DataRequester<?> myTask;

    /**
     * Construct the image handler.
     * <p>
     * <b>Important caching consideration:</b> Two image managers that are equal
     * according to {@link #equals(Object)} can be assumed to provide the same
     * image. This means that the image may only be requested and processed
     * once, which can result in significant performance gains. This class
     * implements {@link #equals(Object)} using the {@code equals} methods of
     * its {@code imageKey} and {@code imageProvider}, so it is important to
     * implement {@code equals} in the {@code imageKey} and
     * {@code imageProvider} if appropriate.
     *
     * @param <T> The type of the image key.
     * @param imageKey The object used to request the image from the image
     *            provider.
     * @param imageProvider The image provider. Note that the image retrieved
     *            from the image provider should be disposed, so the image
     *            provider should not provide the same image more than once
     *            unless the image is not affected by calls to
     *            {@link Image#dispose()}.
     */
    @SuppressWarnings("unchecked")
    public <T> ImageManager(T imageKey, ImageProvider<? super T> imageProvider)
    {
        myImageKey = imageKey;
        myImageProvider = (ImageProvider<Object>)imageProvider;
        if (myImageProvider instanceof ObservableImageProvider)
        {
            ObservableImageProvider<?> observableImageProvider = (ObservableImageProvider<?>)myImageProvider;
            observableImageProvider.addObserver(myImageProviderObserver);
        }
    }

    /**
     * Just get the image, already. Sheesh.
     * 
     * @return the Image (already)
     */
    public Image getImage()
    {
        return myImageProvider.getImage(myImageKey);
    }

    /**
     * Add a list of regions which have changed in the tile's source image.
     * Added regions will be merged with existing regions.
     *
     * @param dirtyRegions The regions which are dirty.
     */
    public void addDirtyRegions(Collection<? extends DirtyRegion> dirtyRegions)
    {
        if (dirtyRegions != null)
        {
            DirtyRegionNode oldHead;
            DirtyRegionNode node;
            do
            {
                oldHead = myDirtyRegionHead;
                node = oldHead;
                for (DirtyRegion dr : dirtyRegions)
                {
                    node = new DirtyRegionNode(dr, node);
                }
            }
            while (!DIRTY_REGION_UPDATER.compareAndSet(this, oldHead, node));
        }
    }

    /**
     * Add an observer to be notified when a new image is available.
     *
     * @param obs The observer.
     */
    public void addObserver(Observer obs)
    {
        synchronized (myChangeSupport)
        {
            // Avoid duplicates.
            myChangeSupport.removeListener(obs);
            myChangeSupport.addListener(obs);
        }
    }

    /**
     * Add an observer to be notified of request start/stops.
     *
     * @param obs The observer.
     */
    public void addRequestObserver(RequestObserver obs)
    {
        myRequestChangeSupport.addListener(obs);
    }

    /**
     * Cancel any pending request.
     *
     * @param interrupt If the thread running the request should be interrupted.
     */
    public void cancelRequest(boolean interrupt)
    {
        synchronized (this)
        {
            if (myFuture != null)
            {
                myFuture.cancel(interrupt);
                myRequestChangeSupport.notifyListeners(listener -> listener.requestComplete());
                myFuture = null;
            }
            clearImages();
        }
    }

    /** Clear my collection of dirty regions. */
    public void clearDirtyRegions()
    {
        myDirtyRegionHead = null;
    }

    /**
     * Clear my out of date images.
     */
    public void clearImages()
    {
        ImageGroup old = IMAGE_CACHE_UPDATER.getAndSet(this, null);
        if (old != null)
        {
            old.dispose();
        }
    }

    /**
     * Copy the dirty regions from another geometry.
     *
     * @param geom The other geometry.
     */
    public void copyDirtyRegions(ImageManager geom)
    {
        myDirtyRegionHead = geom.myDirtyRegionHead;
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
        ImageManager other = (ImageManager)obj;
        return EqualsHelper.equals(myImageKey, other.myImageKey, myImageProvider, other.myImageProvider);
    }

    /**
     * Get the cached image data, or <code>null</code> if no data has been
     * cached.
     *
     * @return the image for this tile, or <code>null</code>
     */
    public ImageGroup getCachedImageData()
    {
        return myImageCache;
    }

    /**
     * Get a copy of the dirty regions without removing them.
     *
     * @return The dirty regions.
     */
    public Collection<? extends DirtyRegion> getDirtyRegions()
    {
        return getDirtyRegions(myDirtyRegionHead);
    }

    /**
     * Accessor for the image key. This key is used to identify the desired
     * image to the image provider, in case the image provider is used for
     * multiple disparate images.
     *
     * @return The image key.
     */
    public Object getImageKey()
    {
        return myImageKey;
    }

    /**
     * Get the image provider.
     *
     * @return The image provider.
     */
    public ImageProvider<? extends Object> getImageProvider()
    {
        return myImageProvider;
    }

    /**
     * Get if there are dirty regions.
     *
     * @return {@code true} if there are dirty regions.
     */
    public boolean hasDirtyRegions()
    {
        return myDirtyRegionHead != null;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myImageProvider == null ? 0 : myImageProvider.hashCode());
        result = prime * result + (myImageKey == null ? 0 : myImageKey.hashCode());
        return result;
    }

    /**
     * Clear the cached image data, return the cleared data or {@code null} if
     * no data has been cached.
     *
     * @return The cached image, or {@code null}.
     */
    public ImageGroup pollCachedImageData()
    {
        return IMAGE_CACHE_UPDATER.getAndSet(this, null);
    }

    /**
     * Get the dirty regions and clear them as well.
     *
     * @return The dirty regions.
     */
    public List<? extends DirtyRegion> pollDirtyRegions()
    {
        return getDirtyRegions(DIRTY_REGION_UPDATER.getAndSet(this, null));
    }

    /**
     * Remove an observer. If I have an image request active and the last
     * observer is removed, I will cancel the request.
     *
     * @param obs The observer.
     */
    public void removeObserver(Observer obs)
    {
        synchronized (myChangeSupport)
        {
            myChangeSupport.removeListener(obs);
            if (myChangeSupport.isEmpty())
            {
                cancelRequest(true);
            }
        }
    }

    /**
     * Remove a request observer.
     *
     * @param obs The observer.
     */
    public void removeRequestObserver(RequestObserver obs)
    {
        myRequestChangeSupport.removeListener(obs);
    }

    /**
     * Request the image data from the image provider. If there is already a
     * cached image, this has no effect.
     *
     * @param <T> The type provided to the observer.
     * @param comparator The comparator used to prioritize requests.
     * @param comparable The object to use in the comparator.
     * @param executor The executor to use when requesting data from the image
     *            provider.
     * @param timeBudget The time budget for retrieving an image in this thread.
     */
    public final <T> void requestImageData(Comparator<? super T> comparator, T comparable, ExecutorService executor,
            TimeBudget timeBudget)
    {
        Utilities.checkNull(executor, "executor");
        Utilities.checkNull(timeBudget, "timeBudget");
        requestImageFromProvider(comparator, comparable, executor, timeBudget);
    }

    /**
     * Convenience method for setting the render image data.
     *
     * @param image The image.
     */
    public void setImageData(Image image)
    {
        setImageData(new ImageGroup(Collections.singletonMap(AbstractGeometry.RenderMode.DRAW, image)));
    }

    /**
     * Set the image data.
     *
     * @param image The image.
     */
    public final void setImageData(ImageGroup image)
    {
        ImageGroup old = IMAGE_CACHE_UPDATER.getAndSet(this, image);
        if (old != null)
        {
            old.dispose();
        }
        notifyObservers();
    }

    @Override
    public String toString()
    {
        return "ImageManager [myImageKey=" + myImageKey + ", myImageProvider=" + myImageProvider + "]";
    }

    /**
     * Get the image data from the image provider, put it in the cache, and
     * notify observers. When the image is not immediately available, this
     * should be done using a {@link DataRequester}.
     */
    private void fetchImageData()
    {
        ImageGroup images;

        if (myImageProvider instanceof ImageGroupProvider)
        {
            images = ((ImageGroupProvider<Object>)myImageProvider).getImages(myImageKey);
        }
        else if (myImageProvider == null)
        {
            images = null;
        }
        else
        {
            Image image = myImageProvider.getImage(myImageKey);
            if (image == null)
            {
                images = null;
            }
            else
            {
                images = new ImageGroup(Collections.singletonMap(AbstractGeometry.RenderMode.DRAW, image));
            }
        }

        if (images != null)
        {
            // This is synchronized for the cancel case. If cancelRequest goes
            // first, it will interrupt this thread and the images will be
            // disposed below. If this thread goes first, the images will be set
            // in myImageCache and then cancelRequest will dispose them.
            boolean notify;
            synchronized (this)
            {
                if (ThreadControl.isThreadCancelled())
                {
                    images.dispose();
                    notify = false;
                }
                else
                {
                    ImageGroup old = IMAGE_CACHE_UPDATER.getAndSet(this, images);
                    if (old != null)
                    {
                        old.dispose();
                    }
                    notify = true;
                }
            }
            if (notify)
            {
                notifyObservers();
            }
        }
    }

    /**
     * Extract the dirty regions from a linked list.
     *
     * @param head The head of the linked list.
     * @return The regions.
     */
    private List<? extends DirtyRegion> getDirtyRegions(DirtyRegionNode head)
    {
        if (head == null)
        {
            return Collections.emptyList();
        }
        DirtyRegionNode node = head;
        List<DirtyRegion> regions = New.list();
        while (node != null)
        {
            DirtyRegion dr = node.getDirtyRegion();
            node = node.getNextNode();

            boolean foundOverlap = false;
            for (int index = 0; index < regions.size() && !foundOverlap; ++index)
            {
                DirtyRegion region = regions.get(index);
                if (region.overlaps(dr))
                {
                    regions.set(index, region.union(dr));
                    foundOverlap = true;
                }
            }
            if (!foundOverlap)
            {
                regions.add(dr);
            }
        }
        return regions;
    }

    /** Notify the observers that a new image is available. */
    private void notifyObservers()
    {
        myChangeSupport.notifyListeners(listener -> listener.dataReady());
    }

    /**
     * Submit a request to the provider for the image for this tile.
     *
     * @param <T> The type provided to the observer.
     * @param comparator The comparator used to prioritize requests.
     * @param comparable The object to use in the comparator.
     * @param executor The executor service to use when requesting data.
     * @param timeBudget Time budget for waiting for an image.
     */
    @SuppressWarnings("PMD.GuardLogStatement")
    private <T> void requestImageFromProvider(Comparator<? super T> comparator, T comparable, ExecutorService executor,
            TimeBudget timeBudget)
    {
        if (getCachedImageData() != null || myImageProvider == null)
        {
            return;
        }

        if (myImageProvider instanceof ImmediateImageProvider
                && ((ImmediateImageProvider<?>)myImageProvider).canProvideImageImmediately())
        {
            // FIXME: This needs to be redesigned. If two threads end up in
            // here, one will get the image but the other may not. The one that
            // doesn't get the image may return before the image is cached, and
            // then when pollCachedImageData is called, null will be returned.
            fetchImageData();
            return;
        }

        Future<?> future;
        synchronized (this)
        {
            // Don't start a request if nobody is listening anymore. This is
            // critical because of the race between this method and
            // removeObserver(). If removeObserver() gets called first, the
            // change support will be empty and then this method will return
            // here. If this method gets called first, the Future will be
            // created and then removeObserver() can cancel the Future.
            if (getCachedImageData() != null || myChangeSupport.isEmpty())
            {
                return;
            }

            if (myFuture == null)
            {
                myRequestChangeSupport.notifyListeners(listener -> listener.requestStarted());
                myTask = new DataRequester<>(comparator, comparable);
                myFuture = executor.submit(myTask);
            }
            else
            {
                // Cancel the current task if the new one has higher priority.
                DataRequester<T> task = new DataRequester<>(comparator, comparable);
                @SuppressWarnings("unchecked")
                DataRequester<T> currentTask = (DataRequester<T>)myTask;
                if (task.compareTo(currentTask) < 0 && myFuture.cancel(false))
                {
                    myTask = task;
                    myFuture = executor.submit(myTask);
                }
            }
            future = myFuture;

            // The future may have completed already; if so, clear the
            // reference.
            if (myFuture.isDone())
            {
                myFuture = null;
            }
        }

        try
        {
            long waitNanos = timeBudget.getRemainingNanoseconds();
            if (waitNanos > 0L || future.isDone())
            {
                future.get(waitNanos, TimeUnit.NANOSECONDS);
            }
        }
        catch (CancellationException e)
        {
            LOGGER.trace("Image retrieval was cancelled.", e);
        }
        catch (InterruptedException e)
        {
            LOGGER.trace("Image retrieval was interrupted.", e);
        }
        catch (ExecutionException e)
        {
            LOGGER.warn("Exception during image retrieval.", e);
        }
        catch (TimeoutException e)
        {
            LOGGER.trace("Image retrieval timed out.", e);
        }
    }

    /** Model for a dirty region in the image. */
    public static class DirtyRegion
    {
        /** The maximum X coordinate. */
        private final int myMaxX;

        /** The maximum Y coordinate. */
        private final int myMaxY;

        /** The minimum X coordinate. */
        private final int myMinX;

        /** The minimum Y coordinate. */
        private final int myMinY;

        /**
         * Constructor.
         *
         * @param minX The minimum X coordinate.
         * @param maxX The maximum X coordinate.
         * @param minY The minimum Y coordinate.
         * @param maxY The maximum Y coordinate.
         *
         */
        public DirtyRegion(int minX, int maxX, int minY, int maxY)
        {
            if (minX >= maxX)
            {
                throw new IllegalArgumentException("minX(" + minX + ") >= maxX(" + maxX + ")");
            }
            if (minY >= maxY)
            {
                throw new IllegalArgumentException("minY(" + minY + ") >= maxY(" + maxY + ")");
            }

            myMinX = minX;
            myMaxX = maxX;
            myMinY = minY;
            myMaxY = maxY;
        }

        /**
         * Get the height of the region.
         *
         * @return The height.
         */
        public int getHeight()
        {
            return myMaxY - myMinY;
        }

        /**
         * Accessor for the maxX.
         *
         * @return The maxX.
         */
        public int getMaxX()
        {
            return myMaxX;
        }

        /**
         * Accessor for the maxY.
         *
         * @return The maxY.
         */
        public int getMaxY()
        {
            return myMaxY;
        }

        /**
         * Accessor for the minX.
         *
         * @return The minX.
         */
        public int getMinX()
        {
            return myMinX;
        }

        /**
         * Accessor for the minY.
         *
         * @return The minY.
         */
        public int getMinY()
        {
            return myMinY;
        }

        /**
         * Get the width of the region.
         *
         * @return The width.
         */
        public int getWidth()
        {
            return myMaxX - myMinX;
        }

        /**
         * Determine if this region overlaps another.
         *
         * @param other The other region.
         * @return {@code true} if the regions overlap.
         */
        public boolean overlaps(DirtyRegion other)
        {
            return other.myMaxX > myMinX && other.myMinX < myMaxX && other.myMaxY > myMinY && other.myMinY < myMaxY;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("[(").append(myMinX).append(", ").append(myMinY).append("), (");
            builder.append(myMaxX).append(", ").append(myMaxY).append(")]");

            return builder.toString();
        }

        /**
         * Get the simple union of this region and another.
         *
         * @param other The other region.
         * @return The union.
         */
        public DirtyRegion union(DirtyRegion other)
        {
            return new DirtyRegion(Math.min(myMinX, other.myMinX), Math.max(myMaxX, other.myMaxX), Math.min(myMinY, other.myMinY),
                    Math.max(myMaxY, other.myMaxY));
        }
    }

    /**
     * Interface for observers to be notified when a new image is available.
     */
    @FunctionalInterface
    public interface Observer
    {
        /**
         * Called when a new image is available.
         */
        void dataReady();
    }

    /**
     * Interface for observers to be notified when an image is requested and
     * when it is received.
     */
    public interface RequestObserver
    {
        /** Called when the request is complete. */
        void requestComplete();

        /** Called when an image is requested. */
        void requestStarted();
    }

    /**
     * A {@link Callable} that requests the data from my image provider.
     *
     * @param <T> The type provided to the comparator.
     */
    private final class DataRequester<T> implements Runnable, Comparable<DataRequester<T>>
    {
        /** The object to use in comparisons. */
        private final T myComparable;

        /** The comparator. */
        private final Comparator<? super T> myComparator;

        /**
         * Constructor.
         *
         * @param comparator The comparator that prioritizes the data requests.
         * @param comparable The object to use in the comparator.
         */
        public DataRequester(Comparator<? super T> comparator, T comparable)
        {
            myComparable = comparable;
            myComparator = comparator;
        }

        @Override
        public int compareTo(DataRequester<T> o)
        {
            if (myComparator == null)
            {
                if (o.myComparator == null)
                {
                    return 0;
                }
                else
                {
                    return o.myComparator.compare(myComparable, o.myComparable);
                }
            }
            else
            {
                return myComparator.compare(myComparable, o.myComparable);
            }
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public boolean equals(Object obj)
        {
            return super.equals(obj);
        }

        @Override
        @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
        public int hashCode()
        {
            return super.hashCode();
        }

        @Override
        public void run()
        {
            try
            {
                fetchImageData();
            }
            catch (RuntimeException e)
            {
                LOGGER.warn("Runtime exception thrown from fetchImageData", e);
                throw e;
            }
            finally
            {
                synchronized (ImageManager.this)
                {
                    if (myFuture != null)
                    {
                        myRequestChangeSupport.notifyListeners(listener -> listener.requestComplete());
                    }
                    myFuture = null;
                }
            }
        }
    }

    /** A node in the dirty region linked list. */
    private static class DirtyRegionNode
    {
        /** The region for this node. */
        private final DirtyRegion myDirtyRegion;

        /** The next node. */
        private final DirtyRegionNode myNextNode;

        /**
         * Constructor.
         *
         * @param dirtyRegion The dirty region for this node.
         * @param nextNode The next node in the linked list.
         */
        public DirtyRegionNode(DirtyRegion dirtyRegion, DirtyRegionNode nextNode)
        {
            myDirtyRegion = dirtyRegion;
            myNextNode = nextNode;
        }

        /**
         * Accessor for the dirtyRegion.
         *
         * @return The dirtyRegion.
         */
        public DirtyRegion getDirtyRegion()
        {
            return myDirtyRegion;
        }

        /**
         * Accessor for the nextNode.
         *
         * @return The nextNode.
         */
        public DirtyRegionNode getNextNode()
        {
            return myNextNode;
        }
    }
}
