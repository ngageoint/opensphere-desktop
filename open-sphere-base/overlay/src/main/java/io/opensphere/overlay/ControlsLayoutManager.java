package io.opensphere.overlay;

import java.awt.Dimension;

import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.hud.framework.Window.ToolLocation;
import io.opensphere.core.model.ScreenPosition;

/**
 * 
 */
public interface ControlsLayoutManager
{
    void registerControl(Window window, ToolLocation location, int columnNumber, Dimension size);

    void removeControl(Window window);

    ScreenPosition getNextScreenPosition(ToolLocation location, int columnNumber, Dimension size);
}
