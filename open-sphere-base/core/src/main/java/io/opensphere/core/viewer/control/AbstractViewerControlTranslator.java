package io.opensphere.core.viewer.control;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.util.ref.VolatileReference;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.ViewControlTranslator;

/**
 * Translate raw mouse events into viewer movements.
 */
@SuppressWarnings("PMD.GodClass")
public abstract class AbstractViewerControlTranslator implements ViewControlTranslator
{
    /** Handler for events which cause position of the viewer to be dragged. */
    private final CompoundEarthDragHandler myCompoundEarthDragHandler = new CompoundEarthDragHandler();

    /** Handler for events which cause the rotation axis to be moved. */
    private final CompoundMoveAxisHandler myCompoundMoveAxisHandler = new CompoundMoveAxisHandler();

    /** Handler for events which cause pitch changes. */
    private final CompoundViewPitchHandler myCompoundViewPitchHandler = new CompoundViewPitchHandler();

    /** Handler for events which cause yaw changes. */
    private final CompoundViewYawHandler myCompoundViewYawHandler = new CompoundViewYawHandler();

    /** Handler for events which cause zooming. */
    private final CompoundZoomHandler myCompoundZoomHandler = new CompoundZoomHandler();

    /** A reference to the viewer. */
    private final VolatileReference<DynamicViewer> myViewer;

    /** The Zoom rate. */
    private int myZoomRate = -20;

    /**
     * Construct a View3DControlTranslator.
     *
     * @param viewer The viewer to manipulate.
     */
    public AbstractViewerControlTranslator(VolatileReference<DynamicViewer> viewer)
    {
        myViewer = viewer;
    }

    /**
     * Check to see if the viewer can be adjusted.
     *
     * @param from start point on screen based on the origin being in the upper
     *            left corner.
     * @param to end point on screen based on the origin being in the upper left
     *            corner.
     * @return true when the viewer can be adjusted.
     */
    public abstract boolean canAdjustViewer(Vector2i from, Vector2i to);

    @Override
    public void compoundEarthDrag(InputEvent event)
    {
        myCompoundEarthDragHandler.performAction(event, this);
    }

    @Override
    public void compoundEarthDragEnd(InputEvent event)
    {
        myCompoundEarthDragHandler.eventEnded(event);
    }

    @Override
    public void compoundEarthDragStart(InputEvent event)
    {
        myCompoundEarthDragHandler.eventStarted(event);
    }

    @Override
    public void compoundMoveAxisDrag(InputEvent event)
    {
        myCompoundMoveAxisHandler.performAction(event, this);
    }

    @Override
    public void compoundMoveAxisEnd(InputEvent event)
    {
        myCompoundMoveAxisHandler.eventEnded(event);
    }

    @Override
    public void compoundMoveAxisStart(InputEvent event)
    {
        myCompoundMoveAxisHandler.eventStarted(event);
    }

    @Override
    public void compoundViewPitchDrag(InputEvent event)
    {
        myCompoundViewPitchHandler.performAction(event, this);
    }

    @Override
    public void compoundViewPitchEnd(InputEvent event)
    {
        myCompoundViewPitchHandler.eventEnded(event);
    }

    @Override
    public void compoundViewPitchStart(InputEvent event)
    {
        myCompoundViewPitchHandler.eventStarted(event);
    }

    @Override
    public void compoundViewYawDrag(InputEvent event)
    {
        myCompoundViewYawHandler.performAction(event, this);
    }

    @Override
    public void compoundViewYawEnd(InputEvent event)
    {
        myCompoundViewYawHandler.eventEnded(event);
    }

    @Override
    public void compoundViewYawStart(InputEvent event)
    {
        myCompoundViewYawHandler.eventStarted(event);
    }

    @Override
    public void compoundZoomAction(InputEvent event)
    {
        myCompoundZoomHandler.performAction(event, this);
    }

    @Override
    public void compoundZoomEnd(InputEvent event)
    {
        myCompoundZoomHandler.eventEnded(event);
    }

    @Override
    public void compoundZoomStart(InputEvent event)
    {
        myCompoundZoomHandler.eventStarted(event);
    }

    /**
     * Move the view.
     *
     * @param deltaX The amount to move the view in X window coordinates.
     * @param deltaY The amount to move the view in Y window coordinates.
     */
    public abstract void moveView(double deltaX, double deltaY);

    /**
     * Move the view.
     *
     * @param from The start point in window coordinates based on the origin
     *            being in the upper left corner.
     * @param to The end point in window coordinates based on the origin being
     *            in the upper left corner.
     */
    public abstract void moveView(Vector2i from, Vector2i to);

    /**
     * Move the view axes.
     *
     * @param deltaX The amount to move the axes in X window coordinates.
     * @param deltaY The amount to move the axes in Y window coordinates.
     */
    public abstract void moveViewAxes(double deltaX, double deltaY);

    /**
     * Move the view axes.
     *
     * @param from The start point in window coordinates based on the origin
     *            being in the upper left corner.
     * @param to The end point in window coordinates based on the origin being
     *            in the upper left corner.
     */
    public abstract void moveViewAxes(Vector2i from, Vector2i to);

    /**
     * Move the yaw axis.
     *
     * @param deltaX The amount to move the axis in X window coordinates.
     * @param deltaY The amount to move the axis in Y window coordinates.
     */
    public abstract void moveYawAxis(double deltaX, double deltaY);

    /**
     * Move the yaw axis.
     *
     * @param from The start point in window coordinates based on the origin
     *            being in the upper left corner.
     * @param to The end point in window coordinates based on the origin being
     *            in the upper left corner.
     */
    public abstract void moveYawAxis(Vector2i from, Vector2i to);

    /**
     * Pitch the view.
     *
     * @param radians The angle to pitch.
     */
    public abstract void pitchView(double radians);

    /**
     * Pitch the view.
     *
     * @param from The start point in window coordinates based on the origin
     *            being in the upper left corner.
     * @param to The end point in window coordinates based on the origin being
     *            in the upper left corner.
     */
    public abstract void pitchView(Vector2i from, Vector2i to);

    /**
     * Roll the view.
     *
     * @param angleRad The amount to roll, in radians.
     */
    public abstract void rollView(double angleRad);

    /**
     * Roll the view.
     *
     * @param from The start point in window coordinates based on the origin
     *            being in the upper left corner.
     * @param to The end point in window coordinates based on the origin being
     *            in the upper left corner.
     */
    public abstract void rollView(Vector2i from, Vector2i to);

    @Override
    public void setZoomRate(int rate)
    {
        myZoomRate = -rate;
    }

    /**
     * Spin the view on the main spin axis.
     *
     * @param angleRads The amount to spin, in radians.
     */
    public abstract void spinOnAxis(double angleRads);

    /**
     * Spin the view on an arbitrary axis.
     *
     * @param angleRads The amount to spin, in radians.
     * @param spinAxis The spin axis.
     */
    public abstract void spinOnAxis(double angleRads, Vector3d spinAxis);

    /**
     * Yaw the view.
     *
     * @param angleRads The amount to yaw the view, in radians.
     */
    public abstract void yawView(double angleRads);

    /**
     * Yaw the view.
     *
     * @param from The start point in window coordinates based on the origin
     *            being in the upper left corner.
     * @param to The end point in window coordinates based on the origin being
     *            in the upper left corner.
     */
    public abstract void yawView(Vector2i from, Vector2i to);

    @Override
    public void zoomInView(InputEvent event)
    {
        if (event instanceof MouseWheelEvent)
        {
            int rotation = ((MouseWheelEvent)event).getWheelRotation();
            if (Math.abs(rotation) > 2)
            {
                rotation = rotation < 0 ? -2 : 2;
            }
            zoomView(myZoomRate * rotation);
        }
        else
        {
            zoomView(-myZoomRate);
        }
    }

    @Override
    public void zoomOutView(InputEvent event)
    {
        if (event instanceof MouseWheelEvent)
        {
            int rotation = ((MouseWheelEvent)event).getWheelRotation();
            if (Math.abs(rotation) > 2)
            {
                rotation = rotation < 0 ? -2 : 2;
            }
            zoomView(myZoomRate * rotation);
        }
        else
        {
            zoomView(myZoomRate);
        }
    }

    /**
     * Zoom the view an arbitrary amount.
     *
     * @param delta The amount to zoom the view.
     */
    public abstract void zoomView(double delta);

    /**
     * Get the current viewer.
     *
     * @return The viewer.
     */
    protected DynamicViewer getViewer()
    {
        return myViewer.get();
    }

    /**
     * Handler for earth drag events.
     */
    protected static class CompoundEarthDragHandler extends ViewerControlTranslatorHandler
    {
        @Override
        public void doViewerAction(InputEvent event, AbstractViewerControlTranslator viewer)
        {
            MouseEvent e = (MouseEvent)event;
            viewer.moveView(new Vector2i(getPoint()), new Vector2i(e.getPoint()));
        }
    }

    /**
     * Handler for axis move events.
     */
    protected static class CompoundMoveAxisHandler extends ViewerControlTranslatorHandler
    {
        @Override
        public void doViewerAction(InputEvent event, AbstractViewerControlTranslator viewer)
        {
            MouseEvent e = (MouseEvent)event;
            viewer.moveViewAxes(new Vector2i(getPoint()), new Vector2i(e.getPoint()));
        }
    }

    /**
     * Handler for pitch events.
     */
    protected static class CompoundViewPitchHandler extends ViewerControlTranslatorHandler
    {
        @Override
        public void doViewerAction(InputEvent event, AbstractViewerControlTranslator viewer)
        {
            MouseEvent e = (MouseEvent)event;
            viewer.pitchView(new Vector2i(getPoint()), new Vector2i(e.getPoint()));
        }
    }

    /**
     * Handler for yaw events.
     */
    protected static class CompoundViewYawHandler extends ViewerControlTranslatorHandler
    {
        @Override
        public void doViewerAction(InputEvent event, AbstractViewerControlTranslator viewer)
        {
            MouseEvent e = (MouseEvent)event;
            viewer.yawView(new Vector2i(getPoint()), new Vector2i(e.getPoint()));
        }
    }

    /**
     * Handler for zoom events.
     */
    protected static class CompoundZoomHandler extends ViewerControlTranslatorHandler
    {
        @Override
        public void doViewerAction(InputEvent event, AbstractViewerControlTranslator viewer)
        {
            MouseEvent e = (MouseEvent)event;

            if (!viewer.canAdjustViewer(new Vector2i(getPoint()), new Vector2i(e.getPoint())))
            {
                return;
            }

            int zoomFactor = getPoint().y - e.getPoint().y;

            if (zoomFactor != 0)
            {
                viewer.zoomView(zoomFactor);
            }
        }
    }
}
