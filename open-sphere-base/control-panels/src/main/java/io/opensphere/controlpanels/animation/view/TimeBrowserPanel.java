package io.opensphere.controlpanels.animation.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JToggleButton;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.core.Notify;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.DurationUnitsProvider;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.ObservableValueListenerHandle;
import io.opensphere.core.util.ValidationStatus;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.input.FactoryViewPanel;
import io.opensphere.core.util.swing.input.controller.TimeInstantTextFieldController;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.TimeInstantModel;
import io.opensphere.core.util.swing.input.view.DateTextFieldFormat;
import io.opensphere.core.util.time.TimelineUtilities;

/**
 * The time browser panel for selecting time when the full timeline isn't shown.
 */
@SuppressWarnings("PMD.GodClass")
public class TimeBrowserPanel extends FactoryViewPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The animation model. */
    private final transient AnimationModel myAnimationModel;

    /** The end time instant (bound to the time span). */
    private final TimeBrowserSpanWrapper myEndInstant;

    /** The end instant controller. */
    private final transient TimeInstantTextFieldController myEndInstantController;

    /** The next button. */
    private IconButton myNextButton;

    /** The previous button. */
    private IconButton myPreviousButton;

    /** The show timeline button. */
    private IconButton myShowTimelineButton;

    /** The start time instant (bound to the time span). */
    private final TimeBrowserSpanWrapper myStartInstant;

    /** The start instant controller. */
    private final transient TimeInstantTextFieldController myStartInstantController;

    /** The time period type. */
    private final ChoiceModel<TimePeriodType> myTimePeriodType;

    /** The time span. */
    private final transient ObservableValue<TimeSpan> myTimeSpan;

    /** Time span listener handle. */
    private final transient ObservableValueListenerHandle<TimeSpan> myTimeSpanListenerHandle;

    /** The time preset dialog. */
    private TimePresetDialog myTimePresetDialog;

    /**
     * Constructor.
     *
     * @param timeSpan the time span model
     * @param animationModel the animation model
     */
    public TimeBrowserPanel(ObservableValue<TimeSpan> timeSpan, AnimationModel animationModel)
    {
        super();

        myTimeSpan = timeSpan;
        myAnimationModel = animationModel;
        myStartInstant = new TimeBrowserSpanWrapper(myTimeSpan, true);
        myEndInstant = new TimeBrowserSpanWrapper(myTimeSpan, false);
        myTimePeriodType = new ChoiceModel<>(TimePeriodType.values());
        myTimePeriodType.setDescription("The active time duration");
        myTimePeriodType.set(getTimePeriodType());
        getFactory().addService(myStartInstantController = new TimeInstantTextFieldController(myStartInstant));
        Function<Date, TimeInstant> fromPickerConverter = d -> TimeInstant.get(d).plus(Days.ONE);
        Function<Date, Date> toPickerConverter = d -> new Date(d.getTime() - 1000);
        getFactory()
                .addService(myEndInstantController = new TimeController(myEndInstant, fromPickerConverter, toPickerConverter));

        createPreviousButton();
        createNextButton();

        // Add listeners
        getFactory().bindModel(myTimePeriodType, (obs, old, newValue) -> handleTimePeriodTypeChange());
        myTimeSpanListenerHandle = getFactory().bindModel(myTimeSpan, (obs, old, newValue) -> handleTimeSpanChange());
        getFactory().bindModel(myAnimationModel.playStateProperty(), (obs, old, newValue) -> handlePlayStateChanged());

        myStartInstantController.getView().setOpaque(false);
        myEndInstantController.getView().setOpaque(false);

        setOpaque(false);
        setInsets(0, 4, 0, 0);
        add(myPreviousButton);
        add(myStartInstantController.getView());
        add(myEndInstantController.getView());
        add(myNextButton);
        add(getFactory().createComponent(myTimePeriodType, JComboBox.class));
        setInsets(0, 4, 0, 4);
        add(createPresetsButton());
        add(getFactory().createComponent(myAnimationModel.getLiveMode(), JToggleButton.class));
        add(createTimelineButton());
    }

    @Override
    public void open()
    {
        super.open();
        handleTimePeriodTypeChange();
    }

    /**
     * Adds a listener for when the show timeline button is clicked.
     *
     * @param l the listener
     */
    public void addActionListener(ActionListener l)
    {
        myShowTimelineButton.addActionListener(l);
    }

    /**
     * Creates the next button.
     *
     * @return the next button
     */
    private IconButton createNextButton()
    {
        myNextButton = new IconButton();
        IconUtil.setIcons(myNextButton, IconType.NEXT);
        myNextButton.setToolTipText("Go to the next frame");
        myNextButton.setHoldDelay(200);
        myNextButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Duration duration = getDuration();
                if (duration != null)
                {
                    myStartInstant.plus(duration);
                }
            }
        });
        return myNextButton;
    }

    /**
     * Creates the previous button.
     *
     * @return the previous button
     */
    private IconButton createPreviousButton()
    {
        myPreviousButton = new IconButton();
        IconUtil.setIcons(myPreviousButton, IconType.PREVIOUS);
        myPreviousButton.setToolTipText("Go to the previous frame");
        myPreviousButton.setHoldDelay(200);
        myPreviousButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Duration duration = getDuration();
                if (duration != null)
                {
                    myStartInstant.minus(duration);
                }
            }
        });
        return myPreviousButton;
    }

    /**
     * Create the show timeline button.
     *
     * @return the show timeline button
     */
    private IconButton createTimelineButton()
    {
        myShowTimelineButton = new IconButton("Timeline");
        myShowTimelineButton.setToolTipText("Show the timeline");
        IconUtil.setIcons(myShowTimelineButton, IconType.CLOCK);
        return myShowTimelineButton;
    }

    /**
     * Creates the presets button.
     *
     * @return the button
     */
    private JButton createPresetsButton()
    {
        IconButton button = new IconButton("Presets");
        button.setForeground(IconUtil.DEFAULT_ICON_FOREGROUND);
        button.setToolTipText("Quickly set the active time to a preset time");
        button.addActionListener(e ->
        {
            if (myTimePresetDialog == null)
            {
                myTimePresetDialog = new TimePresetDialog(button, myTimeSpan, myTimePeriodType, myAnimationModel);
            }

            long delta = System.currentTimeMillis() - myTimePresetDialog.getLastFocusLostTime();
            if (delta > 500)
            {
                myTimePresetDialog.setLocation(button);
                myTimePresetDialog.setVisible(true);
            }
        });
        return button;
    }

    /**
     * Gets the unit duration from the time period type.
     *
     * @return the duration
     */
    private Duration getDuration()
    {
        return myTimePeriodType.get() != null && myTimePeriodType.get().getChronoUnit() != null
                ? Duration.create(myTimePeriodType.get().getChronoUnit(), 1) : null;
    }

    /**
     * Gets the time period type from the time span.
     *
     * @return the time period type
     */
    private TimePeriodType getTimePeriodType()
    {
        TimePeriodType timePeriodType;

        Duration duration = new DurationUnitsProvider().getLargestIntegerUnitType(myTimeSpan.get().getDuration());
        TimePeriodType chronoTimePeriod = TimePeriodType.fromChronoUnit(duration.getChronoUnit());
        if (duration.isOne() && chronoTimePeriod != TimePeriodType.CUSTOM
                && TimelineUtilities.isRounded(myTimeSpan.get().getStartDate(), duration))
        {
            timePeriodType = chronoTimePeriod;
        }
//        else if ((MathUtil.between(intValue, 28, 31) && duration.getChronoUnit() == ChronoUnit.DAYS
//                || intValue == 4 && duration.getChronoUnit() == ChronoUnit.WEEKS)
//                && TimelineUtilities.isRounded(myTimeSpan.get().getStartDate(), Months.ONE))
//        {
//            timePeriodType = TimePeriodType.MONTHS;
//        }
        else
        {
            timePeriodType = TimePeriodType.CUSTOM;
        }

        return timePeriodType;
    }

    /**
     * Handles a change in the play state.
     */
    private void handlePlayStateChanged()
    {
        // Enable/disable buttons
        boolean enabled = !myAnimationModel.getPlayState().isPlaying();
        myPreviousButton.setEnabled(enabled);
        myStartInstantController.getView().setEnabled(enabled);
        myEndInstantController.getView().setEnabled(enabled);
        myNextButton.setEnabled(enabled);
        myTimePeriodType.setEnabled(enabled);

        // Set the format and what's visible
        if (myAnimationModel.getPlayState().isPlaying())
        {
            boolean lockedAndLoaded = myAnimationModel.getSnapToDataBoundaries().get().booleanValue()
                    && myAnimationModel.getSelectedDataLoadDuration().get() != null;
            if (!lockedAndLoaded)
            {
                myStartInstantController.setFormat(DateTextFieldFormat.DATE_TIME);
                myEndInstantController.setFormat(DateTextFieldFormat.DATE_TIME);
                myEndInstant.setVisible(true);
            }
        }
        else
        {
            handleTimePeriodTypeChange();
        }
    }

    /**
     * Handles a change in the time span.
     */
    private void handleTimeSpanChange()
    {
        /* This check is here to prevent constantly switching it out of custom
         * mode just because the time happens to align on an interval
         * boundary. */
        if (myTimePeriodType.get() != TimePeriodType.CUSTOM || myAnimationModel.getSnapToDataBoundaries().get().booleanValue()
                && myAnimationModel.getSelectedDataLoadDuration().get() != null)
        {
            myTimePeriodType.set(getTimePeriodType());
        }
    }

    /**
     * Handles a change in the time period type.
     */
    private void handleTimePeriodTypeChange()
    {
        myTimeSpanListenerHandle.pause();

        TimePeriodType timePeriodType = myTimePeriodType.get();

        if (timePeriodType == TimePeriodType.CUSTOM)
        {
            myAnimationModel.getSnapToDataBoundaries().set(Boolean.FALSE);
        }
        else
        {
            /* Ignore custom because there's no reason to update the time
             * (custom is unrestricted). */
            myStartInstant.set(myTimeSpan.get().getStartInstant());
        }

        myStartInstantController.setFormat(timePeriodType.getFormat());
        myEndInstantController.setFormat(timePeriodType.getFormat());
        myEndInstant.setVisible(timePeriodType.isEndVisible());

        ChronoUnit selectionUnit = timePeriodType.getChronoUnit() != null ? timePeriodType.getChronoUnit() : ChronoUnit.DAYS;
        myStartInstantController.setSelectionUnit(selectionUnit);
        myEndInstantController.setSelectionUnit(selectionUnit);

        boolean notCustom = timePeriodType != TimePeriodType.CUSTOM;
        myPreviousButton.setEnabled(notCustom);
        myPreviousButton.setToolTipText(notCustom ? "Go to the previous " + timePeriodType.toString().toLowerCase() : null);
        myNextButton.setEnabled(notCustom);
        myNextButton.setToolTipText(notCustom ? "Go to the next " + timePeriodType.toString().toLowerCase() : null);

        myTimeSpanListenerHandle.resume();
    }

    /**
     * Extends {@link TimeInstantSpanWrapper} to provide some restrictions on
     * setting the span.
     */
    private class TimeBrowserSpanWrapper extends TimeInstantSpanWrapper
    {
        /** The serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param span The span inner model
         * @param isStart Whether to use the start time (true for start, false
         *            for end)
         */
        public TimeBrowserSpanWrapper(ObservableValue<TimeSpan> span, boolean isStart)
        {
            super(span, isStart);
        }

        @Override
        public boolean set(TimeInstant value)
        {
            boolean changed = false;
            TimePeriodType timePeriodType = myTimePeriodType.get();
            if (isStart() || EqualsHelper.equalsAny(timePeriodType, TimePeriodType.CUSTOM, TimePeriodType.WEEKS))
            {
                TimeSpan newSpan = getNewSpan(value);
                changed = newSpan != null && !getSpan().get().equals(newSpan);
                if (changed)
                {
                    // Set the loop span first to avoid problems if the loop
                    // span is locked.
                    myAnimationModel.getLoopSpan().set(newSpan);

                    getSpan().set(newSpan);

                    if (timePeriodType != TimePeriodType.CUSTOM && myAnimationModel.getSnapToDataBoundaries().isEnabled())
                    {
                        Duration activeDuration = myAnimationModel.getActiveSpanDuration().get();
                        if (Duration.containsDuration(myAnimationModel.getDataLoadDurations().get(), activeDuration))
                        {
                            Duration unitDuration = new DurationUnitsProvider().getLargestIntegerUnitType(activeDuration);
                            myAnimationModel.getSelectedDataLoadDuration().set(unitDuration);
                            myAnimationModel.getSnapToDataBoundaries().set(Boolean.TRUE);
                        }
                    }

                    setValid(true, this);

                    if (!myAnimationModel.loadIntervalsProperty().isEmpty())
                    {
                        Notify.info(
                                "You have load intervals in the timeline, so data may not load for the time you just selected");
                    }
                }
            }
            return changed;
        }

        @Override
        protected TimeSpan getNewSpan(TimeInstant value)
        {
            TimeSpan newSpan = null;
            if (value != null)
            {
                TimePeriodType timePeriodType = myTimePeriodType.get();
                if (timePeriodType == TimePeriodType.CUSTOM && !value.equals(get()))
                {
                    boolean isBad = false;
                    try
                    {
                        newSpan = super.getNewSpan(value);
                        isBad = newSpan.isInstantaneous();
                    }
                    // end is before start
                    catch (IllegalArgumentException e)
                    {
                        isBad = true;
                    }

                    if (isBad)
                    {
                        newSpan = isStart() ? TimeSpan.get(value, value.plus(Days.ONE))
                                : TimeSpan.get(value.minus(Days.ONE), value);
                    }
                }
                else if (timePeriodType != TimePeriodType.CUSTOM)
                {
                    Date date = value.toDate();
                    if (!isStart() && timePeriodType == TimePeriodType.WEEKS)
                    {
                        date = new Date(date.getTime() - 1000);
                    }
                    Duration duration = getDuration();
                    Calendar roundedValue = TimelineUtilities.roundDown(date, duration);
                    newSpan = TimeSpan.get(roundedValue.getTimeInMillis(), duration);
                }
            }
            return newSpan;
        }
    }

    /**
     * Controller for the time instant models/views.
     */
    private static class TimeController extends TimeInstantTextFieldController
    {
        /**
         * Constructor.
         *
         * @param model the model
         * @param fromPickerConverter the converter from picker date to model
         *            time instant
         * @param toPickerConverter the converter from model date to picker date
         */
        public TimeController(TimeInstantModel model, Function<Date, TimeInstant> fromPickerConverter,
                Function<Date, Date> toPickerConverter)
        {
            super(model, fromPickerConverter, toPickerConverter);
            getView().setUserMessage("Note, this time is exclusive");
        }

        @Override
        protected void updateViewLookAndFeel()
        {
            // Override base class behavior to not muck with tooltips
            getView().getTextField().setBackground(
                    getModel().getValidationStatus() == ValidationStatus.VALID ? getDefaultBackground() : getErrorBackground());
        }
    }
}
