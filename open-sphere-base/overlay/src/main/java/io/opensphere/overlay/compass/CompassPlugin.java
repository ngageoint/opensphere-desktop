package io.opensphere.overlay.compass;

import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.api.adapter.AbstractHUDWindowMenuItemPlugin;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.Window.ResizeOption;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** Plugin that displays a compass overlay. */
public class CompassPlugin extends AbstractHUDWindowMenuItemPlugin
{
    /**
     * Constructor.
     */
    public CompassPlugin()
    {
        super("Compass", true, true);
    }

    @Override
    protected Window<?, ?> createWindow(TransformerHelper helper, ScheduledExecutorService executor)
    {
        ScreenPosition compassUpLeft = new ScreenPosition(1200, 50);
        ScreenPosition compassLowRight = new ScreenPosition(1350, 200);
        ScreenBoundingBox compassLocation = new ScreenBoundingBox(compassUpLeft, compassLowRight);
        Window<?, ?> window = new HUDCompass(helper, executor, compassLocation, ToolLocation.EAST,
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
