package io.opensphere.core.metrics.impl;

/**
 * The Class DefaultMetricsProvider.
 */
public class DefaultMetricsProvider extends AbstractMetricsProvider
{
    /** The Constant EMPTY_STRING. */
    private static final String EMPTY_STRING = "";

    /** The Current value. */
    private String myCurrentValue = EMPTY_STRING;

    /**
     * Instantiates a new default metrics provider.
     *
     * @param displayPriority the display priority
     * @param topic the topic
     * @param subTopic the sub topic
     * @param label the label
     */
    public DefaultMetricsProvider(int displayPriority, String topic, String subTopic, String label)
    {
        super(displayPriority, topic, subTopic, label);
    }

    @Override
    public String getValue()
    {
        return getPostfix() == null ? myCurrentValue : myCurrentValue + " " + getPostfix();
    }

    /**
     * Sets the value and fires updates to listeners.
     *
     * @param value the new value
     */
    public void setValue(String value)
    {
        if (getEventType() == EventStrategy.EVENT_ON_ALL_UPDATES || !myCurrentValue.equals(value))
        {
            myCurrentValue = value == null ? EMPTY_STRING : value;
            fireUpdated();
        }
    }
}
