package io.opensphere.core.pipeline;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;

import io.opensphere.core.control.ui.UIRegistry;

/**
 * Detects what scale factor is currently set for the display on high dpi
 * monitors.
 */
public class ScaleDetector
{
    /**
     * The current dpi scale on the current monitor.
     */
    private float myDPIScale = -1;

    /**
     * Used to figure out which monitor the main frame is located
     *            on.
     */
    private final UIRegistry myRegistry;

    /**
     * Constructor.
     *
     * @param uiRegistry Used to figure out which monitor the main frame is located
     *            on.
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
        if (myDPIScale == -1)
        {
            double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int gdHeight = gd.getDisplayMode().getHeight();
            myDPIScale = (float)(gdHeight / screenHeight);
        }

        return myDPIScale;
    }
}
