package io.opensphere.core.viewer.control;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import org.apache.log4j.Logger;

/**
 * Abstract class for translator event handlers.
 */
public abstract class ViewerControlTranslatorHandler
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ViewerControlTranslatorHandler.class);

    /**
     * Most recent previous point associated with this action.
     */
    private Point myPoint;

    /**
     * Tell the viewer to perform the appropriate action for this handler.
     *
     * @param event Event giving the current mouse state.
     * @param viewer Viewer associated with this handler.
     */
    public abstract void doViewerAction(InputEvent event, AbstractViewerControlTranslator viewer);

    /**
     * The handler is going from active to non-active.
     *
     * @param event mouse event at the time of transition.
     */
    public void eventEnded(InputEvent event)
    {
        myPoint = null;
    }

    /**
     * The mouse is being dragged and this handler is active.
     *
     * @param event mouse event associated with the starting of a viewer event.
     */
    public void eventStarted(InputEvent event)
    {
        if (event instanceof MouseEvent)
        {
            myPoint = ((MouseEvent)event).getPoint();
        }
        else
        {
            // if the user has assigned this event to a key, we don't know
            // the first point until the mouse moves.
            // so mark the point with a max_value and check for it in the
            // mouse moved and mouse dragged calls.
            myPoint = new Point(Integer.MAX_VALUE, 0);
        }
    }

    /**
     * Get the point.
     *
     * @return the point
     */
    public Point getPoint()
    {
        return myPoint;
    }

    /**
     * Perform the action associated with this handler.
     *
     * @param event Event giving the current mouse state.
     * @param viewer Viewer associated with this handler.
     */
    public void performAction(InputEvent event, AbstractViewerControlTranslator viewer)
    {
        if (!(event instanceof MouseEvent))
        {
            LOGGER.warn("Error- somehow non-MouseEvents are making it into the drag earth action");
            return;
        }

        MouseEvent e = (MouseEvent)event;
        if (myPoint != null)
        {
            if (myPoint.x != Integer.MAX_VALUE)
            {
                doViewerAction(event, viewer);
            }
            myPoint = e.getPoint();
        }
    }
}
