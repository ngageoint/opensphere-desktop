package io.opensphere.core.options.impl;

import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * Abstract options provider that provides a preferences registry.
 */
public abstract class AbstractPreferencesOptionsProvider extends AbstractOptionsProvider
{
    /** The preferences registry. */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Instantiates a new options provider.
     *
     * @param preferencesRegistry The preferences registry.
     * @param topic The options topic.
     */
    public AbstractPreferencesOptionsProvider(PreferencesRegistry preferencesRegistry, String topic)
    {
        super(topic);
        myPreferencesRegistry = preferencesRegistry;
    }

    /**
     * Get the preferences registry.
     *
     * @return The preferences registry.
     */
    protected PreferencesRegistry getPreferencesRegistry()
    {
        return myPreferencesRegistry;
    }
}
