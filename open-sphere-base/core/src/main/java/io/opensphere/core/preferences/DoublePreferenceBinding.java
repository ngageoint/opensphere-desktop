package io.opensphere.core.preferences;

import java.util.concurrent.Executor;

import io.opensphere.core.util.Service;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;

/**
 * Handler that can update a double preference based on changes to a double
 * property and vice-versa.
 */
public class DoublePreferenceBinding implements Service
{
    /** The property. */
    private final DoubleProperty myProperty;

    /** The key. */
    private final String myKey;

    /** The default value. */
    private final double myDefault;

    /** The prefs. */
    private final Preferences myPrefs;

    /** Listener for preferences changes. */
    private final PreferenceChangeListener myPrefsListener;

    /** The property listener. */
    private final ChangeListener<? super Number> myPropertyListener;

    /** The executor to use for updating the property. */
    private final Executor myExecutor;

    /**
     * Constructor.
     *
     * @param property The property.
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @param prefs The preferences.
     * @param executor The executor to use for updating the property
     */
    public DoublePreferenceBinding(DoubleProperty property, String key, double def, Preferences prefs, Executor executor)
    {
        myProperty = property;
        myPrefs = prefs;
        myKey = key;
        myDefault = def;
        myExecutor = executor;
        myPrefsListener = evt -> myExecutor.execute(() -> myProperty.set(evt.getValueAsDouble(myDefault)));
        myPropertyListener = (v, o, n) -> myPrefs.putDouble(myKey, n.doubleValue(), null);
    }

    @Override
    public void open()
    {
        myPrefs.addPreferenceChangeListener(myKey, myPrefsListener);
        myExecutor.execute(() ->
        {
            myProperty.set(myPrefs.getDouble(myKey, myDefault));
            myProperty.addListener(myPropertyListener);
        });
    }

    @Override
    public void close()
    {
        myPrefs.removePreferenceChangeListener(myKey, myPrefsListener);
        myExecutor.execute(() -> myProperty.removeListener(myPropertyListener));
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public DoubleProperty getProperty()
    {
        return myProperty;
    }

    /**
     * Restores the default value in the preferences and property.
     */
    public void restoreDefault()
    {
        myPrefs.remove(myKey, null);
    }
}
