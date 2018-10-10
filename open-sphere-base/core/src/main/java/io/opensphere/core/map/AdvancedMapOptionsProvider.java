package io.opensphere.core.map;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.LinkedSliderTextField;
import io.opensphere.core.util.swing.LinkedSliderTextField.PanelSizeParameters;

/**
 * This provider provides the panel for settings relating to the map. The Apply
 * and Restore buttons are disabled for this panel.
 */
public class AdvancedMapOptionsProvider extends AbstractOptionsProvider
{
    /** The options topic for zoom preferences. */
    public static final String MAP_ADVANCED_TOPIC = "Advanced";

    /** The Preference key for the zoom rate. */
    public static final String MODEL_DENSITY_KEY = "MODEL_DENSITY";

    /**
     * The range for the pixel width spread. The slider is on a scale from 1 to
     * 100, but the values used are based on the size of the tessera on screen,
     * so scaling is required.
     */
    private static final double SCALE_SPREAD = 0.8;

    /** The preferences for map options. */
    private final Preferences myPreferences;

    /**
     * This listener is used in combination with {@link WeakChangeSupport}, so
     * keep a reference to prevent the listener from becoming weakly reachable.
     */
    private final ActionListener mySliderListener;

    /**
     * The main panel for holding any display elements used for setting the
     * options.
     */
    private GridBagPanel myTerrainPanel;

    /**
     * Constructor.
     *
     * @param prefsRegistry The system preferences registry.
     */
    public AdvancedMapOptionsProvider(PreferencesRegistry prefsRegistry)
    {
        super(MAP_ADVANCED_TOPIC);
        myPreferences = prefsRegistry.getPreferences(AdvancedMapOptionsProvider.class);
        mySliderListener = evt ->
        {
            Quantify.collectMetric("mist3d.settings.map.advanced.terrain-density-change");
            LinkedSliderTextField sfp = (LinkedSliderTextField)evt.getSource();
            int reverse = 101 - sfp.getValue();
            // scale to a number between 40 and 120
            int scaled = (int)(40 + reverse * SCALE_SPREAD);

            myPreferences.putInt(MODEL_DENSITY_KEY, scaled, AdvancedMapOptionsProvider.this);
        };
    }

    @Override
    public void applyChanges()
    {
    }

    @Override
    public JPanel getOptionsPanel()
    {
        // Doing a lazy create here allows the panel to be created after the
        // look and feel has been loaded.
        if (myTerrainPanel == null)
        {
            myTerrainPanel = new GridBagPanel();
            myTerrainPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 50, 10));
            LinkedSliderTextField slider = new LinkedSliderTextField("Terrain Density:", 1, 125, 50,
                    new PanelSizeParameters(35, 24, 0));
            int setting = myPreferences.getInt(MODEL_DENSITY_KEY, 80);
            int adjusted = (int)(100 - (setting - 40) * (1. / SCALE_SPREAD));
            slider.setValues(adjusted);
            slider.addSliderFieldChangeListener(mySliderListener);

            myTerrainPanel.setInsets(0, 35, 0, 50).setGridx(0).setGridy(0).anchorCenter().fillHorizontal();
            String terrainText = "<html>Increasing terrain density will increase the amount of resources used.<br>"
                    + "In some cases, using the maximum terrain density setting<br>"
                    + "may cause the tool to become unstable.<br><br>"
                    + "Note: Some terrain providers have a maximum density built in.<br>"
                    + "In those cases, increasing the density setting past the built in<br>"
                    + "maximum will have no effect on the terrain density.<br<br></html>";
            myTerrainPanel.add(new JLabel(terrainText));
            myTerrainPanel.setInsets(0, 35, 15, 50).incrementGridy().add(slider);
            myTerrainPanel.incrementGridy().add(new JLabel("Default Terrain Density is 50"));
            myTerrainPanel.setGridx(0).incrementGridy().fillVerticalSpace();
        }
        return myTerrainPanel;
    }

    @Override
    public void restoreDefaults()
    {
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public boolean usesRestore()
    {
        return false;
    }
}
