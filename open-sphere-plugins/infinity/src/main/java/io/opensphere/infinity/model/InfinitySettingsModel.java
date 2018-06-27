package io.opensphere.infinity.model;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import io.opensphere.core.preferences.BooleanPreferenceBinding;
import io.opensphere.core.preferences.Preferences;

/** Infinity settings model. */
public class InfinitySettingsModel
{
    /** The enabled setting. */
    private final BooleanProperty myEnabled = new SimpleBooleanProperty(this, "enabled");

    /**
     * Constructor.
     *
     * @param preferences the preferences
     */
    public InfinitySettingsModel(Preferences preferences)
    {
        new BooleanPreferenceBinding(myEnabled, preferences, Platform::runLater).open();
    }

    /**
     * Gets the enabled property.
     *
     * @return the enabled property
     */
    public BooleanProperty enabledProperty()
    {
        return myEnabled;
    }
}
