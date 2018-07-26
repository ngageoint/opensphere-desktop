package io.opensphere.controlpanels.animation.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;

import io.opensphere.controlpanels.animation.model.Action;
import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.CompositeLayer;
import io.opensphere.controlpanels.timeline.DragHandlesLayer;
import io.opensphere.controlpanels.timeline.DraggableTimeWindowLayer;
import io.opensphere.controlpanels.timeline.ObservableTimeSpan;
import io.opensphere.controlpanels.timeline.ResolutionBasedSnapFunction;
import io.opensphere.controlpanels.timeline.SnapFunction;
import io.opensphere.controlpanels.timeline.TimeWindowLayer;
import io.opensphere.controlpanels.timeline.TimelineLayer;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.context.TimespanContextKey;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ListDataEvent;
import io.opensphere.core.util.ListDataListener;
import io.opensphere.core.util.ObservableList;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.StrongObservableValue;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.swing.GenericFontIcon;
import io.opensphere.core.util.swing.SwingUtilities;

/** Load span(s) layer in the timeline. */
public class LoadIntervalsLayer extends CompositeLayer
{
    /** The intervals from the external model. */
    private final ObservableList<TimeSpan> myExternalModel;

    /**
     * The internal model (matches the external one, but with observable
     * individual spans).
     */
    private final List<ObservableTimeSpan> myInternalModel = New.list();

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The outline color. */
    private final Color myColorOutline;

    /** Whether initialization has been completed. */
    private boolean myInitComplete;

    /** The intervals listener. */
    private ListDataListener<TimeSpan> myIntervalsListener;

    /** The milliseconds per pixel. */
    private final ObservableValue<Double> myMillisPerPixel;

    /** The loop span listener to synchronize it's load span. */
    private final ChangeListener<? super TimeSpan> myLoopSpanListener = (obs, o, n) -> handleLoopChange(o, n);

    /** The toolbox through which application state is accessed. */
    private final Toolbox myToolbox;

    /**
     * Constructor.
     * 
     * @param toolbox The toolbox through which application state is accessed.
     * @param intervals The observable list of intervals
     * @param millisPerPixel The milliseconds per pixel
     * @param animationModel The animation model
     */
    public LoadIntervalsLayer(Toolbox toolbox, ObservableList<TimeSpan> intervals, ObservableValue<Double> millisPerPixel,
            AnimationModel animationModel)
    {
        super();
        myToolbox = toolbox;
        myExternalModel = intervals;
        myMillisPerPixel = millisPerPixel;
        myAnimationModel = animationModel;
        myColorOutline = Color.YELLOW;
    }

    @Override
    public void getMenuItems(Point p, List<JMenuItem> menuItems)
    {
        super.getMenuItems(p, menuItems);

        final TimeInstant time = getUIModel().xToTime(p.x);

        final TimeWindowLayer matchingLayer = getOverlappingLayer(time);
        if (matchingLayer != null)
        {
            menuItems.add(
                    SwingUtilities.newMenuItem("Remove load span", e -> handleRemoveAction(matchingLayer.getTimeSpan().get())));
        }
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, TimespanContextKey key)
    {
        List<JMenuItem> menuItems = New.list(2);
        menuItems.add(SwingUtilities.newMenuItem("Load", new GenericFontIcon(AwesomeIconSolid.CLOUD_DOWNLOAD_ALT, Color.YELLOW),
                e -> loadIntervalExternal(key.getTimeSpan())));
        menuItems.add(SwingUtilities.newMenuItem("Add", new GenericFontIcon(AwesomeIconSolid.PLUS, Color.YELLOW),
                e -> addIntervalExternal(key.getTimeSpan())));
        return menuItems;
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
                toolTipText = "Load span";

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
     * Initializes stuff.
     */
    private void initialize()
    {
        if (!myInitComplete)
        {
            for (TimeSpan span : myExternalModel)
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
                    for (TimeSpan span : e.getPreviousElements())
                    {
                        removeInterval(span);
                    }
                    for (TimeSpan span : e.getChangedElements())
                    {
                        addInterval(span);
                    }
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
            myExternalModel.addChangeListener(myIntervalsListener);

            myInitComplete = true;
        }
    }

    /**
     * Adds the given interval.
     *
     * @param span the interval
     */
    private void addInterval(TimeSpan span)
    {
        StrongObservableValue<TimeSpan> observableSpan = new StrongObservableValue<>();
        observableSpan.set(span);
        observableSpan.addListener((obs, oldSpan, newSpan) ->
        {
            int index = myExternalModel.indexOf(oldSpan);
            if (index != -1)
            {
                myExternalModel.set(index, newSpan);
            }
        });
        ObservableTimeSpan wrapperSpan = new ObservableTimeSpan(observableSpan);
        myInternalModel.add(wrapperSpan);

        SnapFunction snap = new ResolutionBasedSnapFunction(myMillisPerPixel);

        addLayer(new DraggableTimeWindowLayer(observableSpan, myColorOutline, null, Function.identity(),
                () -> observableSpan.get().getDuration(), snap, this::handleRemoveAction)
        {
            @Override
            public boolean canDrag(Point p)
            {
                return false;
            }
        });

        DragHandlesLayer dragHandles = new DragHandlesLayer(wrapperSpan, "load", Function.identity(), snap, snap, myColorOutline,
                myColorOutline.brighter())
        {
            @Override
            public int getDragPriority(Point p)
            {
                // Make the drag handles higher priority so that they can be
                // dragged under the active span
                return canDrag(p) ? 1 : 0;
            }
        };
        dragHandles.setAboveLine(false);
        addLayer(dragHandles);

        getUIModel().setFirstMousePoint(null);
        getUIModel().setLockSelection(false);
        getUIModel().repaint();
    }

    /**
     * Removes the given interval.
     *
     * @param span the interval
     */
    private void removeInterval(final TimeSpan span)
    {
        // Remove the layers
        List<TimelineLayer> layers = getLayers().stream()
                .filter(l -> l instanceof TimeWindowLayer && ((TimeWindowLayer)l).getTimeSpan().get().equals(span)
                        || l instanceof DragHandlesLayer
                                && ((DragHandlesLayer)l).getObservableTimeSpan().getSpan().get().equals(span))
                .collect(Collectors.toList());
        for (TimelineLayer layer : layers)
        {
            removeLayer(layer);
        }

        myInternalModel.removeIf(s -> s.getSpan().get().equals(span));

        getUIModel().repaint();
    }

    /**
     * Gets the first layer that overlaps the time, or null.
     *
     * @param time the time
     * @return the layer, or null
     */
    private TimeWindowLayer getOverlappingLayer(final TimeInstant time)
    {
        return (TimeWindowLayer)StreamUtilities.filterOne(getLayers(),
                l -> l instanceof TimeWindowLayer && ((TimeWindowLayer)l).getTimeSpan().get().overlaps(time));
    }

    /**
     * Adds a span to the external model.
     *
     * @param span the span to add
     */
    private void addIntervalExternal(TimeSpan span)
    {
        Quantify.collectMetric("mist3d.timeline.drag.add-load-span");
        myAnimationModel.lastActionProperty().set(Action.ADD);

        // Add a load span for the loop span
        if (myExternalModel.isEmpty())
        {
            myExternalModel.add(myAnimationModel.getLoopSpan().get());
        }

        boolean overlaps = myExternalModel.stream().anyMatch(s -> s.overlaps(span));
        if (overlaps)
        {
            /* The following logic is complex in order to minimize the number of
             * events and prevent WFS from clearing and re-requesting data. */
            List<TimeSpan> mergedCopy = New.list(myExternalModel);
            CollectionUtilities.addSorted(mergedCopy, Collections.singleton(span));
            mergeSpans(mergedCopy);

            for (int i = 0; i < mergedCopy.size(); i++)
            {
                TimeSpan mergedSpan = mergedCopy.get(i);
                List<TimeSpan> overlappedItems = myExternalModel.stream().filter(s -> s.overlaps(mergedSpan))
                        .filter(s -> !s.equals(mergedSpan)).collect(Collectors.toList());
                // replacing the items that the merged span overlaps as a single
                // operation, so as to fire only a single event from the
                // observable list:
                myExternalModel.replaceMultipleWithOne(i, mergedSpan, overlappedItems);
            }
        }
        else
        {
            CollectionUtilities.addSorted(myExternalModel, Collections.singleton(span));
        }
    }

    /**
     * Adds a span to the external model (load variation).
     *
     * @param span the span to add
     */
    private void loadIntervalExternal(TimeSpan span)
    {
        Quantify.collectMetric("mist3d.timeline.drag.set-load-span");
        myAnimationModel.lastActionProperty().set(Action.LOAD);
        myExternalModel.clear();
        myExternalModel.add(span);
    }

    /**
     * Handles a change in the loop span, synchronizing the loop span's load
     * span, if available.
     *
     * @param oldLoop the old loop span
     * @param newLoop the new loop span
     */
    private void handleLoopChange(TimeSpan oldLoop, TimeSpan newLoop)
    {
        int index = myExternalModel.indexOf(oldLoop);
        if (index != -1)
        {
            myExternalModel.set(index, newLoop);
        }
        else
        {
            myAnimationModel.getLoopSpan().removeListener(myLoopSpanListener);
        }
    }

    /**
     * Handles a remove time span action by the user.
     *
     * @param span the span to remove
     */
    private void handleRemoveAction(TimeSpan span)
    {
        Quantify.collectMetric("mist3d.timeline.drag.remove-load-span");
        // Do this later to prevent co-modification of the list of composite
        // layers
        EventQueue.invokeLater(() ->
        {
            myExternalModel.remove(span);

            // Remove the loop span's load span if it's the only one left
            if (myExternalModel.size() == 1 && myExternalModel.get(0).equals(myAnimationModel.getLoopSpan().get()))
            {
                myExternalModel.clear();
            }
        });
    }

    /**
     * Merges a list of TimeSpans. The spans must be in sorted order.
     *
     * @param spans the spans to merge
     */
    private void mergeSpans(List<TimeSpan> spans)
    {
        Quantify.collectMetric("mist3d.timeline.drag.merge-load-span");
        boolean hadOverlap;
        do
        {
            hadOverlap = false;
            for (int i = 1; i < spans.size(); i++)
            {
                TimeSpan span1 = spans.get(i - 1);
                TimeSpan span2 = spans.get(i);
                if (span1.overlaps(span2))
                {
                    TimeSpan merged = span1.union(span2);
                    spans.set(i - 1, merged);
                    spans.remove(i);
                    i--;
                    hadOverlap = true;
                }
            }
        }
        while (hadOverlap);
    }
}
