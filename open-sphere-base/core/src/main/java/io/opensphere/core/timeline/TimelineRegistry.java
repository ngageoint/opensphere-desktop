package io.opensphere.core.timeline;

import java.awt.Color;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.opensphere.core.order.OrderParticipantKey;

/** Timeline registry. */
public interface TimelineRegistry
{
    /**
     * Adds a timeline layer to the registry.
     *
     * @param key the key
     * @param name the name
     * @param color the color
     * @param visible whether the layer is visible
     */
    void addLayer(OrderParticipantKey key, String name, Color color, boolean visible);

    /**
     * Removes a timeline layer from the registry.
     *
     * @param key the key
     */
    void removeLayer(OrderParticipantKey key);

    /**
     * Adds data for the given key.
     *
     * @param key the key
     * @param data the data to add
     */
    void addData(OrderParticipantKey key, Collection<? extends TimelineDatum> data);

    /**
     * Removes data for the given key.
     *
     * @param key the key
     * @param ids the ids to remove
     */
    void removeData(OrderParticipantKey key, Collection<? extends Long> ids);

    /**
     * Sets the data for the given key.
     *
     * @param key the key
     * @param data the data
     */
    void setData(OrderParticipantKey key, Collection<? extends TimelineDatum> data);

    /**
     * Sets the layer color for the given key.
     *
     * @param key the key
     * @param color the color
     */
    void setColor(OrderParticipantKey key, Color color);

    /**
     * Sets the layer visibility for the given key.
     *
     * @param key the key
     * @param visible the visibility
     */
    void setVisible(OrderParticipantKey key, boolean visible);

    /**
     * Returns whether the registry contains the key.
     *
     * @param key the key
     * @return whether the registry contains the key
     */
    boolean hasKey(OrderParticipantKey key);

    /**
     * Gets the keys currently in the registry.
     *
     * @return the keys
     */
    Collection<OrderParticipantKey> getKeys();

    /**
     * Gets the time spans for the given key.
     *
     * @param key the key
     * @param filter optional predicate to filter the spans
     * @return the time spans
     */
    Collection<TimelineDatum> getSpans(OrderParticipantKey key, Predicate<? super TimelineDatum> filter);

    /**
     * Gets the layer name for the given key.
     *
     * @param key the key
     * @return the name
     */
    String getName(OrderParticipantKey key);

    /**
     * Gets the layer color for the given key.
     *
     * @param key the key
     * @return the color
     */
    Color getColor(OrderParticipantKey key);

    /**
     * Gets the layer visibility for the given key.
     *
     * @param key the key
     * @return the visibility
     */
    boolean isVisible(OrderParticipantKey key);

    /**
     * Adds a listener.
     *
     * @param listener the listener
     */
    void addListener(Consumer<TimelineChangeEvent> listener);

    /**
     * Removes a listener.
     *
     * @param listener the listener
     */
    void removeListener(Consumer<TimelineChangeEvent> listener);
}
