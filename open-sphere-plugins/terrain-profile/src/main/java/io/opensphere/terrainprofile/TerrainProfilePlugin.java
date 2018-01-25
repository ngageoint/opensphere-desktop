package io.opensphere.terrainprofile;

import java.util.concurrent.ScheduledExecutorService;

import io.opensphere.core.api.adapter.AbstractHUDWindowMenuItemPlugin;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.core.hud.framework.Window;
import io.opensphere.core.units.length.Length;

/** Plug-in that provides a terrain profile display. */
public class TerrainProfilePlugin extends AbstractHUDWindowMenuItemPlugin
{
    /** Terrain profile chart window. */
    private TerrainProfileManager myTerrainProfileManager;

    /**
     * Constructor.
     */
    public TerrainProfilePlugin()
    {
        super("TerrainProfile", true, false);
    }

    @Override
    protected void buttonDeselected()
    {
        if (myTerrainProfileManager != null)
        {
            myTerrainProfileManager.close();
            myTerrainProfileManager = null;
        }
    }

    @Override
    protected void buttonSelected()
    {
        if (myTerrainProfileManager == null)
        {
            myTerrainProfileManager = new TerrainProfileManager(getHelper(), getExecutor(),
                    getToolbox().getUnitsRegistry().getUnitsProvider(Length.class));
            myTerrainProfileManager.open();
        }
    }

    @Override
    protected Window<?, ?> createWindow(TransformerHelper helper, ScheduledExecutorService executor)
    {
        return null;
    }
}
