package io.opensphere.core.viewer.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import io.opensphere.core.MapManager;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.Altitude.ReferenceLevel;
import io.opensphere.core.model.GeographicPosition;
import io.opensphere.core.util.ref.VolatileReference;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.MapContext;
import io.opensphere.core.viewer.impl.ViewControlTranslator;

/**
 * A listener that handles zooming with a mouse drag.
 */
public class FastZoomListener extends AbstractTargetedViewAdapter
{
    /**
     * Moves the globe after a zoom.
     */
    private final GlobeMover myGlobeMover = new GlobeMover();

    /**
     * Used to determine the starting geographic position.
     */
    private MapManager myMapManager;

    /**
     * The starting mouse position.
     */
    private Vector2i myMouseStartPosition;

    /**
     * The starting geographic position.
     */
    private GeographicPosition myStartPosition;

    /** A reference to the view control translator. */
    private final VolatileReference<ViewControlTranslator> myViewControlTranslator;

    /**
     * Construct the listener.
     *
     * @param viewControlTranslator A reference to the current view control
     *            translator.
     * @param context The map context.
     */
    public FastZoomListener(VolatileReference<ViewControlTranslator> viewControlTranslator, MapContext<DynamicViewer> context)
    {
        super("View", "Fast Mouse Zoom", "A fast mouse-driven zoom, engaged with the bound button or key, <br/>"
                + "and sliding the mouse either forwards or backwards");
        myViewControlTranslator = viewControlTranslator;
        if (context instanceof MapManager)
        {
            myMapManager = (MapManager)context;
        }
    }

    @Override
    public void eventEnded(InputEvent event)
    {
        myStartPosition = null;
        myMouseStartPosition = null;
        myViewControlTranslator.get().compoundZoomEnd(event);
    }

    @Override
    public void eventStarted(InputEvent event)
    {
        if (myMapManager != null && event instanceof MouseEvent)
        {
            myMouseStartPosition = new Vector2i(((MouseEvent)event).getX(), ((MouseEvent)event).getY());
            myStartPosition = myMapManager.convertToPosition(myMouseStartPosition, ReferenceLevel.ELLIPSOID);
        }

        myViewControlTranslator.get().compoundZoomStart(event);
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        myViewControlTranslator.get().compoundZoomAction(e);
        moveGlobe();
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        myViewControlTranslator.get().compoundZoomAction(e);
        moveGlobe();
    }

    /**
     * Moves the globe so that the location the start position after will be at
     * the same screen location after zoom.
     */
    private void moveGlobe()
    {
        if (myMapManager != null && myStartPosition != null
                && myViewControlTranslator.get() instanceof AbstractViewerControlTranslator && myMouseStartPosition != null)
        {
            AbstractViewerControlTranslator viewer = (AbstractViewerControlTranslator)myViewControlTranslator.get();
            myGlobeMover.moveGlobe(viewer, myMapManager, myStartPosition, myMouseStartPosition);
        }
    }
}
