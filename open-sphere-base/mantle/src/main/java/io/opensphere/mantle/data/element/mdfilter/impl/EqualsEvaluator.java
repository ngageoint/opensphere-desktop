
package io.opensphere.mantle.data.element.mdfilter.impl;

import java.util.Date;

import io.opensphere.core.model.time.TimeSpan;

/**
 * The Class EqualsEvaluator.
 */
public class EqualsEvaluator extends AbstractEvaluator
{
    /**
     * Instantiates a new equals evaluator.
     *
     * @param value the value
     */
    public EqualsEvaluator(Object value)
    {
        super(value);
    }

    @Override
    public boolean evaluate(Object dataValue)
    {
        Double doubleValue = getValueAsDouble();
        if (dataValue instanceof Number && doubleValue != null)
        {
            return doubleValue.doubleValue() == ((Number)dataValue).doubleValue();
        }
        if (dataValue instanceof Date)
        {
            Date aDate = getAsDate();
            return ((Date)dataValue).equals(aDate);
        }
        if (dataValue instanceof TimeSpan)
        {
            Date aDate = getAsDate();
            TimeSpan timeSpan = (TimeSpan)dataValue;
            return !timeSpan.isUnboundedStart() && timeSpan.getStartDate().equals(aDate);
        }
        String stringValue = getValueAsString();
        if (stringValue != null)
        {
            return stringValue.equals(dataValue == null ? null : dataValue.toString());
        }
        return getValue().equals(dataValue);
    }
}
