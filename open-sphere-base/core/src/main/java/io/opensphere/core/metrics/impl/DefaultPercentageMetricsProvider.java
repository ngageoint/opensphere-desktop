package io.opensphere.core.metrics.impl;

import io.opensphere.core.metrics.PercentageMetricsProvider;

/**
 * The Class DefaultPercentageMetricsProvider.
 */
public class DefaultPercentageMetricsProvider extends DefaultRangedNumberMetricsProvider implements PercentageMetricsProvider
{
    /** The Constant ZERO. */
    private static final Number ZERO = Double.valueOf(0.0);

    /** The Constant ONE. */
    private static final Number ONE = Double.valueOf(1.0);

    /**
     * Instantiates a new default percentage metrics provider.
     *
     * @param displayPriority the display priority
     * @param topic the topic
     * @param subTopic the sub topic
     * @param label the label
     */
    public DefaultPercentageMetricsProvider(int displayPriority, String topic, String subTopic, String label)
    {
        super(displayPriority, topic, subTopic, label, ZERO, ONE);
    }

    /**
     * Instantiates a new default percentage metrics provider.
     *
     * @param topic the topic
     * @param subTopic the sub topic
     * @param label the label
     * @param initialValue the initial value ( if null or &lt; 0.0 set to 0.0,
     *            if greater than 1.0 set to 1.0 )
     */
    public DefaultPercentageMetricsProvider(String topic, String subTopic, String label, Number initialValue)
    {
        super(topic, subTopic, label, ZERO, ONE, initialValue);
    }

    @Override
    public String getValue()
    {
        Number n = getValueAsNumber();
        return String.format("%3.2f", Double.valueOf(n.doubleValue() * 100)) + "%";
    }

    @Override
    public void setPostfix(String postfix)
    {
        throw new UnsupportedOperationException();
    }
}
