package io.opensphere.infinity.model;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;

import io.opensphere.core.preferences.BooleanPreferenceBinding;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.CompositeService;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;

/** Infinity settings model. */
public class InfinitySettingsModel extends CompositeService
{
    /** The enabled setting. */
    private final BooleanProperty myEnabled = new ConcurrentBooleanProperty(this, "enabled", true);

    /**
     * Constructor.
     *
     * @param preferences the preferences
     */
    public InfinitySettingsModel(Preferences preferences)
    {
        addService(new BooleanPreferenceBinding(myEnabled, preferences, Platform::runLater));
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
