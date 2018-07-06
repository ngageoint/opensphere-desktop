package io.opensphere.core.quantify.settings;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.javafx.ConcurrentBooleanProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

/**
 * A simple model bound to the preferences object that keeps the runtime and
 * persisted states in synch.
 */
public class QuantifySettingsModel
{
    /** The preferences in which the state is persisted. */
    private final Preferences myPreferences;

    /** The property in which the remote receiver's URL is maintained. */
    private final StringProperty myUrlProperty = new ConcurrentStringProperty();

    /**
     * The property in which the enabled state of the quantify plugin is
     * maintained.
     */
    private final BooleanProperty myEnabledProperty = new ConcurrentBooleanProperty(true);

    /**
     * The property in which the capture-to-log state of the quantify plugin is
     * maintained.
     */
    private final BooleanProperty myCaptureToLogProperty = new ConcurrentBooleanProperty(true);

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

        myCaptureToLogProperty.set(myPreferences.getBoolean(QuantifyPreferenceKeys.CAPTURE_TO_LOG_KEY, true));
        myCaptureToLogProperty.addListener((obs, ov, nv) -> persist());

        myUrlProperty.set(myPreferences.getString(QuantifyPreferenceKeys.URL_KEY, null));
        myUrlProperty.addListener((obs, ov, nv) -> persist());
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

    /**
     * Gets the 'captureToLog' property.
     *
     * @return the 'captureToLog' property.
     */
    public BooleanProperty captureToLogProperty()
    {
        return myCaptureToLogProperty;
    }

    /**
     * Gets the 'url' property.
     *
     * @return the 'url' property.
     */
    public StringProperty urlProperty()
    {
        return myUrlProperty;
    }

    /** Persist the current state of the model to the preferences object. */
    private void persist()
    {
        myPreferences.putBoolean(QuantifyPreferenceKeys.ENABLED_KEY, myEnabledProperty.get(), this);
        myPreferences.putBoolean(QuantifyPreferenceKeys.CAPTURE_TO_LOG_KEY, myCaptureToLogProperty.get(), this);
        if (StringUtils.isNotBlank(myUrlProperty.get()))
        {
            myPreferences.putString(QuantifyPreferenceKeys.URL_KEY, myUrlProperty.get(), this);
        }
        else
        {
            myPreferences.remove(QuantifyPreferenceKeys.URL_KEY, this);
        }
    }
}
