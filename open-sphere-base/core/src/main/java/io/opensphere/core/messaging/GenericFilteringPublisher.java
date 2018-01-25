package io.opensphere.core.messaging;

/**
 * Interface for a publisher that performs filtering for its subscribers.
 *
 * @param <E> The type of objects published by this publisher.
 */
public interface GenericFilteringPublisher<E> extends GenericPublisher<E>
{
    /**
     * Adds a filter for a given subscriber. If no filter exists for a
     * subscriber all messages are passed. Only one filter is allowed per
     * subscriber. Adding an additional filter will replace the filter already
     * in place.
     *
     * @param subscriber - the subscriber that wishes to place the filter ( also
     *            a weak reference )
     * @param filter - the filter for the subscriber, if null does nothing (
     *            returns false)
     * @return true if added, false if there was no such subscriber
     */
    boolean addSubscriberAcceptFilter(GenericSubscriber<E> subscriber, GenericSubscriberAcceptFilter filter);

    /**
     * Removes a filter for a subscriber. If the subscriber remains subscribed
     * to the publisher it will now receive all messages.
     *
     * @param subscriber - the subscriber for which the filter is to be removed.
     */
    void removeSubscriberAcceptFilter(GenericSubscriber<E> subscriber);
}
