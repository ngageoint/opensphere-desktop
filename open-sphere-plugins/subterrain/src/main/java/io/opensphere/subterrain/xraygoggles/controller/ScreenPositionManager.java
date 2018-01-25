package io.opensphere.subterrain.xraygoggles.controller;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import io.opensphere.core.MapManager;
import io.opensphere.core.control.ControlContext;
import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.control.DefaultMouseBinding;
import io.opensphere.core.control.DiscreteEventListener;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.viewer.ViewChangeSupport;
import io.opensphere.core.viewer.ViewChangeSupport.ViewChangeListener;
import io.opensphere.core.viewer.Viewer;
import io.opensphere.subterrain.xraygoggles.model.XrayGogglesModel;

/**
 * Manages the Xray goggles screen position, and changes the corner position if
 * and when the main window is resized.
 */
public class ScreenPositionManager implements ViewChangeListener, DiscreteEventListener
{
    /**
     * The amount of change needed before we update our xray window.
     */
    private static final double PITCH_DELTA_FOR_UPDATE = 0.01;

    /**
     * Used to listen to the pick and mouse events.
     */
    private final ControlRegistry myControlRegistry;

    /**
     * Indicates if the user is tilting the camera.
     */
    private boolean myIsUserPitching;

    /**
     * Used to get the screen size, also notifies us on screen size changes.
     */
    private final MapManager myMapManager;

    /**
     * The model used by the xray goggles components.
     */
    private final XrayGogglesModel myModel;

    /**
     * The previous pitch angle.
     */
    private double myPreviousPitch = Double.MIN_VALUE;

    /**
     * Constructs a new screen position manager.
     *
     * @param mapManager Used to get the screen size, also notifies us on screen
     *            size changes.
     * @param controlRegistry Used to detect when user is tilting the camera.
     * @param model The model used by the xray goggles components.
     */
    public ScreenPositionManager(MapManager mapManager, ControlRegistry controlRegistry, XrayGogglesModel model)
    {
        myMapManager = mapManager;
        myControlRegistry = controlRegistry;
        myModel = model;
        calculateScreenPos(myMapManager.getScreenViewer(), myMapManager.getStandardViewer().getPitch());
        myMapManager.getViewChangeSupport().addViewChangeListener(this);
        DefaultMouseBinding pressed = new DefaultMouseBinding(MouseEvent.MOUSE_PRESSED, InputEvent.BUTTON3_DOWN_MASK);
        DefaultMouseBinding released = new DefaultMouseBinding(MouseEvent.MOUSE_RELEASED, InputEvent.BUTTON3_MASK);
        ControlContext globeContext = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        globeContext.addListener(this, pressed, released);
    }

    /**
     * Stops listening for screen size changes.
     */
    public void close()
    {
        ControlContext globeContext = myControlRegistry.getControlContext(ControlRegistry.GLOBE_CONTROL_CONTEXT);
        globeContext.removeListener(this);
        myMapManager.getViewChangeSupport().removeViewChangeListener(this);
        myModel.setScreenPosition(null, null, null, null);
        myModel.setGeoPosition(null, null, null, null, null);
    }

    @Override
    public void eventOccurred(InputEvent event)
    {
        if (event instanceof MouseEvent)
        {
            MouseEvent mouseEvent = (MouseEvent)event;
            if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED)
            {
                myIsUserPitching = true;
            }
            else if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED)
            {
                myIsUserPitching = false;
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
        return "Xray Goggles Screen Manager";
    }

    @Override
    public int getTargetPriority()
    {
        return 10002;
    }

    @Override
    public String getTitle()
    {
        return "Xray Goggles Screen Manager";
    }

    @Override
    public boolean isReassignable()
    {
        return false;
    }

    @Override
    public boolean isTargeted()
    {
        return false;
    }

    @Override
    public boolean mustBeTargeted()
    {
        return false;
    }

    @Override
    public void viewChanged(Viewer viewer, ViewChangeSupport.ViewChangeType type)
    {
        ThreadUtilities.runCpu(() ->
        {
            double currentPitch = Math.toDegrees(myMapManager.getStandardViewer().getPitch());
            if (type == ViewChangeSupport.ViewChangeType.WINDOW_RESIZE || pitchChanged(currentPitch) && myIsUserPitching)
            {
                calculateScreenPos(viewer, currentPitch);
            }
        });
    }

    /**
     * Calculates the trapezoidal window position and sets the
     * {@link XrayGogglesModel} with those positions.
     *
     * @param viewer The main view.
     * @param currentPitch The current camera pitch angle in degrees.
     */
    private void calculateScreenPos(Viewer viewer, double currentPitch)
    {
        int screenHeight = viewer.getViewportHeight();
        int screenWidth = viewer.getViewportWidth();

        // Make the trapezoid shape
        int topWidth = screenWidth / 2;
        int bottomWidth = screenWidth / 3;
        int height = screenHeight / 3;
        int center = screenWidth / 2;
        int topWidthHalf = topWidth / 2;
        int bottomWidthHalf = bottomWidth / 2;

        int topLeftX = center - topWidthHalf;
        int topRightX = center + topWidthHalf;
        int bottomLeftX = center - bottomWidthHalf;
        int bottomRightX = center + bottomWidthHalf;

        int topY = screenHeight - height;
        int bottomY = screenHeight;

        ScreenPosition topLeft = new ScreenPosition(topLeftX, topY);
        ScreenPosition topRight = new ScreenPosition(topRightX, topY);
        ScreenPosition bottomLeft = new ScreenPosition(bottomLeftX, bottomY);
        ScreenPosition bottomRight = new ScreenPosition(bottomRightX, bottomY);

        if (pitchChanged(currentPitch) && myModel.getUpperLeftGeo() != null && myModel.getUpperRightGeo() != null)
        {
            Vector2i upperLeft = myMapManager.convertToPoint(myModel.getUpperLeftGeo());
            Vector2i upperRight = myMapManager.convertToPoint(myModel.getUpperRightGeo());

            topLeft = new ScreenPosition(upperLeft.getX(), upperLeft.getY());
            topRight = new ScreenPosition(upperRight.getX(), upperLeft.getY());
            bottomLeft = myModel.getLowerLeft();
            bottomRight = myModel.getLowerRight();
        }

        myModel.setScreenPosition(topLeft, topRight, bottomLeft, bottomRight);

        myPreviousPitch = currentPitch;
    }

    /**
     * Checks to see if the camera's pitch angle has changed.
     *
     * @param currentPitch The current camera pitch angle in degrees.
     * @return True it is has change false otherwise.
     */
    private boolean pitchChanged(double currentPitch)
    {
        return Math.abs(myPreviousPitch - currentPitch) > PITCH_DELTA_FOR_UPDATE;
    }
}
