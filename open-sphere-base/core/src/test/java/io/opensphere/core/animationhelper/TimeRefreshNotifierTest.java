package io.opensphere.core.animationhelper;

import java.util.concurrent.Phaser;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpans;
import io.opensphere.core.animation.AnimationState;

/**
 * Unit test for {@link TimeRefreshNotifier}.
 */
public class TimeRefreshNotifierTest
{
    /**
     * Tests when the active time spans changed.
     */
    @Test
    public void testActiveTimeSpansChanged()
    {
        EasyMockSupport support = new EasyMockSupport();

        RefreshListener listener = createListener(support);
        TimeManager timeManager = createTimeManager(support);
        AnimationManager animationManager = createAnimationManager(support);
        ActiveTimeSpans active = createActive(support);

        support.replayAll();

        try (TimeRefreshNotifier notifier = new TimeRefreshNotifier(listener, timeManager, animationManager))
        {
            notifier.activeTimeSpansChanged(active);
        }

        support.verifyAll();
    }

    /**
     * Tests when the animation plan changes.
     */
    @Test
    public void testCommit()
    {
        EasyMockSupport support = new EasyMockSupport();

        RefreshListener listener = support.createMock(RefreshListener.class);
        listener.refreshNow();
        TimeManager timeManager = createTimeManager(support);
        AnimationManager animationManager = createAnimationManager(support);
        AnimationState state = support.createMock(AnimationState.class);

        support.replayAll();

        try (TimeRefreshNotifier notifier = new TimeRefreshNotifier(listener, timeManager, animationManager))
        {
            Phaser phaser = new Phaser(1);
            notifier.commit(state, phaser);
            phaser.arriveAndAwaitAdvance();
        }

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link ActiveTimeSpans}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link ActiveTimeSpans}.
     */
    private ActiveTimeSpans createActive(EasyMockSupport support)
    {
        ActiveTimeSpans active = support.createMock(ActiveTimeSpans.class);

        return active;
    }

    /**
     * Create an easy mocked {@link AnimationManager}.
     *
     * @param support Used to create the mock.
     * @return The {@link AnimationManager}.
     */
    private AnimationManager createAnimationManager(EasyMockSupport support)
    {
        AnimationManager animationManager = support.createMock(AnimationManager.class);

        animationManager.addAnimationChangeListener(EasyMock.isA(TimeRefreshNotifier.class));
        animationManager.removeAnimationChangeListener(EasyMock.isA(TimeRefreshNotifier.class));

        return animationManager;
    }

    /**
     * Creates an easy mocked {@link RefreshListener}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link RefreshListener}.
     */
    private RefreshListener createListener(EasyMockSupport support)
    {
        RefreshListener listener = support.createMock(RefreshListener.class);
        listener.refresh(EasyMock.eq(false));

        return listener;
    }

    /**
     * Creates an easy mocked {@link TimeManager}.
     *
     * @param support Used to create the mock.
     * @return The mocked {@link TimeManager}.
     */
    private TimeManager createTimeManager(EasyMockSupport support)
    {
        TimeManager timeManager = support.createMock(TimeManager.class);

        timeManager.addActiveTimeSpanChangeListener(EasyMock.isA(TimeRefreshNotifier.class));
        timeManager.removeActiveTimeSpanChangeListener(EasyMock.isA(TimeRefreshNotifier.class));

        return timeManager;
    }
}
