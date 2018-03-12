package io.opensphere.analysis.table.functions;

import java.time.Instant;
import java.util.Date;

/** Class representation of a delta between two Date objects. */
public class TimeDeltaFunction extends ColumnFunction
{
    /**
     * Constructs a TimeDeltaFunction.
     */
    public TimeDeltaFunction()
    {
        super("Time Delta", (left, right) ->
        {
            if (!(left instanceof Date) || !(right instanceof Date))
            {
                return null;
            }

            long minusTime = ((Date)right).toInstant().toEpochMilli();
            Instant result = ((Date)left).toInstant().minusMillis(minusTime);

            return result;
        });
    }
}
