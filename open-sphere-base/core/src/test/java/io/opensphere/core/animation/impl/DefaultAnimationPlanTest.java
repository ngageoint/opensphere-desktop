package io.opensphere.core.animation.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanArrayList;

/**
 * Test for {@link DefaultAnimationPlan}.
 */
public class DefaultAnimationPlanTest
{
    /**
     * Test for
     * {@link AnimationPlan#calculateDistance(AnimationState, AnimationState)} .
     */
    @Test
    public void testCalculateDistanceAnimationStateAnimationState()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        Assert.assertEquals(2, stopPlan.calculateDistance(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(Integer.MAX_VALUE,
                stopPlan.calculateDistance(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                        new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(0, stopPlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(0, stopPlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(Integer.MAX_VALUE,
                stopPlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                        new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(1, stopPlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(Integer.MAX_VALUE,
                stopPlan.calculateDistance(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                        new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(1, stopPlan.calculateDistance(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));

        DefaultAnimationPlan bouncePlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.BOUNCE);
        Assert.assertEquals(2, bouncePlan.calculateDistance(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(2, bouncePlan.calculateDistance(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(0, bouncePlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(0, bouncePlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(1, bouncePlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(1, bouncePlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(3, bouncePlan.calculateDistance(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(1, bouncePlan.calculateDistance(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));

        DefaultAnimationPlan wrapPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.WRAP);
        Assert.assertEquals(2, wrapPlan.calculateDistance(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(1, wrapPlan.calculateDistance(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(0, wrapPlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(0, wrapPlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(2, wrapPlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(1, wrapPlan.calculateDistance(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(2, wrapPlan.calculateDistance(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(1, wrapPlan.calculateDistance(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
    }

    /**
     * Test for
     * {@link AnimationPlan#calculateDistance(AnimationState, AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateDistanceAnimationStateDateBadDate()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        stopPlan.calculateDistance(new DefaultAnimationState(0, AnimationState.Direction.FORWARD), null);
    }

    /**
     * Test for
     * {@link AnimationPlan#calculateDistance(AnimationState, AnimationState)}
     * with bad input .
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateDistanceAnimationStateDateBadState1()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        stopPlan.calculateDistance(new DefaultAnimationState(-1, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(3, AnimationState.Direction.FORWARD));
    }

    /**
     * Test for
     * {@link AnimationPlan#calculateDistance(AnimationState, AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateDistanceAnimationStateDateBadState2()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        stopPlan.calculateDistance(new DefaultAnimationState(3, AnimationState.Direction.FORWARD),
                new DefaultAnimationState(3, AnimationState.Direction.FORWARD));
    }

    /** Test normal construction. */
    @SuppressWarnings("unused")
    @Test
    public void testConstructor()
    {
        new DefaultAnimationPlan(Collections.singletonList(TimeSpan.get(0L, 100L)), AnimationPlan.EndBehavior.STOP);
    }

    /** Test construction with an empty sequence. */
    @SuppressWarnings("unused")
    @Test
    public void testConstructorEmptySequence()
    {
        new DefaultAnimationPlan(Collections.<TimeSpan>emptyList(), AnimationPlan.EndBehavior.STOP);
    }

    /** Test construction with a null end behavior. */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullEndBehavior()
    {
        new DefaultAnimationPlan(Collections.singletonList(TimeSpan.get(0L, 100L)), null);
    }

    /** Test construction with a null sequence. */
    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullSequence()
    {
        new DefaultAnimationPlan(null, AnimationPlan.EndBehavior.STOP);
    }

    /** Test for {@link AnimationPlan#determineNextState(AnimationState)}. */
    @Test
    public void testDetermineNextState()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                stopPlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                stopPlan.determineNextState(new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertNull(stopPlan.determineNextState(new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                stopPlan.determineNextState(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                stopPlan.determineNextState(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD)));
        Assert.assertNull(stopPlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));

        DefaultAnimationPlan bouncePlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.BOUNCE);
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                bouncePlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                bouncePlan.determineNextState(new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                bouncePlan.determineNextState(new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                bouncePlan.determineNextState(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                bouncePlan.determineNextState(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                bouncePlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));

        DefaultAnimationPlan wrapPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.WRAP);
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                wrapPlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                wrapPlan.determineNextState(new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                wrapPlan.determineNextState(new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                wrapPlan.determineNextState(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                wrapPlan.determineNextState(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                wrapPlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));
    }

    /**
     * Test for {@link AnimationPlan#determineNextState(AnimationState)} with
     * bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDetermineNextStateBadState1()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        stopPlan.determineNextState(new DefaultAnimationState(-1, AnimationState.Direction.FORWARD));
    }

    /**
     * Test for {@link AnimationPlan#determineNextState(AnimationState)} with
     * bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDetermineNextStateBadState2()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        stopPlan.determineNextState(new DefaultAnimationState(3, AnimationState.Direction.FORWARD));
    }

    /**
     * Test for {@link AnimationPlan#determineNextState(AnimationState)} with
     * bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDetermineNextStateBadState3()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        stopPlan.determineNextState(null);
    }

    /**
     * Test for {@link AnimationPlan#determineNextState(AnimationState)} with a
     * singleton sequence.
     */
    @Test
    public void testDetermineNextStateSingletonSequence()
    {
        List<? extends TimeSpan> sequence = Collections.singletonList(TimeSpan.get(100L, 200L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        Assert.assertNull(stopPlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertNull(stopPlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));

        DefaultAnimationPlan bouncePlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.BOUNCE);
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                bouncePlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                bouncePlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));

        DefaultAnimationPlan wrapPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.WRAP);
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                wrapPlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                wrapPlan.determineNextState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));
    }

    /**
     * Test for {@link AnimationPlan#determinePreviousState(AnimationState)}.
     */
    @Test
    public void testDeterminePreviousState()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                stopPlan.determinePreviousState(new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                stopPlan.determinePreviousState(new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertNull(stopPlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                stopPlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                stopPlan.determinePreviousState(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD)));
        Assert.assertNull(stopPlan.determinePreviousState(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD)));

        DefaultAnimationPlan bouncePlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.BOUNCE);
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                bouncePlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                bouncePlan.determinePreviousState(new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                bouncePlan.determinePreviousState(new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                bouncePlan.determinePreviousState(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                bouncePlan.determinePreviousState(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                bouncePlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));

        DefaultAnimationPlan wrapPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.WRAP);
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                wrapPlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                wrapPlan.determinePreviousState(new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                wrapPlan.determinePreviousState(new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));

        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                wrapPlan.determinePreviousState(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                wrapPlan.determinePreviousState(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD)));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                wrapPlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));
    }

    /**
     * Test for {@link AnimationPlan#determinePreviousState(AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeterminePreviousStateBadState1()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        stopPlan.determinePreviousState(new DefaultAnimationState(-1, AnimationState.Direction.FORWARD));
    }

    /**
     * Test for {@link AnimationPlan#determinePreviousState(AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeterminePreviousStateBadState2()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        stopPlan.determinePreviousState(new DefaultAnimationState(3, AnimationState.Direction.FORWARD));
    }

    /**
     * Test for {@link AnimationPlan#determinePreviousState(AnimationState)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDeterminePreviousStateBadState3()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        stopPlan.determinePreviousState(null);
    }

    /**
     * Test for {@link AnimationPlan#determinePreviousState(AnimationState)}
     * with a singleton sequence.
     */
    @Test
    public void testDeterminePreviousStateSingletonSequence()
    {
        List<? extends TimeSpan> sequence = Collections.singletonList(TimeSpan.get(100L, 200L));

        DefaultAnimationPlan stopPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        Assert.assertNull(stopPlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertNull(stopPlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));

        DefaultAnimationPlan bouncePlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.BOUNCE);
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                bouncePlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                bouncePlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));

        DefaultAnimationPlan wrapPlan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.WRAP);
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                wrapPlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                wrapPlan.determinePreviousState(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD)));
    }

    /**
     * Test for
     * {@link AnimationPlan#findState(Date, io.opensphere.core.animation.AnimationState.Direction)}
     * .
     */
    @Test
    public void testFindStateDateDirection()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);

        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                plan.findState(new Date(0L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                plan.findState(new Date(0L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                plan.findState(new Date(100L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                plan.findState(new Date(100L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                plan.findState(new Date(150L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                plan.findState(new Date(150L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                plan.findState(new Date(151L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                plan.findState(new Date(151L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                plan.findState(new Date(200L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                plan.findState(new Date(200L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                plan.findState(new Date(299), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                plan.findState(new Date(299L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                plan.findState(new Date(300L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                plan.findState(new Date(300L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                plan.findState(new Date(400L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                plan.findState(new Date(400L), AnimationState.Direction.BACKWARD));
    }

    /**
     * Test for
     * {@link AnimationPlan#findState(Date, io.opensphere.core.animation.AnimationState.Direction)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFindStateDateDirectionBadDate()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);

        plan.findState((Date)null, AnimationState.Direction.FORWARD);
    }

    /**
     * Test for
     * {@link AnimationPlan#findState(Date, io.opensphere.core.animation.AnimationState.Direction)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFindStateDateDirectionBadDirection()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);

        plan.findState(new Date(0L), null);
    }

    /**
     * Test for
     * {@link AnimationPlan#findState(TimeSpan, io.opensphere.core.animation.AnimationState.Direction)}
     * .
     */
    @Test
    public void testFindStateTimeSpanDirection()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L), TimeSpan.get(0L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);

        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD),
                plan.findState(TimeSpan.get(0L, 100L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.BACKWARD),
                plan.findState(TimeSpan.get(0L, 100L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                plan.findState(TimeSpan.get(200L, 300L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                plan.findState(TimeSpan.get(200L, 300L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.FORWARD),
                plan.findState(TimeSpan.get(299L, 300L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(1, AnimationState.Direction.BACKWARD),
                plan.findState(TimeSpan.get(299L, 300L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.FORWARD),
                plan.findState(TimeSpan.get(300L, 400L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.BACKWARD),
                plan.findState(TimeSpan.get(300L, 400L), AnimationState.Direction.BACKWARD));

        Assert.assertEquals(new DefaultAnimationState(3, AnimationState.Direction.FORWARD),
                plan.findState(TimeSpan.get(0L, 400L), AnimationState.Direction.FORWARD));
        Assert.assertEquals(new DefaultAnimationState(3, AnimationState.Direction.BACKWARD),
                plan.findState(TimeSpan.get(0L, 400L), AnimationState.Direction.BACKWARD));
    }

    /**
     * Test for
     * {@link AnimationPlan#findState(TimeSpan, io.opensphere.core.animation.AnimationState.Direction)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFindStateTimeSpanDirectionBadDate()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);

        plan.findState((TimeSpan)null, AnimationState.Direction.FORWARD);
    }

    /**
     * Test for
     * {@link AnimationPlan#findState(TimeSpan, io.opensphere.core.animation.AnimationState.Direction)}
     * with bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testFindStateTimeSpanDirectionBadDirection()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);

        plan.findState(TimeSpan.get(0L, 100L), null);
    }

    /** Test for {@link AnimationPlan#getAnimationSequence()}. */
    @Test
    public void testGetAnimationSequenceDateIntDirection()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        Assert.assertEquals(new TimeSpanArrayList(Collections.<TimeSpan>emptyList()), plan.getAnimationSequence(
                plan.findState(new Date(0L), AnimationState.Direction.FORWARD), 0, AnimationState.Direction.FORWARD));
        Assert.assertEquals(new TimeSpanArrayList(Collections.singleton(TimeSpan.get(200L, 300L))), plan.getAnimationSequence(
                plan.findState(new Date(0L), AnimationState.Direction.FORWARD), 1, AnimationState.Direction.FORWARD));
    }

    /** Test for {@link AnimationPlan#getFinalState()}. */
    @Test
    public void testGetFinalState()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        Assert.assertEquals(new DefaultAnimationState(2, AnimationState.Direction.FORWARD), plan.getFinalState());
    }

    /** Test for {@link AnimationPlan#getInitialState()}. */
    @Test
    public void testGetInitialState()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(100L, 200L),
                TimeSpan.get(200L, 300L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        Assert.assertEquals(new DefaultAnimationState(0, AnimationState.Direction.FORWARD), plan.getInitialState());
    }

    /** Test for {@link AnimationPlan#getTimeSpanForState(AnimationState)}. */
    @Test
    public void testGetTimeSpanForState()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        Assert.assertSame(sequence.get(0),
                plan.getTimeSpanForState(new DefaultAnimationState(0, AnimationState.Direction.FORWARD)));
        Assert.assertSame(sequence.get(1),
                plan.getTimeSpanForState(new DefaultAnimationState(1, AnimationState.Direction.FORWARD)));
        Assert.assertSame(sequence.get(2),
                plan.getTimeSpanForState(new DefaultAnimationState(2, AnimationState.Direction.FORWARD)));
    }

    /**
     * Test for {@link AnimationPlan#getTimeSpanForState(AnimationState)} with
     * bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTimeSpanForStateBadState1()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        plan.getTimeSpanForState(new DefaultAnimationState(-1, AnimationState.Direction.FORWARD));
    }

    /**
     * Test for {@link AnimationPlan#getTimeSpanForState(AnimationState)} with
     * bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTimeSpanForStateBadState2()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        plan.getTimeSpanForState(new DefaultAnimationState(3, AnimationState.Direction.FORWARD));
    }

    /**
     * Test for {@link AnimationPlan#getTimeSpanForState(AnimationState)} with
     * bad input.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetTimeSpanForStateBadState3()
    {
        List<? extends TimeSpan> sequence = Arrays.asList(TimeSpan.get(0L, 100L), TimeSpan.get(200L, 300L),
                TimeSpan.get(300L, 400L));

        DefaultAnimationPlan plan = new DefaultAnimationPlan(sequence, AnimationPlan.EndBehavior.STOP);
        plan.getTimeSpanForState(null);
    }
}
