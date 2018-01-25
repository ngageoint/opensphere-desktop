package io.opensphere.controlpanels.animation.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanArrayList;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * An action that will set a time model to a certain duration.
 */
public final class SetDurationAction extends AbstractAction
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The duration. */
    private final Duration myDuration;

    /** The time model. */
    private final ObservableValue<TimeSpan> myTimeModel;

    /** The uI model. */
    private final TimelineUIModel myUIModel;

    /**
     * Constructor.
     *
     * @param timeModel The time model.
     * @param animationModel The animation model.
     * @param timelineUIModel The timeline ui model.
     * @param dur The duration.
     */
    public SetDurationAction(ObservableValue<TimeSpan> timeModel, AnimationModel animationModel, TimelineUIModel timelineUIModel,
            Duration dur)
    {
        super(dur.toLongLabelString());
        myTimeModel = timeModel;
        myAnimationModel = animationModel;
        myUIModel = timelineUIModel;
        myDuration = dur;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        TimeInstant start = TimeInstant.get(TimelineUtilities.roundDown(myTimeModel.get().getStartDate(), myDuration));
        TimeSpan value = TimeSpan.get(start, myDuration);
        ObservableList<TimeSpan> skippedIntervals = myAnimationModel.getSkippedIntervals();
        if (new TimeSpanArrayList(skippedIntervals).intersects(value))
        {
            myUIModel.getTemporaryMessage().set("Cannot set duration due to skipped span");
        }
        else
        {
            myTimeModel.set(value);
        }
    }
}
