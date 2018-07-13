package io.opensphere.core.map;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.quantify.Quantify;
import io.opensphere.core.util.swing.GridBagPanel;
import io.opensphere.core.util.swing.LinkedSliderTextField;
import io.opensphere.core.util.swing.LinkedSliderTextField.PanelSizeParameters;

/**
 * This provider provides the panel for settings relating to the map. The Apply
 * and Restore buttons are disabled for this panel.
 */
public class MapOptionsProvider extends AbstractOptionsProvider
{
    /** The options topic for zoom preferences. */
    public static final String MAP_TOPIC = "Map";

    /** The Preference key for the zoom rate. */
    public static final String VIEW_ZOOM_RATE_KEY = "VIEW_ZOOM_RATE";

    /**
     * The main panel for holding any display elements used for setting the
     * options.
     */
    private GridBagPanel myZoomPanel;

    /** The preferences for map options. */
    private Preferences myPreferences;

    /**
     * This listener is used in combination with {@link WeakChangeSupport}, so
     * keep a reference to prevent the listener from becoming weakly reachable.
     */
    private final ActionListener mySliderListener = e ->
    {
        Quantify.collectMetric("mist3d.settings.map.zoom-rate-change");
        LinkedSliderTextField sfp = (LinkedSliderTextField)e.getSource();
        myPreferences.putInt(VIEW_ZOOM_RATE_KEY, sfp.getValue(), MapOptionsProvider.this);
    };

    /**
     * Constructor.
     *
     * @param prefsRegistry The system preferences registry.
     */
    public MapOptionsProvider(PreferencesRegistry prefsRegistry)
    {
        super(MAP_TOPIC);
        myPreferences = prefsRegistry.getPreferences(MapOptionsProvider.class);
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
        if (myZoomPanel == null)
        {
            myZoomPanel = new GridBagPanel();
            myZoomPanel.setBorder(BorderFactory.createEmptyBorder(30, 10, 50, 10));
            LinkedSliderTextField slider = new LinkedSliderTextField("Zoom Rate:", 1, 100, 20,
                    new PanelSizeParameters(35, 24, 0));
            slider.setValues(myPreferences.getInt(VIEW_ZOOM_RATE_KEY, 20));
            slider.addSliderFieldChangeListener(mySliderListener);

            myZoomPanel.setInsets(0, 35, 0, 50).setGridx(0).setGridy(0).anchorCenter().fillHorizontal();
            String zoomText = "<html>View zoom is based on a logarithmic scale which means<br>"
                    + "the closer to the surface of the earth the viewer is,<br>" + "the smaller the zoom steps will be.<br><br>"
                    + "Although increasing the zoom rate will increase the amount<br>"
                    + "the viewer zooms with each mouse wheel rotation, the zoom<br>"
                    + "step size will be more noticeable when the viewer is positioned<br>"
                    + "farther away from the earth's surface.<br<br></html>";
            myZoomPanel.add(new JLabel(zoomText));
            myZoomPanel.setInsets(0, 35, 15, 50).incrementGridy().add(slider);
            myZoomPanel.incrementGridy().add(new JLabel("Default Zoom Rate is 20"));
            myZoomPanel.setGridx(0).incrementGridy().fillVerticalSpace();
        }
        return myZoomPanel;
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
