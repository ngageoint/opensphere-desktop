package io.opensphere.csvcommon.detect.util;

import java.util.List;

import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.importer.config.ColumnType;

/**
 * The Class CSVColumnPrefsUtil provides the CSV detectors a way to retrieve
 * column sets for a given column type or a user specified key as well as adding
 * new column names to the applicable column sets.
 */
public final class CSVColumnPrefsUtil
{
    /** Not constructible. */
    private CSVColumnPrefsUtil()
    {
    }

    /**
     * Adds a column name to a known column type.
     *
     * @param prefs the preferences registry
     * @param colName the column to add to the applicable set
     * @param type the known column type
     */
    public static void addSpecialKey(PreferencesRegistry prefs, String colName, ColumnType type)
    {
        addKey(prefs, colName, type.name());
    }

    /**
     * Adds a column name to the list associated with the given key.
     *
     * @param prefs the preferences registry
     * @param colName the column to add to the applicable set
     * @param key the key name
     */
    public static void addCustomKey(PreferencesRegistry prefs, String colName, String key)
    {
        addKey(prefs, colName, key);
    }

    /**
     * Adds a column name to the set associated with the given key.
     *
     * @param prefs the preferences registry
     * @param colName the column name
     * @param key the key name
     */
    private static void addKey(PreferencesRegistry prefs, String colName, String key)
    {
        List<String> specialKeys = prefs.getPreferences(CSVColumnPrefsUtil.class).getStringList(key, null);
        if (specialKeys == null)
        {
            specialKeys = New.list();
        }

        List<String> keys = New.list(specialKeys);
        keys.add(colName);

        prefs.getPreferences(CSVColumnPrefsUtil.class).putStringList(key, keys, CSVColumnPrefsUtil.class);
    }

    /**
     * Gets the set of column names associated with a known column type.
     *
     * @param prefs the preferences registry
     * @param type the know column type
     * @return the special keys the set of column names associated with the know
     *         column type
     */
    public static List<String> getSpecialKeys(PreferencesRegistry prefs, ColumnType type)
    {
        return getKeys(prefs, type.name());
    }

    /**
     * Gets the set of column names associated with a key.
     *
     * @param prefs the preferences registry
     * @param key the key name
     * @return the custom keys the set of names associated with this key
     */
    public static List<String> getCustomKeys(PreferencesRegistry prefs, String key)
    {
        return getKeys(prefs, key);
    }

    /**
     * Gets the set of column names associated with a key.
     *
     * @param prefs the preferences registry
     * @param key the key name
     * @return the keys the set of names associated with this key
     */
    private static List<String> getKeys(PreferencesRegistry prefs, String key)
    {
        return prefs.getPreferences(CSVColumnPrefsUtil.class).getStringList(key, null);
    }
}
