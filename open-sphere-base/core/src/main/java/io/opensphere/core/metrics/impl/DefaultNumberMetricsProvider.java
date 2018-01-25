package io.opensphere.core.metrics.impl;

import java.util.IllegalFormatException;

import io.opensphere.core.metrics.NumberMetricsProvider;

/**
 * The Class DefaultNumberMetricsProvider.
 */
public class DefaultNumberMetricsProvider extends AbstractMetricsProvider implements NumberMetricsProvider
{
    /** The DEFAULT_VALUE. */
    private static final Number DEFAULT_VALUE = Integer.valueOf(0);

    /** The Current value. */
    private Number myCurrentValue = DEFAULT_VALUE;

    /** The Number value formatter. */
    private String myValueFormatter;

    /**
     * Instantiates a new default number metrics provider.
     *
     * @param displayPriority the display priority
     * @param topic the topic
     * @param subTopic the sub topic
     * @param label the label
     */
    public DefaultNumberMetricsProvider(int displayPriority, String topic, String subTopic, String label)
    {
        this(displayPriority, topic, subTopic, label, null);
    }

    /**
     * Instantiates a new default number metrics provider.
     *
     * @param displayPriority the display priority
     * @param topic the topic
     * @param subTopic the sub topic
     * @param label the label
     * @param initialValue the initial value
     */
    public DefaultNumberMetricsProvider(int displayPriority, String topic, String subTopic, String label, Number initialValue)
    {
        super(displayPriority, topic, subTopic, label);
        myCurrentValue = initialValue == null ? DEFAULT_VALUE : initialValue;
    }

    @Override
    public String getValue()
    {
        StringBuilder sb = new StringBuilder();
        if (myValueFormatter != null)
        {
            try
            {
                sb.append(String.format(myValueFormatter, getValueAsNumber()));
            }
            catch (IllegalFormatException e)
            {
                sb.setLength(0);
                sb.append(myCurrentValue.toString());
            }
        }
        else
        {
            sb.append(myCurrentValue.toString());
        }
        if (getPostfix() != null)
        {
            sb.append(getPostfix());
        }
        return sb.toString();
    }

    @Override
    public Number getValueAsNumber()
    {
        return myCurrentValue;
    }

    /**
     * Gets the value format.
     *
     * @return the value format
     */
    public String getValueFormat()
    {
        return myValueFormatter;
    }

    /**
     * Sets the number value formatter.
     *
     * @param formatter the new number value formatter
     */
    public void setNumberValueFormatter(String formatter)
    {
        myValueFormatter = formatter;
    }

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    public void setValue(Number value)
    {
        if (getEventType() == EventStrategy.EVENT_ON_ALL_UPDATES || !myCurrentValue.equals(value))
        {
            myCurrentValue = value == null ? DEFAULT_VALUE : value;
            fireUpdated();
        }
    }
}
