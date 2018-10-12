package io.opensphere.core.preferences;

import java.util.concurrent.Executor;

import org.apache.log4j.Logger;

import com.google.common.base.Objects;

import io.opensphere.core.util.Service;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.util.StringConverter;

/**
 * Handler that can update a string preference based on changes to an object
 * property and vice-versa.
 *
 * @param <T> The object type.
 */
public class ObjectPreferenceBinding<T> implements Service
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ObjectPreferenceBinding.class);

    /** The key. */
    private final String myKey;

    /** The prefs. */
    private final Preferences myPrefs;

    /** Listener for preferences changes. */
    private final PreferenceChangeListener myPrefsListener;

    /** The property. */
    private final ObjectProperty<T> myProperty;

    /** The property listener. */
    private final ChangeListener<? super T> myPropertyListener;

    /** The default. */
    private final T myDefault;

    /** The executor to use for updating the property. */
    private final Executor myExecutor;

    /** The string converter. */
    private final StringConverter<T> myConverter;

    /** If the property should only be set once, at initialization. */
    private final boolean mySetPropertyOnce;

    /**
     * Constructor.
     *
     * @param property The property.
     * @param prefs The preferences.
     * @param key The preferences key.
     * @param def The default value.
     * @param executor The executor to use for updating the property
     * @param converter The string converter
     */
    public ObjectPreferenceBinding(ObjectProperty<T> property, Preferences prefs, String key, T def, Executor executor,
            StringConverter<T> converter)
    {
        this(property, prefs, key, def, executor, converter, false);
    }

    /**
     * Constructor.
     *
     * @param property The property.
     * @param prefs The preferences.
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @param executor The executor to use for updating the property
     * @param converter The string converter
     * @param setPropertyOnce If the property should only be set at
     *            initialization
     */
    public ObjectPreferenceBinding(ObjectProperty<T> property, Preferences prefs, String key, T def, Executor executor,
            StringConverter<T> converter, boolean setPropertyOnce)
    {
        myProperty = property;
        myPrefs = prefs;
        myKey = key;
        myDefault = def;
        myExecutor = executor;
        myConverter = converter;
        myPrefsListener = evt ->
        {
            if (evt.getSource() != this)
            {
                String stringValue = evt.getValueAsString(null);
                myExecutor.execute(() -> setValue(stringValue));
            }
        };
        myPropertyListener = (v, o, value) -> myPrefs.putString(myKey, myConverter.toString(value), this);
        mySetPropertyOnce = setPropertyOnce;
    }

    @Override
    public void open()
    {
        if (!mySetPropertyOnce)
        {
            myPrefs.addPreferenceChangeListener(myKey, myPrefsListener);
        }
        myExecutor.execute(() ->
        {
            String stringValue = myPrefs.getString(myKey, null);
            setValue(stringValue);
            myProperty.addListener(myPropertyListener);
        });
    }

    @Override
    public void close()
    {
        if (!mySetPropertyOnce)
        {
            myPrefs.removePreferenceChangeListener(myKey, myPrefsListener);
        }
        myExecutor.execute(() ->
        {
            myProperty.removeListener(myPropertyListener);
        });
    }

    /**
     * Sets the value of the property.
     *
     * @param stringValue the value
     */
    private void setValue(String stringValue)
    {
        T value = myDefault;
        if (stringValue != null)
        {
            try
            {
                value = myConverter.fromString(stringValue);
            }
            catch (IllegalArgumentException e)
            {
                LOGGER.warn(e.getMessage());
            }
        }

        if (!Objects.equal(myProperty.get(), value))
        {
            myProperty.setValue(value);
        }
    }
}
