package io.opensphere.core.pipeline.cache;

import java.util.Collection;

/**
 * Interface for listeners for insertion or removal events from a cache
 * provider.
 *
 * @param <E> The type of the cache objects.
 */
public interface CacheContentListener<E>
{
    /**
     * Handle a content change event.
     *
     * @param event content change event.
     */
    void handleCacheContentChange(CacheContentEvent<E> event);

    /**
     * Event which contains the details of the contents change.
     *
     * @param <E> The type of the cache objects.
     */
    class CacheContentEvent<E>
    {
        /** Items which have changed containment status. */
        private final Collection<E> myChangedItems;

        /** Type of content change. */
        private final ContentChangeType myChangeType;

        /**
         * Constructor.
         *
         * @param changedItems Items which have been removed or inserted.
         * @param changeType Type of contents change.
         */
        public CacheContentEvent(Collection<E> changedItems, ContentChangeType changeType)
        {
            myChangeType = changeType;
            myChangedItems = changedItems;
        }

        /**
         * Get the changedItems.
         *
         * @return the changedItems
         */
        public Collection<E> getChangedItems()
        {
            return myChangedItems;
        }

        /**
         * Get the changeType.
         *
         * @return the changeType
         */
        public ContentChangeType getChangeType()
        {
            return myChangeType;
        }
    }

    /** Type of containment change. */
    enum ContentChangeType
    {
        /** Removal or insertion of items to/from the cache. */
        ALL,

        /** Insertion of items into the cache. */
        INSERTION,

        /** Removal of items from the cache. */
        REMOVAL
    }
}
