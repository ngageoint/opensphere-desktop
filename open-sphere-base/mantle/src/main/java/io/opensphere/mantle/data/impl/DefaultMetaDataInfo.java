package io.opensphere.mantle.data.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.util.PropertyArrayDescriptor;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import io.opensphere.core.util.lang.ToStringHelper;
import io.opensphere.mantle.data.ColumnTypeDetector;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MetaDataInfo;
import io.opensphere.mantle.data.SpecialKey;
import io.opensphere.mantle.data.event.DataTypeInfoMetaDataKeyAddedChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoMetaDataKeyRemovedChangeEvent;
import io.opensphere.mantle.data.event.DataTypeInfoMetaDataSpecialKeyChangeEvent;
import io.opensphere.mantle.data.impl.specialkey.AltitudeKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseOrientationKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMajorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.EllipseSemiMinorAxisKey;
import io.opensphere.mantle.data.impl.specialkey.LatitudeKey;
import io.opensphere.mantle.data.impl.specialkey.LineOfBearingKey;
import io.opensphere.mantle.data.impl.specialkey.LongitudeKey;
import io.opensphere.mantle.data.impl.specialkey.TimeKey;
import io.opensphere.mantle.data.util.DataElementLookupException;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.mantle.util.NumericDataDeterminationUtil;
import io.opensphere.mantle.util.NumericDataDeterminationUtil.NumericDetermination;
import javafx.beans.property.StringProperty;

/**
 * An class for describing the metadata for a data type.
 */
@SuppressWarnings("PMD.GodClass")
public class DefaultMetaDataInfo implements MetaDataInfo
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DefaultMetaDataInfo.class);

    /** The Constant FORTY_CHARACTER_STRING_LEFT_JUSTIFIED. */
    private static final String FORTY_CHARACTER_STRING_LEFT_JUSTIFIED = "%-40s";

    /** The Constant Line Feed. */
    private static final String LF = "\n";

    /** The Constant NON_NUMERIC_SET. */
    private static final String NON_NUMERIC_SET = "_NON_NUMERIC_SET";

    /** The Constant NUMERIC_SET. */
    private static final String NUMERIC_SET = "_NUMERIC_SET";

    /** Reference to owner DataTypInfo. */
    private DataTypeInfo myDataTypeInfo;

    /** The name of the geometry column. */
    private String myGeometryColumn;

    /** Map of key to class. */
    private final Map<String, Class<?>> myKeyClassTypeMap;

    /** Key Names. */
    private final List<String> myKeyNames;

    /** Map of numeric key types. */
    private final Map<String, Boolean> myNumericKeyMap;

    /** The Numeric key map lock. */
    private final ReentrantReadWriteLock myNumericKeyMapLock;

    /** Original Key Names. */
    private List<String> myOriginalKeyNames;

    /** The property array descriptor. */
    private PropertyArrayDescriptor myPropertyArrayDescriptor;

    /** Map of special keys to type. */
    private final Map<String, SpecialKey> mySpecialKeyToTypeMap;

    /** Map of special types to keys. */
    private final Map<SpecialKey, String> mySpecialTypeToKeyMap;

    /** Whether to auto-detect column types for this metadata. */
    private boolean myAutoDetectColumnTypes = true;

    /**
     * A detector used to find special keys. If present, keys will be examined
     * when they are added.
     */
    private ColumnTypeDetector mySpecialKeyDetector;

    /**
     * A flag used to determine if an external process should initiate a scan
     * for special keys. Defaults to true.
     */
    private boolean mySpecialKeyExaminationRequired = true;

    /**
     * The property in which the unique identifier key is maintained.
     */
    private final StringProperty myUniqueIdentifierKeyProperty = new ConcurrentStringProperty();

    /**
     * Clear preferences registry entry for numeric cache.
     *
     * @param prefsRegistry the prefsRegistry
     * @param dtiKey the dti key
     * @param source the source
     */
    public static void clearPreferencesRegistryEntryForNumericCache(PreferencesRegistry prefsRegistry, String dtiKey,
            Object source)
    {
        prefsRegistry.getPreferences(DefaultMetaDataInfo.class).remove(dtiKey + NUMERIC_SET, source);
        prefsRegistry.getPreferences(DefaultMetaDataInfo.class).remove(dtiKey + NON_NUMERIC_SET, source);
    }

    /**
     * Clear preferences registry entry for numeric cache.
     *
     * @param tb the toolbox through which application state is accessed.
     * @param dtiKey the dti key
     * @param source the source
     */
    public static void clearPreferencesRegistryEntryForNumericCache(Toolbox tb, String dtiKey, Object source)
    {
        clearPreferencesRegistryEntryForNumericCache(tb.getPreferencesRegistry(), dtiKey, source);
    }

    /**
     * Generate property array descriptor.
     *
     * @param keyNames the names of the property keys
     * @param keyClassTypeMap a map of property keys to property types
     *
     * @return the descriptor
     */
    public static PropertyArrayDescriptor generatePropertyArrayDescriptor(List<String> keyNames,
            Map<String, Class<?>> keyClassTypeMap)
    {
        if (keyNames == null || keyNames.isEmpty())
        {
            return null;
        }
        Class<?>[] columnTypes = new Class<?>[keyNames.size()];
        for (int index = 0; index < columnTypes.length; ++index)
        {
            columnTypes[index] = keyClassTypeMap.get(keyNames.get(index));
        }
        return new PropertyArrayDescriptor("propertyArray", columnTypes);
    }

    /**
     * Copy constructor.
     *
     * @param source the object from which to copy data.
     */
    protected DefaultMetaDataInfo(DefaultMetaDataInfo source)
    {
        myAutoDetectColumnTypes = source.myAutoDetectColumnTypes;
        myDataTypeInfo = source.myDataTypeInfo;
        myGeometryColumn = source.myGeometryColumn;
        myKeyClassTypeMap = source.myKeyClassTypeMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        myKeyNames = New.list(source.myKeyNames);
        myNumericKeyMap = source.myNumericKeyMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        myNumericKeyMapLock = new ReentrantReadWriteLock();
        myOriginalKeyNames = New.list(source.myOriginalKeyNames);

        myPropertyArrayDescriptor = new PropertyArrayDescriptor(source.myPropertyArrayDescriptor.getPropertyName(),
                Arrays.copyOf(source.myPropertyArrayDescriptor.getColumnTypes(),
                        source.myPropertyArrayDescriptor.getColumnTypes().length),
                Arrays.copyOf(source.myPropertyArrayDescriptor.getActiveColumns(),
                        source.myPropertyArrayDescriptor.getActiveColumns().length),
                source.myPropertyArrayDescriptor.getOrderByColumn());

        mySpecialKeyDetector = source.mySpecialKeyDetector.createCopy();
        mySpecialKeyExaminationRequired = source.mySpecialKeyExaminationRequired;
        mySpecialKeyToTypeMap = source.mySpecialKeyToTypeMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        mySpecialTypeToKeyMap = source.mySpecialTypeToKeyMap.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        myUniqueIdentifierKeyProperty.set(source.myUniqueIdentifierKeyProperty.get());

    }

    /** Default Constructor. */
    public DefaultMetaDataInfo()
    {
        myKeyNames = new ArrayList<>();
        myOriginalKeyNames = new ArrayList<>();
        myKeyClassTypeMap = new ConcurrentHashMap<>();
        myNumericKeyMapLock = new ReentrantReadWriteLock();
        myNumericKeyMap = new ConcurrentHashMap<>();
        mySpecialKeyToTypeMap = new ConcurrentHashMap<>();
        mySpecialTypeToKeyMap = new ConcurrentHashMap<>();
    }

    /**
     * CTOR with just key names. Note that when using this CTOR the original and
     * display names will be initial set to the same as the key names.
     *
     * @param keyNames - the key names
     */
    public DefaultMetaDataInfo(List<String> keyNames)
    {
        this(keyNames, keyNames);
    }

    /**
     * CTOR with original and key names, note that when using this CTOR the
     * display names will be set to the key names.
     *
     * @param origkeyNames - the original key names
     * @param keyNames - the key names
     */
    public DefaultMetaDataInfo(List<String> origkeyNames, List<String> keyNames)
    {
        myKeyNames = new ArrayList<>(keyNames);
        myOriginalKeyNames = new ArrayList<>(origkeyNames);
        myKeyClassTypeMap = new ConcurrentHashMap<>();
        myNumericKeyMapLock = new ReentrantReadWriteLock();
        myNumericKeyMap = new ConcurrentHashMap<>();
        mySpecialKeyToTypeMap = new ConcurrentHashMap<>();
        mySpecialTypeToKeyMap = new ConcurrentHashMap<>();
        myPropertyArrayDescriptor = generatePropertyArrayDescriptor(myKeyNames, myKeyClassTypeMap);
    }

    /**
     * Sets the value of the specialKeyDetector ({@link #mySpecialKeyDetector})
     * field.
     *
     * @param specialKeyDetector the value to store in the
     *            {@link #mySpecialKeyDetector} field.
     */
    @Override
    public void setSpecialKeyDetector(ColumnTypeDetector specialKeyDetector)
    {
        mySpecialKeyDetector = specialKeyDetector;
        if (myKeyNames.isEmpty())
        {
            // if the key detector is added before any keys, then we can infer
            // that the key detector will be used when keys are added instead of
            // needing to re-iterate over all of the keys later:
            mySpecialKeyExaminationRequired = false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.MetaDataInfo#isSpecialKeyExaminationRequired()
     */
    @Override
    public boolean isSpecialKeyExaminationRequired()
    {
        return mySpecialKeyExaminationRequired;
    }

    /**
     * Sets the value of the specialKeyExaminationRequired
     * ({@link #mySpecialKeyExaminationRequired}) field.
     *
     * @param specialKeyExaminationRequired the value to store in the
     *            {@link #mySpecialKeyExaminationRequired} field.
     */
    @Override
    public void setSpecialKeyExaminationRequired(boolean specialKeyExaminationRequired)
    {
        mySpecialKeyExaminationRequired = specialKeyExaminationRequired;
    }

    /**
     * Adds a key to the key names. ( At the end of the list )
     *
     * @param key - the key to add
     * @param keyClass - the class for the key
     * @param source - the calling object
     * @return true if added, false if already in set.
     */
    @Override
    public boolean addKey(String key, Class<?> keyClass, Object source)
    {
        int index = myKeyNames.indexOf(key);
        if (index >= 0)
        {
            return false;
        }
        myKeyNames.add(key);
        myKeyClassTypeMap.put(key, keyClass);
        myPropertyArrayDescriptor = generatePropertyArrayDescriptor(myKeyNames, myKeyClassTypeMap);

        if (myAutoDetectColumnTypes)
        {
            if (mySpecialKeyDetector != null)
            {
                mySpecialKeyDetector.examineColumn(this, key);
            }
            else
            {
                // the key was added when no special key detector was present,
                // so force an external examination of the keys:
                mySpecialKeyExaminationRequired = true;
            }
        }

        // If the class of the key being added is a Number then we will
        // automatically set it to numeric. If it is not a number, then we will
        // have to do on the fly determination.
        if (Number.class.isAssignableFrom(keyClass))
        {
            myNumericKeyMapLock.writeLock().lock();
            try
            {
                myNumericKeyMap.put(key, Boolean.TRUE);
            }
            finally
            {
                myNumericKeyMapLock.writeLock().unlock();
            }
        }

        if (myDataTypeInfo != null)
        {
            myDataTypeInfo.fireChangeEvent(new DataTypeInfoMetaDataKeyAddedChangeEvent(myDataTypeInfo, key, source));
        }
        return true;
    }

    /**
     * Removes a key from the key names.
     *
     * @param key - the key to add
     * @param keyClass - the class for the key
     * @param source - the calling object
     * @return true if removed, false if not in set.
     */
    @Override
    public boolean removeKey(String key, Class<?> keyClass, Object source)
    {
        int index = myKeyNames.indexOf(key);
        if (index < 0)
        {
            return false;
        }

        myKeyNames.remove(key);
        myKeyClassTypeMap.remove(key);
        myPropertyArrayDescriptor = generatePropertyArrayDescriptor(myKeyNames, myKeyClassTypeMap);

        SpecialKey sk = getSpecialTypeForKey(key);
        if (sk != null)
        {
            removeSpecialKey(sk, source);
        }

        // If the class of the key being added is a Number then we will
        // automatically set it to numeric. If it is not a number, then we will
        // have to do on the fly determination.
        if (Number.class.isAssignableFrom(keyClass))
        {
            myNumericKeyMapLock.writeLock().lock();
            try
            {
                myNumericKeyMap.remove(key);
            }
            finally
            {
                myNumericKeyMapLock.writeLock().unlock();
            }
        }

        if (myDataTypeInfo != null)
        {
            myDataTypeInfo.fireChangeEvent(new DataTypeInfoMetaDataKeyRemovedChangeEvent(myDataTypeInfo, key, source));
        }

        return true;
    }

    /**
     * Checks the key's current numeric determination without re-testing or
     * trying to make a determination.
     *
     * @param tb the {@link Toolbox}
     * @param key the key to check.
     * @return the {@link NumericDetermination}
     */
    public NumericDetermination checkKeyNumericDetermination(Toolbox tb, String key)
    {
        Boolean val = null;
        myNumericKeyMapLock.readLock().lock();
        try
        {
            val = myNumericKeyMap.get(key);
            if (val == null)
            {
                Set<String> numericColumnSet = new HashSet<>(tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                        .getStringSet(myDataTypeInfo.getTypeKey() + NUMERIC_SET, new HashSet<String>()));

                if (numericColumnSet.contains(key))
                {
                    val = Boolean.TRUE;
                }
                else
                {
                    Set<String> nonNumericColumnSet = new HashSet<>(
                            tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                                    .getStringSet(myDataTypeInfo.getTypeKey() + NON_NUMERIC_SET, new HashSet<String>()));
                    if (nonNumericColumnSet.contains(key))
                    {
                        val = Boolean.FALSE;
                    }
                }
            }
        }
        finally
        {
            myNumericKeyMapLock.readLock().unlock();
        }
        return val == null ? NumericDetermination.UNDETERMINED
                : val.booleanValue() ? NumericDetermination.NUMERIC : NumericDetermination.NOT_NUMERIC;
    }

    /**
     * Copy the keys from another metadata object to me.
     *
     * @param other The other metadata object.
     * @param source The source of the change.
     */
    public void copyKeys(MetaDataInfo other, Object source)
    {
        other.getKeyNames().forEach(k -> addKey(k, other.getKeyClassType(k), this));
        other.getSpecialKeyToTypeMap().entrySet().stream().forEach(e -> setSpecialKey(e.getKey(), e.getValue(), this));
    }

    /**
     * Takes the current key name set and makes a copy into the original key
     * name set.
     */
    public void copyKeysToOriginalKeys()
    {
        myOriginalKeyNames = new ArrayList<>(myKeyNames);
    }

    /**
     * Quick access to key for {@link SpecialKey}.Altitude.
     *
     * @return the key or null if none designated for this type.
     */
    @Override
    public String getAltitudeKey()
    {
        return getKeyForSpecialType(AltitudeKey.DEFAULT);
    }

    /**
     * Gets the data type info.
     *
     * @return the data type info
     */
    public DataTypeInfo getDataTypeInfo()
    {
        return myDataTypeInfo;
    }

    @Override
    public String getGeometryColumn()
    {
        return myGeometryColumn;
    }

    @Override
    public Class<?> getKeyClassType(String key)
    {
        return myKeyClassTypeMap.get(key);
    }

    @Override
    public Map<String, Class<?>> getKeyClassTypeMap()
    {
        return Collections.unmodifiableMap(myKeyClassTypeMap);
    }

    @Override
    public int getKeyCount()
    {
        return myKeyNames.size();
    }

    @Override
    public String getKeyForSpecialType(SpecialKey specialType)
    {
        return mySpecialTypeToKeyMap.get(specialType);
    }

    @Override
    public int getKeyIndex(String key)
    {
        return myKeyNames.indexOf(key);
    }

    @Override
    public List<String> getKeyNames()
    {
        List<String> returnList = null;
        synchronized (myKeyNames)
        {
            returnList = Collections.unmodifiableList(myKeyNames);
        }
        return returnList;
    }

    @Override
    public String getLatitudeKey()
    {
        return getKeyForSpecialType(LatitudeKey.DEFAULT);
    }

    @Override
    public String getLineOfBearingKey()
    {
        return getKeyForSpecialType(LineOfBearingKey.DEFAULT);
    }

    @Override
    public String getLongitudeKey()
    {
        return getKeyForSpecialType(LongitudeKey.DEFAULT);
    }

    @Override
    public List<String> getNumericKeyList(Toolbox tb)
    {
        synchronized (myKeyNames)
        {
            return myKeyNames.stream().filter(k -> isKeyNumeric(tb, k)).collect(Collectors.toList());
        }
    }

    @Override
    public String getOrientationKey()
    {
        return getKeyForSpecialType(EllipseOrientationKey.DEFAULT);
    }

    @Override
    public List<String> getOriginalKeyNames()
    {
        return Collections.unmodifiableList(myOriginalKeyNames);
    }

    @Override
    public PropertyArrayDescriptor getPropertyArrayDescriptor()
    {
        return myPropertyArrayDescriptor;
    }

    @Override
    public String getSemiMajorAxisKey()
    {
        return getKeyForSpecialType(EllipseSemiMajorAxisKey.DEFAULT);
    }

    @Override
    public String getSemiMinorAxisKey()
    {
        return getKeyForSpecialType(EllipseSemiMinorAxisKey.DEFAULT);
    }

    @Override
    public Map<String, SpecialKey> getSpecialKeyToTypeMap()
    {
        return New.unmodifiableMap(mySpecialKeyToTypeMap);
    }

    @Override
    public SpecialKey getSpecialTypeForKey(String key)
    {
        return mySpecialKeyToTypeMap.get(key);
    }

    @Override
    public String getTimeKey()
    {
        return getKeyForSpecialType(TimeKey.DEFAULT);
    }

    @Override
    public boolean hasKey(String key)
    {
        return myKeyNames.contains(key);
    }

    @Override
    public boolean hasTypeForSpecialKey(SpecialKey key)
    {
        return mySpecialTypeToKeyMap.get(key) != null;
    }

    @Override
    public boolean isKeyNumeric(Toolbox tb, String key)
    {
        boolean numeric = false;
        Boolean val = null;
        myNumericKeyMapLock.readLock().lock();
        try
        {
            val = myNumericKeyMap.get(key);
        }
        finally
        {
            myNumericKeyMapLock.readLock().unlock();
        }

        if (val == null)
        {
            myNumericKeyMapLock.writeLock().lock();
            try
            {
                // Retrieve the previously determined numeric/nonnumeric set
                // information for this data type.
                // or an empty set if never previously examined.
                Set<String> numericColumnSet = new HashSet<>(tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                        .getStringSet(myDataTypeInfo.getTypeKey() + NUMERIC_SET, new HashSet<String>()));
                Set<String> nonNumericColumnSet = new HashSet<>(
                        tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                                .getStringSet(myDataTypeInfo.getTypeKey() + NON_NUMERIC_SET, new HashSet<String>()));

                // Determine if in prefs and if numeric/non-numeric, if so set
                // our
                // in-memory map and return.
                boolean wasInPrefs = false;
                if (numericColumnSet.contains(key))
                {
                    wasInPrefs = true;
                    numeric = true;
                    myNumericKeyMap.put(key, Boolean.TRUE);
                }
                if (!wasInPrefs && nonNumericColumnSet.contains(key))
                {
                    wasInPrefs = true;
                    numeric = false;
                    myNumericKeyMap.put(key, Boolean.FALSE);
                }

                if (!wasInPrefs)
                {
                    // Was not in prefs, try to determine numeric by the class
                    // type
                    // of the column.
                    Class<?> keyClass = myKeyClassTypeMap.get(key);
                    if (keyClass != null && Number.class.isAssignableFrom(keyClass))
                    {
                        numeric = true;
                        myNumericKeyMap.put(key, Boolean.TRUE);
                        numericColumnSet.add(key);

                        // Update the preferences with the new determination.
                        tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                                .putStringSet(myDataTypeInfo.getTypeKey() + NUMERIC_SET, numericColumnSet, null);
                    }
                    if (!numeric)
                    {
                        // Class type was indeterminate, get a sample of the
                        // data and try to make a determination. If we don't
                        // get any samples to process, just return false and
                        // we'll
                        // try again next time if there is data available.
                        try
                        {
                            List<Object> sampleValues = MantleToolboxUtils.getDataElementLookupUtils(tb)
                                    .getMetaDataPropertySamples(key, myDataTypeInfo, null, 100, 20000);
                            if (sampleValues != null && !sampleValues.isEmpty())
                            {
                                NumericDataDeterminationUtil.NumericDetermination det = NumericDataDeterminationUtil
                                        .isSampleNumeric(sampleValues);
                                if (det == NumericDetermination.NUMERIC)
                                {
                                    numeric = true;
                                    myNumericKeyMap.put(key, Boolean.TRUE);
                                    numericColumnSet.add(key);
                                    // Update the preferences with the new
                                    // determination.
                                    tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                                            .putStringSet(myDataTypeInfo.getTypeKey() + NUMERIC_SET, numericColumnSet, null);
                                }
                                else if (det == NumericDetermination.NOT_NUMERIC)
                                {
                                    myNumericKeyMap.put(key, Boolean.FALSE);
                                    nonNumericColumnSet.add(key);
                                    // Update the preferences with the new
                                    // determination.
                                    tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class).putStringSet(
                                            myDataTypeInfo.getTypeKey() + NON_NUMERIC_SET, nonNumericColumnSet, null);
                                }
                            }
                            // }
                        }
                        catch (DataElementLookupException e)
                        {
                            LOGGER.debug(e);
                            numeric = false;
                        }
                    }
                }
            }
            finally
            {
                myNumericKeyMapLock.writeLock().unlock();
            }
        }
        else
        {
            numeric = val.booleanValue();
        }
        return numeric;
    }

    @Override
    public void removeSpecialKey(SpecialKey specialType, Object source)
    {
        if (specialType == null)
        {
            throw new IllegalArgumentException("SpecialKey cannot be null.");
        }

        boolean changed = false;

        String key = mySpecialTypeToKeyMap.get(specialType);
        if (key != null)
        {
            mySpecialTypeToKeyMap.remove(specialType);
            mySpecialKeyToTypeMap.remove(key);
            changed = true;
        }

        // Event if a change was made.
        if (changed && myDataTypeInfo != null)
        {
            myDataTypeInfo.fireChangeEvent(
                    new DataTypeInfoMetaDataSpecialKeyChangeEvent(myDataTypeInfo, null, null, key, specialType, source));
        }
    }

    @Override
    public void resetKeyNumeric(Toolbox tb, String key)
    {
        myNumericKeyMapLock.writeLock().lock();
        try
        {
            if (myNumericKeyMap != null && myNumericKeyMap.containsKey(key))
            {
                myNumericKeyMap.remove(key);

                Set<String> numericColumnSet = new HashSet<>(tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                        .getStringSet(myDataTypeInfo.getTypeKey() + NUMERIC_SET, new HashSet<String>()));

                if (numericColumnSet.contains(key))
                {
                    numericColumnSet.remove(key);
                    tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                            .putStringSet(myDataTypeInfo.getTypeKey() + NUMERIC_SET, numericColumnSet, null);
                }

                Set<String> nonNumericColumnSet = new HashSet<>(
                        tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                                .getStringSet(myDataTypeInfo.getTypeKey() + NON_NUMERIC_SET, new HashSet<String>()));

                if (nonNumericColumnSet.contains(key))
                {
                    nonNumericColumnSet.remove(key);
                    tb.getPreferencesRegistry().getPreferences(DefaultMetaDataInfo.class)
                            .putStringSet(myDataTypeInfo.getTypeKey() + NON_NUMERIC_SET, nonNumericColumnSet, null);
                }
            }
        }
        finally
        {
            myNumericKeyMapLock.writeLock().unlock();
        }
    }

    @Override
    public void resetNumericMapForAllKeys(Toolbox tb)
    {
        myNumericKeyMapLock.writeLock().lock();
        try
        {
            myNumericKeyMap.clear();
        }
        finally
        {
            myNumericKeyMapLock.writeLock().unlock();
        }
        clearPreferencesRegistryEntryForNumericCache(tb, myDataTypeInfo.getTypeKey(), this);
    }

    @Override
    public void setAltitudeKey(String key, Object source)
    {
        setSpecialKey(key, AltitudeKey.DEFAULT, source);
    }

    @Override
    public void setDataTypeInfo(DataTypeInfo dti)
    {
        myDataTypeInfo = dti;
    }

    @Override
    public void setGeometryColumn(String geometryColumn)
    {
        myGeometryColumn = geometryColumn;
    }

    @Override
    public void setKeyNumeric(String key)
    {
        myNumericKeyMapLock.writeLock().lock();
        try
        {
            myNumericKeyMap.put(key, Boolean.TRUE);
        }
        finally
        {
            myNumericKeyMapLock.writeLock().unlock();
        }
    }

    @Override
    public void setSpecialKey(String key, SpecialKey specialType, Object source)
    {
        if (key == null)
        {
            throw new IllegalArgumentException("Key cannot be null.");
        }

        boolean changed = false;
        SpecialKey oldSpecialTypeForKey = mySpecialKeyToTypeMap.get(key);
        String oldKey = null;

        // If special type is null, we are being asked to removed
        // that special type designator.
        if (specialType == null)
        {
            if (oldSpecialTypeForKey != null)
            {
                changed = true;
                mySpecialKeyToTypeMap.remove(key);
                mySpecialTypeToKeyMap.remove(oldSpecialTypeForKey);
            }
        }
        else
        {
            oldKey = mySpecialTypeToKeyMap.get(specialType);
            if (oldKey == null || !oldKey.equals(key))
            {
                changed = true;

                if (oldKey != null)
                {
                    mySpecialKeyToTypeMap.remove(oldKey);
                }

                mySpecialKeyToTypeMap.put(key, specialType);
                mySpecialTypeToKeyMap.put(specialType, key);
            }
        }
        // Event if a change was made.
        if (changed && myDataTypeInfo != null)
        {
            myDataTypeInfo.fireChangeEvent(new DataTypeInfoMetaDataSpecialKeyChangeEvent(myDataTypeInfo, key, specialType, oldKey,
                    oldSpecialTypeForKey, source));
        }
    }

    @Override
    public boolean isAutoDetectColumnTypes()
    {
        return myAutoDetectColumnTypes;
    }

    @Override
    public void setAutoDetectColumnTypes(boolean autoDetectColumnTypes)
    {
        myAutoDetectColumnTypes = autoDetectColumnTypes;
    }

    @Override
    public String toString()
    {
        return toStringMultiLine(1);
    }

    /**
     * Returns a multi-line string representation of the object.
     *
     * @param indentLevel the indent level (0-based)
     * @return the multi-line string
     */
    public String toStringMultiLine(int indentLevel)
    {
        ToStringHelper helper = new ToStringHelper(this, 256);
        helper.add("Type Key", myDataTypeInfo.getTypeKey());
        helper.add("Orig Key Names", myOriginalKeyNames);
        helper.add("Keys", toStringKeys());
        return helper.toStringMultiLine(indentLevel);
    }

    /**
     * To string for the keys.
     *
     * @return the string
     */
    public String toStringKeys()
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("\n    ").append(String.format(FORTY_CHARACTER_STRING_LEFT_JUSTIFIED, "Key"))
                .append(String.format("%-30s", "Class")).append(String.format("%-20s", "Numeric")).append("Special Type")
                .append(LF);
        String dashes = "----------";
        sb.append("    ").append(String.format(FORTY_CHARACTER_STRING_LEFT_JUSTIFIED, dashes))
                .append(String.format("%-30s", dashes)).append(String.format("%-20s", dashes)).append(dashes).append(LF);
        myNumericKeyMapLock.readLock().lock();
        try
        {
            for (String key : myKeyNames)
            {
                Boolean numeric = myNumericKeyMap.get(key);
                String numericStr = numeric == null ? "UNKNOWN" : numeric.booleanValue() ? "NUMERIC" : "NOT NUMERIC";
                Class<?> aClass = myKeyClassTypeMap.get(key);
                String className = aClass == null ? "NULL" : aClass.getSimpleName();
                SpecialKey sk = mySpecialKeyToTypeMap.get(key);
                sb.append("    ").append(String.format(FORTY_CHARACTER_STRING_LEFT_JUSTIFIED, key))
                        .append(String.format("%-30s", className)).append(String.format("%-20s", numericStr))
                        .append(sk == null ? "NONE" : sk.toString()).append(LF);
            }
        }
        finally
        {
            myNumericKeyMapLock.readLock().unlock();
        }

        return sb.toString();
    }

    /**
     * Clear all the key related data in the meta data info. Warning: Do not
     * call this method unless you know what you are doing. It should never be
     * called while a MetaDataInfo is active in the mantle.
     */
    protected void clearKeyData()
    {
        myKeyNames.clear();
        myOriginalKeyNames.clear();
        myKeyClassTypeMap.clear();
        myNumericKeyMapLock.writeLock().lock();
        try
        {
            myNumericKeyMap.clear();
        }
        finally
        {
            myNumericKeyMapLock.writeLock().unlock();
        }
        mySpecialKeyToTypeMap.clear();
        mySpecialTypeToKeyMap.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.MetaDataInfo#uniqueIdentifierKeyProperty()
     */
    @Override
    public StringProperty uniqueIdentifierKeyProperty()
    {
        return myUniqueIdentifierKeyProperty;
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.mantle.data.MetaDataInfo#createCopy()
     */
    @Override
    public MetaDataInfo createCopy()
    {
        return new DefaultMetaDataInfo(this);
    }
}
