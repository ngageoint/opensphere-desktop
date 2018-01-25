package io.opensphere.xyztile.mantle;

import java.io.Closeable;
import java.util.Observable;
import java.util.Observer;

import javax.xml.ws.WebServiceException;

import io.opensphere.xyztile.model.XYZSettings;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * The controller for the xyz settings ui.
 */
public class XYZSettingsController implements Observer, Closeable
{
    /**
     * The layer we are controlling settings for.
     */
    private final XYZTileLayerInfo myLayer;

    /**
     * The xyz settings.
     */
    private final XYZSettings myModel;

    /**
     * Used to save changes made to the settings.
     */
    private final SettingsBroker mySaver;

    /**
     * Constructs a new controller that saves changes made to the passed in
     * {@link XYZSettings}.
     *
     * @param saver Used to save changes made to the model.
     * @param layer The layer we are controlling settings for.
     * @param model The settings to save the changes made to it.
     */
    public XYZSettingsController(SettingsBroker saver, XYZTileLayerInfo layer, XYZSettings model)
    {
        mySaver = saver;
        myLayer = layer;
        myModel = model;
        myModel.setMinZoomLevel(layer.getMinZoomLevel() + 1);
        myModel.addObserver(this);
    }

    @Override
    public void close() throws WebServiceException
    {
        myModel.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        mySaver.saveSettings(myModel);
        myLayer.setMaxLevelsUser(myModel.getMaxZoomLevelCurrent());
    }
}
