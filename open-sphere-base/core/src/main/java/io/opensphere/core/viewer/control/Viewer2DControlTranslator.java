package io.opensphere.core.viewer.control;

import java.awt.event.InputEvent;

import io.opensphere.core.math.Vector2i;
import io.opensphere.core.math.Vector3d;
import io.opensphere.core.util.MathUtil;
import io.opensphere.core.util.ref.VolatileReference;
import io.opensphere.core.viewer.impl.DynamicViewer;
import io.opensphere.core.viewer.impl.Viewer2D;
import io.opensphere.core.viewer.impl.Viewer2D.ViewerPosition2D;

/**
 * Translate raw mouse events into viewer movements.
 */
@SuppressWarnings("PMD.GodClass")
public class Viewer2DControlTranslator extends AbstractViewerControlTranslator
{
    /**
     * The amount zoom input is multiplied by to determine the change in scale.
     */
    private static final double ZOOM_FACTOR = .05;

    /** True when the controls are enabled and false when they are not. */
    private boolean myControlEnabled = true;

    /**
     * Construct a Viewer2DControlTranslator.
     *
     * @param viewer The viewer to manipulate.
     */
    public Viewer2DControlTranslator(VolatileReference<DynamicViewer> viewer)
    {
        super(viewer);
    }

    @Override
    public boolean canAdjustViewer(Vector2i from, Vector2i to)
    {
        return true;
    }

    @Override
    public void compoundMoveAxisDrag(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public void compoundMoveAxisEnd(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public void compoundMoveAxisStart(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public void compoundViewPitchDrag(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public void compoundViewPitchEnd(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public void compoundViewPitchStart(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public void compoundViewYawDrag(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public void compoundViewYawEnd(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public void compoundViewYawStart(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public synchronized void moveView(double deltaX, double deltaY)
    {
        if (!myControlEnabled)
        {
            return;
        }
        ViewerPosition2D viewPosition = getViewer().getPosition();
        double modelWidth = getViewer().getModelWidth();
        double modelHeight = getViewer().getModelHeight();
        double stretchFactor = getViewer().getStretchFactor();
        Vector3d location = new Vector3d(
                viewPosition.getLocation().getX() - deltaX / getViewer().getViewportWidth() * modelWidth
                        / (stretchFactor > 1. ? stretchFactor : 1.) / getViewer().getScale(),
                viewPosition.getLocation().getY() + deltaY / getViewer().getViewportHeight() * modelHeight
                        * (stretchFactor < 1. ? stretchFactor : 1.) / getViewer().getScale(),
                0.);

        getViewer().setPosition(new ViewerPosition2D(location, getViewer().getScale()));
    }

    @Override
    public void moveView(Vector2i from, Vector2i to)
    {
        if (!myControlEnabled)
        {
            return;
        }
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        moveView(dx, dy);
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
    public void pitchView(double angleRads)
    {
    }

    @Override
    public void pitchView(Vector2i from, Vector2i to)
    {
    }

    @Override
    public void pitchViewDown(InputEvent event)
    {
        pitchView(1f);
    }

    @Override
    public void pitchViewUp(InputEvent event)
    {
        pitchView(-1f);
    }

    @Override
    public void resetView(InputEvent event)
    {
        // Not Applicable to this map type (2d)
    }

    @Override
    public void rollView(double angleRads)
    {
    }

    @Override
    public void rollView(Vector2i from, Vector2i to)
    {
    }

    @Override
    public void rollViewLeft(InputEvent event)
    {
        rollView(1f);
    }

    @Override
    public void rollViewRight(InputEvent event)
    {
        rollView(-1f);
    }

    @Override
    public void setControlEnabled(boolean enable)
    {
        myControlEnabled = enable;
    }

    @Override
    public void spinOnAxis(double angleRads)
    {
        // Not implemented for this map type
    }

    @Override
    public void spinOnAxis(double angleRads, Vector3d spinAxis)
    {
        // Not implemented for this map type
    }

    @Override
    public void viewDown(InputEvent event, boolean microMovement)
    {
        if (!myControlEnabled)
        {
            return;
        }
        int yMove = microMovement ? -1 : -10;
        moveView(new Vector2i(0, 0), new Vector2i(0, yMove));
    }

    @Override
    public void viewLeft(InputEvent event, boolean microMovement)
    {
        if (!myControlEnabled)
        {
            return;
        }
        int xMove = microMovement ? 1 : 10;
        moveView(new Vector2i(0, 0), new Vector2i(xMove, 0));
    }

    @Override
    public void viewRight(InputEvent event, boolean microMovement)
    {
        if (!myControlEnabled)
        {
            return;
        }
        int xMove = microMovement ? -1 : -10;
        moveView(new Vector2i(0, 0), new Vector2i(xMove, 0));
    }

    @Override
    public void viewUp(InputEvent event, boolean microMovement)
    {
        if (!myControlEnabled)
        {
            return;
        }
        int yMove = microMovement ? 1 : 10;
        moveView(new Vector2i(0, 0), new Vector2i(0, yMove));
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
    public void yawViewLeft(InputEvent event)
    {
        yawView(-1f);
    }

    @Override
    public void yawViewRight(InputEvent event)
    {
        yawView(1f);
    }

    @Override
    public void zoomView(double amount)
    {
        if (!myControlEnabled)
        {
            return;
        }
        double newScale = getViewer().getScale() * Math.pow(2, amount * ZOOM_FACTOR);
        if (Math.abs(newScale - getViewer().getScale()) > MathUtil.DBL_EPSILON)
        {
            ViewerPosition2D viewPosition = getViewer().getPosition();
            getViewer().setPosition(new ViewerPosition2D(viewPosition.getLocation(), newScale));
        }
    }

    @Override
    protected Viewer2D getViewer()
    {
        return (Viewer2D)super.getViewer();
    }
}
