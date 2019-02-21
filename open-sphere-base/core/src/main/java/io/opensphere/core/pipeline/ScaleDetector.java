package io.opensphere.core.pipeline;

import java.awt.GraphicsDevice;
import java.awt.Point;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import io.opensphere.core.control.ui.UIRegistry;

/**
 * Detects what scale factor is currently set for the display on high dpi
 * monitors.
 */
public class ScaleDetector
{
    /**
     * Used to log messages.
     */
    private static final Logger LOG = Logger.getLogger(ScaleDetector.class);

    /**
     * The current dpi scale on the current monitor.
     */
    private float myDPIScale = 1.0f;

    /**
     * The previous location of the main window.
     */
    private Point myPreviousLocation;

    /**
     * Used to figure out which monitor the main frame is located on.
     */
    private final UIRegistry myRegistry;

    /**
     * Constructor.
     *
     * @param uiRegistry Used to figure out which monitor the main frame is
     *            located on.
     */
    public ScaleDetector(UIRegistry uiRegistry)
    {
        myRegistry = uiRegistry;
    }

    /**
     * Gets the currently set scale for this monitor.
     *
     * @return The scale.
     */
    public float getScale()
    {
        JFrame mainFrame = myRegistry.getMainFrameProvider().get();
        if (mainFrame.isShowing())
        {
            Point currentLocation = mainFrame.getLocationOnScreen();
            if (!currentLocation.equals(myPreviousLocation))
            {
                myPreviousLocation = currentLocation;
                GraphicsDevice gd = mainFrame.getGraphicsConfiguration().getDevice();
                try
                {
                    Method method = gd.getClass().getMethod("getDefaultScaleY");
                    if (method != null)
                    {
                        Float yScale = (Float)method.invoke(gd);
                        if (yScale != null)
                        {
                            myDPIScale = yScale.floatValue();
                        }
                    }
                }
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
                {
                    LOG.error(e, e);
                }
            }
        }

        return myDPIScale;
    }
}
