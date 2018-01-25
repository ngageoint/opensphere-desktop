package io.opensphere.csvcommon.detect.datetime.algorithm.deciders;

import io.opensphere.core.common.configuration.date.DateFormat.Type;

/**
 * Scores potential date columns assuming they contain just dates.
 *
 */
public class DateDecider extends SingleValueDecider
{
    @Override
    protected Type getTimeType()
    {
        return Type.DATE;
    }
}
