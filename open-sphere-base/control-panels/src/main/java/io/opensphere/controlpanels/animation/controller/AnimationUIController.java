package io.opensphere.controlpanels.animation.controller;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import io.opensphere.controlpanels.animation.event.ShowTimelineEvent;
import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.ViewPreference;
import io.opensphere.controlpanels.animation.view.AnimationInternalFrame;
import io.opensphere.controlpanels.animation.view.AnimationOptionsProvider;
import io.opensphere.controlpanels.animation.view.AnimationPanel;
import io.opensphere.controlpanels.animation.view.TimeBrowserPanel;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.event.EventListenerService;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.swing.FramePreferencesMonitor;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * Controller for the time browser and timeline UIs.
 */
public class AnimationUIController extends EventListenerService
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The model for the active time. */
    private final ObservableValue<TimeSpan> myTimeModel;

    /** The timeline UI model. */
    private final TimelineUIModel myUIModel;

    /** The animation panel. */
    private AnimationPanel myAnimationPanel;

    /** The chart data controller. */
    private ChartDataController myChartDataController;

    /** Monitor for the timeline bounds. */
    private FramePreferencesMonitor myFramePrefsMonitor;

    /** The time browser panel. */
    private TimeBrowserPanel myTimeBrowser;

    /** The timeline frame. */
    private AnimationInternalFrame myTimelineFrame;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param animationModel the animation model
     * @param timeModel the time model
     * @param uiModel the timeline UI model
     */
    public AnimationUIController(Toolbox toolbox, AnimationModel animationModel, ObservableValue<TimeSpan> timeModel,
            TimelineUIModel uiModel)
    {
        super(toolbox.getEventManager(), 1);
        myToolbox = toolbox;
        myAnimationModel = animationModel;
        myTimeModel = timeModel;
        myUIModel = uiModel;

        bindEvent(ShowTimelineEvent.class, e -> EventQueue.invokeLater(() -> showView(e.getViewToShow())));
    }

    @Override
    public void open()
    {
        ViewPreference viewToShow = myAnimationModel.getViewPreference().get() == ViewPreference.LAST_SHOWN
                ? myAnimationModel.getLastShownView().get() : myAnimationModel.getViewPreference().get();

        // Timeline UI
        myTimelineFrame = new AnimationInternalFrame(myToolbox.getUIRegistry());
        myAnimationPanel = new AnimationPanel(myToolbox, myTimeModel, myAnimationModel, myUIModel, myToolbox.getTimeManager());
        myTimelineFrame.setComponent(myAnimationPanel);
        myFramePrefsMonitor = new FramePreferencesMonitor(myToolbox.getPreferencesRegistry(), myTimelineFrame.getTitle(),
                myTimelineFrame, myTimelineFrame.getBounds());

        myChartDataController = new ChartDataController(myToolbox.getUIRegistry().getTimelineRegistry(),
                myToolbox.getOrderManagerRegistry(), myUIModel, myAnimationPanel.getChartLayer().getLayerModels());
        myChartDataController.open();

        // Time browser UI
        myTimeBrowser = new TimeBrowserPanel(myTimeModel, myAnimationModel);
        myTimeBrowser.open();
        myTimeBrowser.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showView(ViewPreference.TIMELINE);
            }
        });
        myToolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH_BOTTOM,
                "TimeBrowser", myTimeBrowser, 600, SeparatorLocation.NONE);

        myTimelineFrame.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentHidden(ComponentEvent e)
            {
                showView(ViewPreference.TIME_BROWSER);
            }

            @Override
            public void componentShown(ComponentEvent e)
            {
                myTimeBrowser.setVisible(false);
            }
        });

        if (viewToShow == ViewPreference.TIMELINE)
        {
            showView(ViewPreference.TIMELINE);
        }

        myToolbox.getUIRegistry().getOptionsRegistry()
                .addOptionsProvider(new AnimationOptionsProvider(myToolbox, myAnimationModel, myTimelineFrame));

        super.open();
    }

    @Override
    public void close()
    {
        super.close();
        myChartDataController.close();
        myFramePrefsMonitor.close();
    }

    /**
     * Fits the UI span around the loop span.
     */
    public final void fitUIToLoopSpan()
    {
        assert EventQueue.isDispatchThread();
        myAnimationPanel.fitUIToLoopSpan();
    }

    /**
     * Shows the given view.
     *
     * @param view the view
     */
    public final void showView(ViewPreference view)
    {
        assert EventQueue.isDispatchThread();
        if (view == ViewPreference.TIMELINE)
        {
            if (!myTimelineFrame.isVisible())
            {
                if (myAnimationModel.getLoopSpan().get().equals(myTimeModel.get()))
                {
                    final double scale = 7;
                    myUIModel.getUISpan().set(TimelineUtilities.scale(myAnimationModel.getLoopSpan().get(), scale));
                }
                else
                {
                    fitUIToLoopSpan();
                }

                myTimeBrowser.setVisible(false);
                myTimelineFrame.setVisible(true);
                myAnimationPanel.requestFocusInWindow();
            }
        }
        else
        {
            myTimeBrowser.setVisible(true);
        }
        myAnimationModel.getLastShownView().set(view);
    }
}
