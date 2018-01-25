package io.opensphere.core.messaging;

import java.util.Set;

/**
 * This interface can be optionally provided by a {@link GenericSubscriber} to a
 * {@link GenericPublisher} to allow the publisher to know which sources and
 * classes of sources for which it desires to receive updates.
 *
 * The provider of the filter should accurately set the flag based checks so
 * that the publisher can quickly check to see if this filter even cares about
 * any individual filter component.
 *
 * It is assumed that if a subscriber does not provide a filter, it desires to
 * accept all updates.
 *
 * The implementer of the filter should filter first by source, then by source
 * class, and finally by send type. If filtering by source is enabled, filtering
 * by source class should be ignored.
 *
 */
public interface GenericSubscriberAcceptFilter
{
    /**
     * Returns true if this filter will accept the source.
     *
     * @param source - the source to check for acceptance
     * @return true if the subscriber will accept from this source
     */
    boolean acceptsSource(Object source);

    /**
     * Returns true if this filter will accept from this source class.
     *
     * @param aClass - the class to check for acceptance
     * @return true if the subscriber will accept from this source class
     */
    boolean acceptsSourceClass(Class<?> aClass);

    /**
     * Returns true if this filter will filter by specific sources. ( objects
     * not classes )
     *
     * @return true if it will
     */
    boolean filtersBySource();

    /**
     * Returns true if this filter will filter by specific source classes. (
     * class types not specific objects ).
     *
     * @return true if it will
     */
    boolean filtersBySourceClasses();

    /**
     * A set of source classes from which this subscriber wishes to receive
     * updates.
     *
     * @return the {@link Set} of source classes
     */
    Set<Class<?>> getAcceptSourceClasses();

    /**
     * A set of sources from which this subscriber wishes to receive updates.
     *
     * @return the {@link Set} of sources
     */
    Set<Object> getAcceptSources();
}
