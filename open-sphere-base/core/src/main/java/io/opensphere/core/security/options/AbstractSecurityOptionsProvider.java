package io.opensphere.core.security.options;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.options.impl.AbstractOptionsProvider;
import io.opensphere.core.preferences.PreferenceChangeListener;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.security.SecurityManagerImpl;
import io.opensphere.core.security.config.v1.SecurityConfiguration;
import io.opensphere.core.util.swing.EventQueueUtilities;

/**
 * Common functionality for security options providers.
 */
public abstract class AbstractSecurityOptionsProvider extends AbstractOptionsProvider
{
    /** The preference change listener. */
    private final PreferenceChangeListener myPreferenceChangeListener = evt -> EventQueueUtilities.invokeLater(AbstractSecurityOptionsProvider.this::handlePreferenceChange);

    /** The security preferences. */
    private final Preferences myPrefs;

    /** The security manager. */
    private final SecurityManager mySecurityManager;

    /**
     * Constructor.
     *
     * @param securityManager The system security manager.
     * @param preferencesRegistry The preferences registry.
     * @param topic The options topic.
     */
    public AbstractSecurityOptionsProvider(SecurityManager securityManager, PreferencesRegistry preferencesRegistry, String topic)
    {
        super(topic);
        mySecurityManager = securityManager;
        myPrefs = preferencesRegistry.getPreferences(SecurityManagerImpl.class);
        myPrefs.addPreferenceChangeListener(SecurityManagerImpl.SECURITY_CONFIG_KEY, myPreferenceChangeListener);
    }

    @Override
    public void applyChanges()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void restoreDefaults()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean usesApply()
    {
        return false;
    }

    @Override
    public boolean usesRestore()
    {
        return false;
    }

    /**
     * Get the security config from the preferences.
     *
     * @return The config.
     */
    protected final SecurityConfiguration getConfig()
    {
        return myPrefs.getJAXBObject(SecurityConfiguration.class, SecurityManagerImpl.SECURITY_CONFIG_KEY,
                new SecurityConfiguration());
    }

    /**
     * Get the system security manager.
     *
     * @return The security manager.
     */
    protected final SecurityManager getSecurityManager()
    {
        return mySecurityManager;
    }

    /**
     * Handle a change to the security preferences.
     */
    protected abstract void handlePreferenceChange();

    /**
     * Save the security configuration.
     *
     * @param config The configuration.
     */
    protected final void saveConfig(SecurityConfiguration config)
    {
        myPrefs.putJAXBObject(SecurityManagerImpl.SECURITY_CONFIG_KEY, config, true, this);
    }
}
