package io.opensphere.core.event;

import java.util.Collection;

/**
 * The Interface EventConsolidator.
 *
 * A class that will consolidate a type of event into a summary and when asked
 * issue a finalized consolidated event representing the summary.
 *
 * @param <E> the event that will be consolidated
 */
public interface EventConsolidator<E extends Event>
{
    /**
     * Adds a new event to the consolidator.
     *
     * @param event - the event to add.
     */
    void addEvent(E event);

    /**
     * Adds the events.
     *
     * @param events the events
     */
    void addEvents(Collection<E> events);

    /**
     * Complete the consolidation and constructs the consolidated event.
     *
     * @return the consolidated event.
     */
    Event createConsolidatedEvent();

    /**
     * Returns true if events were consolidated, false if not.
     *
     * @return true if events were consolidated.
     */
    boolean hadEvents();

    /**
     * Gets a new instance of this consolidator.
     *
     * @return a new instance of this consolidator.
     */
    EventConsolidator<E> newInstance();

    /**
     * Resets this Consolidator to its initial state with no summary
     * information. Where the next call to addEvent would start a new
     * consolidation.
     */
    void reset();
}
