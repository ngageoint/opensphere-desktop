package io.opensphere.core.animation.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlan.EndBehavior;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.animation.AnimationState.Direction;
import io.opensphere.core.animation.ContinuousAnimationPlan;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.units.duration.Minutes;
import io.opensphere.core.units.duration.Seconds;

/** Test for {@link DefaultContinuousAnimationPlan}. */
public class DefaultContinuousAnimationPlanTest
{
    /**
     * Test constructor.
     */
    @SuppressWarnings("unused")
    @Test
    public void testDefaultContinuousAnimationPlan()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L));
        Duration window = Minutes.ONE;
        Duration advance = new Seconds(10);
        EndBehavior endBehavior = EndBehavior.STOP;
        TimeSpan limit = null;
        Duration fade = null;
        new DefaultContinuousAnimationPlan(sequence, window, advance, endBehavior, limit);
    }

    /**
     * Test constructor with bad input.
     */
    @SuppressWarnings("unused")
    @Test
    public void testDefaultContinuousAnimationPlanEmptySequence()
    {
        List<? extends TimeSpan> sequence = Collections.emptyList();
        Duration window = Minutes.ONE;
        Duration advance = new Seconds(10);
        EndBehavior endBehavior = EndBehavior.STOP;
        TimeSpan limit = null;
        Duration fade = null;
        new DefaultContinuousAnimationPlan(sequence, window, advance, endBehavior, limit);
    }

    /**
     * Test constructor with bad input.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testDefaultContinuousAnimationPlanNullAdvance()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L));
        Duration window = Minutes.ONE;
        Duration advance = null;
        EndBehavior endBehavior = EndBehavior.STOP;
        TimeSpan limit = null;
        Duration fade = null;
        new DefaultContinuousAnimationPlan(sequence, window, advance, endBehavior, limit);
    }

    /**
     * Test constructor with bad input.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testDefaultContinuousAnimationPlanNullEndBehavior()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L));
        Duration window = Minutes.ONE;
        Duration advance = new Seconds(10);
        EndBehavior endBehavior = null;
        TimeSpan limit = null;
        Duration fade = null;
        new DefaultContinuousAnimationPlan(sequence, window, advance, endBehavior, limit);
    }

    /**
     * Test constructor with bad input.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testDefaultContinuousAnimationPlanNullSequence()
    {
        List<? extends TimeSpan> sequence = null;
        Duration window = Minutes.ONE;
        Duration advance = new Seconds(10);
        EndBehavior endBehavior = EndBehavior.STOP;
        TimeSpan limit = null;
        Duration fade = null;
        new DefaultContinuousAnimationPlan(sequence, window, advance, endBehavior, limit);
    }

    /**
     * Test constructor with bad input.
     */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testDefaultContinuousAnimationPlanNullWindow()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L));
        Duration window = null;
        Duration advance = new Seconds(10);
        EndBehavior endBehavior = EndBehavior.STOP;
        TimeSpan limit = null;
        Duration fade = null;
        new DefaultContinuousAnimationPlan(sequence, window, advance, endBehavior, limit);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(io.opensphere.core.animation.AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDetermineNextStateBadState1()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, null);

        plan.determineNextState(new DefaultContinuousAnimationState(2, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(io.opensphere.core.animation.AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDetermineNextStateBadState2()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, null);

        plan.determineNextState(new DefaultContinuousAnimationState(1, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(AnimationState)} with
     * {@link EndBehavior#BOUNCE} end behavior.
     */
    @Test
    public void testDetermineNextStateBounceAtEnd()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.BOUNCE, null);

        DefaultContinuousAnimationState result1 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        testDetermineNextStateCommon1(plan);

        DefaultContinuousAnimationState result7 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7130000L, 7190000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected7 = new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected7, result7);

        DefaultContinuousAnimationState result8 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected8 = new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected8, result8);

        testDetermineNextStateCommon2(plan);

        DefaultContinuousAnimationState result13 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected13 = new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected13, result13);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(AnimationState)} with
     * {@link EndBehavior#BOUNCE} end behavior and a limit window.
     */
    @Test
    public void testDetermineNextStateBounceAtEndLimited()
    {
        TimeSpan limit = TimeSpan.get(1800000L, 4800000L);
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.BOUNCE, limit);

        DefaultContinuousAnimationState result1 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(1810000L, 1870000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        testDetermineNextStateCommon1(plan);

        DefaultContinuousAnimationState result7 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(4730000L, 4790000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected7 = new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected7, result7);

        DefaultContinuousAnimationState result8 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected8 = new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected8, result8);

        testDetermineNextStateCommon3(plan);

        DefaultContinuousAnimationState result13 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected13 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected13, result13);

        DefaultContinuousAnimationState result14 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1700000L, 1760000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected14 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected14, result14);

        DefaultContinuousAnimationState result15 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1700000L, 1760000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected15 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected15, result15);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(AnimationState)} with
     * {@link EndBehavior#BOUNCE} end behavior and a single span in the
     * sequence.
     */
    @Test
    public void testDetermineNextStateSingleSpanSequenceBounceAtEnd()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.BOUNCE, null);

        DefaultContinuousAnimationState result1 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        DefaultContinuousAnimationState result2 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(3530000L, 3590000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected2 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected2, result2);

        DefaultContinuousAnimationState result3 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected3 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected3, result3);

        DefaultContinuousAnimationState result4 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected4 = new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected4, result4);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(AnimationState)} with
     * {@link EndBehavior#STOP} end behavior and a single span in the sequence.
     */
    @Test
    public void testDetermineNextStateSingleSpanSequenceStopAtEnd()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, null);

        DefaultContinuousAnimationState result1 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        DefaultContinuousAnimationState result2 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(3530000L, 3590000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected2 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected2, result2);

        DefaultContinuousAnimationState result3 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L), AnimationState.Direction.FORWARD));
        Assert.assertNull(result3);

        DefaultContinuousAnimationState result4 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), AnimationState.Direction.BACKWARD));
        Assert.assertNull(result4);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(AnimationState)} with
     * {@link EndBehavior#WRAP} end behavior and a single span in the sequence.
     */
    @Test
    public void testDetermineNextStateSingleSpanSequenceWrapAtEnd()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.WRAP, null);

        DefaultContinuousAnimationState result1 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        DefaultContinuousAnimationState result2 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(3530000L, 3590000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected2 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected2, result2);

        DefaultContinuousAnimationState result3 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected3 = new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected3, result3);

        DefaultContinuousAnimationState result4 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected4 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected4, result4);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(AnimationState)} with
     * {@link EndBehavior#STOP} end behavior.
     */
    @Test
    public void testDetermineNextStateStopAtEnd()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, null);

        DefaultContinuousAnimationState result1 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        testDetermineNextStateCommon1(plan);

        DefaultContinuousAnimationState result7 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7130000L, 7190000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected7 = new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected7, result7);

        DefaultContinuousAnimationState result8 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7180000L, 7200000L), AnimationState.Direction.FORWARD));
        Assert.assertNull(result8);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(AnimationState)} with
     * {@link EndBehavior#STOP} end behavior and a limit window.
     */
    @Test
    public void testDetermineNextStateStopAtEndLimited()
    {
        TimeSpan limit = TimeSpan.get(1800000L, 4800000L);
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3000000L), TimeSpan.get(3000000L, 4000000L),
                TimeSpan.get(4000000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, limit);

        DefaultContinuousAnimationState result1 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1810000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(1810000L, 1870000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        DefaultContinuousAnimationState result2 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(2940000L, 3000000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected2 = new DefaultContinuousAnimationState(1, TimeSpan.get(3000000L, 3060000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected2, result2);

        DefaultContinuousAnimationState result3 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(2930000L, 2990000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected3 = new DefaultContinuousAnimationState(0, TimeSpan.get(2940000L, 3000000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected3, result3);

        DefaultContinuousAnimationState result4 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(2940000L, 3000000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected4 = new DefaultContinuousAnimationState(1, TimeSpan.get(3000000L, 3060000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected4, result4);

        DefaultContinuousAnimationState result5 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3000000L, 3060000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected5 = new DefaultContinuousAnimationState(1, TimeSpan.get(3010000L, 3070000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected5, result5);

        DefaultContinuousAnimationState result7 = plan.determineNextState(
                new DefaultContinuousAnimationState(2, TimeSpan.get(4730000L, 4790000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected7 = new DefaultContinuousAnimationState(2, TimeSpan.get(4740000L, 4800000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected7, result7);

        DefaultContinuousAnimationState result8 = plan.determineNextState(
                new DefaultContinuousAnimationState(2, TimeSpan.get(4780000L, 4800000L), AnimationState.Direction.FORWARD));
        Assert.assertNull(result8);

        DefaultContinuousAnimationState result14 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1700000L, 1760000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected14 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected14, result14);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(AnimationState)} with
     * {@link EndBehavior#WRAP} end behavior.
     */
    @Test
    public void testDetermineNextStateWrapAtEnd()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.WRAP, null);

        DefaultContinuousAnimationState result1 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        testDetermineNextStateCommon1(plan);

        DefaultContinuousAnimationState result7 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7130000L, 7190000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected7 = new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected7, result7);

        DefaultContinuousAnimationState result8 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected8 = new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected8, result8);

        testDetermineNextStateCommon2(plan);

        DefaultContinuousAnimationState result13 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected13 = new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected13, result13);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determineNextState(AnimationState)} with
     * {@link EndBehavior#WRAP} end behavior and a limit window.
     */
    @Test
    public void testDetermineNextStateWrapAtEndLimited()
    {
        TimeSpan limit = TimeSpan.get(1800000L, 4800000L);
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.WRAP, limit);

        DefaultContinuousAnimationState result1 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1800001L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(1810000L, 1870000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        testDetermineNextStateCommon1(plan);

        DefaultContinuousAnimationState result7 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(4730000L, 4790000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected7 = new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected7, result7);

        DefaultContinuousAnimationState result8 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected8 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected8, result8);

        testDetermineNextStateCommon3(plan);

        DefaultContinuousAnimationState result13 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected13 = new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected13, result13);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determinePreviousState(io.opensphere.core.animation.AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeterminePreviousStateBadState1()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, null);

        plan.determinePreviousState(
                new DefaultContinuousAnimationState(2, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determinePreviousState(io.opensphere.core.animation.AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeterminePreviousStateBadState2()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, null);

        plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(0L, 1L), AnimationState.Direction.FORWARD));
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determinePreviousState(io.opensphere.core.animation.AnimationState)}
     * with {@link EndBehavior#BOUNCE} end behavior.
     */
    @Test
    public void testDeterminePreviousStateBounceAtEnd()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.BOUNCE, null);

        DefaultContinuousAnimationState result1 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected1, result1);

        testDeterminePreviousStateCommon2(plan);

        DefaultContinuousAnimationState result8 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected9 = new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected9, result8);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determinePreviousState(io.opensphere.core.animation.AnimationState)}
     * with {@link EndBehavior#BOUNCE} end behavior and a limit window.
     */
    @Test
    public void testDeterminePreviousStateBounceAtEndLimited()
    {
        TimeSpan limit = TimeSpan.get(1800000L, 4800000L);
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.BOUNCE, limit);

        DefaultContinuousAnimationState result1 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected1, result1);

        testDeterminePreviousStateCommon1(plan);

        DefaultContinuousAnimationState result8 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected9 = new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected9, result8);

        DefaultContinuousAnimationState result14 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1700000L, 1760000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected14 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected14, result14);

        DefaultContinuousAnimationState result15 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1700000L, 1760000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected15 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected15, result15);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determinePreviousState(io.opensphere.core.animation.AnimationState)}
     * with {@link EndBehavior#STOP} end behavior.
     */
    @Test
    public void testDeterminePreviousStateStopAtEnd()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, null);

        DefaultContinuousAnimationState result1 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 10000L), AnimationState.Direction.FORWARD));
        Assert.assertNull(result1);

        testDeterminePreviousStateCommon2(plan);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determinePreviousState(io.opensphere.core.animation.AnimationState)}
     * with {@link EndBehavior#STOP} end behavior and a limit window.
     */
    @Test
    public void testDeterminePreviousStateStopAtEndLimited()
    {
        TimeSpan limit = TimeSpan.get(1800000L, 4800000L);
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 4800000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, limit);

        DefaultContinuousAnimationState result1 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1810000L), AnimationState.Direction.FORWARD));
        Assert.assertNull(result1);

        testDeterminePreviousStateCommon1(plan);

        DefaultContinuousAnimationState result14 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1700000L, 1760000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected14 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected14, result14);

        DefaultContinuousAnimationState result15 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1700000L, 1760000L), AnimationState.Direction.BACKWARD));
        Assert.assertNull(result15);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determinePreviousState(io.opensphere.core.animation.AnimationState)}
     * with {@link EndBehavior#WRAP} end behavior.
     */
    @Test
    public void testDeterminePreviousStateWrapAtEnd()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.WRAP, null);

        DefaultContinuousAnimationState result1 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 10000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        testDeterminePreviousStateCommon2(plan);

        DefaultContinuousAnimationState result8 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected9 = new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected9, result8);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#determinePreviousState(io.opensphere.core.animation.AnimationState)}
     * with {@link EndBehavior#WRAP} end behavior and a limit window.
     */
    @Test
    public void testDeterminePreviousStateWrapAtEndLimited()
    {
        TimeSpan limit = TimeSpan.get(1800000L, 4800000L);
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 4800000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.WRAP, limit);

        DefaultContinuousAnimationState result1 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1810000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected1 = new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected1, result1);

        testDeterminePreviousStateCommon1(plan);

        DefaultContinuousAnimationState result8 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected9 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected9, result8);

        DefaultContinuousAnimationState result14 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1700000L, 1760000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected14 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected14, result14);

        DefaultContinuousAnimationState result15 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1700000L, 1760000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected15 = new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected15, result15);
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#findState(java.util.Date, io.opensphere.core.animation.AnimationState.Direction)}
     * .
     */
    @Test
    public void testFindStateDateDirection()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, null);

        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.FORWARD),
                plan.findState(new Date(0L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L), Direction.FORWARD),
                plan.findState(new Date(10000L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), Direction.FORWARD),
                plan.findState(new Date(3600000L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.BACKWARD),
                plan.findState(new Date(60000L), Direction.BACKWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L), Direction.BACKWARD),
                plan.findState(new Date(70000L), Direction.BACKWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), Direction.BACKWARD),
                plan.findState(new Date(3660000L), Direction.BACKWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.FORWARD),
                plan.findState(new Date(-1L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.BACKWARD),
                plan.findState(new Date(-1L), Direction.BACKWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), Direction.FORWARD),
                plan.findState(new Date(7200000L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), Direction.BACKWARD),
                plan.findState(new Date(7200000L), Direction.BACKWARD));
    }

    /**
     * Test for {@link AnimationPlan#findState(TimeSpan, Direction)} .
     */
    @Test
    public void testFindStateTimeSpanDirection()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L), TimeSpan.get(3600000L, 7200000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, Minutes.ONE, new Seconds(10),
                EndBehavior.STOP, null);

        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.FORWARD),
                plan.findState(TimeSpan.get(0L, 50000L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L), Direction.FORWARD),
                plan.findState(TimeSpan.get(10000L, 60000L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), Direction.FORWARD),
                plan.findState(TimeSpan.get(3600000L, 3650000L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.BACKWARD),
                plan.findState(TimeSpan.get(1L, 60000L), Direction.BACKWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L), Direction.BACKWARD),
                plan.findState(TimeSpan.get(10001L, 70000L), Direction.BACKWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), Direction.BACKWARD),
                plan.findState(TimeSpan.get(3601000L, 3660000L), Direction.BACKWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.FORWARD),
                plan.findState(TimeSpan.get(0L, 1L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.BACKWARD),
                plan.findState(TimeSpan.get(0L, 1L), Direction.BACKWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), Direction.FORWARD),
                plan.findState(TimeSpan.get(7140000L, 7150000L), Direction.FORWARD));
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), Direction.BACKWARD),
                plan.findState(TimeSpan.get(7140001L, 7200000L), Direction.BACKWARD));
    }

    /**
     * Test for {@link ContinuousAnimationPlan#getInitialState()} .
     */
    @Test
    public void testGetInitialState()
    {
        DefaultContinuousAnimationPlan plan = createPlan();

        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.FORWARD),
                plan.getInitialState());
    }

    /**
     * Test for {@link ContinuousAnimationPlan#getFinalState()} .
     */
    @Test
    public void testGetFinalState()
    {
        DefaultContinuousAnimationPlan plan = createPlan();

        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L), Direction.FORWARD),
                plan.getFinalState());
    }

    /**
     * Test for {@link ContinuousAnimationPlan#getFinalState(AnimationState)} .
     */
    @Test
    public void testGetFinalStateAnimationState()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 1000L), TimeSpan.get(1000L, 2000L));
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, new Milliseconds(500L),
                new Milliseconds(15L), EndBehavior.WRAP, null);

        DefaultContinuousAnimationState start;
        start = new DefaultContinuousAnimationState(0, TimeSpan.get(500L, 1000L), Direction.FORWARD);
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(1495L, 1995L), Direction.FORWARD),
                plan.getFinalState(start));
        start = new DefaultContinuousAnimationState(1, TimeSpan.get(1005L, 1505L), Direction.FORWARD);
        Assert.assertEquals(new DefaultContinuousAnimationState(1, TimeSpan.get(1500L, 2000L), Direction.FORWARD),
                plan.getFinalState(start));

        // go backwards
        start = new DefaultContinuousAnimationState(1, TimeSpan.get(1000L, 1500L), Direction.BACKWARD);
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(5L, 505L), Direction.BACKWARD),
                plan.getFinalState(start));
        start = new DefaultContinuousAnimationState(0, TimeSpan.get(490L, 990L), Direction.BACKWARD);
        Assert.assertEquals(new DefaultContinuousAnimationState(0, TimeSpan.get(10L, 510L), Direction.BACKWARD),
                plan.getFinalState(start));
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#getTimeSpanForState(io.opensphere.core.animation.AnimationState)}
     * .
     */
    @Test
    public void testGetTimeSpanForState()
    {
        Assert.assertEquals(TimeSpan.get(0L, 60000L), createPlan()
                .getTimeSpanForState(new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L), Direction.FORWARD)));
    }

    /**
     * Test for
     * {@link ContinuousAnimationPlan#getTimeSpanForState(io.opensphere.core.animation.AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTimeSpanForStateBadState()
    {
        createPlan().getTimeSpanForState(null);
    }

    /**
     * Helper that creates a plan.
     *
     * @return The plan.
     */
    protected DefaultContinuousAnimationPlan createPlan()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 3600000L));
        Duration window = Minutes.ONE;
        Duration advance = new Seconds(10);
        EndBehavior endBehavior = EndBehavior.STOP;
        TimeSpan limit = null;
        DefaultContinuousAnimationPlan plan = new DefaultContinuousAnimationPlan(sequence, window, advance, endBehavior, limit);
        return plan;
    }

    /**
     * Test some
     * {@link DefaultContinuousAnimationPlan#determineNextState(DefaultContinuousAnimationState)}
     * results that are common amongst some different end behaviors.
     *
     * @param plan The plan.
     */
    private void testDetermineNextStateCommon1(DefaultContinuousAnimationPlan plan)
    {
        DefaultContinuousAnimationState result2 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected2 = new DefaultContinuousAnimationState(1, TimeSpan.get(3610000L, 3670000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected2, result2);

        DefaultContinuousAnimationState result3 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(3530000L, 3590000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected3 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected3, result3);

        DefaultContinuousAnimationState result4 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected4 = new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected4, result4);

        DefaultContinuousAnimationState result5 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected5 = new DefaultContinuousAnimationState(1, TimeSpan.get(3610000L, 3670000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected5, result5);
    }

    /**
     * Test some
     * {@link DefaultContinuousAnimationPlan#determineNextState(DefaultContinuousAnimationState)}
     * results that are common amongst some different end behaviors.
     *
     * @param plan The plan.
     */
    private void testDetermineNextStateCommon2(DefaultContinuousAnimationPlan plan)
    {
        DefaultContinuousAnimationState result9 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected9 = new DefaultContinuousAnimationState(1, TimeSpan.get(7130000L, 7190000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected9, result9);

        DefaultContinuousAnimationState result10 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3610000L, 3670000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected10 = new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected10, result10);

        DefaultContinuousAnimationState result11 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected11 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected11, result11);

        DefaultContinuousAnimationState result12 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(10000L, 70000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected12 = new DefaultContinuousAnimationState(0, TimeSpan.get(0L, 60000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected12, result12);
    }

    /**
     * Test some
     * {@link DefaultContinuousAnimationPlan#determineNextState(DefaultContinuousAnimationState)}
     * results that are common amongst some different end behaviors.
     *
     * @param plan The plan.
     */
    private void testDetermineNextStateCommon3(DefaultContinuousAnimationPlan plan)
    {
        DefaultContinuousAnimationState result9 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected9 = new DefaultContinuousAnimationState(1, TimeSpan.get(4730000L, 4790000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected9, result9);

        DefaultContinuousAnimationState result10 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3610000L, 3670000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected10 = new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected10, result10);

        DefaultContinuousAnimationState result11 = plan.determineNextState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected11 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected11, result11);

        DefaultContinuousAnimationState result12 = plan.determineNextState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(1810000L, 1870000L), AnimationState.Direction.BACKWARD));
        DefaultAnimationState expected12 = new DefaultContinuousAnimationState(0, TimeSpan.get(1800000L, 1860000L),
                AnimationState.Direction.BACKWARD);
        Assert.assertEquals(expected12, result12);
    }

    /**
     * Test some
     * {@link DefaultContinuousAnimationPlan#determinePreviousState(DefaultContinuousAnimationState)}
     * results that are common amongst some different end behaviors.
     *
     * @param plan The plan.
     */
    private void testDeterminePreviousStateCommon(DefaultContinuousAnimationPlan plan)
    {
        DefaultContinuousAnimationState result2 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected2 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected2, result2);

        DefaultContinuousAnimationState result3 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3610000L, 3670000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected3 = new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected3, result3);

        DefaultContinuousAnimationState result4 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected4 = new DefaultContinuousAnimationState(0, TimeSpan.get(3530000L, 3590000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected4, result4);

        DefaultContinuousAnimationState result5 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(3600000L, 3660000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected5 = new DefaultContinuousAnimationState(0, TimeSpan.get(3540000L, 3600000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected5, result5);
    }

    /**
     * Test some
     * {@link DefaultContinuousAnimationPlan#determinePreviousState(DefaultContinuousAnimationState)}
     * results that are common amongst some different end behaviors.
     *
     * @param plan The plan.
     */
    private void testDeterminePreviousStateCommon1(DefaultContinuousAnimationPlan plan)
    {
        testDeterminePreviousStateCommon(plan);

        DefaultContinuousAnimationState result6 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(4730000L, 4790000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected7 = new DefaultContinuousAnimationState(1, TimeSpan.get(4720000L, 4780000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected7, result6);

        DefaultContinuousAnimationState result7 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(4740000L, 4800000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected8 = new DefaultContinuousAnimationState(1, TimeSpan.get(4730000L, 4790000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected8, result7);
    }

    /**
     * Test some
     * {@link DefaultContinuousAnimationPlan#determinePreviousState(DefaultContinuousAnimationState)}
     * results that are common amongst some different end behaviors.
     *
     * @param plan The plan.
     */
    private void testDeterminePreviousStateCommon2(DefaultContinuousAnimationPlan plan)
    {
        testDeterminePreviousStateCommon(plan);

        DefaultContinuousAnimationState result6 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7130000L, 7190000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected7 = new DefaultContinuousAnimationState(1, TimeSpan.get(7120000L, 7180000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected7, result6);

        DefaultContinuousAnimationState result7 = plan.determinePreviousState(
                new DefaultContinuousAnimationState(1, TimeSpan.get(7140000L, 7200000L), AnimationState.Direction.FORWARD));
        DefaultAnimationState expected8 = new DefaultContinuousAnimationState(1, TimeSpan.get(7130000L, 7190000L),
                AnimationState.Direction.FORWARD);
        Assert.assertEquals(expected8, result7);
    }
}
