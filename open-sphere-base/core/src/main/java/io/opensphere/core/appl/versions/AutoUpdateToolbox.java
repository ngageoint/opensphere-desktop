package io.opensphere.core.appl.versions;

import io.opensphere.core.PluginToolbox;
import io.opensphere.core.appl.versions.model.AutoUpdatePreferences;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.Service;

/** A plugin-toolbox in which an auto-update items are maintained. */
public class AutoUpdateToolbox implements PluginToolbox, Service
{
    /** The model in which auto-update state is stored. */
    private final AutoUpdatePreferences myPreferences;

    /**
     * Creates a new auto-update toolbox, configured with the supplied
     * parameters.
     *
     * @param preferences the preferences container in which user-preferences
     *            are stored.
     */
    public AutoUpdateToolbox(Preferences preferences)
    {
        myPreferences = new AutoUpdatePreferences(preferences);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.PluginToolbox#getDescription()
     */
    @Override
    public String getDescription()
    {
        return "The AutoUpdate toolbox";
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Service#open()
     */
    @Override
    public void open()
    {
        myPreferences.open();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.util.Service#close()
     */
    @Override
    public void close()
    {
        myPreferences.close();
    }

    /**
     * Gets the preferences.
     *
     * @return the preferences
     */
    public AutoUpdatePreferences getPreferences()
    {
        return myPreferences;
    }
}
