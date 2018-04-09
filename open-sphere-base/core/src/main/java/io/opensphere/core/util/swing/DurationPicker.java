package io.opensphere.core.util.swing;

import java.awt.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.DurationUnitsProvider;
import io.opensphere.core.units.duration.Milliseconds;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.StrongObservableValue;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.IntegerModel;

/** Picker for a duration of time. */
public class DurationPicker extends JPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The duration model. */
    private final transient ObservableValue<Duration> myDuration = new StrongObservableValue<>();

    /** The duration magnitude (UI model). */
    private final IntegerModel myDurationMagnitude = new IntegerModel(1, 200_000);

    /** The duration unit (UI model). */
    private final ChoiceModel<ChronoUnit> myDurationUnit;

    /**
     * Used to indicate when a listener is being fired from within another
     * listener.
     */
    private boolean myUpdating;

    /** The duration units provider. */
    private final transient DurationUnitsProvider myDurationUnitsProvider = new DurationUnitsProvider();

    /**
     * Shows a duration picker dialog.
     *
     * @param parent the parent component
     * @param title the dialog title
     * @param message a message to display
     * @param duration the initial duration
     * @param unitOptions the unit options
     * @return the picked duration, or null
     */
    public static Duration show(Component parent, String title, String message, Duration duration, ChronoUnit... unitOptions)
    {
        Duration pickedDuration = null;

        DurationPicker picker = new DurationPicker(unitOptions);
        picker.getDuration().set(duration);

        GridBagPanel panel = new GridBagPanel();
        panel.anchorWest();
        panel.addRow(new JLabel(message));
        panel.addRow(picker);

        OptionDialog dialog = new OptionDialog(parent, panel, title);
        dialog.buildAndShow();
        if (dialog.getSelection() == JOptionPane.OK_OPTION)
        {
            pickedDuration = picker.getDuration().get();
        }

        return pickedDuration;
    }

    /**
     * Constructor.
     *
     * @param unitOptions the unit options
     */
    public DurationPicker(ChronoUnit... unitOptions)
    {
        super();

        myDurationMagnitude.setDescription("The magnitude of the duration");
        myDurationMagnitude.set(Integer.valueOf(1));
        myDurationUnit = new ChoiceModel<>(unitOptions);
        myDurationUnit.setDescription("The unit of the duration");
        myDurationUnit.set(ChronoUnit.MILLIS);

        JSpinner durationMagnitudeSpinner = ControllerFactory.createComponent(myDurationMagnitude, JSpinner.class);
        ComponentUtilities.setPreferredWidth(durationMagnitudeSpinner, 100);
        JComboBox<ChronoUnit> durationMagnitudeCombo = ControllerFactory.createComponent(myDurationUnit, JComboBox.class);

        add(durationMagnitudeSpinner);
        add(durationMagnitudeCombo);

        myDuration.addListener((obs, old, value) -> handleModelChange(value));
        myDuration.set(Milliseconds.ONE);

        ChangeListener<Object> uiListener = (obs, old, value) -> handleUIChange();
        myDurationMagnitude.addListener(uiListener);
        myDurationUnit.addListener(uiListener);
    }

    /**
     * Gets the duration.
     *
     * @return the duration
     */
    public ObservableValue<Duration> getDuration()
    {
        return myDuration;
    }

    /**
     * Handles a change in the duration model.
     *
     * @param duration the duration
     */
    private void handleModelChange(Duration duration)
    {
        if (!myUpdating)
        {
            myUpdating = true;
            try
            {
                Duration convertedDuration = myDurationUnitsProvider.getLargestIntegerUnitType(duration);
                while (!convertedDuration.isInIntRange())
                {
                    BigDecimal mag = convertedDuration.getMagnitude();
                    convertedDuration = myDurationUnitsProvider.getLargestIntegerUnitType(
                            Duration.create(convertedDuration.getClass(), mag.setScale(mag.scale() - 3, RoundingMode.HALF_UP)));
                }
                myDurationMagnitude.set(Integer.valueOf(convertedDuration.intValue()));
                myDurationUnit.set(convertedDuration.getChronoUnit());
            }
            finally
            {
                myUpdating = false;
            }
        }
    }

    /**
     * Handles a change in the UI.
     */
    private void handleUIChange()
    {
        if (!myUpdating)
        {
            myUpdating = true;
            try
            {
                Duration duration = Duration.create(myDurationUnit.get(), myDurationMagnitude.get().longValue());
                myDuration.set(duration);
            }
            finally
            {
                myUpdating = false;
            }
        }
    }
}
