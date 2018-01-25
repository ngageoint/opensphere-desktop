package io.opensphere.core.preferences;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.w3c.dom.Element;

import io.opensphere.core.util.JAXBWrapper;
import io.opensphere.core.util.JAXBable;
import io.opensphere.core.util.Service;
import io.opensphere.core.util.SupplierX;

/**
 * Manages a set of preferences as key/value pairs. Each value may be any of the
 * following:
 * <ul>
 * <li>A string</li>
 * <li>A boolean</li>
 * <li>A double, float, int, or long</li>
 * <li>An XML element</li>
 * <li>A JAXB object</li>
 * <li>A list of strings</li>
 * <li>A set of strings</li>
 * <li>A map of strings to strings</li>
 * </ul>
 * This also provides change support that allows listeners to subscribe to
 * preference changes.
 */
public interface Preferences
{
    /** The preference key to use to listen to changes in all preferences. */
    String ALL_KEY = "ALL_KEY_";

    /**
     * Adds a byte[] element to an existing list.
     *
     * This is a convenience method so the user does not have to get the list,
     * check if it's null, make a new one if necessary, then insert their
     * object, and putList. Note: This allows duplicate list entries.
     *
     * @param key The key.
     * @param element The element to add.
     * @param source The originator of the change.
     */
    void addElementToList(String key, byte[] element, Object source);

    /**
     * Adds a string element to an existing list.
     *
     * This is a convenience method so the user does not have to get the list,
     * check if it's null, make a new one if necessary, then insert their
     * object, and putList. Note: This allows duplicate list entries.
     *
     * @param key The key.
     * @param element The element to add.
     * @param source The originator of the change.
     */
    void addElementToList(String key, String element, Object source);

    /**
     * Adds a string element to an existing set.
     *
     * This is a convenience method so the user does not have to get the set,
     * check if it's null, make a new one if necessary, then insert their
     * object, and putSet.
     *
     * @param key The key.
     * @param element The element to add.
     * @param source The originator of the change.
     * @return {@code true} iff the element was added.
     */
    boolean addElementToSet(String key, String element, Object source);

    /**
     * Adds a {@link PreferenceChangeListener} to the list to receive events
     * when the specified property changes. Holds a reference to listener as a
     * weak reference to allow garbage collection as needed.
     *
     * @param preference The preference which the listener is registering
     * @param lstr The listener to register.
     * @return {@code true} Iff registered.
     */
    boolean addPreferenceChangeListener(String preference, PreferenceChangeListener lstr);

    /**
     * Gets a boolean value from the preferences.
     *
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @return The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    boolean getBoolean(String key, boolean def);

    /**
     * Retrieves a list of byte arrays with the specified preference key.
     *
     * @param key The key.
     * @param def The default list to be returned if the key does not exist or
     *            the value is not a list of byte arrays.
     * @return The list, or the default.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    List<byte[]> getByteArrayList(String key, List<byte[]> def);

    /**
     * Gets a double value from the preferences.
     *
     * @param key The key.
     * @param def the default value to return if the key does not exist.
     * @return The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    double getDouble(String key, double def);

    /**
     * Gets a float value from the preferences.
     *
     * @param key The key.
     * @param def the default value to return if the key does not exist.
     * @return The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    float getFloat(String key, float def);

    /**
     * Gets a int value from the preferences.
     *
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @return The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    int getInt(String key, int def);

    /**
     * <p>
     * Get a JAXBable object value from the preferences.
     * </p>
     * <h3>Notes on object versioning:</h3><br>
     * It is recommended that JAXB classes that are stored as preferences have a
     * version identifier in their fully-qualified class name (i.e., in their
     * package name or class name).<br>
     * For example:<br>
     *
     * <pre>
     * {@code com.prefs.v1_0.DisplayPreferences.java}
     * </pre>
     *
     * When retrieving the object, {@code Object.class} may be passed to this
     * function, and then the version may be tested via {@code instanceof} and
     * migration can take place as necessary.<br>
     * For example:<br>
     *
     * <pre>
     *     Preferences prefs = ...
     *     JAXBable val = prefs.getJAXBableObject(JAXBable.class, "key", null);
     *     if (val instanceof com.prefs.v1_1.DisplayPreferences)
     *     {
     *         return (com.prefs.v1_1.DisplayPreferences)val;
     *     }
     *     else if (val instanceof com.prefs.v1_0.DisplayPreferences)
     *     {
     *         return com.prefs.v1_1.DisplayPreferences.migrateFrom((com.prefs.v1_0.DisplayPreferences)val);
     *     }
     *     else if (val != null)
     *     {
     *         LOGGER.warn("Unsupported version of DisplayPreferences: " + val.getClass());
     *     }
     * </pre>
     *
     * @param <S> The type of the wrapper object.
     * @param <T> The expected type of object.
     * @param type The expected type of object.
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @return The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    <S extends JAXBWrapper<T>, T extends JAXBable<S>> T getJAXBableObject(Class<T> type, String key, T def);

    /**
     * <p>
     * Get a JAXB object value from the preferences.
     * </p>
     * <h3>Notes on object versioning:</h3><br>
     * It is recommended that JAXB classes that are stored as preferences have a
     * version identifier in their fully-qualified class name (i.e., in their
     * package name or class name).<br>
     * For example:<br>
     *
     * <pre>
     * {@code com.prefs.v1_0.DisplayPreferences.java}
     * </pre>
     *
     * When retrieving the object, {@code Object.class} may be passed to this
     * function, and then the version may be tested via {@code instanceof} and
     * migration can take place as necessary.<br>
     * For example:<br>
     *
     * <pre>
     *     Preferences prefs = ...
     *     Object val = prefs.getJAXBObject(Object.class, "key", null);
     *     if (val instanceof com.prefs.v1_1.DisplayPreferences)
     *     {
     *         return (com.prefs.v1_1.DisplayPreferences)val;
     *     }
     *     else if (val instanceof com.prefs.v1_0.DisplayPreferences)
     *     {
     *         return com.prefs.v1_1.DisplayPreferences.migrateFrom((com.prefs.v1_0.DisplayPreferences)val);
     *     }
     *     else if (val != null)
     *     {
     *         LOGGER.warn("Unsupported version of DisplayPreferences: " + val.getClass());
     *     }
     * </pre>
     *
     * @param <T> The expected type of object.
     * @param type The expected type of object.
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @return The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    <T> T getJAXBObject(Class<T> type, String key, T def);

    /**
     * Similar to {@link #getJAXBObject(Class, String, Object)} but allows
     * customization of the JAXB context.
     *
     * @param <T> The expected type of object.
     * @param type The expected type of object.
     * @param key The key.
     * @param contextSupplier Supplier of the JAXB context to be used for
     *            unmarshalling.
     * @param def The default value to return if the key does not exist.
     * @return The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    <T> T getJAXBObject(Class<T> type, String key, SupplierX<JAXBContext, JAXBException> contextSupplier, T def);

    /**
     * Gets a long value from the preferences.
     *
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @return The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    long getLong(String key, long def);

    /**
     * Gets a string value from the preferences.
     *
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @return The value.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    String getString(String key, String def);

    /**
     * Retrieves a list of strings with the specified preference key.
     *
     * @param key The key.
     * @param def The default list to be returned if the key does not exist.
     * @return the list, or the default if there were no values or no property
     *         was found that matched key
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    List<String> getStringList(String key, List<String> def);

    /**
     * Retrieves a map of strings to strings.
     *
     * @param key The key.
     * @param def The default map to be returned if the key does not exist.
     * @return The map, or the default if there were no values or no property
     *         was found that matched key.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Map<String, String> getStringMap(String key, Map<String, String> def);

    /**
     * Retrieves a set of strings.
     *
     * @param key The key.
     * @param def The default set to be returned if the key does not exist.
     * @return The set, or the default if there were no values or no property
     *         was found that matched key.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Set<String> getStringSet(String key, Set<String> def);

    /**
     * Gets the topic for this preferences set. The topic may be used as a
     * namespace to avoid key naming conflicts.
     *
     * @return The topic.
     */
    String getTopic();

    /**
     * Get a DOM element value from the preferences.
     *
     * @param key The key.
     * @param def The default value to return if the key does not exist.
     * @return The value.
     */
    Element getXMLElement(String key, Element def);

    /**
     * Get the preference keys.
     *
     * @return The keys.
     */
    Collection<String> keys();

    /**
     * Gets the keys in the preferences store that have the provided prefix.
     *
     * @param prefix The prefix of the keys to retrieve from the store.
     * @return The keys with the provided prefix.
     */
    Collection<String> keysWithPrefix(String prefix);

    /**
     * Gets the keys in the preferences store that use the provided suffix.
     *
     * @param suffix The prefix of the keys to retrieve from the store.
     * @return The list of keys with the provided suffix.
     */
    Collection<String> keysWithSuffix(String suffix);

    /**
     * Log the current Preferences settings.
     */
    void printPrefs();

    /**
     * Put a boolean into the preferences.
     *
     * @param key The key.
     * @param value The value.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Boolean putBoolean(String key, boolean value, Object source);

    /**
     * Put a byte array into the preferences.
     *
     * @param key The key.
     * @param value The value.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    byte[] putByteArray(String key, byte[] value, Object source);

    /**
     * Saves a list of byte arrays.
     *
     * @param key The key.
     * @param list The list of byte arrays.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    List<byte[]> putByteArrayList(String key, List<byte[]> list, Object source);

    /**
     * Put a double into the preferences.
     *
     * @param key The key.
     * @param value The value.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Double putDouble(String key, double value, Object source);

    /**
     * Put a float into the preferences.
     *
     * @param key The key.
     * @param value The value.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Float putFloat(String key, float value, Object source);

    /**
     * Put an int into the preferences.
     *
     * @param key The key.
     * @param value The value.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If key is null.
     */
    Integer putInt(String key, int value, Object source);

    /**
     * Put a {@link JAXBable} object into the preferences.
     * <p>
     * WARNING: Passing immutable or cloned objects is recommended. The object
     * may be marshalled on a separate thread, and if the object is changed
     * while it is being marshalled, a {@link ConcurrentModificationException}
     * could occur.
     * <p>
     * If {@code compareToOld} is {@code true} and there is an old value for
     * this preference, the old value will be compared to the new value using
     * {@link Object#equals(Object)}. If it is determined that the objects are
     * equal, the operation will have no effect. In this way, unnecessary saves
     * and listener notifications may be avoided. For this to work properly, the
     * objects must be immutable or they must properly implement
     * {@link Object#equals(Object)}. Otherwise, updates to the object will not
     * be saved.
     *
     * @param <S> The type of the wrapped object.
     * @param <T> The type of the wrapper object.
     * @param key The key.
     * @param value The value.
     * @param compareToOld Indicates if the new value should be compared to the
     *            old value to eliminate duplicate updates. If the value is a
     *            mutable object, this must be {@code false}.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    <S extends JAXBable<T>, T extends JAXBWrapper<S>> Object putJAXBableObject(String key, S value, boolean compareToOld,
            Object source);

    /**
     * Put a JAXB object into the preferences using the type of the JAXB object
     * for the JAXB context.
     * <p>
     * WARNING: Passing immutable or cloned objects is recommended. The object
     * may be marshalled on a separate thread, and if the object is changed
     * while it is being marshalled, a {@link ConcurrentModificationException}
     * could occur.
     * <p>
     * If {@code compareToOld} is {@code true} and there is an old value for
     * this preference, the old value will be compared to the new value using
     * {@link Object#equals(Object)}. If it is determined that the objects are
     * equal, the operation will have no effect. In this way, unnecessary saves
     * and listener notifications may be avoided. For this to work properly, the
     * objects must be immutable or they must properly implement
     * {@link Object#equals(Object)}. Otherwise, updates to the object will not
     * be saved.
     *
     * @param key The key.
     * @param value The value.
     * @param compareToOld Indicates if the new value should be compared to the
     *            old value to eliminate duplicate updates. If the value is a
     *            mutable object that does not override
     *            {@link Object#equals(Object)}, this must be {@code false}.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    Object putJAXBObject(String key, Object value, boolean compareToOld, Object source);

    /**
     * Similar to {@link #putJAXBObject(String, Object, boolean, Object)} but
     * allows customization of the JAXB context.
     *
     * @param key The key.
     * @param value The value.
     * @param compareToOld Indicates if the new value should be compared to the
     *            old value to eliminate duplicate updates. If the value is a
     *            mutable object that does not override
     *            {@link Object#equals(Object)}, this must be {@code false}.
     * @param contextSupplier Supplier of the JAXB context to be used for
     *            marshalling.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    Object putJAXBObject(String key, Object value, boolean compareToOld, SupplierX<JAXBContext, JAXBException> contextSupplier,
            Object source);

    /**
     * Puts a long into the preferences.
     *
     * @param key The key.
     * @param value The value.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Long putLong(String key, long value, Object source);

    /**
     * Put a string into the preferences.
     *
     * @param key The key.
     * @param value The value.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    String putString(String key, String value, Object source);

    /**
     * Saves a list of strings.
     *
     * @param key The key.
     * @param list The list of strings.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    List<String> putStringList(String key, List<String> list, Object source);

    /**
     * Saves a map of strings to strings.
     *
     * @param key The property key to store in.
     * @param map The map.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    Map<String, String> putStringMap(String key, Map<String, String> map, Object source);

    /**
     * Saves a set of strings.
     *
     * @param key The key.
     * @param set the set of strings to store.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    Set<String> putStringSet(String key, Set<String> set, Object source);

    /**
     * Put a DOM element into the preferences.
     *
     * @param key The key.
     * @param value The value.
     * @param compareToOld Indicates if the new value should be compared to the
     *            old value to eliminate duplicate updates.
     * @param source The originator of the change.
     * @return The previous value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key or value is {@code null}.
     */
    Element putXMLElement(String key, Element value, boolean compareToOld, Object source);

    /**
     * Removes a preference from the preferences store.
     *
     * @param key The key to remove.
     * @param source The originator of the change.
     * @return {@code true} if the key was removed.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    boolean remove(String key, Object source);

    /**
     * Removes a boolean from the preferences.
     *
     * @param key The key.
     * @param source The originator of the change.
     * @return The removed boolean, or {@code null} if one was not found.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Boolean removeBoolean(String key, Object source);

    /**
     * Remove a double from the preferences.
     *
     * @param key The key.
     * @param source The originator of the change.
     * @return The removed value, or {@code null} if one was not found.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Double removeDouble(String key, Object source);

    /**
     * Remove the first occurrence of an element from an existing list if it is
     * present. If the set 'key' does not exist, this call has no effect.
     *
     * @param key The set to remove from.
     * @param element The element to remove.
     * @param source The originator of the change.
     * @return True if removed, false if not in the list.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    boolean removeElementFromList(String key, String element, Object source);

    /**
     * Remove an element from an existing set. If the set 'key' does not exist,
     * this call has no effect.
     *
     * @param key the set to remove from.
     * @param element The element to remove.
     * @param source The originator of the change.
     * @return True if removed, false if not in set.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    boolean removeElementFromSet(String key, String element, Object source);

    /**
     * Remove a float from the preferences.
     *
     * @param key The key.
     * @param source The originator of the change.
     * @return The removed value, or {@code null} if one was not found.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Float removeFloat(String key, Object source);

    /**
     * Remove an int from the preferences.
     *
     * @param key The key.
     * @param source The originator of the change.
     * @return The removed int, or {@code null} if one was not found.
     * @throws IllegalArgumentException If key is null.
     */
    Integer removeInt(String key, Object source);

    /**
     * Remove a JAXB object from the preferences.
     *
     * @param key The key.
     * @param source The originator of the change.
     * @return The value, or {@code null} if one was not found.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Object removeJAXBObject(String key, Object source);

    /**
     * Remove a list from the preferences.
     *
     * @param key The preferences key.
     * @param source The originator of the change.
     * @return The removed list, or {@code null} if one was not found.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    List<String> removeList(String key, Object source);

    /**
     * Remove a long from the preferences.
     *
     * @param key The key.
     * @param source The originator of the change.
     * @return The removed long, or {@code null} if one was not found.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Long removeLong(String key, Object source);

    /**
     * Remove a map from the preferences.
     *
     * @param key The property key to store in.
     * @param source The originator of the change.
     * @return The removed map, or {@code null} if one was not found.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Map<String, String> removeMap(String key, Object source);

    /**
     * Removes all registrations for the provided listener.
     *
     * @param lstr The listener to remove.
     * @throws IllegalArgumentException If the listener is {@code null}.
     */
    void removePreferenceChangeListener(PreferenceChangeListener lstr);

    /**
     * Removes a property change listener from the registered preference.
     *
     * @param preference The preference key to which the listener is subscribed
     *            for changes.
     * @param lstr The listener to unsubscribe.
     * @return True if removed, false if not or not in list.
     */
    boolean removePreferenceChangeListener(String preference, PreferenceChangeListener lstr);

    /**
     * Remove a set from the preferences.
     *
     * @param key The key.
     * @param source The originator of the change.
     * @return The removed set, or {@code null} if one was not found.
     */
    Set<String> removeSet(String key, Object source);

    /**
     * Remove a string from the preferences.
     *
     * @param key The key.
     * @param source The originator of the change.
     * @return The value, or {@code null} if one was not found.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    String removeString(String key, Object source);

    /**
     * Remove a DOM element from the preferences.
     *
     * @param key The key.
     * @param source The originator of the change.
     * @return The element, or {@code null} if one was not found.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    Element removeXMLElement(String key, Object source);

    /**
     * Request that the preferences be persisted if necessary, and block until
     * the persist is complete.
     */
    void waitForPersist();

    /**
     * Creates a service that can be used to add/remove the given listener.
     *
     * @param preference the preference
     * @param listener the listener
     * @return the service
     */
    default Service getListenerService(String preference, PreferenceChangeListener listener)
    {
        return new Service()
        {
            @Override
            public void open()
            {
                addPreferenceChangeListener(preference, listener);
            }

            @Override
            public void close()
            {
                removePreferenceChangeListener(preference, listener);
            }
        };
    }
}
