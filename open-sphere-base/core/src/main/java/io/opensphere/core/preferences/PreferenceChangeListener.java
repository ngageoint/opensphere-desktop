package io.opensphere.core.preferences;

/**
 * A listener for preference changes.
 */
@FunctionalInterface
public interface PreferenceChangeListener
{
    /**
     * Invoked when a preference change has occurred.
     *
     * @param evt - the {@link PreferenceChangeEvent}
     */
    void preferenceChange(PreferenceChangeEvent evt);
}
