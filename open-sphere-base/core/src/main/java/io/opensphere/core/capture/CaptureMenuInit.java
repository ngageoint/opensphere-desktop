package io.opensphere.core.capture;

import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

import io.opensphere.core.Toolbox;
import io.opensphere.core.control.ui.ToolbarManager.SeparatorLocation;
import io.opensphere.core.control.ui.ToolbarManager.ToolbarLocation;
import io.opensphere.core.quantify.QuantifyToolboxUtils;
import io.opensphere.core.util.image.IconUtil;
import io.opensphere.core.util.swing.IconButton;

/**
 * Menu initializer for Capture.
 */
public final class CaptureMenuInit
{
    /**
     * Static reference to the screen capture object.
     */
    private static Capture ourScreenCapture;

    /**
     * Used to synchronize access to screen capture.
     */
    private static Object ourLockPad = new Object();

    /**
     * Get the screen capture menu item.
     *
     * @param toolbox The toolbox.
     * @return The menu item.
     */
    public static JMenuItem getScreenCaptureMenuItem(final Toolbox toolbox)
    {
        final JMenuItem captureMenuItem = new JMenuItem("Screen Capture");
        captureMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
                launchScreenCapture(toolbox);
            }
        });

        final IconButton screenCaptureActivationButton = new IconButton("Screen Capture");
        IconUtil.setIcons(screenCaptureActivationButton, "/images/screencapture.png");
        screenCaptureActivationButton.setToolTipText("Save a screen shot");
        screenCaptureActivationButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                launchScreenCapture(toolbox);
            }
        });
        toolbox.getUIRegistry().getToolbarComponentRegistry().registerToolbarComponent(ToolbarLocation.NORTH, "Screen Capture",
                screenCaptureActivationButton, 455, SeparatorLocation.NONE, new Insets(0, 2, 0, 2));

        return captureMenuItem;
    }

    /**
     * Launches the screen capture window.
     *
     * @param toolbox The application toolbox.
     */
    private static void launchScreenCapture(Toolbox toolbox)
    {
        QuantifyToolboxUtils.collectMetric(toolbox, "mist3d.capture.button.capture-screenshot");
        synchronized (ourLockPad)
        {
            if (ourScreenCapture != null)
            {
                if (ourScreenCapture.getState() == Frame.ICONIFIED)
                {
                    ourScreenCapture.setState(Frame.NORMAL);
                }

                ourScreenCapture.setVisible(true);
                ourScreenCapture.requestFocus();
            }
            else
            {
                ourScreenCapture = new Capture(toolbox);
                ourScreenCapture.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                ourScreenCapture.setVisible(true);
            }
        }
    }

    /** Disallow instantiation. */
    private CaptureMenuInit()
    {
    }
}
