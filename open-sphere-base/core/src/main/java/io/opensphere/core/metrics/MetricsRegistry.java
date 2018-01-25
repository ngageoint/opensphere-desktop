package io.opensphere.core.metrics;

import java.util.Set;
import java.util.function.Predicate;

/**
 * The Interface MetricsRegistry.
 */
public interface MetricsRegistry
{
    /** The DEFAULT_TOPIC. */
    String DEFAULT_TOPIC = "Default";

    /** The DEFAULT_SUB_TOPIC. */
    String DEFAULT_SUB_TOPIC = "DEFAULT_SUB_TOPIC";

    /**
     * Adds the MetricsProvider to the registry.
     *
     * @param provider the {@link MetricsProvider} to add.
     */
    void addMetricsProvider(MetricsProvider provider);

    /**
     * Adds the {@link MetricsRegistryListener}. Note: Held as weak reference.
     *
     * @param listener the listener
     */
    void addMetricsRegistryListener(MetricsRegistryListener listener);

    /**
     * Gets all the providers in the registry.
     *
     * @return the providers
     */
    Set<MetricsProvider> getProviders();

    /**
     * Gets the MetricsProvider that pass the specified filter.
     *
     * @param filter the {@link Predicate}
     * @return the providers that pass the filter or empty set if no
     */
    Set<MetricsProvider> getProviders(Predicate<? super MetricsProvider> filter);

    /**
     * Gets the MetricsProvider for a specified topic.
     *
     * @param topic the topic
     * @return the {@link Set} of {@link MetricsProvider}
     */
    Set<MetricsProvider> getProviders(String topic);

    /**
     * Gets the MetricsProviders for a specific topic and sub-topic.
     *
     * @param topic the topic (null is default topic)
     * @param subTopic the sub topic (null is default sub topic)
     * @return the {@link Set} of {@link MetricsProvider}
     */
    Set<MetricsProvider> getProviders(String topic, String subTopic);

    /**
     * Gets the sub topics in the registry for a specific topic.
     *
     * @param topic the topic
     * @return the sub topics
     */
    Set<String> getSubTopics(String topic);

    /**
     * Gets the topics in the registry.
     *
     * @return the topics
     */
    Set<String> getTopics();

    /**
     * Removes the MetricsProvider from the registry.
     *
     * @param provider the {@link MetricsProvider} to remove.
     */
    void removeMetricsProvider(MetricsProvider provider);

    /**
     * Removes the {@link MetricsRegistryListener}.
     *
     * @param listener the listener
     * @return true, if successful
     */
    boolean removeMetricsRegistryListener(MetricsRegistryListener listener);

    /**
     * The listener interface for receiving metrics registry changes.
     *
     */
    public interface MetricsRegistryListener
    {
        /**
         * Metrics provider added.
         *
         * @param model the {@link MetricsProvider}
         */
        void metricsProviderAdded(MetricsProvider model);

        /**
         * Metrics provider removed.
         *
         * @param model the {@link MetricsProvider}
         */
        void metricsProviderRemoved(MetricsProvider model);
    }
}
