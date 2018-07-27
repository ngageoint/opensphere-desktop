package io.opensphere.overlay.worldmap;

import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.api.adapter.AbstractHUDWindowMenuItemPlugin;
import io.opensphere.core.hud.framework.Component;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.Window.ResizeOption;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;
import io.opensphere.core.util.AwesomeIconSolid;
import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.overlay.controls.BufferedImageButton;
import io.opensphere.overlay.controls.ControlComponentContainer;
import io.opensphere.overlay.controls.HUDGraphicUtilities;
import javafx.beans.property.BooleanProperty;

/** Plug-in that provides a 2D world map overlay. */
public class WorldMapPlugin extends AbstractHUDWindowMenuItemPlugin
{
    /** The width of the container to draw. */
    private static final int WIDTH = 300;

    /** The distance from the right of the screen to draw the controls. */
    private static final int DEFAULT_RIGHT_MARGIN = 48;

    /** The distance from the top of the screen to draw the controls. */
    private static final int DEFAULT_TOP_MARGIN = 14;

    /**
     * The property in which the minimized state is tracked. True indicates that
     * the window is minimized, false restored.
     */
    private final BooleanProperty myMinimizedStateProperty;

    /** The window displayed when the plugin is minimized. */
    private Window<?, ?> myMinimizedWindow;

    /** The foreground color with which to draw controls. */
    private static final Color DEFAULT_CONTROL_FOREGROUND = Color.WHITE;

    /** The background color with which to draw controls. */
    private static final Color DEFAULT_CONTROL_BACKGROUND = ColorUtilities.convertFromHexString("71333333", 1, 2, 3, 0);

    /**
     * Constructor.
     */
    public WorldMapPlugin()
    {
        super("WorldMap", true, true);
        myMinimizedStateProperty = new ConcurrentBooleanProperty(false);
    }

    /**
     * Creates and displays the "minimized window".
     */
    protected void openMinimizedWindow()
    {
        if (myMinimizedWindow == null)
        {
            myMinimizedWindow = createMinimizedWindow(getHelper());
            myMinimizedWindow.init();
            myMinimizedWindow.display();
        }
    }

    /**
     * Creates the window displayed when the plugin is minimized.
     * 
     * @param helper the helper to use when executing transform operations.
     * @return a window to be displayed when the plugin is minimized.
     */
    private Window<?, ?> createMinimizedWindow(TransformerHelper helper)
    {
        Dimension screenSize = getToolbox().getUIRegistry().getMainFrameProvider().get().getSize();

        int topLeftX = (int)screenSize.getWidth() - DEFAULT_RIGHT_MARGIN - 30;
        int topLeftY = DEFAULT_TOP_MARGIN + 1;

        int bottomRightX = topLeftX + 30;
        int bottomRightY = topLeftY + 30;

        ScreenPosition topLeft = new ScreenPosition(topLeftX, topLeftY);
        ScreenPosition bottomRight = new ScreenPosition(bottomRightX, bottomRightY);
        ScreenBoundingBox size = new ScreenBoundingBox(topLeft, bottomRight);

        return new ControlComponentContainer(helper, size, ToolLocation.NORTHEAST, ResizeOption.RESIZE_KEEP_FIXED_SIZE,
                this::createRestoreButton);
    }

    /**
     * Creates a button used to restore the main mini-map window.
     * 
     * @param parent the component to which the button will be bound.
     * @return a buffered image button bound to the supplied parent.
     */
    private BufferedImageButton createRestoreButton(Component parent)
    {
        BufferedImageButton button = new BufferedImageButton(parent, this::restore,
                HUDGraphicUtilities.drawIcon(AwesomeIconSolid.ANGLE_DOUBLE_LEFT, ControlComponentContainer.DEFAULT_COMPONENT_SIZE,
                        14, DEFAULT_CONTROL_BACKGROUND, DEFAULT_CONTROL_FOREGROUND));
        button.setFrameLocation(new ScreenBoundingBox(new ScreenPosition(0, 0), new ScreenPosition(
                ControlComponentContainer.DEFAULT_COMPONENT_SIZE, ControlComponentContainer.DEFAULT_COMPONENT_SIZE)));
        return button;
    }

    /**
     * Closes the minimized window, if it is currently displayed (not null).
     */
    protected void closeMinimizedWindow()
    {
        if (myMinimizedWindow != null)
        {
            myMinimizedWindow.closeWindow();
            myMinimizedWindow = null;
        }
    }

    /**
     * Executes the "minimize" functionality. This removes the primary window
     * and disposes of it, and displays the minimize window.
     */
    protected synchronized void minimize()
    {
        if (!myMinimizedStateProperty.get())
        {
            openMinimizedWindow();
            // this is a bit wonky, but due to the parent class's layout, it
            // needs to work like this. buttonDeselected closes the main window.
            super.buttonDeselected();
            myMinimizedStateProperty.set(true);
        }
    }

    /**
     * Executes the "restore" functionality. This removes the minimize window
     * and disposes of it, then displays the primary window.
     * 
     * @param button the button that triggered the restore operation.
     */
    protected synchronized void restore(BufferedImageButton button)
    {
        if (myMinimizedStateProperty.get())
        {
            // this is a bit wonky, but due to the parent class's layout, it
            // needs to work like this. buttonSelected opens the main window.
            super.buttonSelected();
            closeMinimizedWindow();
            myMinimizedStateProperty.set(false);
        }
    }

    @Override
    protected Window<?, ?> createWindow(TransformerHelper helper, ScheduledExecutorService executor)
    {
        Dimension screenSize = getToolbox().getUIRegistry().getMainFrameProvider().get().getSize();

        int topLeftX = (int)screenSize.getWidth() - DEFAULT_RIGHT_MARGIN - WIDTH;
        int topLeftY = DEFAULT_TOP_MARGIN;

        ScreenPosition mapUpLeft = new ScreenPosition(topLeftX, topLeftY);
        ScreenPosition mapLowRight = new ScreenPosition(mapUpLeft.getX() + WIDTH, mapUpLeft.getY() + 150);

        ScreenBoundingBox worldmapLocation = new ScreenBoundingBox(mapUpLeft, mapLowRight);
        Window<?, ?> window = new HUDWorldMap(helper, executor, worldmapLocation, ToolLocation.NORTHEAST,
                ResizeOption.RESIZE_KEEP_FIXED_SIZE, this::minimize)
        {
            @Override
            public synchronized void moveWindow(ScreenPosition delta)
            {
                super.moveWindow(delta);
                updateStoredLocationPreference();
            }
        };

        return window;
    }
}
