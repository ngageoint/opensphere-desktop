package io.opensphere.mantle.data;

import java.util.List;
import java.util.Map;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.util.PropertyArrayDescriptor;
import javafx.beans.property.StringProperty;

/**
 * An interface for describing the metadata for a data type.
 */
public interface MetaDataInfo
{
    /** The Empty {@link PropertyArrayDescriptor}. */
    PropertyArrayDescriptor EMPTY_PROPERTY_ARRAY_DESCRIPTOR = new PropertyArrayDescriptor("propertyArray", new Class<?>[0]);

    /**
     * Adds a key to the key names. ( At the end of the list )
     *
     * @param key - the key to add
     * @param keyClass - the class for the key
     * @param source - the calling object
     * @return true if added, false if already in set.
     */
    boolean addKey(String key, Class<?> keyClass, Object source);

    /**
     * Quick access to key for {@link SpecialKey}.Altitude.
     *
     * @return the key or null if none designated for this type.
     */
    String getAltitudeKey();

    /**
     * Gets the WKT(Well Known Text) geometry column.
     *
     * @return the geometry column
     */
    String getGeometryColumn();

    /**
     * Gets the class type of the data used for the specified key.
     *
     * @param key the key
     * @return the key class
     */
    Class<?> getKeyClassType(String key);

    /**
     * Gets the mapping between a key of metadata and the class type expected to
     * be used to store the actual data.
     *
     * @return the map of key to class type
     */
    Map<String, Class<?>> getKeyClassTypeMap();

    /**
     * Gets the count of keys in the KeyNames list.
     *
     * @return the key count.
     */
    int getKeyCount();

    /**
     * Gets the key for a special type.
     *
     * @param specialType - the special type to check.
     * @return the key or null if no key for the special designation.
     */
    String getKeyForSpecialType(SpecialKey specialType);

    /**
     * Returns the index of the specified key in the KeyNames list.
     *
     * @param key - the key to check
     * @return the index or -1 if not in list.
     */
    int getKeyIndex(String key);

    /**
     * Get the list of keys for the meta data This may be a filtered set of the
     * original keys.
     *
     * Note: this is an unmodifiable list.
     *
     * @return the list of keys
     */
    List<String> getKeyNames();

    /**
     * Quick access to key for {@link SpecialKey}.Latitude.
     *
     * @return the key or null if none designated for this type.
     */
    String getLatitudeKey();

    /**
     * Quick access to key for {@link SpecialKey}.LineOfBearing.
     *
     * @return the key or null if none designated for this type.
     */
    String getLineOfBearingKey();

    /**
     * Quick access to key for {@link SpecialKey}.Longitude.
     *
     * @return the key or null if none designated for this type.
     */
    String getLongitudeKey();

    /**
     * Returns a list of the keys that represent numeric data. If those keys
     * have not yet been determined they will be determined and the answers
     * cached.
     *
     * NOTE: This could be a costly call as all data is checked for
     * determination, the fewer keys that have been determined the longer it
     * will take.
     *
     * @param tb the {@link Toolbox}
     * @return List of the keys in the same order they appear in the primary key
     *         list.
     */
    List<String> getNumericKeyList(Toolbox tb);

    /**
     * Quick access to key for {@link SpecialKey}.Orientation.
     *
     * @return the key or null if none designated for this type.
     */
    String getOrientationKey();

    /**
     * Get the original un-altered list of key names as setup the the original
     * creator of this DataType.
     *
     * @return the list of original keys
     */
    List<String> getOriginalKeyNames();

    /**
     * Gets the property array descriptor.
     *
     * @return the property array descriptor
     */
    PropertyArrayDescriptor getPropertyArrayDescriptor();

    /**
     * Quick access to key for {@link SpecialKey}.SemiMajorAxis.
     *
     * @return the key or null if none designated for this type.
     */
    String getSemiMajorAxisKey();

    /**
     * Quick access to key for {@link SpecialKey}.SemiMinorAxis.
     *
     * @return the key or null if none designated for this type.
     */
    String getSemiMinorAxisKey();

    /**
     * Get a map of special keys to types.
     *
     * @return The map.
     */
    Map<String, SpecialKey> getSpecialKeyToTypeMap();

    /**
     * Gets the special type for the specified key.
     *
     * @param key - the key
     * @return the special type or null if no special designation
     */
    SpecialKey getSpecialTypeForKey(String key);

    /**
     * Quick access to key for {@link SpecialKey}.Time.
     *
     * @return the key or null if none designated for this type.
     */
    String getTimeKey();

    /**
     * Returns true if the key name list contains the specified key.
     *
     * @param key - the key to check.
     * @return true if it has the key, false if not
     */
    boolean hasKey(String key);

    /**
     * Checks for type type with a given special key.
     *
     * @param specialType the special type to look for.
     * @return true, if the set of keys includes one that is of the specified
     *         special type.
     */
    boolean hasTypeForSpecialKey(SpecialKey specialType);

    /**
     * Returns true if the key represents a set of numeric data. If it is not
     * yet known, a lazy determination may be made.
     *
     * @param tb - the toolbox.
     * @param key - the key to check.
     * @return true if the key represents numeric data, false if not
     */
    boolean isKeyNumeric(Toolbox tb, String key);

    /**
     * Removes the special type. See {@link SpecialKey} for some of the
     * pre-defined special types.
     *
     * @param specialType - the special type to remove
     * @param source - the object making the change.
     */
    void removeSpecialKey(SpecialKey specialType, Object source);

    /**
     * Resets the internal map that tracks which keys represent numeric data and
     * which are not for the selected column so that it will be re-determined
     * the next time isKeyNumeric is called for a specific key.
     *
     * @param tb the {@link Toolbox}
     * @param key the key to be reset
     */
    void resetKeyNumeric(Toolbox tb, String key);

    /**
     * Resets the internal map that tracks which keys represent numeric data and
     * which are not for the selected column so that it will be re-determined
     * the next time isKeyNumeric is called for all keys.
     *
     * @param tb the {@link Toolbox}
     */
    void resetNumericMapForAllKeys(Toolbox tb);

    /**
     * Sets the altitude key.
     *
     * @param key the new altitude key
     * @param source the source
     */
    void setAltitudeKey(String key, Object source);

    /**
     * Sets the DataTypeInfo for this visualization info.
     *
     * @param dti - the {@link DataTypeInfo}
     */
    void setDataTypeInfo(DataTypeInfo dti);

    /**
     * Sets the WKT(Well Known Text) geometry column.
     *
     * @param geometryColumn the new geometry column
     */
    void setGeometryColumn(String geometryColumn);

    /**
     * Instructs that the data represented by the key argument represents
     * numerical data. (i.e. safe to convert to numbers without exceptions,
     * null, and empty string excepted).
     *
     * Only use this if you are sure, if not the numerical nature of the data
     * will be determined lazily when needed.
     *
     * @param key - the key to set.
     */
    void setKeyNumeric(String key);

    /**
     * Sets the special type for a key. See {@link SpecialKey} for some of the
     * pre-defined special types.
     *
     * @param key - the key
     * @param specialType - the special type to designate ( if null it will
     *            un-set the special type assigned to the key if any )
     * @param source - the object making the change.
     */
    void setSpecialKey(String key, SpecialKey specialType, Object source);

    /**
     * Whether column types are to be auto-detected by mantle.
     *
     * @return whether column types are to be auto-detected by mantle
     */
    boolean isAutoDetectColumnTypes();

    /**
     * Sets whether column types are to be auto-detected by mantle.
     *
     * @param autoDetectColumnTypes whether column types are to be auto-detected
     *            by mantle
     */
    void setAutoDetectColumnTypes(boolean autoDetectColumnTypes);

    /**
     * Sets the column detector used to find special columns. This should be
     * used with {@link #setAutoDetectColumnTypes(boolean)} to avoid excessive
     * iteration over the columns.
     *
     * @param specialKeyDetector the column detector used to find special keys.
     */
    void setSpecialKeyDetector(ColumnTypeDetector specialKeyDetector);

    /**
     * Sets the value of the specialKeyExaminationRequired field.
     *
     * @param specialKeyExaminationRequired the value to store in the field.
     */
    void setSpecialKeyExaminationRequired(boolean specialKeyExaminationRequired);

    /**
     * Test to determine if a special key-detection process should be run. If
     * they detector is not present when the keys are added, then a separate
     * examination of the keys should be performed.
     *
     * @return true if the metadata object should be examined for special keys,
     *         or if the process was performed inline.
     */
    boolean isSpecialKeyExaminationRequired();

    /**
     * Gets the property in which the unique identifier key property is
     * maintained. The key in this property corresponds to the same key
     * associated with the association ID special key type. The property from
     * this method must never be null, but the value accessed within the
     * returned property may be null.
     *
     * @return the property in which the unique identifier key property is
     *         maintained.
     */
    StringProperty uniqueIdentifierKeyProperty();
}
