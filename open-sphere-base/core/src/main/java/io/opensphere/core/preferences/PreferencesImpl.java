package io.opensphere.core.preferences;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.JAXBWrapper;
import io.opensphere.core.util.JAXBable;
import io.opensphere.core.util.SupplierX;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.LazyMap;
import io.opensphere.core.util.collections.LazyMap.Factory;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ToStringHelper;
import io.opensphere.core.util.security.CipherFactory;
import io.opensphere.core.util.xml.ByteArrayList;
import io.opensphere.core.util.xml.StringList;
import io.opensphere.core.util.xml.StringMap;
import io.opensphere.core.util.xml.StringSet;

/**
 * A class that manages preferences as key/value pairs. It can load from and
 * persist to a backing file. It registers listeners for specific preference
 * change events and notifies listeners when changes occur.
 */
@XmlRootElement(name = "preferences")
@XmlAccessorType(XmlAccessType.NONE)
@SuppressWarnings("PMD.GodClass")
public class PreferencesImpl implements InternalPreferencesIF
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(PreferencesImpl.class);

    /** A map of preferences to change support objects. */
    private final Map<String, ChangeSupport<PreferenceChangeListener>> myChangeSupportMap;

    /**
     * Optional cipher factory to use for encryption.
     */
    private CipherFactory myCipherFactory;

    /**
     * Flag indicating if these preferences should be compressed on-disk.
     */
    private boolean myCompressed;

    /**
     * Executor for sending change events.
     */
    private Executor myEventExecutor;

    /** The preferences persistence manager. */
    private PreferencesPersistenceManager myPersistenceManager;

    /**
     * Executor for persisting preferences.
     */
    private ProcrastinatingExecutor myPersistExecutor;

    /** Queue of latches to be counted down when a persist executes. */
    private final BlockingQueue<CountDownLatch> myPersistLatches = new LinkedBlockingQueue<>();

    /**
     * The key/value set for the preferences.
     */
    private final Map<String, Preference<?>> myPreferencesMap = new ConcurrentHashMap<>();

    /**
     * Flag indicating that an error has already been reported, so don't keep
     * reporting it.
     */
    private boolean mySuppressWarnings;

    /**
     * The topic for this preferences set.
     */
    @XmlAttribute(name = "topic")
    private String myTopic;

    /**
     * Constructor.
     *
     * @param topic The topic of this preferences set.
     */
    public PreferencesImpl(String topic)
    {
        this();
        Utilities.checkNull(topic, "topic");

        myTopic = topic;
    }

    /**
     * Default constructor for JAXB.
     */
    protected PreferencesImpl()
    {
        Factory<String, ChangeSupport<PreferenceChangeListener>> changeSupportFactory = key -> new WeakChangeSupport<>();
        myChangeSupportMap = new LazyMap<>(
                new ConcurrentHashMap<String, ChangeSupport<PreferenceChangeListener>>(), String.class, changeSupportFactory);
    }

    @Override
    public synchronized void addElementToList(String key, byte[] element, Object source)
    {
        List<byte[]> oldList = getByteArrayList(key, null);
        List<byte[]> newList = oldList == null ? New.<byte[]>list() : New.list(oldList);
        newList.add(element);
        putByteArrayList(key, newList, source);
    }

    @Override
    public synchronized void addElementToList(String key, String element, Object source)
    {
        List<String> oldList = getStringList(key, null);
        List<String> newList = oldList == null ? New.<String>list() : New.list(oldList);
        newList.add(element);
        putStringList(key, newList, source);
    }

    @Override
    public synchronized boolean addElementToSet(String key, String element, Object source)
    {
        Set<String> set = getStringSet(key, null);
        Set<String> newSet = set == null ? New.<String>set() : New.set(set);
        boolean result = newSet.add(element);
        putStringSet(key, newSet, source);
        return result;
    }

    @Override
    public boolean addPreferenceChangeListener(String preference, PreferenceChangeListener lstr)
    {
        if (StringUtils.isEmpty(preference) || lstr == null)
        {
            return false;
        }

        // Synchronize to ensure the change support does not get removed before
        // the listener gets added.
        synchronized (myChangeSupportMap)
        {
            myChangeSupportMap.get(preference).addListener(lstr);
        }

        return true;
    }

    @Override
    public boolean getBoolean(String key, boolean def)
    {
        Preference<?> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        Boolean value = pref.getBooleanValue(Boolean.valueOf(def));
        return value == null ? def : value.booleanValue();
    }

    @Override
    public List<byte[]> getByteArrayList(String key, List<byte[]> def)
    {
        Preference<?> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        List<byte[]> value = pref.getByteArrayListValue(def);
        return value == null ? def : value;
    }

    @Override
    public double getDouble(String key, double def)
    {
        Preference<?> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        Double value = pref.getDoubleValue(Double.valueOf(def));
        return value == null ? def : value.doubleValue();
    }

    @Override
    public float getFloat(String key, float def)
    {
        Preference<?> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        Float value = pref.getFloatValue(Float.valueOf(def));
        return value == null ? def : value.floatValue();
    }

    @Override
    public int getInt(String key, int def)
    {
        Preference<?> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        Integer value = pref.getIntegerValue(Integer.valueOf(def));
        return value == null ? def : value.intValue();
    }

    @Override
    public <S extends JAXBWrapper<T>, T extends JAXBable<S>> T getJAXBableObject(Class<T> type, String key, T def)

    {
        Preference<? extends Object> pref = getPreference(key);
        if (pref != null)
        {
            if (pref.getValueType() != null && JAXBWrapper.class.isAssignableFrom(pref.getValueType()))
            {
                @SuppressWarnings("unchecked")
                Preference<JAXBWrapper<?>> castPref = (Preference<JAXBWrapper<?>>)pref;

                JAXBWrapper<?> value = castPref.getValue((JAXBWrapper<?>)null);
                Object result = value == null ? null : value.getWrappedObject();
                if (result != null && type.isInstance(result))
                {
                    @SuppressWarnings("unchecked")
                    T castResult = (T)result;
                    return castResult;
                }
                LOGGER.warn("Could not cast wrapped preference value [" + result + "] to [" + type + "]");
            }
            else
            {
                LOGGER.warn("Could not cast preference value from [" + pref.getValueType() + "] to [" + JAXBWrapper.class + "]");
            }
        }
        return def;
    }

    @Override
    public <T> T getJAXBObject(Class<T> type, String key, SupplierX<JAXBContext, JAXBException> contextSupplier, T def)

    {
        Preference<? extends Object> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        if (pref.getValueType() != null && type.isAssignableFrom(pref.getValueType()))
        {
            @SuppressWarnings("unchecked")
            Preference<T> castPref = (Preference<T>)pref;

            T value = castPref.getValue(def, contextSupplier);

            return value == null ? def : value;
        }
        LOGGER.warn("Could not cast preference value for key [" + key + "] from [" + pref.getValueType() + "] to [" + type + "]");
        return def;
    }

    @Override
    public <T> T getJAXBObject(Class<T> type, String key, T def)
    {
        return getJAXBObject(type, key, (SupplierX<JAXBContext, JAXBException>)null, def);
    }

    @Override
    public long getLong(String key, long def)
    {
        Preference<?> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        Long value = pref.getLongValue(Long.valueOf(def));
        return value == null ? def : value.longValue();
    }

    @Override
    public String getString(String key, String def)
    {
        Preference<?> pref = getPreference(key);
        return pref == null ? def : pref.getStringValue(def);
    }

    @Override
    public List<String> getStringList(String key, List<String> def)
    {
        Preference<?> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        List<String> value = pref.getStringListValue(def);
        return value == null ? def : value;
    }

    @Override
    public Map<String, String> getStringMap(String key, Map<String, String> def)
    {
        Preference<?> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        Map<String, String> value = pref.getStringMapValue(def);
        return value == null ? def : value;
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> def)
    {
        Preference<?> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        Set<String> value = pref.getStringSetValue(def);
        return value == null ? def : value;
    }

    @Override
    public String getTopic()
    {
        return myTopic;
    }

    @Override
    public Element getXMLElement(String key, Element def)
    {
        Preference<? extends Object> pref = getPreference(key);
        if (pref == null)
        {
            return def;
        }
        if (Element.class.isAssignableFrom(pref.getValueType()))
        {
            @SuppressWarnings("unchecked")
            Preference<Element> castPref = (Preference<Element>)pref;

            Element value = castPref.getValue(def);

            return value == null ? def : value;
        }
        LOGGER.warn("Could not cast preference value from [" + pref.getValueType() + "] to [" + Element.class + "]");
        return def;
    }

    @Override
    public Collection<String> keys()
    {
        return myPreferencesMap.keySet();
    }

    @Override
    public Collection<String> keysWithPrefix(String prefix)
    {
        Collection<String> result = New.list();
        for (String key : keys())
        {
            if (key.startsWith(prefix))
            {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public Collection<String> keysWithSuffix(String suffix)
    {
        Collection<String> result = New.list();
        for (String key : keys())
        {
            if (key.endsWith(suffix))
            {
                result.add(key);
            }
        }
        return result;
    }

    @Override
    public void merge(Preferences other)
    {
        if (other instanceof PreferencesImpl)
        {
            for (Preference<?> pref : ((PreferencesImpl)other).myPreferencesMap.values())
            {
                if (!myPreferencesMap.containsKey(pref.getKey()))
                {
                    myPreferencesMap.put(pref.getKey(), pref);
                }
            }
        }
    }

    @Override
    public void printPrefs()
    {
        ToStringHelper helper = new ToStringHelper((Class<?>)null, 0);
        helper.add("======= Preferences for " + getTopic() + " =======");
        Collection<Entry<String, Preference<?>>> entries = CollectionUtilities.sort(myPreferencesMap.entrySet(),
                (o1, o2) -> o1.getKey().compareTo(o2.getKey()));
        for (Map.Entry<String, Preference<?>> entry : entries)
        {
            helper.add("[" + entry.getKey() + "]", "[" + entry.getValue() + "]");
        }
        LOGGER.info(helper.toStringPreferenceDump());
    }

    @Override
    public Boolean putBoolean(String key, boolean value, Object source)
    {
        Preference<?> oldPref = putPreference(new NonJAXBObjectPreference<>(key, Boolean.valueOf(value)), true, source);
        return oldPref == null ? null : oldPref.getBooleanValue(null);
    }

    @Override
    public byte[] putByteArray(String key, byte[] value, Object source)
    {
        Preference<?> oldPref = putPreference(new NonJAXBObjectPreference<>(key, value), true, source);
        return oldPref == null ? null : oldPref.getByteArrayValue(null);
    }

    @Override
    public List<byte[]> putByteArrayList(String key, List<byte[]> list, Object source)
    {
        Preference<?> oldPref = putPreference(new JAXBObjectPreference<>(key, new ByteArrayList(list)), true,
                source);
        return oldPref == null ? null : oldPref.getByteArrayListValue(null);
    }

    @Override
    public Double putDouble(String key, double value, Object source) throws NullPointerException
    {
        Preference<?> oldPref = putPreference(new NonJAXBObjectPreference<>(key, Double.valueOf(value)), true, source);
        return oldPref == null ? null : oldPref.getDoubleValue(null);
    }

    @Override
    public Float putFloat(String key, float value, Object source) throws NullPointerException
    {
        Preference<?> oldPref = putPreference(new NonJAXBObjectPreference<>(key, Float.valueOf(value)), true, source);
        return oldPref == null ? null : oldPref.getFloatValue(null);
    }

    @Override
    public Integer putInt(String key, int value, Object source) throws NullPointerException
    {
        Preference<?> oldPref = putPreference(new NonJAXBObjectPreference<>(key, Integer.valueOf(value)), true, source);
        return oldPref == null ? null : oldPref.getIntegerValue(null);
    }

    @Override
    public <S extends JAXBable<T>, T extends JAXBWrapper<S>> Object putJAXBableObject(String key, S value, boolean compareToOld,
            Object source)
    {
        Preference<?> oldPref = putPreference(new JAXBObjectPreference<Object>(key, value.getWrapper()), compareToOld, source);
        Object oldValue = oldPref == null ? null : oldPref.getValue(null, null);
        return oldValue instanceof JAXBWrapper ? ((JAXBWrapper<?>)oldValue).getWrappedObject() : null;
    }

    @Override
    public Object putJAXBObject(String key, Object value, boolean compareToOld,
            SupplierX<JAXBContext, JAXBException> contextSupplier, Object source)
    {
        Preference<?> oldPref = putPreference(new JAXBObjectPreference<>(key, contextSupplier, value), compareToOld,
                source);
        return oldPref == null ? null : oldPref.getValue(null, contextSupplier);
    }

    @Override
    public Object putJAXBObject(String key, Object value, boolean compareToOld, Object source)
    {
        Preference<?> oldPref = putPreference(new JAXBObjectPreference<>(key, value), compareToOld, source);
        return oldPref == null ? null : oldPref.getValue(null, null);
    }

    @Override
    public Long putLong(String key, long value, Object source) throws NullPointerException
    {
        Preference<?> oldPref = putPreference(new NonJAXBObjectPreference<>(key, Long.valueOf(value)), true, source);
        return oldPref == null ? null : oldPref.getLongValue(null);
    }

    @Override
    public String putString(String key, String value, Object source)
    {
        Preference<?> oldPref = putPreference(new NonJAXBObjectPreference<>(key, value), true, source);
        return oldPref == null ? null : oldPref.getStringValue(null);
    }

    @Override
    public List<String> putStringList(String key, List<String> list, Object source)
    {
        Preference<?> oldPref = putPreference(new JAXBObjectPreference<>(key, new StringList(list)), true, source);
        return oldPref == null ? null : oldPref.getStringListValue(null);
    }

    @Override
    public Map<String, String> putStringMap(String key, Map<String, String> map, Object source)
    {
        Preference<?> oldPref = putPreference(new JAXBObjectPreference<>(key, new StringMap(map)), true, source);
        return oldPref == null ? null : oldPref.getStringMapValue(null);
    }

    @Override
    public Set<String> putStringSet(String key, Set<String> set, Object source)
    {
        Preference<?> oldPref = putPreference(new JAXBObjectPreference<>(key, new StringSet(set)), true, source);
        return oldPref == null ? null : oldPref.getStringSetValue(null);
    }

    @Override
    public Element putXMLElement(String key, Element value, boolean compareToOld, Object source)
    {
        Preference<?> oldPref = putPreference(new JAXBObjectPreference<Object>(key, value), compareToOld, source);
        return oldPref == null ? null : oldPref.getDOMElementValue(null);
    }

    @Override
    public boolean remove(String key, Object source)
    {
        return removeKey(key, source) != null;
    }

    @Override
    public Boolean removeBoolean(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getBooleanValue(null);
    }

    @Override
    public Double removeDouble(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getDoubleValue(null);
    }

    @Override
    public boolean removeElementFromList(String key, String element, Object source)
    {
        List<String> aList = getStringList(key, null);
        if (aList != null && aList.remove(element))
        {
            putStringList(key, aList, source);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeElementFromSet(String key, String element, Object source)
    {
        Set<String> aSet = New.set(getStringSet(key, New.set()));
        if (aSet.remove(element))
        {
            putStringSet(key, aSet, source);
            return true;
        }
        return false;
    }

    @Override
    public Float removeFloat(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getFloatValue(null);
    }

    @Override
    public Integer removeInt(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getIntegerValue(null);
    }

    @Override
    public Object removeJAXBObject(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getValue(null, null);
    }

    @Override
    public List<String> removeList(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getStringListValue(null);
    }

    @Override
    public Long removeLong(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getLongValue(null);
    }

    @Override
    public Map<String, String> removeMap(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getStringMapValue(null);
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener lstr)
    {
        Utilities.checkNull(lstr, "lstr");

        for (Map.Entry<String, ChangeSupport<PreferenceChangeListener>> entry : myChangeSupportMap.entrySet())
        {
            ChangeSupport<PreferenceChangeListener> changeSupport = entry.getValue();
            if (changeSupport.removeListener(lstr))
            {
                synchronized (myChangeSupportMap)
                {
                    if (changeSupport.isEmpty())
                    {
                        myChangeSupportMap.remove(entry.getKey());
                    }
                }
            }
        }
    }

    @Override
    public boolean removePreferenceChangeListener(String preference, PreferenceChangeListener lstr)
    {
        if (StringUtils.isEmpty(preference) || lstr == null)
        {
            return false;
        }

        boolean removed = false;
        if (myChangeSupportMap.containsKey(preference))
        {
            ChangeSupport<PreferenceChangeListener> changeSupport = myChangeSupportMap.get(preference);
            if (changeSupport != null)
            {
                removed = changeSupport.removeListener(lstr);
                if (removed)
                {
                    // Synchronize to ensure a listener is not added between the
                    // isEmpty call and the remove call.
                    synchronized (myChangeSupportMap)
                    {
                        if (changeSupport.isEmpty())
                        {
                            myChangeSupportMap.remove(preference);
                        }
                    }
                }
            }
        }

        return removed;
    }

    @Override
    public Set<String> removeSet(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getStringSetValue(null);
    }

    @Override
    public String removeString(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getStringValue(null);
    }

    @Override
    public Element removeXMLElement(String key, Object source)
    {
        Preference<?> pref = removeKey(key, source);
        return pref == null ? null : pref.getDOMElementValue(null);
    }

    @Override
    public void replacePreferences(InternalPreferencesIF prefs, boolean persist, Object source)
    {
        Collection<PreferenceChangeEvent> events = New.<PreferenceChangeEvent>list();

        Set<String> oldKeys = New.set(myPreferencesMap.keySet());
        if (prefs != null)
        {
            for (Preference<?> entry : prefs.values())
            {
                if (entry.validatePreference(getTopic()))
                {
                    // Remove the keys that exist in the srcMap from the oldKeys
                    // set.
                    oldKeys.remove(entry.getKey());

                    Preference<?> oldValue = myPreferencesMap.put(entry.getKey(), entry);
                    if (!EqualsHelper.equals(oldValue, entry))
                    {
                        events.add(new PreferenceChangeEvent(myTopic, entry, source));
                    }
                }
            }
        }

        // Now remove all the properties that aren't set in the new
        // properties.
        for (String key : oldKeys)
        {
            events.add(new PreferenceChangeEvent(myTopic, new NonJAXBObjectPreference<>(key, null), source));
            myPreferencesMap.remove(key);
        }

        for (PreferenceChangeEvent event : events)
        {
            firePreferenceChanged(event);
        }

        if (persist)
        {
            schedulePersist();
        }
    }

    @Override
    public void setCipherFactory(CipherFactory cipherFactory)
    {
        myCipherFactory = cipherFactory;
    }

    @Override
    public void setCompressed(boolean flag)
    {
        myCompressed = flag;
    }

    @Override
    public void setEventDispatchExecutor(Executor eventDispatchExecutor)
    {
        Utilities.checkNull(eventDispatchExecutor, "eventDispatchExecutor");
        myEventExecutor = eventDispatchExecutor;
    }

    @Override
    public void setPersistenceManager(PreferencesPersistenceManager persister)
    {
        myPersistenceManager = persister;
    }

    @Override
    public void setPersistExecutor(ScheduledExecutorService persistExecutor)
    {
        Utilities.checkNull(persistExecutor, "persistExecutor");
        myPersistExecutor = new ProcrastinatingExecutor(persistExecutor, 300, 20000);
    }

    @Override
    public Collection<? extends Preference<?>> values()
    {
        return myPreferencesMap.values();
    }

    @Override
    @SuppressWarnings("PMD.CollapsibleIfStatements")
    public void waitForPersist()
    {
        CountDownLatch latch = new CountDownLatch(1);
        if (!myPersistLatches.offer(latch) && LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Unable to add to queue: no space currently available" + latch.toString());
        }
        if (!myPersistExecutor.hasPending())
        {
            schedulePersist();
        }

        while (latch.getCount() > 0)
        {
            try
            {
                latch.await();
            }
            catch (InterruptedException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(e, e);
                }
            }
        }
    }

    /**
     * Get a preference.
     *
     * @param key The key for the preference.
     * @return The preference, or {@code null} if one hasn't been set.
     */
    protected Preference<?> getPreference(String key)
    {
        return myPreferencesMap.get(key);
    }

    /**
     * Method for use by JAXB to get the preferences as an array.
     *
     * @return The preferences as an array, ordered by key.
     */
    protected Preference<?>[] getPreferences()
    {
        Preference<?>[] arr = new Preference<?>[myPreferencesMap.size()];
        TreeMap<String, Preference<?>> sortedMap = new TreeMap<>();
        for (Map.Entry<String, Preference<?>> e : myPreferencesMap.entrySet())
        {
            if (e.getValue().isSaveable())
            {
                sortedMap.put(e.getKey(), e.getValue());
            }
        }
        return sortedMap.values().toArray(arr);
    }

    /**
     * Adds a key to the store and generates the appropriate change events.
     *
     * @param pref The new preference.
     * @param compareToOld A flag that indicates if the new preference should be
     *            compared with the old preference. If the preferences are
     *            equal, nothing will be done and no listeners will be notified.
     * @param source The originator of the change.
     * @return The old preference, or {@code null} if there was none.
     */
    protected Preference<?> putPreference(Preference<?> pref, boolean compareToOld, Object source)
    {
        if (pref.validatePreference(getTopic()))
        {
            Preference<?> old = myPreferencesMap.put(pref.getKey(), pref);

            if (old == null || !compareToOld || !pref.equals(old))
            {
                firePreferenceChanged(new PreferenceChangeEvent(myTopic, pref, source));
                schedulePersist();
            }
            return old;
        }
        throw new IllegalArgumentException("Invalid preference.");
    }

    /**
     * Removes a key from the store.
     *
     * @param key The key to remove from the store.
     * @param source The originator of the change.
     * @return The removed value, or {@code null} if there was none.
     * @throws IllegalArgumentException If the key is {@code null}.
     */
    protected Preference<?> removeKey(String key, Object source)
    {
        Utilities.checkNull(key, "key");
        Preference<?> removed = myPreferencesMap.remove(key);
        if (removed != null)
        {
            firePreferenceChanged(new PreferenceChangeEvent(myTopic, new NonJAXBObjectPreference<>(key, null), source));
            schedulePersist();
        }
        return removed;
    }

    /**
     * Schedule a persist of the preferences.
     */
    protected void schedulePersist()
    {
        Executor persistExecutor = myPersistExecutor;
        final PreferencesPersistenceManager persistenceManager = myPersistenceManager;
        if (persistExecutor != null && persistenceManager != null)
        {
            persistExecutor.execute(() ->
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Saving User Preferences[" + getTopic() + "]");
                }
                try
                {
                    Collection<CountDownLatch> listeners = New.collection();
                    myPersistLatches.drainTo(listeners);

                    persistenceManager.save(this, myCipherFactory, myCompressed);
                    mySuppressWarnings = false;

                    for (CountDownLatch listener : listeners)
                    {
                        listener.countDown();
                    }
                }
                catch (IOException | JAXBException | RuntimeException e)
                {
                    if (!mySuppressWarnings)
                    {
                        mySuppressWarnings = true;
                        LOGGER.error("Error Saving User Preferences[" + getTopic() + "]", e);
                    }
                }
            });
        }
    }

    /**
     * Method for use by JAXB to set the preferences using an array.
     *
     * @param prefs The preferences.
     */
    @XmlElement(name = "preference")
    protected void setPreferences(Preference<?>[] prefs)
    {
        myPreferencesMap.clear();
        for (Preference<?> preference : prefs)
        {
            if (preference.validatePreference(getTopic()))
            {
                myPreferencesMap.put(preference.getKey(), preference);
            }
        }
    }

    /**
     * Set the preferences saveable.
     *
     * @param b {@code true} if the preferences should be saveable.
     */
    void setPreferencesSaveable(boolean b)
    {
        for (Preference<?> preference : myPreferencesMap.values())
        {
            preference.setSaveable(b);
        }
    }

    /**
     * Fires a {@link PreferenceChangeEvent} to all listeners registered for the
     * preference.
     *
     * @param event the event to fire on
     */
    private void firePreferenceChanged(final PreferenceChangeEvent event)
    {
        firePreferenceChange(event, event.getKey());
        firePreferenceChange(event, ALL_KEY);
    }

    /**
     * Fires a {@link PreferenceChangeEvent} to all listeners registered for the
     * key.
     *
     * @param event the event to fire on
     * @param key the key
     */
    private void firePreferenceChange(final PreferenceChangeEvent event, String key)
    {
        if (myChangeSupportMap.containsKey(key))
        {
            ChangeSupport<PreferenceChangeListener> changeSupport = myChangeSupportMap.get(key);
            if (changeSupport != null)
            {
                changeSupport.notifyListeners(listener -> listener.preferenceChange(event), myEventExecutor);
            }
        }
    }
}
