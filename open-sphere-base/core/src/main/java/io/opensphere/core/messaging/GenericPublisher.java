package io.opensphere.core.messaging;

import io.opensphere.core.util.Service;

/**
 * Interface for a publisher.
 *
 * @param <E> The type of objects published by this publisher.
 */
public interface GenericPublisher<E>
{
    /**
     * Add a subscriber.
     * <p>
     * <b>Note:</b> To avoid memory leaks, implementations of this method may
     * not hold a strong reference to the {@code subscriber}. Callers of this
     * method should be sure the maintain a strong reference to the subscriber
     * as long as it is needed, to avoid garbage collection.
     *
     * <b>Note:</b> Also removes any filters for this subscriber.
     *
     * @param subscriber A subscriber.
     */
    void addSubscriber(GenericSubscriber<E> subscriber);

    /**
     * Remove a subscriber.
     *
     * @param subscriber A subscriber.
     */
    void removeSubscriber(GenericSubscriber<E> subscriber);

    /**
     * Creates a service that can be used to add/remove the given subscriber.
     *
     * @param subscriber A subscriber.
     * @return the service
     */
    default Service getSubscriberService(final GenericSubscriber<E> subscriber)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                addSubscriber(subscriber);
            }

            @Override
            public void close()
            {
                removeSubscriber(subscriber);
            }
        };
    }
}
