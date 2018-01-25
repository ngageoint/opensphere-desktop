package io.opensphere.core.metrics.impl;

import java.util.IllegalFormatException;

import io.opensphere.core.metrics.RangedNumberMetricsProvider;
import io.opensphere.core.util.Utilities;

/**
 * The Class DefaultRangedNumberMetricsProvider.
 */
public class DefaultRangedNumberMetricsProvider extends AbstractMetricsProvider implements RangedNumberMetricsProvider
{
    /** The Current value. */
    private Number myCurrentValue;

    /** The Min value. */
    private final Number myMinValue;

    /** The Max value. */
    private final Number myMaxValue;

    /** The Number value formatter. */
    private String myNumberValueFormatter;

    /**
     * Gets a valid in range value ( if the value is null it is set to range min
     * ) if the value is not within the range of the number it is set to the
     * closes range end point.
     *
     * The return value is guaranteed to be non null and:
     *
     * minRangeValue &lt;= value &lt;= maxRangeValue
     *
     * @param value the value
     * @param minRangeValue the min range value
     * @param maxRangeValue the max range value
     * @return the valid in range value
     */
    private static Number getValidInRangeValue(Number value, Number minRangeValue, Number maxRangeValue)
    {
        return value == null ? minRangeValue : value.doubleValue() < minRangeValue.doubleValue() ? minRangeValue
                : value.doubleValue() > maxRangeValue.doubleValue() ? maxRangeValue : value;
    }

    /**
     * In range inclusive.
     *
     * @param initialValue the initial value
     * @param minValue the min value
     * @param maxValue the max value
     * @return true, if successful
     */
    private static boolean inRangeInclusive(Number initialValue, Number minValue, Number maxValue)
    {
        return initialValue != null && initialValue.doubleValue() >= minValue.doubleValue()
                && initialValue.doubleValue() <= maxValue.doubleValue();
    }

    /**
     * Instantiates a new default ranged number metrics provider.
     *
     * Note: if min &gt; max they will be swapped.
     *
     * @param displayPriority the display priority
     * @param topic the topic
     * @param subTopic the sub topic
     * @param label the label
     * @param minValue the min value
     * @param maxValue the max value
     */
    public DefaultRangedNumberMetricsProvider(int displayPriority, String topic, String subTopic, String label, Number minValue,
            Number maxValue)
    {
        super(displayPriority, topic, subTopic, label);
        Utilities.checkNull(minValue, "minValue");
        Utilities.checkNull(maxValue, "maxValue");
        if (minValue.equals(maxValue))
        {
            throw new IllegalArgumentException("Min value and max value can not be equal!");
        }
        myMinValue = minValue.doubleValue() < maxValue.doubleValue() ? minValue : maxValue;
        myMaxValue = minValue.doubleValue() < maxValue.doubleValue() ? maxValue : minValue;
        myCurrentValue = myMinValue;
    }

    /**
     * Instantiates a new default ranged number metrics provider with an initial
     * value.
     *
     * Note: If min &gt; max they will be swapped. If initialValue is null it
     * will be set to minValue. If initialValue not in valid range it will be
     * adjusted to closest range boundary.
     *
     * @param topic the topic
     * @param subTopic the sub topic
     * @param label the label
     * @param minValue the min value
     * @param maxValue the max value
     * @param initialValue the initial value
     */
    public DefaultRangedNumberMetricsProvider(String topic, String subTopic, String label, Number minValue, Number maxValue,
            Number initialValue)
    {
        this(0, topic, subTopic, label, minValue, maxValue);

        if (initialValue != null && inRangeInclusive(initialValue, minValue, maxValue))
        {
            myCurrentValue = initialValue;
        }
    }

    @Override
    public Number getMaxValue()
    {
        return myMaxValue;
    }

    @Override
    public Number getMinValue()
    {
        return myMinValue;
    }

    @Override
    public String getValue()
    {
        StringBuilder builder = new StringBuilder();
        if (myNumberValueFormatter != null)
        {
            try
            {
                builder.append(String.format(myNumberValueFormatter, getValueAsNumber()));
            }
            catch (IllegalFormatException e)
            {
                builder.setLength(0);
                builder.append(myCurrentValue.toString());
            }
        }
        else
        {
            builder.append(myCurrentValue.toString());
        }
        if (getPostfix() != null)
        {
            builder.append(getPostfix());
        }
        return builder.toString();
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
        return myNumberValueFormatter;
    }

    /**
     * Sets the number value formatter.
     *
     * @param formatter the new number value formatter
     */
    public void setNumberValueFormatter(String formatter)
    {
        myNumberValueFormatter = formatter;
    }

    /**
     * Sets the value ( if the value is null it is set to range min ) if the
     * value is not within the range of the number it is set to the closes range
     * end point.
     *
     * @param value the new value
     */
    public void setValue(Number value)
    {
        Number tValue = getValidInRangeValue(value, myMinValue, myMaxValue);
        if (getEventType() == EventStrategy.EVENT_ON_ALL_UPDATES || !myCurrentValue.equals(tValue))
        {
            myCurrentValue = tValue;
            fireUpdated();
        }
    }
}
