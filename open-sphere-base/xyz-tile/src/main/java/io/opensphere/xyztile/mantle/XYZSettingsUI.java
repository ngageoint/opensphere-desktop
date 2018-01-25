package io.opensphere.xyztile.mantle;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * The top level UI for XYZ settings.
 */
public class XYZSettingsUI extends JFXPanel
{
    /**
     * Default serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reads and saves settings.
     */
    private final SettingsBroker myBroker;

    /**
     * Saves changes made to the model.
     */
    private final XYZSettingsController myController;

    /**
     * The layer to show settings for.
     */
    private final XYZTileLayerInfo myLayer;

    /**
     * The model containing the settings values.
     */
    private final XYZSettings myModel;

    /**
     * The panel showing the settings.
     */
    private XYZSettingsPanel mySettingsPanel;

    /**
     * Constructs a new settings panel.
     *
     * @param layer The layer to show settings for.
     * @param broker Reads and saves settings.
     */
    public XYZSettingsUI(XYZTileLayerInfo layer, SettingsBroker broker)
    {
        myLayer = layer;
        myBroker = broker;
        myModel = myBroker.getSettings(myLayer);
        myController = new XYZSettingsController(myBroker, myLayer, myModel);
        FXUtilities.runOnFXThread(this::initFX);
    }

    /**
     * Stops listening for changes.
     */
    public void close()
    {
        myController.close();
    }

    /**
     * Gets the layer this UI is displaying settings for.
     *
     * @return The layer this UI is displaying settings for.
     */
    protected XYZTileLayerInfo getLayer()
    {
        return myLayer;
    }

    /**
     * Gets the panel that shows the layers settings.
     *
     * @return The panel that shows the layer's setting values.
     */
    protected XYZSettingsPanel getSettingsPanel()
    {
        return mySettingsPanel;
    }

    /**
     * Initializes the JavaFX components.
     */
    private void initFX()
    {
        mySettingsPanel = new XYZSettingsPanel(myModel);
        Scene scene = new Scene(mySettingsPanel);
        FXUtilities.addDesktopStyle(scene);
        setScene(scene);
    }
}
