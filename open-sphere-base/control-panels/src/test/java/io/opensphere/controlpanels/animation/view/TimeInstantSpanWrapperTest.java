package io.opensphere.controlpanels.animation.view;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.StrongObservableValue;

/** Tests for {@link TimeInstantSpanWrapper}. */
public class TimeInstantSpanWrapperTest
{
    /** Test for {@link TimeInstantSpanWrapper}. */
    @Test
    public void testIt()
    {
        long start = 1487314800000L; // 2017/02/17
        long end = start + 86400_000L;
        ObservableValue<TimeSpan> span = new StrongObservableValue<>();
        span.set(TimeSpan.get(start, Days.ONE));

        TimeInstantSpanWrapper startWrapper = new TimeInstantSpanWrapper(span, true);
        assertEquals(start, startWrapper.get().getEpochMillis());

        TimeInstantSpanWrapper endWrapper = new TimeInstantSpanWrapper(span, false);
        assertEquals(end, endWrapper.get().getEpochMillis());

        startWrapper.set(TimeInstant.get(start + 100));
        assertEquals(start + 100, span.get().getStart());
        assertEquals(end, span.get().getEnd());

        endWrapper.set(TimeInstant.get(end + 100));
        assertEquals(start + 100, span.get().getStart());
        assertEquals(end + 100, span.get().getEnd());

        // End < Start
        try
        {
            endWrapper.set(TimeInstant.get(start));
            Assert.fail("Should have thrown exception");
        }
        catch (IllegalArgumentException e)
        {
            // eat exception
            Assert.assertNotNull(e);
        }
        assertEquals(start + 100, span.get().getStart());
        assertEquals(end + 100, span.get().getEnd());
    }
}
