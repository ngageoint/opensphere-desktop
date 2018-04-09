package io.opensphere.core.animationhelper;

import java.util.concurrent.Phaser;

import io.opensphere.core.AnimationChangeAdapter;
import io.opensphere.core.AnimationManager;
import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.TimeManager.ActiveTimeSpans;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.util.lang.QuietCloseable;
import io.opensphere.core.util.lang.ThreadUtilities;

/**
 * Notifies a {@link RefreshListener} when the time has changed. Also prevents
 * the timeline from moving to the next step until the {@link RefreshListener}
 * is done with refreshImages.
 */
public class TimeRefreshNotifier extends AnimationChangeAdapter implements QuietCloseable, ActiveTimeSpanChangeListener
{
    /**
     * The animation manager.
     */
    private final AnimationManager myAnimationManager;

    /**
     * The listener to notify.
     */
    private final RefreshListener myListener;

    /**
     * Keeps track of the time.
     */
    private final TimeManager myTimeManager;

    /**
     * Constructs a new {@link TimeRefreshNotifier}.
     *
     * @param refreshListener The listener to notify.
     * @param timeManager Used to notify us of time changes.
     * @param animationManager Used to help prevent the timeline from moving to
     *            the next step before the WAMI images have downloaded.
     */
    public TimeRefreshNotifier(RefreshListener refreshListener, TimeManager timeManager, AnimationManager animationManager)
    {
        myListener = refreshListener;
        myTimeManager = timeManager;
        myAnimationManager = animationManager;

        myTimeManager.addActiveTimeSpanChangeListener(this);
        myAnimationManager.addAnimationChangeListener(this);
    }

    @Override
    public void activeTimeSpansChanged(ActiveTimeSpans active)
    {
        synchronized (myListener)
        {
            myListener.refresh(false);
        }
    }

    @Override
    public void close()
    {
        myTimeManager.removeActiveTimeSpanChangeListener(this);
        myAnimationManager.removeAnimationChangeListener(this);
    }

    @Override
    public void commit(AnimationState state, Phaser phaser)
    {
        phaser.register();
        ThreadUtilities.runBackground(() ->
        {
            try
            {
                myListener.refreshNow();
            }
            finally
            {
                phaser.arriveAndDeregister();
            }
        });
    }

    /**
     * Gets the {@link RefreshListener}.
     *
     * @return The refresh listener.
     */
    protected RefreshListener getListener()
    {
        return myListener;
    }
}
