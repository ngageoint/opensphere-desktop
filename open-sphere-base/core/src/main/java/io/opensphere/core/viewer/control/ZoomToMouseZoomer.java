package io.opensphere.core.viewer.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.ViewControlTranslator;

/**
 * Zooms in or out of the map and keeps the map coordinates the mouse is
 * currently pointing at, at the same screen location while the zoom in or out
 * is executing.
 */
public class ZoomToMouseZoomer
{
    /**
     * Moves the globe so the original mouse location is at the original screen
     * position after a zoom.
     */
    private final GlobeMover myGlobeMover = new GlobeMover();

    /**
     * Zooms the view in while keeping the position the mouse is pointed to on
     * the globe at the same screen position.
     *
     * @param viewer Used to perform the zoom and the move of the globe.
     * @param mapManager Used to get the geographic position of the mouse
     *            position.
     * @param event The actual mouse event.
     */
    public void zoomInView(ViewControlTranslator viewer, MapContext<DynamicViewer> mapManager, InputEvent event)
    {
        zoom(viewer, mapManager, event, true);
    }

    /**
     * Zooms the view out while keeping the position the mouse is pointed to on
     * the globe at the same screen position.
     *
     * @param viewer Used to perform the zoom and the move of the globe.
     * @param mapManager Used to get the geographic position of the mouse
     *            position.
     * @param event The actual mouse event.
     */
    public void zoomOutView(ViewControlTranslator viewer, MapContext<DynamicViewer> mapManager, InputEvent event)
    {
        zoom(viewer, mapManager, event, false);
    }

    /**
     * Zooms the view while keeping the position the mouse is pointed to on the
     * globe at the same screen position.
     *
     * @param viewer Used to perform the zoom and the move of the globe.
     * @param mapManager Used to get the geographic position of the mouse
     *            position.
     * @param event The actual mouse event.
     * @param isZoomIn True if the view should zoom in, false if the view should
     *            zoom out.
     */
    private void zoom(ViewControlTranslator viewer, MapContext<DynamicViewer> mapManager, InputEvent event, boolean isZoomIn)
    {
        if (viewer == null)
        {
            // we're doing this before the viewer is initialized. weird.
            return;
        }

        Vector2i mouseScreenPos = null;
        GeographicPosition position = null;
        MapManager theMapManager = null;
        if (event instanceof MouseEvent)
        {
            MouseEvent mouseEvent = (MouseEvent)event;
            mouseScreenPos = new Vector2i(mouseEvent.getX(), mouseEvent.getY());

            if (mapManager instanceof MapManager)
            {
                theMapManager = (MapManager)mapManager;
                position = theMapManager.convertToPosition(mouseScreenPos, ReferenceLevel.ELLIPSOID);
            }
        }

        if (isZoomIn)
        {
            viewer.zoomInView(event);
        }
        else
        {
            viewer.zoomOutView(event);
        }

        if (viewer instanceof AbstractViewerControlTranslator && theMapManager != null)
        {
            myGlobeMover.moveGlobe((AbstractViewerControlTranslator)viewer, theMapManager, position, mouseScreenPos);
        }
    }
}
