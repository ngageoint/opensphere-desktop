package io.opensphere.controlpanels.animation.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.CompositeLayer;
import io.opensphere.controlpanels.timeline.TimeWindowLayer;
import io.opensphere.controlpanels.timeline.TimelineLayer;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.core.control.action.context.TimespanContextKey;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.util.ConstantObservableValue;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.SwingUtilities;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * Layer for abstract intervals.
 */
abstract class IntervalsLayer extends CompositeLayer
{
    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The fill color. */
    private final Color myColorFill;

    /** The outline color. */
    private final Color myColorOutline;

    /** Whether initialization has been completed. */
    private boolean myInitComplete;

    /** The intervals. */
    private final ObservableList<TimeSpan> myIntervals;

    /** The intervals listener. */
    private ListDataListener<TimeSpan> myIntervalsListener;

    /** The past participle for generating labels. */
    private final String myPastParticiple;

    /** The present tense verb for generating labels. */
    private final String myPresentVerb;

    /** The menu icon for this layer. */
    private final Icon myMenuIcon;

    /**
     * Constructor.
     *
     * @param animationModel The animation model
     * @param intervals The observable list of intervals
     * @param presentVerb The present-tense label
     * @param pastParticiple The past participle label
     * @param colorOutline The outline color
     * @param colorFill The fill color
     */
    public IntervalsLayer(AnimationModel animationModel, ObservableList<TimeSpan> intervals, String presentVerb,
            String pastParticiple, Color colorOutline, Color colorFill)
    {
        this(animationModel, intervals, presentVerb, pastParticiple, null, colorOutline, colorFill);
    }

    /**
     * Constructor.
     *
     * @param animationModel The animation model
     * @param intervals The observable list of intervals
     * @param presentVerb The present-tense label
     * @param pastParticiple The past participle label
     * @param menuIcon the optional icon to use for menu items.
     * @param colorOutline The outline color
     * @param colorFill The fill color
     */
    public IntervalsLayer(AnimationModel animationModel, ObservableList<TimeSpan> intervals, String presentVerb,
            String pastParticiple, Icon menuIcon, Color colorOutline, Color colorFill)
    {
        super();
        myAnimationModel = animationModel;
        myIntervals = intervals;
        myPresentVerb = presentVerb;
        myPastParticiple = pastParticiple;
        myMenuIcon = menuIcon;
        myColorOutline = colorOutline;
        myColorFill = colorFill;
    }

    @Override
    public void getMenuItems(Point p, List<JMenuItem> menuItems)
    {
        super.getMenuItems(p, menuItems);

        if (!myAnimationModel.getPlayState().isPlaying())
        {
            final TimeInstant time = getUIModel().xToTime(p.x);

            final TimeWindowLayer matchingLayer = getOverlappingLayer(time);
            if (matchingLayer != null)
            {
                String text = StringUtilities.concat("Remove ", myPastParticiple, " span");
                menuItems.add(SwingUtilities.newMenuItem(text, e -> handleRemoveAction(matchingLayer.getTimeSpan().get())));
            }
            else
            {
                final Duration dataLoadDuration = myAnimationModel.getSelectedDataLoadDuration().get();
                if (dataLoadDuration != null)
                {
                    JMenuItem item;
                    if (myMenuIcon == null)
                    {
                        item = new JMenuItem(StringUtilities.concat(StringUtilities.capitalize(myPresentVerb), " this ",
                                dataLoadDuration.getLongLabel(false)));
                    }
                    else
                    {
                        item = new JMenuItem(StringUtilities.concat(StringUtilities.capitalize(myPresentVerb), " this ",
                                dataLoadDuration.getLongLabel(false)), myMenuIcon);
                    }
                    item.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            Calendar start = TimelineUtilities.roundDown(time.toDate(), dataLoadDuration);
                            myIntervals.add(TimeSpan.get(start.getTime(), dataLoadDuration));
                        }
                    });
                    menuItems.add(item);
                }
            }
        }
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, TimespanContextKey key)
    {
        if (!myAnimationModel.getPlayState().isPlaying())
        {
            String text = StringUtilities.capitalize(myPresentVerb + " span");
            return Collections.singletonList(
                    SwingUtilities.newMenuItem(text, myMenuIcon, e -> myIntervals.add(snapSpan(key.getTimeSpan()))));
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getToolTipText(MouseEvent event, String incoming)
    {
        String toolTipText = incoming;
        if (!getLayers().isEmpty())
        {
            // This should probably done in paint() but seems to work pretty
            // well here
            for (TimelineLayer layer : getLayers())
            {
                if (layer instanceof TimeWindowLayer)
                {
                    ((TimeWindowLayer)layer).setDurationLabelVisible(false);
                    ((TimeWindowLayer)layer).setStartLabelVisible(false);
                    ((TimeWindowLayer)layer).setEndLabelVisible(false);
                }
            }

            TimeWindowLayer matchingLayer = (TimeWindowLayer)StreamUtilities.filterOne(getLayers(),
                    l -> l instanceof TimeWindowLayer && ((TimeWindowLayer)l).getRectangle().contains(event.getPoint()));
            if (matchingLayer != null)
            {
                toolTipText = StringUtilities.capitalize(myPastParticiple + " span");

                // This should probably done in paint() but seems to work pretty
                // well here
                matchingLayer.setDurationLabelVisible(true);
                matchingLayer.setStartLabelVisible(true);
                matchingLayer.setEndLabelVisible(true);
            }
            else
            {
                toolTipText = super.getToolTipText(event, incoming);
            }
        }
        return toolTipText;
    }

    @Override
    public void setUIModel(TimelineUIModel model)
    {
        super.setUIModel(model);
        initialize();
    }

    /**
     * Gets the first layer that overlaps the time, or null.
     *
     * @param time the time
     * @return the layer, or null
     */
    protected TimeWindowLayer getOverlappingLayer(final TimeInstant time)
    {
        return (TimeWindowLayer)StreamUtilities.filterOne(getLayers(),
                l -> l instanceof TimeWindowLayer && ((TimeWindowLayer)l).getTimeSpan().get().overlaps(time));
    }

    /**
     * Handles a remove time span action by the user.
     *
     * @param span the span to remove
     */
    protected void handleRemoveAction(TimeSpan span)
    {
        // Do this later to prevent co-modification of the list of composite
        // layers
        EventQueue.invokeLater(() -> myIntervals.remove(span));
    }

    /**
     * Adds the given interval.
     *
     * @param span the interval
     */
    private void addInterval(TimeSpan span)
    {
        addLayer(new TimeWindowLayer(new ConstantObservableValue<>(span), myColorOutline, myColorFill, this::handleRemoveAction));

        getUIModel().setFirstMousePoint(null);
        getUIModel().setLockSelection(false);
        getUIModel().repaint();
    }

    /**
     * Initializes stuff.
     */
    private void initialize()
    {
        if (!myInitComplete)
        {
            for (TimeSpan span : myIntervals)
            {
                addInterval(span);
            }

            myIntervalsListener = new ListDataListener<TimeSpan>()
            {
                @Override
                public void elementsAdded(ListDataEvent<TimeSpan> e)
                {
                    for (TimeSpan span : e.getChangedElements())
                    {
                        addInterval(span);
                    }
                }

                @Override
                public void elementsChanged(ListDataEvent<TimeSpan> e)
                {
                }

                @Override
                public void elementsRemoved(ListDataEvent<TimeSpan> e)
                {
                    for (TimeSpan span : e.getChangedElements())
                    {
                        removeInterval(span);
                    }
                }
            };
            myIntervals.addChangeListener(myIntervalsListener);

            myInitComplete = true;
        }
    }

    /**
     * Removes the given interval.
     *
     * @param span the interval
     */
    private void removeInterval(final TimeSpan span)
    {
        Predicate<TimelineLayer> equalsSpanPredicate = layer -> layer instanceof TimeWindowLayer
                && ((TimeWindowLayer)layer).getTimeSpan().get().equals(span);
        TimelineLayer matchingLayer = StreamUtilities.filterOne(getLayers(), equalsSpanPredicate);
        if (matchingLayer != null)
        {
            removeLayer(matchingLayer);
        }

        getUIModel().repaint();
    }

    /**
     * Snaps the span to the data load duration if necessary.
     *
     * @param span the span
     * @return the snapped span
     */
    protected TimeSpan snapSpan(TimeSpan span)
    {
        TimeSpan snappedSpan = span;
        Duration snapDuration = myAnimationModel.getSnapToDataBoundaries().get().booleanValue()
                ? myAnimationModel.getSelectedDataLoadDuration().get() : null;
        if (snapDuration != null)
        {
            DurationBasedSnapFunction snapFunction = new DurationBasedSnapFunction(TimeInstant.get(0), snapDuration);
            TimeInstant start = snapFunction.getSnapDestination(span.getStartInstant(), RoundingMode.FLOOR);
            TimeInstant end = snapFunction.getSnapDestination(span.getEndInstant(), RoundingMode.CEILING);
            snappedSpan = TimeSpan.get(start, end);
        }
        return snappedSpan;
    }
}
