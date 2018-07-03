package io.opensphere.core.quantify.settings;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import javafx.beans.property.BooleanProperty;

/**
 * A simple model bound to the preferences object that keeps the runtime and
 * persisted states in synch.
 */
public class QuantifySettingsModel
{
    /** The preferences in which the state is persisted. */
    private final Preferences myPreferences;

    /**
     * The property in which the enabled state of the quantify plugin is
     * maintained.
     */
    private final BooleanProperty myEnabledProperty = new ConcurrentBooleanProperty(true);

    /**
     * Creates a new model bound to the supplied preferences.
     *
     * @param preferences the preferences in which the state is persisted.
     */
    public QuantifySettingsModel(Preferences preferences)
    {
        myPreferences = preferences;

        myEnabledProperty.set(myPreferences.getBoolean(QuantifyPreferenceKeys.ENABLED_KEY, true));
        myEnabledProperty.addListener((obs, ov, nv) -> persist());
    }

    /**
     * Gets the value of the preferences ({@link #myPreferences}) field.
     *
     * @return the value stored in the {@link #myPreferences} field.
     */
    public Preferences getPreferences()
    {
        return myPreferences;
    }

    /**
     * Gets the 'enabled' property.
     *
     * @return the 'enabled' property.
     */
    public BooleanProperty enabledProperty()
    {
        return myEnabledProperty;
    }

    /** Persist the current state of the model to the preferences object. */
    private void persist()
    {
        myPreferences.putBoolean(QuantifyPreferenceKeys.ENABLED_KEY, myEnabledProperty.get(), this);
    }
}
