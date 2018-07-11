package io.opensphere.controlpanels.animation.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.controlpanels.recording.gif.AnimationOptions;
import io.opensphere.controlpanels.recording.gif.AnimationOptions.ResizeOption;
import io.opensphere.controlpanels.recording.gif.GIFRecorder;
import io.opensphere.controlpanels.recording.gif.GifFileChooser;
import io.opensphere.controlpanels.timeline.TimelineUIModel;
import io.opensphere.controlpanels.timeline.chart.ChartType;
import io.opensphere.controlpanels.timeline.chart.MasterChartLayer;
import io.opensphere.core.TimeManager.PrimaryTimeSpanChangeListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.DisabledGlassPane;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.input.FactoryViewPanel;
import io.opensphere.core.util.swing.input.controller.SingleIconProvider;
import io.opensphere.core.util.swing.input.controller.ViewSettings;
import io.opensphere.core.viewer.impl.ScreenViewer;

/** The control panel. */
@SuppressWarnings("PMD.GodClass")
class ControlPanel extends FactoryViewPanel
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ControlPanel.class);

    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The speeds in frames per second. */
    private static final float[] SPEEDS = new float[] { 0.5f, 1f, 2f, 4f, 6f, 8f, 12f, 18f, 24f, 30f };

    /** The animation model. */
    private final transient AnimationModel myAnimationModel;

    /** The master chart layer. */
    private final transient MasterChartLayer myChartLayer;

    /** The chart type toggle button. */
    private final IconButton myChartTypeButton = new IconButton();

    /** The controls button. */
    private final IconButton myControlsButton = new IconButton();

    /** The controls dialog. */
    private OptionDialog myControlsDialog;

    /** The end label. */
    private final JLabel myEndLabel = new JLabel();

    /** The button for going to the first frame. */
    private final IconButton myFirstButton = new IconButton();

    /** The button for going to the last frame. */
    private final IconButton myLastButton = new IconButton();

    /** The button for going to the next step. */
    private final IconButton myNextButton = new IconButton();

    /** The play/stop button. */
    private final IconButton myPlayStopButton = new IconButton();

    /** The button for going to the previous step. */
    private final IconButton myPreviousButton = new IconButton();

    /** The button for recording an animated GIF. */
    private final IconButton myRecordButton = new IconButton();

    /**
     * The GIF recorder will be set when we are prepared to record and will be
     * null otherwise.
     */
    private transient GIFRecorder myRecorder;

    /** The speed label. */
    private final JLabel mySpeedLabel = new JLabel();

    /** The speed slider. */
    private final JSlider mySpeedSlider = new JSlider(0, SPEEDS.length - 1);

    /** The start label. */
    private final JLabel myStartLabel = new JLabel();

    /** The toolbox for interacting with core functionality. */
    private final transient Toolbox myToolbox;

    /** The timeline UI model. */
    private final transient TimelineUIModel myUIModel;

    /** The zoom in button. */
    private final IconButton myZoomInButton = new IconButton();

    /** The zoom out button. */
    private final IconButton myZoomOutButton = new IconButton();

    /** The left panel. */
    private final JPanel myLeftPanel;

    /** The center panel. */
    private final JPanel myCenterPanel;

    /** The right panel. */
    private final JPanel myRightPanel;

    /** The time label to display while recording. */
    private final JLabel myRecordingTimeLabel;

    /**
     * Converts a speed to an index in the speeds array.
     *
     * @param speed the speed
     * @return the array index
     */
    private static int indexOfSpeed(float speed)
    {
        int index = Arrays.binarySearch(SPEEDS, speed);
        if (index < 0)
        {
            index = -(index + 1);
            if (index >= SPEEDS.length)
            {
                index = SPEEDS.length - 1;
            }
        }
        return index;
    }

    /**
     * Constructor.
     *
     * @param toolbox The toolbox for interacting with core functionality.
     * @param animationModel the animation model
     * @param uiModel the UI model
     * @param chartLayer the chart layer
     */
    public ControlPanel(Toolbox toolbox, AnimationModel animationModel, TimelineUIModel uiModel, MasterChartLayer chartLayer)
    {
        super();
        myToolbox = toolbox;
        myAnimationModel = animationModel;
        myUIModel = uiModel;
        myChartLayer = chartLayer;
        myChartLayer.setChartType(myAnimationModel.getChartType().get());

        updateTimeSpanLabels(myUIModel.getUISpan().get());

        myLeftPanel = buildLeftPanel();
        myCenterPanel = buildCenterPanel();
        myRightPanel = buildRightPanel();
        ComponentUtilities.setPreferredWidthsEqual(myLeftPanel, myRightPanel);
        setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        myRecordingTimeLabel = new JLabel();
        myRecordingTimeLabel.setFont(myRecordingTimeLabel.getFont().deriveFont(Font.BOLD, 14));

        layoutMainPanel();
        setPreferredSize(getPreferredSize());

        addGuiListenersMovement();
        addGuiListenersOther();
        addModelListeners();
    }

    /**
     * Adds all the normal panels to the main panel.
     */
    private void layoutMainPanel()
    {
        removeAll();
        anchorCenter();
        fillHorizontal().add(myLeftPanel);
        fillNone().setInsets(0, 8, 0, 8).add(myCenterPanel);
        fillHorizontal().setInsets(0, 0, 0, 0).add(myRightPanel);
        revalidate();
    }

    /**
     * Adds all the recording panels to the main panel.
     */
    private void layoutRecordingPanel()
    {
        removeAll();
        anchorCenter().fillNone().add(myRecordingTimeLabel);
        revalidate();
    }

    /** Adds listeners for the play movement controls. */
    private void addGuiListenersMovement()
    {
        myRecordButton.addActionListener(e -> handleRecordPressed());
        myPlayStopButton.addActionListener(e -> handlePlayStopPressed());
        myPreviousButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.step-backward");
            myAnimationModel.playStateProperty().set(PlayState.STEP_BACKWARD, true);
        });
        myNextButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.step-forward");
            myAnimationModel.playStateProperty().set(PlayState.STEP_FORWARD, true);
        });
        myFirstButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.step-first");
            myAnimationModel.playStateProperty().set(PlayState.STEP_FIRST, true);
        });
        myLastButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.step-last");
            myAnimationModel.playStateProperty().set(PlayState.STEP_LAST, true);
        });
    }

    /** Adds listeners for GUI elements not related to play movement. */
    private void addGuiListenersOther()
    {
        myZoomInButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.zoom-in");
            myUIModel.zoom(true, .5f);
        });
        myZoomOutButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.zoom-in");
            myUIModel.zoom(false, .5f);
        });
        myChartTypeButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.change-chart-type");
            myAnimationModel.getChartType().set(myChartLayer.nextChart());
            updateChartTypeTooltip();
            if (myChartLayer.getChartType() == ChartType.NONE)
            {
                myUIModel.getTemporaryMessage().set("Charting disabled");
            }
            else
            {
                myUIModel.getTemporaryMessage().set("Chart type is now " + myChartLayer.getChartType().getDescription());
            }
        });
        myControlsButton.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.launch-animation-controls");
            if (myControlsDialog == null)
            {
                myControlsDialog = new OptionDialog(getParent(),
                        new AdvancedControlPanel(myToolbox, myToolbox.getUIRegistry().getOptionsRegistry(), myAnimationModel));
                myControlsDialog.setModal(false);
                myControlsDialog.build();
                setLocationAbove(myControlsDialog, getParent());
            }
            myControlsDialog.setVisible(!myControlsDialog.isVisible());
        });
        mySpeedSlider.getModel().addChangeListener(e ->
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.drag-speed-slider");
            float speed = updateSpeedLabel();
            myAnimationModel.getFPS().set(Float.valueOf(speed));
        });
        bindModel(myUIModel.getUISpan(), (obs, o, n) -> updateTimeSpanLabels(myUIModel.getUISpan().get()));
    }

    /** Add listeners for changes to the animation model. */
    private void addModelListeners()
    {
        bindModel(myAnimationModel.playStateProperty(), (obs, o, n) -> setPlayState(myAnimationModel.getPlayState()));

        bindModel(myAnimationModel.getFPS(), new io.opensphere.core.util.ChangeListener<Float>()
        {
            @Override
            public void changed(ObservableValue<? extends Float> observable, Float oldValue, Float newValue)
            {
                mySpeedSlider.setValue(indexOfSpeed(myAnimationModel.getFPS().get().floatValue()));
                updateSpeedLabel();
            }
        });

        bindModel(myAnimationModel.getLiveMode(), new io.opensphere.core.util.ChangeListener<Boolean>()
        {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
            {
                QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.live-mode-change");
                if (myAnimationModel.getLiveMode().get().booleanValue())
                {
                    myAnimationModel.getLiveMode().setNameAndDescription("<html><b>LIVE</b></html>", "");
                }
                else
                {
                    myAnimationModel.getLiveMode().setNameAndDescription("LIVE", "");
                }
                myUIModel.getTemporaryMessage()
                        .set("Live mode " + (myAnimationModel.getLiveMode().get().booleanValue() ? "activated" : "deactivated"));
            }
        });

        getFactory().addService(new Service()
        {
            private final PrimaryTimeSpanChangeListener myListener = new PrimaryTimeSpanChangeListener()
            {
                @Override
                public void primaryTimeSpansChanged(final TimeSpanList spans)
                {
                    EventQueueUtilities.invokeLater(() -> myRecordingTimeLabel.setText(spans.get(0).toSmartString()));
                }

                @Override
                public void primaryTimeSpansCleared()
                {
                }
            };

            @Override
            public void open()
            {
                myToolbox.getTimeManager().addPrimaryTimeSpanChangeListener(myListener);
            }

            @Override
            public void close()
            {
                myToolbox.getTimeManager().removePrimaryTimeSpanChangeListener(myListener);
            }
        });
    }

    /**
     * Builds the center panel.
     *
     * @return the center panel
     */
    private JPanel buildCenterPanel()
    {
        // Play control buttons
        IconUtil.setIcons(myPreviousButton, IconType.PREVIOUS);
        myPreviousButton.setToolTipText("Go to the previous frame");
        myPreviousButton.setHoldDelay(200);
        updatePlayStopButton();
        IconUtil.setIcons(myNextButton, IconType.NEXT);
        myNextButton.setToolTipText("Go to the next frame");
        myNextButton.setHoldDelay(200);
        IconUtil.setIcons(myFirstButton, IconType.FIRST);
        myFirstButton.setToolTipText("Go to the first frame");
        IconUtil.setIcons(myLastButton, IconType.LAST);
        myLastButton.setToolTipText("Go to the last frame");
        IconUtil.setIcons(myRecordButton, IconType.RECORD);
        myRecordButton.setToolTipText("Record an animated GIF");

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        panel.add(myFirstButton);
        panel.add(myPreviousButton);
        panel.add(myRecordButton);
        panel.add(myPlayStopButton);
        panel.add(myNextButton);
        panel.add(myLastButton);
        return panel;
    }

    /**
     * Builds the left panel.
     *
     * @return the left panel
     */
    private JPanel buildLeftPanel()
    {
        // Label
        ComponentUtilities.setPreferredWidth(myStartLabel, 139);

        // Zoom buttons
        IconUtil.setIcons(myZoomInButton, "/images/zoom-in.png");
        myZoomInButton.setToolTipText("Zoom in on the timeline");
        myZoomInButton.setHoldDelay(100);
        IconUtil.setIcons(myZoomOutButton, "/images/zoom-out.png");
        myZoomOutButton.setToolTipText("Zoom out on the timeline");
        myZoomOutButton.setHoldDelay(100);

        // Chart type
        IconUtil.setIcons(myChartTypeButton, "/images/bars.png");
        updateChartTypeTooltip();

        // Controls
        IconUtil.setIcons(myControlsButton, IconType.COG);
        myControlsButton.setToolTipText("Animation controls");

        GridBagPanel panel = new GridBagPanel();
        panel.add(myStartLabel);
        panel.fillHorizontalSpace().fillNone();
        panel.setInsets(0, 12, 0, 0).add(myZoomInButton);
        panel.setInsets(0, 4, 0, 0).add(myZoomOutButton);
        panel.setInsets(0, 12, 0, 0).add(myChartTypeButton);
        panel.setInsets(0, 12, 0, 0).add(myControlsButton);
        return panel;
    }

    /**
     * Builds the right panel.
     *
     * @return the right panel
     */
    private JPanel buildRightPanel()
    {
        // Speed
        mySpeedSlider.setValue(indexOfSpeed(myAnimationModel.getFPS().get().floatValue()));
        mySpeedSlider.setToolTipText("The speed of the animation in frames-per-second (fps)");
        ComponentUtilities.setPreferredWidth(mySpeedSlider, 100);
        updateSpeedLabel();
        ComponentUtilities.setPreferredWidth(mySpeedLabel, 50);

        // Label
        ComponentUtilities.setPreferredWidth(myEndLabel, 139);
        myEndLabel.setHorizontalAlignment(JLabel.RIGHT);

        JToggleButton snaptoButton = getFactory().createComponent(myAnimationModel.getSnapToDataBoundaries(), JToggleButton.class,
                new ViewSettings<Boolean>().setIconProvider(new SingleIconProvider(IconType.SNAPTO)));

        JToggleButton liveButton = getFactory().createComponent(myAnimationModel.getLiveMode(), JToggleButton.class);

        GridBagPanel panel = new GridBagPanel();
        panel.add(new JLabel("Speed:"));
        panel.add(mySpeedSlider);
        panel.add(mySpeedLabel);
        panel.setInsets(0, 4, 0, 4).add(snaptoButton);
        panel.add(liveButton);
        panel.fillHorizontalSpace().fillNone();
        panel.setInsets(0, 0, 0, 0).add(myEndLabel);
        return panel;
    }

    /**
     * Recording will not be done, reset GUI components and recorder as
     * necessary. This will also restore the frame size if it has been changed
     * in preparation for recording.
     */
    private void cancelRecord()
    {
        myRecordButton.setSelected(false);
        IconUtil.setIcons(myRecordButton, IconType.RECORD);
        if (myRecorder != null)
        {
            if (myRecorder.getOptions().getResizeOption() == ResizeOption.RESIZE)
            {
                myToolbox.getUIRegistry().setMainPaneSize(myRecorder.getOptions().getOriginalWidth(),
                        myRecorder.getOptions().getOriginalHeight());
            }
            myRecorder = null;
        }
    }

    /** Handle when the play/stop button has been pressed. */
    private void handlePlayStopPressed()
    {
        if (myRecorder != null)
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.recording-play");
            record();
        }
        else
        {
            PlayState currentState = myAnimationModel.getPlayState();
            if (currentState.isPlaying())
            {
                QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.animate-stop");
                setPlayState(PlayState.STOP);
            }
            else
            {
                QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.animate-start");
                setPlayState(PlayState.FORWARD);
            }
        }
    }

    /** Handle when the record button has been pressed. */
    private void handleRecordPressed()
    {
        myRecordButton.setSelected(!myRecordButton.isSelected());
        if (myRecordButton.isSelected())
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.record-start");
            setPlayState(PlayState.STOP);

            ScreenViewer viewer = myToolbox.getMapManager().getScreenViewer();
            int origWidth = viewer.getViewportWidth();
            int origHeight = viewer.getViewportHeight();
            GifFileChooser chooser = new GifFileChooser(myToolbox.getUIRegistry(), origWidth, origHeight,
                    myToolbox.getPreferencesRegistry());
            AnimationOptions options = chooser.showFileDialog();
            if (options != null)
            {
                IconUtil.setIcons(myRecordButton, IconType.RECORD, Color.RED);
                if (options.getResizeOption() == ResizeOption.RESIZE)
                {
                    myToolbox.getUIRegistry().setMainPaneSize(options.getWidth(), options.getHeight());
                }
                int millis = Milliseconds.get(myToolbox.getAnimationManager().getChangeRate()).intValue();
                options.setFrameIntervalMS(millis);
                myRecorder = new GIFRecorder(options, myToolbox.getAnimationManager(),
                        myToolbox.getUIRegistry().getComponentRegistry(),
                        myToolbox.getFrameBufferCaptureManager().getCaptureProvider());
            }
            else
            {
                QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.record-cancel");
                cancelRecord();
            }
        }
        else
        {
            QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.timeline.buttons.record-cancel");
            cancelRecord();
        }
    }

    /**
     * Does the recording.
     */
    private void record()
    {
        final Component glass = DisabledGlassPane.disableParentFrame(this);
        layoutRecordingPanel();

        new SwingWorker<Void, Void>()
        {
            @Override
            protected Void doInBackground()
            {
                CountDownLatch recordLatch = new CountDownLatch(1);
                myRecorder.recordSinglePass(recordLatch);
                EventQueueUtilities.invokeLater(() -> setPlayState(PlayState.FORWARD));
                try
                {
                    recordLatch.await();
                }
                catch (InterruptedException e)
                {
                    LOGGER.error("Recording latch interrupted." + e, e);
                }
                return null;
            }

            @Override
            protected void done()
            {
                try
                {
                    setPlayState(PlayState.STOP);
                    get();
                }
                catch (InterruptedException | ExecutionException e)
                {
                    LOGGER.error(e, e);
                }
                finally
                {
                    DisabledGlassPane.enableParentFrame(ControlPanel.this, glass);
                    cancelRecord();
                    layoutMainPanel();
                }
            }
        }.execute();
    }

    /**
     * Sets the location of the window above the given component.
     *
     * @param w the window
     * @param c the component
     */
    private void setLocationAbove(Window w, Component c)
    {
        Dimension windowSize = w.getSize();
        Dimension compSize = c.getSize();
        Point compLocation = c.getLocationOnScreen();
        int dx = compLocation.x + (compSize.width - windowSize.width) / 2;
        int dy = compLocation.y - windowSize.height;
        w.setLocation(dx, dy);
    }

    /**
     * Set the play state and adjust the GUI element states as necessary.
     *
     * @param playState The new play state.
     */
    private void setPlayState(PlayState playState)
    {
        boolean isPlaying = playState.isPlaying();
        myAnimationModel.setPlayState(playState);
        myPreviousButton.setEnabled(!isPlaying);
        myNextButton.setEnabled(!isPlaying);
        myFirstButton.setEnabled(!isPlaying);
        myLastButton.setEnabled(!isPlaying);
        updatePlayStopButton();
    }

    /**
     * Formats the given time span in a "smart" way.
     *
     * @param date the date
     * @param durationMs the duration of the interval in milliseconds
     * @return the formatted text
     */
    private String smartFormat(Date date, long durationMs)
    {
        SimpleDateFormat format;
        if (durationMs >= Constants.MILLIS_PER_WEEK)
        {
            format = new SimpleDateFormat("yyyy MMM dd");
        }
        else if (durationMs >= Constants.MILLIS_PER_HOUR)
        {
            format = new SimpleDateFormat("yyyy MMM dd HH:mm");
        }
        else
        {
            format = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
        }
        return format.format(date);
    }

    /**
     * Updates the chart type button tool tip.
     */
    private void updateChartTypeTooltip()
    {
        myChartTypeButton.setToolTipText(
                StringUtilities.concat("Change the chart type. Currently ", myChartLayer.getChartType().getDescription(), "."));
    }

    /**
     * Updates the play/stop button from the model.
     */
    private void updatePlayStopButton()
    {
        if (myAnimationModel.getPlayState() == PlayState.STOP)
        {
            IconUtil.setIcons(myPlayStopButton, IconType.PLAY, Color.GREEN);
            myPlayStopButton.setToolTipText("Play the animation");
        }
        else if (myAnimationModel.getPlayState() == PlayState.FORWARD)
        {
            IconUtil.setIcons(myPlayStopButton, IconType.PAUSE, Color.YELLOW);
            myPlayStopButton.setToolTipText("Stop the animation");
        }
    }

    /**
     * Updates the speed label.
     *
     * @return the speed
     */
    private float updateSpeedLabel()
    {
        float speed = SPEEDS[mySpeedSlider.getValue()];
        mySpeedLabel.setText(speed + " fps");
        return speed;
    }

    /**
     * Updates the time span labels.
     *
     * @param timeSpan the time span
     */
    private void updateTimeSpanLabels(TimeSpan timeSpan)
    {
        long durationMs = timeSpan.getDurationMs();
        myStartLabel.setText(smartFormat(timeSpan.getStartDate(), durationMs));
        myEndLabel.setText(smartFormat(timeSpan.getEndDate(), durationMs));
    }
}
