package io.opensphere.core.preferences;

import java.util.concurrent.Executor;

import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;

import io.opensphere.core.util.Service;

/**
 * Handler that can update a integer preference based on changes to a integer
 * property and vice-versa.
 */
public class IntegerPreferenceBinding implements Service
{
    /** The property. */
    private final IntegerProperty myProperty;

    /** The key. */
    private final String myKey;

    /** The default value. */
    private final int myDefault;

    /** The prefs. */
    private final Preferences myPrefs;

    /** Listener for preferences changes. */
    private final PreferenceChangeListener myPrefsListener;

    /** The property listener. */
    private final ChangeListener<? super Number> myPropertyListener;

    /**
     * Constructor.
     *
     * @param property The property.
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @param prefs The preferences.
     * @param executor The executor to use for updating the property
     */
    public IntegerPreferenceBinding(IntegerProperty property, String key, int def, Preferences prefs, Executor executor)
    {
        myProperty = property;
        myPrefs = prefs;
        myKey = key;
        myDefault = def;
        myPrefsListener = evt -> executor.execute(() -> myProperty.set(evt.getValueAsInt(myDefault)));
        myPropertyListener = (v, o, n) -> myPrefs.putInt(myKey, n.intValue(), null);
        myProperty.set(myPrefs.getInt(myKey, myDefault));
    }

    @Override
    public void open()
    {
        myProperty.addListener(myPropertyListener);
        myPrefs.addPreferenceChangeListener(myKey, myPrefsListener);
    }

    @Override
    public void close()
    {
        myProperty.removeListener(myPropertyListener);
        myPrefs.removePreferenceChangeListener(myKey, myPrefsListener);
    }

    /**
     * Gets the property.
     *
     * @return the property
     */
    public IntegerProperty getProperty()
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
