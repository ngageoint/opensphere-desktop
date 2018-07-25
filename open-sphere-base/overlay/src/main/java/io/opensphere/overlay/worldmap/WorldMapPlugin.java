package io.opensphere.overlay.worldmap;

import java.awt.Dimension;
import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.api.adapter.AbstractHUDWindowMenuItemPlugin;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.Window.ResizeOption;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

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
     * Constructor.
     */
    public WorldMapPlugin()
    {
        super("WorldMap", true, true);
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
                ResizeOption.RESIZE_KEEP_FIXED_SIZE)
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
