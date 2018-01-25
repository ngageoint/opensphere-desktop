package io.opensphere.controlpanels.animation.view;

import java.awt.Component;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.core.Notify;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.units.duration.Days;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.Seconds;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.IntegerModel;
import io.opensphere.core.util.time.TimelineUtilities;

/** The time presets dialog. */
public class TimePresetDialog extends JDialog
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The time span model. */
    private final ObservableValue<TimeSpan> myTimeSpan;

    /** The time period type model. */
    private final ChoiceModel<TimePeriodType> myTimePeriodType;

    /** The animation model. */
    private final AnimationModel myAnimationModel;

    /** The duration magnitude. */
    private final IntegerModel myDurationMagnitude = new IntegerModel(1, Integer.MAX_VALUE);

    /** The duration unit. */
    private final ChoiceModel<ChronoUnit> myDurationUnit = new ChoiceModel<>(ChronoUnit.MONTHS, ChronoUnit.WEEKS, ChronoUnit.DAYS,
            ChronoUnit.HOURS);

    /** The last time focus was lost. */
    private long myLastFocusLostTime;

    /**
     * Constructor.
     *
     * @param component The parent component
     * @param timeSpan The time span model
     * @param timePeriodType The time period type model
     * @param animationModel the animation model
     */
    public TimePresetDialog(Component component, ObservableValue<TimeSpan> timeSpan, ChoiceModel<TimePeriodType> timePeriodType,
            AnimationModel animationModel)
    {
        super(SwingUtilities.getWindowAncestor(component), "Time Presets");
        myTimeSpan = timeSpan;
        myTimePeriodType = timePeriodType;
        myAnimationModel = animationModel;

        myDurationMagnitude.setName("Duration");
        myDurationMagnitude.set(Integer.valueOf(1));

        myDurationUnit.setName("Unit");
        myDurationUnit.set(ChronoUnit.DAYS);

        setSize(256, 104);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setUndecorated(true);
        add(createPanel());

        addWindowFocusListener(new WindowFocusListener()
        {
            @Override
            public void windowLostFocus(WindowEvent e)
            {
                myLastFocusLostTime = System.currentTimeMillis();
                setVisible(false);
            }

            @Override
            public void windowGainedFocus(WindowEvent e)
            {
            }
        });
    }

    /**
     * Gets the lastFocusLostTime.
     *
     * @return the lastFocusLostTime
     */
    public long getLastFocusLostTime()
    {
        return myLastFocusLostTime;
    }

    /**
     * Sets the location of this dialog based on the component location.
     *
     * @param component the component
     */
    public void setLocation(Component component)
    {
        Point compLocation = component.getLocationOnScreen();
        int dx = compLocation.x - 2;
        int dy = compLocation.y + component.getHeight() + 4;
        setLocation(dx, dy);
    }

    /**
     * Creates the main panel.
     *
     * @return the panel
     */
    private JPanel createPanel()
    {
        GridBagPanel panel = new GridBagPanel();
        panel.anchorWest();
        panel.setInsets(4, 4, 0, 0);

        panel.setGridwidth(3);
        panel.addRow(new JLabel("Set the active time to:"));

        JSpinner durationMagnitudeSpinner = ControllerFactory.createComponent(myDurationMagnitude, JSpinner.class);
        ComponentUtilities.setPreferredWidth(durationMagnitudeSpinner, 100);

        JComboBox<ChronoUnit> durationMagnitudeCombo = ControllerFactory.createComponent(myDurationUnit, JComboBox.class);

        JButton applyButton = new JButton("Apply");
        applyButton.setMargin(new Insets(3, 8, 3, 8));
        applyButton.addActionListener(e -> updateTimeSpan(myDurationMagnitude.get().intValue(), myDurationUnit.get()));

        panel.setGridwidth(1);
        panel.addRow(new JLabel("The last"), durationMagnitudeSpinner, durationMagnitudeCombo);
        panel.add((Component)null);
        panel.incrementGridx().setGridwidth(2).anchorEast().setInsets(8, 0, 0, 0).add(applyButton);

        return panel;
    }

    /**
     * Updates the time span and period type models.
     *
     * @param magnitude the duration magnitude
     * @param unit the duration unit
     */
    private void updateTimeSpan(int magnitude, ChronoUnit unit)
    {
        myTimePeriodType.set(TimePeriodType.CUSTOM);

        Duration duration = Duration.create(unit, magnitude);
        Duration roundInterval = unit == ChronoUnit.HOURS ? Seconds.ONE : Days.ONE;
        long endDate = TimelineUtilities.roundUp(new Date(), roundInterval).getTimeInMillis();
        TimeSpan span = TimeSpan.get(duration, endDate);

        /* Set the loop span first to avoid problems if the loop span is locked. */
        myAnimationModel.getLoopSpan().set(span);

        myTimeSpan.set(span);

        if (!myAnimationModel.loadIntervalsProperty().isEmpty())
        {
            Notify.info("You have load intervals in the timeline, so data may not load for the time you just selected");
        }
    }
}
