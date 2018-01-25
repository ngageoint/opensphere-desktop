package io.opensphere.controlpanels.animation.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.ObservableValue;

/**
 * An action that will add an interval to an observable list of intervals.
 */
public class IntervalAction extends AbstractAction
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The intervals to be added to. */
    private final ObservableList<TimeSpan> myIntervals;

    /**
     * The span to add to the intervals when this action is invoked.
     */
    private final ObservableValue<TimeSpan> myTimeSpan;

    /**
     * Constructor.
     *
     * @param name The name of the action.
     * @param intervals The spans to be added to.
     * @param timeSpan The span to add to the spans when this action is invoked.
     */
    public IntervalAction(String name, ObservableList<TimeSpan> intervals, ObservableValue<TimeSpan> timeSpan)
    {
        super(name);
        myIntervals = intervals;
        myTimeSpan = timeSpan;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        myIntervals.add(myTimeSpan.get());
    }
}
