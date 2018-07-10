package io.opensphere.controlpanels.animation.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.timeline.CompositeLayer;
import io.opensphere.controlpanels.timeline.GridTimelineLayer;
import io.opensphere.controlpanels.timeline.ObservableTimeSpan;
import io.opensphere.controlpanels.timeline.TemporaryMessageLayer;
import io.opensphere.controlpanels.timeline.TimeProbe;
import io.opensphere.controlpanels.timeline.TimelineController;
import io.opensphere.controlpanels.timeline.TimelinePanel;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.controlpanels.timeline.chart.MasterChartLayer;
import io.opensphere.core.TimeManager;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextActionManager;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.function.ShortCircuitFunction;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.time.TimelineUtilities;

/** The main animation panel. */
public class AnimationPanel extends AbstractHUDPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The animation model. */
    private final transient AnimationModel myAnimationModel;

    /** The master chart layer. */
    private final transient MasterChartLayer myChartLayer = new MasterChartLayer();

    /** The timeline panel. */
    private final TimelinePanel myTimelinePanel;

    /** The timeline UI model. */
    private final transient TimelineUIModel myUIModel;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param timeModel the time model
     * @param animationModel the animation model
     * @param uiModel the UI model
     * @param timeManager the time manager
     */
    public AnimationPanel(Toolbox toolbox, ObservableValue<TimeSpan> timeModel, AnimationModel animationModel,
            TimelineUIModel uiModel, TimeManager timeManager)
    {
        super(new BorderLayout(), toolbox.getPreferencesRegistry());
        setPreferredSize(new Dimension(800, 150));
        myAnimationModel = animationModel;

        myUIModel = uiModel;
        fitUIToLoopSpan();

        // Build the UI
        myTimelinePanel = buildTimelinePanel(toolbox, timeModel, animationModel, myUIModel, timeManager);
        add(myTimelinePanel, BorderLayout.CENTER);
        ControlPanel controlPanel = new ControlPanel(toolbox, animationModel, myUIModel, myChartLayer);
        controlPanel.open();
        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Fits the UI span around the loop span.
     */
    public final void fitUIToLoopSpan()
    {
        myUIModel.getUISpan().set(TimelineUtilities.scale(myAnimationModel.getLoopSpan().get(), 1.5));
    }

    /**
     * Gets the chart layer.
     *
     * @return the chart layer
     */
    public MasterChartLayer getChartLayer()
    {
        return myChartLayer;
    }

    @Override
    public boolean requestFocusInWindow()
    {
        return myTimelinePanel.requestFocusInWindow();
    }

    /**
     * Get the hold interval menu item.
     *
     * @param animationModel The animation model.
     * @return The hold interval menu item.
     */
    protected JMenuItem getHoldIntervalMenuItem(final AnimationModel animationModel)
    {
        return new JMenuItem(
                new IntervalAction("Hold loop span", animationModel.getHeldIntervals(), animationModel.getLoopSpan()));
    }

    /**
     * Get the lock loop span menu item.
     *
     * @return The lock loop span menu item.
     */
    protected JMenuItem getLockLoopSpanMenuItem()
    {
        return new JMenuItem(new AbstractAction(
                myAnimationModel.getLoopSpanLocked().get().booleanValue() ? "Unlock loop span" : "Lock loop span")
        {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (myAnimationModel.getLoopSpanLocked().get().booleanValue())
                {
                    myAnimationModel.getLoopSpanLocked().set(Boolean.FALSE);
                    myUIModel.getTemporaryMessage().set("Loop span unlocked");
                }
                else
                {
                    myAnimationModel.getLoopSpanLocked().set(Boolean.TRUE);
                    myUIModel.getTemporaryMessage().set("Loop span locked");
                }
            }
        });
    }

    /**
     * Get the UI lock menu item.
     *
     * @return The UI lock menu item.
     */
    protected JMenuItem getUILockMenuItem()
    {
        return new JMenuItem(new AbstractAction(myAnimationModel.getUISpanLock().get().booleanValue()
                ? "Unlock timeline view from loop span" : "Lock timeline view to loop span")
        {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (myAnimationModel.getUISpanLock().get().booleanValue())
                {
                    myAnimationModel.getUISpanLock().set(Boolean.FALSE);
                    myUIModel.getTemporaryMessage().set("Timeline view unlocked from loop span");
                }
                else
                {
                    myAnimationModel.getUISpanLock().set(Boolean.TRUE);
                    myUIModel.getTemporaryMessage().set("Timeline view locked to loop span");
                }
            }
        });
    }

    /**
     * Builds the timeline panel.
     *
     * @param toolbox the toolbox
     * @param timeModel the timeline model
     * @param animationModel the animation model
     * @param uiModel the UI model
     * @param timeManager the time manager
     * @return the timeline panel
     */
    private TimelinePanel buildTimelinePanel(Toolbox toolbox, ObservableValue<TimeSpan> timeModel, AnimationModel animationModel,
            TimelineUIModel uiModel, TimeManager timeManager)
    {
        ContextActionManager contextActionManager = toolbox.getUIRegistry().getContextActionManager();

        CompositeLayer masterLayer = new CompositeLayer();
        TimelinePanel panel = new TimelinePanel(uiModel, masterLayer);
        TimelineController timelineController = new TimelineController(uiModel, panel, masterLayer, contextActionManager);
        panel.setForeground(AnimationConstants.FG_COLOR);
        panel.setBackground(AnimationConstants.BG_COLOR);

        // Chart layer
        timelineController.addLayer(myChartLayer);

        // Data loading boundary layer
        timelineController.addLayer(new DataLoadBoundaryLayer(animationModel.getSelectedDataLoadDuration()));

        // Timeline grid layer
        timelineController.addLayer(new GridTimelineLayer());

        // Load span
        timelineController.addLayer(
                new LoadIntervalsLayer(toolbox, animationModel.loadIntervalsProperty(), uiModel.getMillisPerPixel(), animationModel));

        // Held and skipped intervals layers
        timelineController.addLayer(new SkippedIntervalsLayer(animationModel));
        timelineController
                .addLayer(new HeldIntervalsLayer(toolbox, animationModel, toolbox.getUIRegistry().getMainFrameProvider().get()));

        // Loop span handles
        LoopSpanEndSnapFunction snapFunc = new LoopSpanEndSnapFunction(animationModel, uiModel.getMillisPerPixel());
        AnimationDragHandlesLayer handles = new AnimationDragHandlesLayer(toolbox, new ObservableTimeSpan(animationModel.getLoopSpan()),
                new ShortCircuitFunction<TimeInstant>(), snapFunc, snapFunc, animationModel.playStateProperty(), "loop",
                AnimationConstants.ANIMATION_SPAN_HANDLE_COLOR, AnimationConstants.ANIMATION_SPAN_HANDLE_HOVER_COLOR)
        {
            @Override
            public int getDragPriority(Point p)
            {
                /* Since the load span handles have a custom drag priority, we also need one for the loop span handles so they are
                 * above the load span. */
                return canDrag(p) ? 2 : 0;
            }
        };
        Supplier<Collection<JMenuItem>> menuItemSupplier = new Supplier<Collection<JMenuItem>>()
        {
            @Override
            public Collection<JMenuItem> get()
            {
                return Arrays.asList(getLockLoopSpanMenuItem(), getHoldIntervalMenuItem(animationModel), getUILockMenuItem());
            }
        };
        handles.addDragHandleMenuItemSupplier(menuItemSupplier);
        handles.setAboveLine(false);
        timelineController.addLayer(handles);

        // Advance duration layer
        timelineController.addLayer(new AdvanceDurationLayer(timeModel, animationModel));

        // Active time window
        timelineController.addLayer(new ActiveWindowLayer(toolbox, timeModel, animationModel, uiModel.getMillisPerPixel(), timeManager));

        // Time probe
        timelineController.addLayer(new TimeProbe(uiModel.getCursorTime(), AnimationConstants.TIME_PROBE_COLOR));

        timelineController.addLayer(new TemporaryMessageLayer(uiModel.getTemporaryMessage()));

        return panel;
    }
}
