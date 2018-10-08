package io.opensphere.core.preferences;

import java.util.concurrent.Executor;

import io.opensphere.core.util.Service;
import javafx.beans.property.ObjectProperty;
import javafx.util.StringConverter;

/**
 * Handler that can update a string preference based on changes to an enum
 * property and vice-versa.
 *
 * @param <E> The enum type.
 */
public class EnumPreferenceBinding<E extends Enum<E>> implements Service
{
    /** The binding delegate. */
    private final ObjectPreferenceBinding<E> myBinding;

    /**
     * Constructor.
     *
     * @param type The type.
     * @param property The property.
     * @param prefs The preferences.
     * @param key The preferences key.
     * @param def The default value.
     * @param executor The executor to use for updating the property
     */
    public EnumPreferenceBinding(Class<E> type, ObjectProperty<E> property, Preferences prefs, String key, E def,
            Executor executor)
    {
        this(type, property, prefs, key, def, executor, false);
    }

    /**
     * Constructor.
     *
     * @param type The type.
     * @param property The property.
     * @param prefs The preferences.
     * @param key The preferences key.
     * @param def The default value.
     * @param executor The executor to use for updating the property
     * @param setPropertyOnce If the property should only be set at
     *            initialization
     */
    public EnumPreferenceBinding(Class<E> type, ObjectProperty<E> property, Preferences prefs, String key, E def,
            Executor executor, boolean setPropertyOnce)
    {
        StringConverter<E> converter = new StringConverter<>()
        {
            @Override
            public String toString(E object)
            {
                return object.name();
            }

            @Override
            public E fromString(String string)
            {
                return Enum.valueOf(type, string);
            }
        };
        myBinding = new ObjectPreferenceBinding<>(property, prefs, key, def, executor, converter, setPropertyOnce);
    }

    @Override
    public void open()
    {
        myBinding.open();
    }

    @Override
    public void close()
    {
        myBinding.close();
    }
}
