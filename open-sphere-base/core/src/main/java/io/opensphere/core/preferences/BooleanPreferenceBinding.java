package io.opensphere.core.preferences;

import java.util.concurrent.Executor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;

import io.opensphere.core.util.Service;

/**
 * Handler that can update a boolean preference based on changes to a boolean
 * property and vice-versa.
 */
public class BooleanPreferenceBinding implements Service
{
    /** The property. */
    private final BooleanProperty myProperty;

    /** The key. */
    private final String myKey;

    /** The default value. */
    private final boolean myDefault;

    /** The prefs. */
    private final Preferences myPrefs;

    /** Listener for preferences changes. */
    private final PreferenceChangeListener myPrefsListener;

    /** The property listener. */
    private final ChangeListener<? super Boolean> myPropertyListener;

    /** The executor to use for updating the property. */
    private final Executor myExecutor;

    /**
     * Constructor.
     *
     * @param property The property.
     * @param prefs The preferences.
     * @param executor The executor to use for updating the property
     */
    public BooleanPreferenceBinding(BooleanProperty property, Preferences prefs, Executor executor)
    {
        this(property, property.getName(), property.get(), prefs, executor);
    }

    /**
     * Constructor.
     *
     * @param property The property.
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @param prefs The preferences.
     * @param executor The executor to use for updating the property
     */
    public BooleanPreferenceBinding(BooleanProperty property, String key, boolean def, Preferences prefs, Executor executor)
    {
        myProperty = property;
        myPrefs = prefs;
        myKey = key;
        myDefault = def;
        myExecutor = executor;
        myPrefsListener = evt -> myExecutor.execute(() -> myProperty.set(evt.getValueAsBoolean(myDefault)));
        myPropertyListener = (v, o, n) -> myPrefs.putBoolean(myKey, n.booleanValue(), null);
    }

    @Override
    public void open()
    {
        myPrefs.addPreferenceChangeListener(myKey, myPrefsListener);
        myExecutor.execute(() ->
        {
            myProperty.set(myPrefs.getBoolean(myKey, myDefault));
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
    public BooleanProperty getProperty()
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
