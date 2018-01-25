package io.opensphere.overlay.worldmap;

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
        ScreenPosition mapUpLeft = new ScreenPosition(100, 100);
        ScreenPosition mapLowRight = new ScreenPosition(mapUpLeft.getX() + 300, mapUpLeft.getY() + 150);
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

        if (useDefaultLocation())
        {
            window.moveToDefaultLocation();
        }

        return window;
    }
}
