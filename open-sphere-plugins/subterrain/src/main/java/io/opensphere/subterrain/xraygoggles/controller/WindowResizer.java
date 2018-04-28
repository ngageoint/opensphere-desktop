package io.opensphere.subterrain.xraygoggles.controller;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.control.PickListener;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Resizes the Xray window when the user picks it and drags the mouse.
 */
public class WindowResizer implements PickListener, DiscreteEventListener
{
    /**
     * Used to listen to the pick and mouse events.
     */
    private final ControlRegistry myControlRegistry;

    /**
     * True if the xray window is picked, false otherwise.
     */
    private boolean myIsPicked;

    /**
     * The last point the mouse was at.
     */
    private Point myLastPoint;

    /**
     * The model used by the xray components.
     */
    private final XrayGogglesModel myModel;

    /**
     * Constructs a new window resizer.
     *
     * @param controlRegistry Used to listen to the pick and mouse events.
     * @param model The model used by the xray components.
     */
    public WindowResizer(ControlRegistry controlRegistry, XrayGogglesModel model)
    {
        myControlRegistry = controlRegistry;
        myModel = model;
        ControlContext gluiCtx = myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
        gluiCtx.addPickListener(this);

        DefaultMouseBinding pressed = new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON1_DOWN_MASK);
        DefaultMouseBinding drag = new DefaultMouseBinding(MouseEvent.MOUSE_DRAGGED, InputEvent.BUTTON1_DOWN_MASK);
        DefaultMouseBinding released = new DefaultMouseBinding(MouseEvent.MOUSE_RELEASED, InputEvent.BUTTON1_DOWN_MASK);
        ControlContext globeCtx = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        globeCtx.addListener(this, pressed, drag, released);
    }

    /**
     * Stops listening for mouse events.
     */
    public void close()
    {
        ControlContext gluiCtx = myControlRegistry.getControlContext(ControlRegistry.GLUI_CONTROL_CONTEXT);
        gluiCtx.removePickListener(this);

        ControlContext globeCtx = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        globeCtx.removeListener(this);
    }

    @Override
    public void eventOccurred(InputEvent event)
    {
        if (event instanceof MouseEvent && myIsPicked)
        {
            event.consume();
            MouseEvent mouseEvent = (MouseEvent)event;

            if (event.getID() == MouseEvent.MOUSE_PRESSED)
            {
                myLastPoint = mouseEvent.getPoint();
            }
            else if (event.getID() == MouseEvent.MOUSE_DRAGGED && myLastPoint != null && mouseEvent.getY() > 0)
            {
                double deltaY = myLastPoint.getY() - mouseEvent.getY();
                double deltaX = myLastPoint.getX() - mouseEvent.getX();

                double newY = myModel.getUpperLeft().getY() - deltaY;

                double centerX = (myModel.getUpperRight().getX() - myModel.getUpperLeft().getX()) / 2
                        + myModel.getUpperLeft().getX();
                double newXLeftUp;
                double newXRightUp;
                double newXLeftDown;
                double newXRightDown;
                if (mouseEvent.getPoint().getX() < centerX)
                {
                    newXLeftUp = myModel.getUpperLeft().getX() - deltaX;
                    newXLeftDown = myModel.getLowerLeft().getX() - deltaX;
                    newXRightUp = myModel.getUpperRight().getX() + deltaX;
                    newXRightDown = myModel.getLowerRight().getX() + deltaX;
                }
                else
                {
                    newXLeftUp = myModel.getUpperLeft().getX() + deltaX;
                    newXLeftDown = myModel.getLowerLeft().getX() + deltaX;
                    newXRightUp = myModel.getUpperRight().getX() - deltaX;
                    newXRightDown = myModel.getLowerRight().getX() - deltaX;
                }

                // If the window gets to close to the screen boundaries it
                // draws incorrectly
                ScreenPosition upperLeft = new ScreenPosition(newXLeftUp, newY);
                ScreenPosition upperRight = new ScreenPosition(newXRightUp, newY);
                ScreenPosition lowerLeft = new ScreenPosition(newXLeftDown, myModel.getLowerLeft().getY());
                ScreenPosition lowerRight = new ScreenPosition(newXRightDown, myModel.getLowerRight().getY());

                myModel.setScreenPosition(upperLeft, upperRight, lowerLeft, lowerRight);

                myLastPoint = mouseEvent.getPoint();
            }
            else if (event.getID() == MouseEvent.MOUSE_RELEASED)
            {
                myIsPicked = false;
                myLastPoint = null;
            }
        }
    }

    @Override
    public String getCategory()
    {
        return XrayGogglesModel.XRAY_GOGGLES;
    }

    @Override
    public String getDescription()
    {
        return XrayGogglesModel.XRAY_GOGGLES;
    }

    @Override
    public int getTargetPriority()
    {
        return 10000;
    }

    @Override
    public String getTitle()
    {
        return XrayGogglesModel.XRAY_GOGGLES;
    }

    @Override
    public void handlePickEvent(PickEvent evt)
    {
        if (myLastPoint == null)
        {
            myIsPicked = evt.getPickedGeometry() != null && evt.getPickedGeometry().equals(myModel.getWindowGeometry());
        }
    }

    @Override
    public boolean isReassignable()
    {
        return false;
    }

    @Override
    public boolean isTargeted()
    {
        return myIsPicked;
    }

    @Override
    public boolean mustBeTargeted()
    {
        return true;
    }
}
