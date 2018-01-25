package io.opensphere.hud.dashboard;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.AbstractHUDFrameMenuItemPlugin;
import io.opensphere.core.hud.awt.AbstractInternalFrame;

/**
 * The Class DashboardPlugin.
 */
public class DashboardPlugin extends AbstractHUDFrameMenuItemPlugin
{
    /** The VM metric provider. */
    @SuppressWarnings("PMD.SingularField")
    private JVMMonitor myVMMetricProvider;

    /**
     * Instantiates a new layer manager plugin.
     */
    public DashboardPlugin()
    {
        super(Dashboard.TITLE, true, true);
    }

    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        super.initialize(plugindata, toolbox);
    }

    @Override
    protected AbstractInternalFrame createInternalFrame(Toolbox toolbox)
    {
        Dashboard db = new Dashboard(toolbox);
        db.setVisible(false);
        int mainFrameWidth = toolbox.getUIRegistry().getMainFrameProvider().get().getWidth();
        db.setLocation(mainFrameWidth - 20 - db.getWidth(), 10);

        myVMMetricProvider = new JVMMonitor(getToolbox());
        myVMMetricProvider.start();
        return db;
    }
}
