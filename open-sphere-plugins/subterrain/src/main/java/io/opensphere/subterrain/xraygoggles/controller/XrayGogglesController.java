package io.opensphere.subterrain.xraygoggles.controller;

import io.opensphere.core.MapManager;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.geometry.GeometryRegistry;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Manages the different controller classes used by the xray goggles viewer.
 */
public class XrayGogglesController
{
    /**
     * Resizes the xray window when user clicks and drags a border.
     */
    private final WindowResizer myResizer;

    /**
     * Manages the screen positions of the xray window.
     */
    private final ScreenPositionManager myScreenPosManager;

    /**
     * Converts the screen coordintates of the xray window to geo coordinates.
     */
    private final ScreenToGeo myScreenToGeo;

    /**
     * The xray window validator.
     */
    private final XrayWindowValidator myWindowValidator;

    /**
     * Makes tiles that are within the xray viewer transparent.
     */
    private final XrayTechnician myXrayer;

    /**
     * Constructs a new controller.
     *
     * @param mapManager Used to figure out screen size.
     * @param geometryRegistry Used to get the tiles within the xray window.
     * @param controlRegistry Used to listen for mouse events.
     * @param model The xray goggles model used by the xray goggles componenets.
     */
    public XrayGogglesController(MapManager mapManager, GeometryRegistry geometryRegistry, ControlRegistry controlRegistry,
            XrayGogglesModel model)
    {
        myWindowValidator = new XrayWindowValidator(model);
        myXrayer = new XrayTechnician(mapManager, geometryRegistry, model);
        myScreenPosManager = new ScreenPositionManager(mapManager, controlRegistry, model);
        myScreenToGeo = new ScreenToGeo(mapManager, model);
        myResizer = new WindowResizer(controlRegistry, model);
    }

    /**
     * Stops listening for changes.
     */
    public void close()
    {
        myWindowValidator.close();
        myXrayer.close();
        myScreenPosManager.close();
        myScreenToGeo.close();
        myResizer.close();
    }
}
