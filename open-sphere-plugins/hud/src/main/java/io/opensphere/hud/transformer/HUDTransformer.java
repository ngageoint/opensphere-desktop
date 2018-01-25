package io.opensphere.hud.transformer;

import io.opensphere.core.Toolbox;
import io.opensphere.core.api.DefaultTransformer;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.hud.framework.TransformerHelper;
import io.opensphere.hud.launcher.HUDLauncher;

/**
 * Transforms HUD models into {@link Geometry}s.
 */
public class HUDTransformer extends DefaultTransformer
{
    /** Helper class for handling publishing of geometries. */
    private final TransformerHelper myHelper;

    /** Helper class for launching components. */
    private final HUDLauncher myLauncher;

    /**
     * Constructor.
     *
     * @param toolbox The tool box.
     */
    public HUDTransformer(Toolbox toolbox)
    {
        super((DataRegistry)null);
        myHelper = new TransformerHelper(this, toolbox);
        myLauncher = new HUDLauncher(myHelper, toolbox);
    }

    /** Cleanup all of my windows. */
    public synchronized void cleanup()
    {
        if (!isOpen())
        {
            return;
        }
        if (myHelper != null)
        {
            myHelper.cleanup();
        }
    }

    @Override
    public synchronized void open()
    {
        super.open();

        myLauncher.init();
    }
}
