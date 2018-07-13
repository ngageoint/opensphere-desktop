package io.opensphere.overlay;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.bric.swing.ColorPicker;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.ColorIcon;
import io.opensphere.core.util.swing.SmallColorPalette;
import io.opensphere.core.util.swing.SplitButton;

/**
 * Manages the background for the globe and the UI for background color options.
 */
public final class GlobeBackgroundManager
{
    /** The topic for background color preference. */
    private static final String ourBackgroundColorPrefKey = "background_color";

    /** The topic for background color preference. */
    private static final String ourBackgroundEnabledKey = "background_enabled";

    /** The current background color. */
    private Color myBackgroundColor;

    /** The transformer for displaying background color. */
    private final transient SolidBackgroundTransformer myBackgroundTransformer;

    /** The Color selector button. */
    private SplitButton myColorSelectorSplitButton;

    /** The enabled checkbox. */
    private JCheckBox myEnabledCheckbox;

    /** The preferences. */
    private final transient Preferences myPreferences;

    /** The toolbox. */
    private Toolbox myToolbox;

    /** The system UI registry. */
    private final UIRegistry myUIRegistry;

    /**
     * Constructor.
     *
     * @param toolbox The toolbox to access the state.
     * @param uiRegistry The system UI registry.
     * @param transformer The transformer used to change background color.
     */
    public GlobeBackgroundManager(Toolbox toolbox, UIRegistry uiRegistry,
            SolidBackgroundTransformer transformer)
    {
        myToolbox = toolbox;
        myUIRegistry = uiRegistry;
        myBackgroundTransformer = transformer;
        myPreferences = myToolbox.getPreferencesRegistry().getPreferences(getClass());

        setupToolbarComponent();

        // Set the default values.
        final Color defaultBackgroundColor = new Color(0, 60, 100);
        if (myPreferences != null)
        {
            myBackgroundColor = new Color(myPreferences.getInt(ourBackgroundColorPrefKey, defaultBackgroundColor.getRGB()), true);

            // Turning off the globe background may result in no pickable earth
            // tiles being drawn. When this is the case geometries on the back
            // side of the earth can be picked.
            if (Boolean.getBoolean("opensphere.productionMode"))
            {
                myEnabledCheckbox.setSelected(true);
            }
            else
            {
                final boolean selected = myPreferences.getBoolean(ourBackgroundEnabledKey, true);
                myEnabledCheckbox.setSelected(selected);
            }
        }
        else
        {
            myBackgroundColor = defaultBackgroundColor;
        }

        myBackgroundTransformer.setColor(myBackgroundColor);

        IconUtil.setIcons(myColorSelectorSplitButton, "/images/earth-filled.png", myBackgroundColor);
        myColorSelectorSplitButton.revalidate();
        myColorSelectorSplitButton.repaint();

        if (myEnabledCheckbox.isSelected())
        {
            myBackgroundTransformer.publishBackground();
        }

        uiRegistry.getIconLegendRegistry().addIconToLegend(IconUtil.getNormalIcon("/images/earth-filled.png"),
                "Earth background color",
                "All base layers should be turned off or opacitized before the earth background is visible. "
                        + "Press the button to access the color picker directly or select one of the preset "
                        + "colors from the dropdown.");
    }

    /**
     * Change the background color and update gui elements (if needed).
     *
     * @param color The new color.
     */
    public void setBackgroundColor(Color color)
    {
        myBackgroundColor = color;

        // update the button color
        IconUtil.setIcons(myColorSelectorSplitButton, "/images/earth-filled.png", myBackgroundColor);
        myColorSelectorSplitButton.revalidate();
        myColorSelectorSplitButton.repaint();

        // update the color on the globe
        myBackgroundTransformer.setColor(color);

        // Update preferences
        myPreferences.putInt(ourBackgroundColorPrefKey, myBackgroundColor.getRGB(), this);
        myPreferences.putBoolean(ourBackgroundEnabledKey, myEnabledCheckbox.isSelected(), this);
    }

    /**
     * Setup toolbar component.
     */
    private void setupToolbarComponent()
    {
        myEnabledCheckbox = new JCheckBox("On/Off");
        myEnabledCheckbox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                myPreferences.putBoolean(ourBackgroundEnabledKey, myEnabledCheckbox.isSelected(), this);
                QuantifyToolboxUtils.collectEnableDisableMetric(myToolbox, "mist3d.overlay.globe-color.checkbox.background",
                        myEnabledCheckbox.isSelected());
                if (myEnabledCheckbox.isSelected())
                {
                    myBackgroundTransformer.publishBackground();
                }
                else
                {
                    myBackgroundTransformer.unpublishBackground();
                }
            }
        });
        myEnabledCheckbox.setFocusPainted(false);
        myEnabledCheckbox.setToolTipText("Turn on/off globe background.");

        final SmallColorPalette scp = new SmallColorPalette(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                final JButton button = (JButton)evt.getSource();
                if (button.getIcon() instanceof ColorIcon)
                {
                    QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.overlay.globe-color.button.small-color-palette");
                    myColorSelectorSplitButton.toggleDropComponentVisibility();
                    final ColorIcon cci = (ColorIcon)button.getIcon();
                    setBackgroundColor(cci.getColor());
                }
            }
        });

        final JPanel scpPanel = new JPanel(new BorderLayout());
        if (!Boolean.getBoolean("opensphere.productionMode"))
        {
            scpPanel.add(myEnabledCheckbox, BorderLayout.NORTH);
        }
        scpPanel.add(scp, BorderLayout.CENTER);

        myColorSelectorSplitButton = new SplitButton("Globe Color", null);
        myColorSelectorSplitButton.add(scpPanel);
        myColorSelectorSplitButton.setToolTipText("Set the globe background color");
        myColorSelectorSplitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                QuantifyToolboxUtils.collectMetric(myToolbox, "mist3d.overlay.globe-color.button.open-color-menu");
                showPicker();
            }
        });
        myUIRegistry.getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "GlobeBackground",
                myColorSelectorSplitButton, 405, SeparatorLocation.LEFT, new Insets(0, 2, 0, 2));
    }

    /** Display a color picker to allow selection of a new color. */
    private void showPicker()
    {
        final ColorPicker picker = new ColorPicker(true, true);
        picker.setColor(myBackgroundColor);
        picker.addPropertyChangeListener(new PropertyChangeListener()
        {
            @Override
            public void propertyChange(PropertyChangeEvent evt)
            {
                setBackgroundColor(picker.getColor());
            }
        });

        final Color initialColor = myBackgroundColor;

        final JDialog cpDialog = new JDialog(myUIRegistry.getMainFrameProvider().get(), "Set Globe Background Color");
        cpDialog.setLocationRelativeTo(myColorSelectorSplitButton);
        cpDialog.getContentPane().setLayout(new BorderLayout());
        cpDialog.getContentPane().add(picker, BorderLayout.CENTER);
        final JPanel panel = new JPanel();
        final JButton revert = new JButton("Revert Changes");
        revert.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                picker.setColor(initialColor);
            }
        });
        panel.add(revert);
        cpDialog.getContentPane().add(panel, BorderLayout.SOUTH);
        cpDialog.setSize(500, 425);
        cpDialog.setVisible(true);
    }
}
