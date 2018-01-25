package io.opensphere.core.appl.versions;

import io.opensphere.core.PluginLoaderData;
import io.opensphere.core.Toolbox;
import io.opensphere.core.api.adapter.PluginAdapter;
import io.opensphere.core.appl.versions.controller.AutoUpdateController;
import io.opensphere.core.appl.versions.view.AutoUpdateOptionsProvider;

/** Plugin for notifying users of new versions of the application. */
public class NewVersionPlugin extends PluginAdapter
{
    /** The version options provider. */
    private AutoUpdateOptionsProvider myOptionsProvider;

    /** The toolbox. */
    private Toolbox myToolbox;

    /** The controller used for auto-update synchronization. */
    private AutoUpdateController myController;

    /** The toolbox through which auto-update state access occurs. */
    private AutoUpdateToolbox myAutoUpdateToolbox;

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#initialize(io.opensphere.core.PluginLoaderData,
     *      io.opensphere.core.Toolbox)
     */
    @Override
    public void initialize(PluginLoaderData plugindata, final Toolbox toolbox)
    {
        myToolbox = toolbox;

        myAutoUpdateToolbox = new AutoUpdateToolbox(myToolbox.getPreferencesRegistry().getPreferences(this.getClass()));
        myAutoUpdateToolbox.open();
        myToolbox.getPluginToolboxRegistry().registerPluginToolbox(myAutoUpdateToolbox);

        myController = new AutoUpdateController(myToolbox);
        myController.open();

        myOptionsProvider = new AutoUpdateOptionsProvider(myToolbox, myController);
        myToolbox.getUIRegistry().getOptionsRegistry().addOptionsProvider(myOptionsProvider);

        myController.checkForUpdates(false);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.api.adapter.PluginAdapter#close()
     */
    @Override
    public void close()
    {
        myAutoUpdateToolbox.close();
        myToolbox.getPluginToolboxRegistry().removePluginToolbox(myAutoUpdateToolbox);

        myController.close();
        myToolbox.getUIRegistry().getOptionsRegistry().removeOptionsProvider(myOptionsProvider);

        super.close();
    }
}
