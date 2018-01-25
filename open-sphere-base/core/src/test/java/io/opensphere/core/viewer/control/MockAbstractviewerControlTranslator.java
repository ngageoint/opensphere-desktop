package io.opensphere.core.viewer.control;

import java.awt.event.InputEvent;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;

/**
 * A mock {@link AbstractViewerControlTranslator} class used for testing.
 */
public class MockAbstractviewerControlTranslator extends AbstractViewerControlTranslator
{
    /**
     * The move from vector.
     */
    private Vector2i myMoveFrom;

    /**
     * The move to vector.
     */
    private Vector2i myMoveTo;

    /**
     * The zoom in event.
     */
    private InputEvent myZoomInEvent;

    /**
     * The zoom out event.
     */
    private InputEvent myZoomOutEvent;

    /**
     * Constructs a new mock class.
     */
    public MockAbstractviewerControlTranslator()
    {
        super(null);
    }

    /**
     * Gets the from vector of the last moveView call.
     *
     * @return The move from vector.
     */
    public Vector2i getMoveFrom()
    {
        return myMoveFrom;
    }

    /**
     * Gets the to vector of the last moveView call.
     *
     * @return The move to vector.
     */
    public Vector2i getMoveTo()
    {
        return myMoveTo;
    }

    /**
     * Gets the zoom in event.
     *
     * @return The zoom in event.
     */
    public InputEvent getZoomInEvent()
    {
        return myZoomInEvent;
    }

    /**
     * Gets the zoom out event.
     *
     * @return The zoom out event.
     */
    public InputEvent getZoomOutEvent()
    {
        return myZoomOutEvent;
    }

    @Override
    public void pitchViewDown(InputEvent event)
    {
    }

    @Override
    public void pitchViewUp(InputEvent event)
    {
    }

    @Override
    public void resetView(InputEvent event)
    {
    }

    @Override
    public void rollViewLeft(InputEvent event)
    {
    }

    @Override
    public void rollViewRight(InputEvent event)
    {
    }

    @Override
    public void setControlEnabled(boolean enable)
    {
    }

    @Override
    public void viewDown(InputEvent event, boolean microMovement)
    {
    }

    @Override
    public void viewLeft(InputEvent event, boolean microMovement)
    {
    }

    @Override
    public void viewRight(InputEvent event, boolean microMovement)
    {
    }

    @Override
    public void viewUp(InputEvent event, boolean microMovement)
    {
    }

    @Override
    public void yawViewLeft(InputEvent event)
    {
    }

    @Override
    public void yawViewRight(InputEvent event)
    {
    }

    @Override
    public boolean canAdjustViewer(Vector2i from, Vector2i to)
    {
        return false;
    }

    @Override
    public void moveView(double deltaX, double deltaY)
    {
    }

    @Override
    public void moveView(Vector2i from, Vector2i to)
    {
        myMoveFrom = from;
        myMoveTo = to;
    }

    @Override
    public void moveViewAxes(double deltaX, double deltaY)
    {
    }

    @Override
    public void moveViewAxes(Vector2i from, Vector2i to)
    {
    }

    @Override
    public void moveYawAxis(double deltaX, double deltaY)
    {
    }

    @Override
    public void moveYawAxis(Vector2i from, Vector2i to)
    {
    }

    @Override
    public void pitchView(double radians)
    {
    }

    @Override
    public void pitchView(Vector2i from, Vector2i to)
    {
    }

    @Override
    public void rollView(double angleRad)
    {
    }

    @Override
    public void rollView(Vector2i from, Vector2i to)
    {
    }

    @Override
    public void spinOnAxis(double angleRads)
    {
    }

    @Override
    public void spinOnAxis(double angleRads, Vector3d spinAxis)
    {
    }

    @Override
    public void yawView(double angleRads)
    {
    }

    @Override
    public void yawView(Vector2i from, Vector2i to)
    {
    }

    @Override
    public void zoomView(double delta)
    {
    }

    @Override
    public void zoomInView(InputEvent event)
    {
        myZoomInEvent = event;
    }

    @Override
    public void zoomOutView(InputEvent event)
    {
        myZoomOutEvent = event;
    }
}
