package io.opensphere.subterrain.xraygoggles.controller;

import java.util.Observable;
import java.util.Observer;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeType;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Listens for when the screen coordinates change in the xray model. When
 * changed recalculates the new geographic coordinates of the xray windows
 * corners.
 */
public class ScreenToGeo implements Observer, ViewChangeListener
{
    /**
     * A procrastinating executor to help mitigate the number of xrays applied.
     */
    private final ProcrastinatingExecutor myExecutor = new ProcrastinatingExecutor("Xray Vision", 50);

    /**
     * Used to calculate the geo positions.
     */
    private final MapManager myMapManager;

    /**
     * The {@link XrayGogglesModel} to listen for screen coordinate changes.
     */
    private final XrayGogglesModel myModel;

    /**
     * Constructs a new screen to geo class.
     *
     * @param mapManager Used to calculate the geo positions.
     * @param model The {@link XrayGogglesModel} to listen for screen coordinate
     *            changes.
     */
    public ScreenToGeo(MapManager mapManager, XrayGogglesModel model)
    {
        myMapManager = mapManager;
        myModel = model;
        myMapManager.getViewChangeSupport().addViewChangeListener(this);
        myModel.addObserver(this);
        calculateGeos();
    }

    /**
     * Stops listening for screen coordinate changes and viewer changes.
     */
    public void close()
    {
        myExecutor.shutdownNow();
        myModel.deleteObserver(this);
        myMapManager.getViewChangeSupport().removeViewChangeListener(this);
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if (XrayGogglesModel.SCREEN_POSITION.equals(arg))
        {
            calculateGeosInBackground();
        }
    }

    @Override
    public void viewChanged(Viewer viewer, ViewChangeType type)
    {
        if (ViewChangeType.WINDOW_RESIZE != type)
        {
            calculateGeosInBackground();
        }
    }

    /**
     * Calculates the geographic coordinates of the xray windows corners.
     */
    private void calculateGeos()
    {
        if (myModel.getUpperLeft() != null && myModel.getUpperRight() != null && myModel.getLowerLeft() != null
                && myModel.getLowerRight() != null)
        {
            GeographicPosition upperLeft = myMapManager.convertToPosition(myModel.getUpperLeft().asVector2i(),
                    ReferenceLevel.TERRAIN);
            GeographicPosition upperRight = myMapManager.convertToPosition(myModel.getUpperRight().asVector2i(),
                    ReferenceLevel.TERRAIN);
            GeographicPosition lowerLeft = myMapManager.convertToPosition(myModel.getLowerLeft().asVector2i(),
                    ReferenceLevel.TERRAIN);
            GeographicPosition lowerRight = myMapManager.convertToPosition(myModel.getLowerRight().asVector2i(),
                    ReferenceLevel.TERRAIN);

            Vector2i xrayCenterScreen = new Vector2i(
                    (int)((myModel.getLowerRight().getX() - myModel.getLowerLeft().getX()) / 2 + myModel.getLowerLeft().getX()),
                    (int)((myModel.getLowerLeft().getY() - myModel.getUpperLeft().getY()) / 2 + myModel.getUpperLeft().getY()));
            GeographicPosition center = myMapManager.convertToPosition(xrayCenterScreen, ReferenceLevel.TERRAIN);

            myModel.setGeoPosition(upperLeft, upperRight, lowerLeft, lowerRight, center);
        }
    }

    /**
     * Calculates the geographic coordinates on the cpu thread.
     */
    private void calculateGeosInBackground()
    {
        ThreadUtilities.runCpu(() ->
        {
            calculateGeos();
        });
    }
}
