package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import io.opensphere.core.common.configuration.date.DateFormat.Type;

/**
 * Scores the potential date time columns assuming the columns contain both
 * date/times.
 *
 */
public class DateTimeDecider extends SingleValueDecider
{
    @Override
    protected Type getTimeType()
    {
        return Type.TIMESTAMP;
    }
}
