package io.opensphere.controlpanels.animation.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.RoundingMode;
import java.util.function.Supplier;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.controlpanels.animation.view.ActiveSpanEndSnapFunction;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ObservableValueService;
import io.opensphere.core.util.lang.StringUtilities;

/** Controller for "live" mode. */
class LiveModeController extends ObservableValueService
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LiveModeController.class);

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The last time span that I set in the time model. */
    private TimeSpan myLastTimeSpan;

    /** Timer used to keep the active span covering now. */
    private Timer myLiveTimer;

    /** The active time model. */
    private final TimeManagerWrapper myTimeModel;

    /** The UI model. */
    private final TimelineUIModel myUIModel;

    /**
     * Constructor.
     *
     * @param animationModel The animation model.
     * @param timeModel The time model.
     * @param uiModel The UI model, used for scrolling the timeline to keep up
     *            with time now.
     */
    public LiveModeController(AnimationModel animationModel, TimeManagerWrapper timeModel, TimelineUIModel uiModel)
    {
        myAnimationModel = animationModel;
        myTimeModel = timeModel;
        myUIModel = uiModel;

        bindModel(myAnimationModel.getLiveMode(), new ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                Boolean isLiveMode = newValue;
                if (isLiveMode.booleanValue())
                {
                    myAnimationModel.setPlayState(PlayState.STOP);
                    int initialDelay = (int)(Constants.MILLI_PER_UNIT - System.currentTimeMillis() % Constants.MILLI_PER_UNIT);
                    myLiveTimer = new Timer(initialDelay, new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            setActiveSpanToNow();
                        }
                    });
                    myLiveTimer.setRepeats(false);
                    myLiveTimer.start();

                    ObservableValue<TimeSpan> uiSpan = myUIModel.getUISpan();
                    TimeInstant now = TimeInstant.get();
                    if (!uiSpan.get().overlaps(now))
                    {
                        Duration delta = now.minus(uiSpan.get().getMidpointInstant());
                        if (!delta.isZero())
                        {
                            uiSpan.set(uiSpan.get().plus(delta));
                        }
                    }
                }
                else
                {
                    myLiveTimer.stop();
                    myLiveTimer = null;
                    myLastTimeSpan = null;
                }
            }
        });

        bindModel(myTimeModel, new ChangeListener<TimeSpan>()
        {
            @Override
            public void changed(ObservableValue<? extends TimeSpan> observable, TimeSpan oldValue, TimeSpan newValue)
            {
                if (myLastTimeSpan != null && !myTimeModel.get().equals(myLastTimeSpan))
                {
                    myAnimationModel.getLiveMode().set(Boolean.FALSE);
                }
            }
        });

        bindModel(myUIModel.getMillisPerPixel(), new ChangeListener<Double>()
        {
            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue)
            {
                if (myLiveTimer != null)
                {
                    myLiveTimer.setInitialDelay(Constants.MILLI_PER_UNIT);
                    myLiveTimer.restart();
                }
            }
        });
    }

    /**
     * Adjust the active span to cover the current time.
     */
    private void setActiveSpanToNow()
    {
        @SuppressWarnings("PMD.PrematureDeclaration")
        long t0 = System.nanoTime();

        // Get the current time to the nearest millisecond.
        TimeInstant timenow = TimeInstant.get(MathUtil.roundDownTo(System.currentTimeMillis(), Constants.MILLI_PER_UNIT));

        // Make sure time now isn't in a skipped interval.
        if (timenow.isOverlapped(myAnimationModel.getSkippedIntervals()))
        {
            myAnimationModel.getLiveMode().set(Boolean.FALSE);
            myUIModel.getTemporaryMessage().set("Current time overlaps a skipped interval. Live mode cancelled.");
            return;
        }

        // Add a buffer based on the current zoom level of the display.
        TimeInstant timenowPlusBuffer = timenow.plus(
                new Milliseconds(MathUtil.roundDownTo(myUIModel.getMillisPerPixel().get().intValue(), Constants.MILLI_PER_UNIT)));

        // Do snapping.
        TimeInstant end = new ActiveSpanEndSnapFunction(myAnimationModel, (Supplier<Double>)null)
                .getSnapDestination(timenowPlusBuffer, RoundingMode.CEILING);

        if (myAnimationModel.getLoopSpanLocked().get().booleanValue() && !myAnimationModel.getLoopSpan().get().overlaps(end))
        {
            int answer = JOptionPane.showConfirmDialog(myUIModel.getComponent(),
                    "The loop span is locked and it does not overlap time now.\nWould you like to unlock it?",
                    "Unlock loop span?", JOptionPane.OK_CANCEL_OPTION);
            if (answer == JOptionPane.OK_OPTION)
            {
                myAnimationModel.getLoopSpanLocked().set(Boolean.FALSE);
            }
            else
            {
                myAnimationModel.getLiveMode().set(Boolean.FALSE);
                myUIModel.getTemporaryMessage().set("Live mode cancelled.");
                return;
            }
        }

        myLastTimeSpan = TimeSpan.get(myAnimationModel.getActiveSpanDuration().get(), end);

        // Make sure the active duration wasn't smaller than the buffer.
        if (!myLastTimeSpan.overlaps(timenow))
        {
            end = new ActiveSpanEndSnapFunction(myAnimationModel, (Supplier<Double>)null).getSnapDestination(timenow,
                    RoundingMode.CEILING);
            myLastTimeSpan = TimeSpan.get(myAnimationModel.getActiveSpanDuration().get(), end);
        }

        // Try to keep the active span in the same location on the screen.
        TimeSpan oldActive = myTimeModel.get();
        if (myTimeModel.set(myLastTimeSpan))
        {
            ObservableValue<TimeSpan> uiSpan = myUIModel.getUISpan();
            if (uiSpan.get().overlaps(oldActive))
            {
                Duration delta = myLastTimeSpan.getStartInstant().minus(oldActive.getStartInstant());
                if (!delta.isZero())
                {
                    uiSpan.set(uiSpan.get().plus(delta));
                }
            }
        }

        // Schedule the next update.
        int delay = Math.max(Constants.MILLI_PER_UNIT, myUIModel.getMillisPerPixel().get().intValue())
                - (int)(System.currentTimeMillis() % Constants.MILLI_PER_UNIT);
        myLiveTimer.setInitialDelay(delay);
        myLiveTimer.start();

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace(StringUtilities.formatTimingMessage("Time to set active to now: ", System.nanoTime() - t0));
        }
    }
}
