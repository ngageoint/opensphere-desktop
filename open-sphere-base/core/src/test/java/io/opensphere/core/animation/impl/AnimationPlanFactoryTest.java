package io.opensphere.core.animation.impl;

import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.animation.AnimationPlan.EndBehavior;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanFormatterTest;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.collections.New;

/** Tests {@link AnimationPlanFactory}. */
public class AnimationPlanFactoryTest
{
    /**
     * Tests
     * {@link AnimationPlanFactory#createDefaultAnimationPlan(TimeSpan, Duration, java.util.Collection)}.
     *
     * @exception ParseException if one of the times cannot be parsed.
     */
    @Test
    public void testCreateDefaultAnimationPlan() throws ParseException
    {
        TimeSpan loopSpan;
        Duration frameDuration;
        List<TimeSpan> skippedSpans = New.list();
        List<TimeSpan> sequence = New.list();

        // Simple day test

        loopSpan = TimeSpanFormatterTest.span("2015-04-20 00:00:00", "2015-04-23 00:00:00");
        frameDuration = Days.ONE;
        skippedSpans.clear();

        sequence.clear();
        sequence.add(TimeSpanFormatterTest.span("2015-04-20", "2015-04-21"));
        sequence.add(TimeSpanFormatterTest.span("2015-04-21", "2015-04-22"));
        sequence.add(TimeSpanFormatterTest.span("2015-04-22", "2015-04-23"));

        Assert.assertEquals(new DefaultAnimationPlan(sequence, EndBehavior.WRAP),
                new AnimationPlanFactory().createDefaultAnimationPlan(loopSpan, frameDuration, skippedSpans));

        // Simple day test with skipped interval

        loopSpan = TimeSpanFormatterTest.span("2015-04-20 00:00:00", "2015-04-23 00:00:00");
        frameDuration = Days.ONE;
        skippedSpans.clear();
        skippedSpans.add(TimeSpanFormatterTest.span("2015-04-21 06:00:00", "2015-04-21 09:00:00"));

        sequence.clear();
        sequence.add(TimeSpanFormatterTest.span("2015-04-20", "2015-04-21"));
        sequence.add(TimeSpanFormatterTest.span("2015-04-22", "2015-04-23"));

        Assert.assertEquals(new DefaultAnimationPlan(sequence, EndBehavior.WRAP),
                new AnimationPlanFactory().createDefaultAnimationPlan(loopSpan, frameDuration, skippedSpans));
    }
}
