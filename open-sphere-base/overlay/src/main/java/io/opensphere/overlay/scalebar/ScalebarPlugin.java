package io.opensphere.overlay.scalebar;

import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.api.adapter.AbstractHUDWindowMenuItemPlugin;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.Window.ResizeOption;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.ScreenBoundingBox;
import io.opensphere.core.model.ScreenPosition;

/** Plug-in that provides a scale bar overlay. */
public class ScalebarPlugin extends AbstractHUDWindowMenuItemPlugin
{
    /**
     * Constructor.
     */
    public ScalebarPlugin()
    {
        super("ScaleBar", true, true);
    }

    @Override
    protected Window<?, ?> createWindow(TransformerHelper helper, ScheduledExecutorService executor)
    {
        ScreenPosition scaleUpLeft = new ScreenPosition(0, 0);
        ScreenPosition scaleLowRight = new ScreenPosition(230, 40);
        ScreenBoundingBox scalebarLocation = new ScreenBoundingBox(scaleUpLeft, scaleLowRight);
        Window<?, ?> window = new HUDScalebar(helper, executor, scalebarLocation, ToolLocation.SOUTHEAST,
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
