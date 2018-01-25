package io.opensphere.core.metrics;

import java.awt.Color;
import java.util.Date;

/**
 * The Interface MetricsProvider.
 */
public interface MetricsProvider
{
    /**
     * Adds the {@link MetricsProviderListener}.
     *
     * @param listener the listener
     */
    void addListener(MetricsProviderListener listener);

    /**
     * Gets the color for the value, if the listener knows how to use it.
     *
     * @return the color or null if no color preference is prescribed.
     */
    Color getColor();

    /**
     * Gets the display priority.
     *
     * @return the display priority
     */
    int getDisplayPriority();

    /**
     * Gets the label for the provider.
     *
     * @return the label ( or null if no label)
     */
    String getLabel();

    /**
     * Gets the last updated time.
     *
     * @return the last updated time
     */
    Date getLastUpdatedTime();

    /**
     * Gets the sub topic.
     *
     * @return the sub topic
     */
    String getSubTopic();

    /**
     * Gets the topic.
     *
     * @return the topic
     */
    String getTopic();

    /**
     * Gets the value for the metric as a string.
     *
     * @return the value
     */
    String getValue();

    /**
     * Removes the {@link MetricsProviderListener}.
     *
     * @param listener the listener
     * @return true, if successful
     */
    boolean removeListener(MetricsProviderListener listener);

    /**
     * A listener for updates to {@link MetricsProvider}s.
     */
    @FunctionalInterface
    public interface MetricsProviderListener
    {
        /**
         * Invoked when provider update occurs.
         *
         * @param provider the MetricsProvider.
         */
        void providerUpdated(MetricsProvider provider);
    }
}
