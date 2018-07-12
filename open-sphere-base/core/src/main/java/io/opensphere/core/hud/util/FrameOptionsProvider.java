package io.opensphere.core.hud.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import com.bric.swing.ColorPicker;

import io.opensphere.core.options.impl.AbstractPreferencesOptionsProvider;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.swing.AbstractHUDPanel;
import io.opensphere.core.util.swing.ColorCircleIcon;
import io.opensphere.core.util.swing.LinkedSliderTextField;
import io.opensphere.core.util.swing.LinkedSliderTextField.PanelSizeParameters;

/** The Options provider for options relating to the HUD. */
public class FrameOptionsProvider extends AbstractPreferencesOptionsProvider
{
    /** Constant conversion factor to convert alpha to percent opacity. */
    private static final float ALPHA_FACTOR = 2.55f;

    /** The default frame inset distance. */
    public static final int DEFAULT_INSET = 3;

    /** The default sticky setting. */
    public static final boolean DEFAULT_STICKY = true;

    /** The Options topic for HUD options. */
    private static final String HUD_TOPIC = "HUD Options";

    /** The preference key for the frame inset pixel distance. */
    public static final String INSET_PREFERENCE_KEY = "FRAMES_INSET";

    /** The preference key for the sticky option. */
    public static final String STICKY_PREFERENCE_KEY = "FRAMES_STICKY";

    /** The spinner for the frame inset setting. */
    private JSpinner myInsetSpinner;

    /** The panel which contains the options settings. */
    private final JPanel myMainPanel;

    /** The check box for selecting sticky / non-sticky. */
    private JCheckBox myStickyCheckbox;

    /** The HUD background opacity slider. */
    private LinkedSliderTextField myBackgroundOpacitySlider;

    /** The my background color button. */
    private JButton myBackgroundColorButton;

    /** The current HUD background color. */
    private Color myBackgroundColor;

    /** The default color (no existing preferences). */
    private final Color myDefaultColor = AbstractHUDPanel.ourDefaultHUDBackgroundColor;

    /**
     * Constructor.
     *
     * @param prefsRegistry The system preferences registry.
     */
    public FrameOptionsProvider(PreferencesRegistry prefsRegistry)
    {
        super(prefsRegistry, HUD_TOPIC);
        myMainPanel = new JPanel();
        myMainPanel.setLayout(new BoxLayout(myMainPanel, BoxLayout.Y_AXIS));
        myMainPanel.add(getHudPositionPanel());

        myMainPanel.add(getHudBackgroundPanel());
    }

    @Override
    public void applyChanges()
    {
        boolean sticky = myStickyCheckbox.isSelected();
        int inset = ((Integer)myInsetSpinner.getValue()).intValue();

        getPreferencesRegistry().getPreferences(FrameOptionsProvider.class).putBoolean(STICKY_PREFERENCE_KEY, sticky, this);
        getPreferencesRegistry().getPreferences(FrameOptionsProvider.class).putInt(INSET_PREFERENCE_KEY, inset, this);
        getPreferencesRegistry().getPreferences(AbstractHUDPanel.class).putInt(AbstractHUDPanel.ourHUDBackgroundColorKey,
                myBackgroundColor.getRGB(), this);
    }

    @Override
    public JPanel getOptionsPanel()
    {
        return myMainPanel;
    }

    @Override
    public void restoreDefaults()
    {
        myInsetSpinner.setValue(Integer.valueOf(3));
        myStickyCheckbox.setSelected(true);
        setBackgroundColor(myDefaultColor);
        applyChanges();
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public boolean usesRestore()
    {
        return true;
    }

    /**
     * Create the description for the background color controls.
     *
     * @return The text area containing the description.
     */
    private JTextArea getBackgroundDescription()
    {
        JTextArea description = new JTextArea();
        description.setBackground(TRANSPARENT_COLOR);
        description.setBorder(BorderFactory.createEmptyBorder());
        description.setFont(description.getFont().deriveFont(Font.PLAIN, description.getFont().getSize() + 1));
        description.setEditable(false);
        Dimension size = new Dimension(400, 100);
        description.setSize(size);
        description.setPreferredSize(size);
        description.setMinimumSize(size);
        description.setMaximumSize(size);
        description.setFocusable(true);
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setText("The following controls will change the background color of HUD windows.");

        return description;
    }

    /**
     * Gets the feature color button.
     *
     * @return the feature color button
     */
    private JButton getColorButton()
    {
        if (myBackgroundColorButton == null)
        {
            myBackgroundColorButton = new JButton();
            myBackgroundColorButton.setOpaque(true);
            myBackgroundColorButton.setBackground(Color.LIGHT_GRAY);
            myBackgroundColorButton.setMaximumSize(new Dimension(20, 20));
            myBackgroundColorButton.setIcon(new ColorCircleIcon(Color.black));
            myBackgroundColorButton.addActionListener(e ->
            {
                QuantifyToolboxUtils.collectMetric("mist3d.settings.hud-options.color-button");
                Color c = ColorPicker.showDialog(SwingUtilities.getWindowAncestor(myMainPanel), myBackgroundColor, true);
                if (c != null)
                {
                    setBackgroundColor(c);
                    getPreferencesRegistry().getPreferences(AbstractHUDPanel.class)
                            .putInt(AbstractHUDPanel.ourHUDBackgroundColorKey, myBackgroundColor.getRGB(), this);
                }
            });
        }
        return myBackgroundColorButton;
    }

    /**
     * Accessor for the panel that holds the controls to change the background
     * color of HUD windows.
     *
     * @return The HUD background color panel.
     */
    private JPanel getHudBackgroundPanel()
    {
        myBackgroundColor = new Color(getPreferencesRegistry().getPreferences(AbstractHUDPanel.class)
                .getInt(AbstractHUDPanel.ourHUDBackgroundColorKey, myDefaultColor.getRGB()), true);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        Dimension size = new Dimension(500, 200);
        panel.setSize(size);
        panel.setPreferredSize(size);
        panel.setMaximumSize(size);

        Box sliderBox = Box.createHorizontalBox();
        sliderBox.add(getOpacitySlider());
        sliderBox.add(new JLabel("%"));
        sliderBox.add(Box.createHorizontalStrut(15));
        sliderBox.add(new JLabel("Color:"));
        sliderBox.add(getColorButton());

        panel.add(Box.createVerticalGlue());
        panel.add(getBackgroundDescription());
        panel.add(Box.createVerticalStrut(15));
        panel.add(sliderBox);
        panel.add(Box.createVerticalGlue());

        setBackgroundColor(myBackgroundColor);
        return panel;
    }

    /**
     * Create the panel which deals with position related options.
     *
     * @return The newly created panel.
     */
    private JPanel getHudPositionPanel()
    {
        JPanel panel = new JPanel();

        boolean stickToEdge = getPreferencesRegistry().getPreferences(FrameOptionsProvider.class)
                .getBoolean(STICKY_PREFERENCE_KEY, DEFAULT_STICKY);
        int inset = getPreferencesRegistry().getPreferences(FrameOptionsProvider.class).getInt(INSET_PREFERENCE_KEY,
                DEFAULT_INSET);

        myStickyCheckbox = new JCheckBox("Stick to Edges");
        myStickyCheckbox.setSelected(stickToEdge);
        myStickyCheckbox.addActionListener(e ->
        {
            QuantifyToolboxUtils.collectMetric("mist3d.settings.hud-options.stick-to-edge-checkbox");
            updateStickyPreference();
        });
        myInsetSpinner = new JSpinner(new SpinnerNumberModel(inset, 0, 25, 1));
        myInsetSpinner.addChangeListener(e ->
        {
            QuantifyToolboxUtils.collectMetric("mist3d.settings.hud-options.inset-spinner");
            updateStickyPreference();
        });

        panel.add(myStickyCheckbox);
        panel.add(myInsetSpinner);
        panel.add(new JLabel("Window Inset"));

        return panel;
    }

    /**
     * Accessor for the background opacity slider.
     *
     * @return The background opacity slider.
     */
    private LinkedSliderTextField getOpacitySlider()
    {
        if (myBackgroundOpacitySlider == null)
        {
            int opacityPct = (int)(myBackgroundColor.getAlpha() / ALPHA_FACTOR);
            Dimension sliderSize = new Dimension(350, 30);
            myBackgroundOpacitySlider = new LinkedSliderTextField("Opacity ", 0, 100, opacityPct,
                    new PanelSizeParameters(35, 28, 0));
            myBackgroundOpacitySlider.setMaximumSize(sliderSize);
            myBackgroundOpacitySlider.setPreferredSize(sliderSize);
            myBackgroundOpacitySlider.setSize(sliderSize);
            myBackgroundOpacitySlider.addSliderFieldChangeListener(e ->
            {
                QuantifyToolboxUtils.collectMetric("mist3d.settings.hud-options.opacity-slider");
                int alpha = (int)(getOpacitySlider().getSliderValue() * ALPHA_FACTOR);
                Color color = new Color(myBackgroundColor.getRed(), myBackgroundColor.getGreen(), myBackgroundColor.getBlue(),
                        alpha);
                setBackgroundColor(color);
                getPreferencesRegistry().getPreferences(AbstractHUDPanel.class).putInt(AbstractHUDPanel.ourHUDBackgroundColorKey,
                        myBackgroundColor.getRGB(), this);
            });
            myBackgroundOpacitySlider.setBackground(myBackgroundColor);
            myBackgroundOpacitySlider.setValues(opacityPct);
        }
        return myBackgroundOpacitySlider;
    }

    /**
     * Sets the background color and updates the controls.
     *
     * @param color The new color.
     */
    private void setBackgroundColor(Color color)
    {
        // Update the sliders (if needed).
        int sliderAlpha = (int)(getOpacitySlider().getSliderValue() * ALPHA_FACTOR);
        if (sliderAlpha != color.getAlpha())
        {
            getOpacitySlider().setValues((int)(color.getAlpha() / ALPHA_FACTOR));
        }

        // Update color picker button (if needed).
        if (!((ColorCircleIcon)getColorButton().getIcon()).getColor().equals(color))
        {
            getColorButton().setIcon(new ColorCircleIcon(color));
        }

        myBackgroundColor = color;
    }

    /**
     * Update sticky preference.
     */
    private void updateStickyPreference()
    {
        boolean sticky = myStickyCheckbox.isSelected();
        int inset = ((Integer)myInsetSpinner.getValue()).intValue();

        getPreferencesRegistry().getPreferences(FrameOptionsProvider.class).putBoolean(STICKY_PREFERENCE_KEY, sticky, this);
        getPreferencesRegistry().getPreferences(FrameOptionsProvider.class).putInt(INSET_PREFERENCE_KEY, inset, this);
    }
}
