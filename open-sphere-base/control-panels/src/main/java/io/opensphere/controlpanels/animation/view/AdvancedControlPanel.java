package io.opensphere.controlpanels.animation.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import io.opensphere.controlpanels.animation.model.AnimationModel;
import io.opensphere.controlpanels.animation.model.PlayState;
import io.opensphere.core.options.OptionsRegistry;
import io.opensphere.core.units.duration.Duration;
import io.opensphere.core.units.duration.DurationUnitsProvider;
import io.opensphere.core.util.ChangeListener;
import io.opensphere.core.util.ObservableValue;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.image.IconUtil.IconType;
import io.opensphere.core.util.swing.ButtonPanel;
import io.opensphere.core.util.swing.ComponentUtilities;
import io.opensphere.core.util.swing.DialogPanel;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.IconButton;
import io.opensphere.core.util.swing.input.controller.ControllerFactory;
import io.opensphere.core.util.swing.input.model.ChoiceModel;
import io.opensphere.core.util.swing.input.model.IntegerModel;

/**
 * Advanced control panel.
 */
class AdvancedControlPanel extends GridBagPanel implements DialogPanel
{
    /** The serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Listener for changes to the advance duration. */
    private final transient ChangeListener<Duration> myAdvanceDurationListener = new ChangeListener<Duration>()
    {
        @Override
        public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue)
        {
            updateAdvanceDurationUIs();
        }
    };

    /** The advance duration magnitude. */
    private final IntegerModel myAdvanceDurationMagnitude = new IntegerModel(1, Integer.MAX_VALUE);

    /** The advance duration unit. */
    private final ChoiceModel<ChronoUnit> myAdvanceDurationUnit = new ChoiceModel<>(ChronoUnit.MONTHS, ChronoUnit.WEEKS,
            ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS);

    /** The advance duration magnitude listener. */
    private transient ChangeListener<Integer> myAdvanceDurationMagnitudeListener;

    /** The advance duration unit listener. */
    private transient ChangeListener<ChronoUnit> myAdvanceDurationUnitListener;

    /** The animation model. */
    private final transient AnimationModel myAnimationModel;

    /** The options registry. */
    private final transient OptionsRegistry myOptionsRegistry;

    /** Play state listener. */
    private transient ChangeListener<PlayState> myPlayStateListener;

    /** The settings button. */
    private final IconButton mySettingsButton = new IconButton("Settings");

    /**
     * Used to indicate when a listener is being fired from within another
     * listener.
     */
    private boolean myUpdating;

    /** The duration units provider. */
    private final transient DurationUnitsProvider myDurationUnitsProvider = new DurationUnitsProvider();

    /**
     * Constructor.
     *
     * @param optionsRegistry the options registry
     * @param animationModel the animation model
     */
    public AdvancedControlPanel(OptionsRegistry optionsRegistry, AnimationModel animationModel)
    {
        super();
        myOptionsRegistry = optionsRegistry;
        myAnimationModel = animationModel;

        // Settings button
        IconUtil.setIcons(mySettingsButton, IconType.COGS);
        mySettingsButton.setToolTipText("Settings");

        // Advance duration
        updateAdvanceDurationUIs();
        updateAdvanceDurationEnabled();
        myAdvanceDurationMagnitude.setName("Advance duration");
        myAdvanceDurationMagnitude.setDescription("The amount the animation moves with each step");
        JSpinner advanceDurationMagnitudeSpinner = ControllerFactory.createComponent(myAdvanceDurationMagnitude, JSpinner.class);
        ComponentUtilities.setPreferredWidth(advanceDurationMagnitudeSpinner, 100);
        myAdvanceDurationUnit.setDescription("The units of the advance duration");
        JComboBox<ChronoUnit> advanceDurationMagnitudeCombo = ControllerFactory.createComponent(myAdvanceDurationUnit,
                JComboBox.class);

        // Fade
        myAnimationModel.getFade().setNameAndDescription("Fade",
                "The percentage of the active window in which features are faded out.");

        // Build the panel
        init0().anchorWest();
        style("doublewide").setGridwidth(2);
        style();
        addRow(ControllerFactory.createLabel(myAdvanceDurationMagnitude, advanceDurationMagnitudeSpinner),
                advanceDurationMagnitudeSpinner, advanceDurationMagnitudeCombo);
        JSlider fadeSlider = ControllerFactory.createComponent(myAnimationModel.getFadeUser(), JSlider.class);
        fadeSlider.setMajorTickSpacing(100);
        fadeSlider.setPaintLabels(true);
        style(null, "doublewide").addRow(ControllerFactory.createLabel(myAnimationModel.getFade(), fadeSlider), fadeSlider);

        addGUIListeners();
        addAnimationStateListeners();
    }

    @Override
    public boolean accept()
    {
        return true;
    }

    @Override
    public void cancel()
    {
    }

    @Override
    public Collection<? extends Component> getContentButtons()
    {
        return Collections.singletonList(mySettingsButton);
    }

    @Override
    public Collection<String> getDialogButtonLabels()
    {
        return Collections.singletonList(ButtonPanel.CLOSE);
    }

    @Override
    public String getTitle()
    {
        return "Animation Controls";
    }

    /**
     * Adds listeners for changes to the animation state.
     */
    private void addAnimationStateListeners()
    {
        myPlayStateListener = new ChangeListener<PlayState>()
        {
            @Override
            public void changed(ObservableValue<? extends PlayState> observable, PlayState oldValue, PlayState newValue)
            {
                updateAdvanceDurationEnabled();
            }
        };
        myAnimationModel.playStateProperty().addListener(myPlayStateListener);
        myAnimationModel.advanceDurationProperty().addListener(myAdvanceDurationListener);
    }

    /**
     * Adds listeners for my GUI elements.
     */
    private void addGUIListeners()
    {
        mySettingsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                myOptionsRegistry.requestShowTopic(AnimationOptionsProvider.TOPIC);
            }
        });

        myAdvanceDurationMagnitudeListener = new ChangeListener<Integer>()
        {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue)
            {
                if (!myUpdating)
                {
                    myUpdating = true;
                    myAnimationModel.setAdvanceDuration(getAdvanceDuration());
                    myUpdating = false;
                }
            }
        };
        myAdvanceDurationMagnitude.addListener(myAdvanceDurationMagnitudeListener);

        myAdvanceDurationUnitListener = new ChangeListener<ChronoUnit>()
        {
            @Override
            public void changed(ObservableValue<? extends ChronoUnit> observable, ChronoUnit oldValue, ChronoUnit newValue)
            {
                if (!myUpdating)
                {
                    myUpdating = true;
                    myAnimationModel.setAdvanceDuration(getAdvanceDuration());
                    myUpdating = false;
                }
            }
        };
        myAdvanceDurationUnit.addListener(myAdvanceDurationUnitListener);
    }

    /**
     * Gets the advance duration from the UI components.
     *
     * @return the advance duration
     */
    private Duration getAdvanceDuration()
    {
        return Duration.create(myAdvanceDurationUnit.get(), myAdvanceDurationMagnitude.get().longValue());
    }

    /**
     * Updates the enabled state of the advance duration UIs based on the model.
     */
    private void updateAdvanceDurationEnabled()
    {
        boolean enabled = !myAnimationModel.getPlayState().isPlaying();
        myAdvanceDurationMagnitude.setEnabled(enabled);
        myAdvanceDurationUnit.setEnabled(enabled);
    }

    /**
     * Updates the advance duration UIs from the model.
     */
    private void updateAdvanceDurationUIs()
    {
        // View events are ignored partly to cut the listener loop short, but
        // mostly to prevent the convertUp method from changing the units on the
        // user as they're using the UI.
        if (!myUpdating)
        {
            myUpdating = true;
            Duration advanceDuration = myDurationUnitsProvider.getLargestIntegerUnitType(myAnimationModel.getAdvanceDuration());
            while (!advanceDuration.isInIntRange())
            {
                BigDecimal mag = advanceDuration.getMagnitude();
                advanceDuration = myDurationUnitsProvider.getLargestIntegerUnitType(
                        Duration.create(advanceDuration.getClass(), mag.setScale(mag.scale() - 3, RoundingMode.HALF_UP)));
            }
            myAdvanceDurationMagnitude.set(Integer.valueOf(advanceDuration.intValue()));
            myAdvanceDurationUnit.set(advanceDuration.getChronoUnit());
            myUpdating = false;
        }
    }
}
