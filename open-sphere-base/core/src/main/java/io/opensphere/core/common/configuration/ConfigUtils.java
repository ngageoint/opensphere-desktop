package io.opensphere.core.common.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides configuration utility methods for reading properties.
 */
public abstract class ConfigUtils
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Log logger = LogFactory.getLog(ConfigUtils.class);

    /**
     * Property file configuration divider
     */
    private static final String CONFIG_DIVIDER = ".";

    /**
     * Optional suffix to look for at the end of configurations. When not found,
     * an attempt to find the configuration without the suffix is made.
     */
    private String suffix;

    /**
     * Sets the value of the suffix ({@link #suffix}) field.
     *
     * @param suffix the value to store in the {@link #suffix} field.
     */
    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    /**
     * Get the base configuration name.
     *
     * @return the base configuration name
     */
    protected abstract String getBaseConfig();

    /**
     * Get the configuration path, appending the provided configuration paths.
     *
     * @param paths the paths to append.
     * @return a path generated using the default base configuration and the
     *         supplied suffixes.
     */
    private String getConfigPath(String... paths)
    {
        return createConfigPath(getBaseConfig(), paths);
    }

    /**
     * Get the configuration path with the appended optional suffix, or null if no
     * suffix
     *
     * @param path the optional suffix to append.
     * @return a path generated using the supplied path and the
     *         internal suffix.
     */
    private String getConfigPathSuffix(String path)
    {
        String suffixPath = null;
        if (suffix != null && !suffix.isEmpty())
        {
            suffixPath = createConfigPath(path, suffix);
        }
        return suffixPath;
    }

    /**
     * Get a configuration sub path combining the supplied paths.
     *
     * @param paths the paths with which to create the configuration path.
     * @return
     */
    public String createPartialConfigPath(String... paths)
    {
        return createConfigPath(null, paths);
    }

    /**
     * Get a configuration sub path combining the supplied base and suffixes.
     *
     * @param basePath the optional base path to use in the configuration path.
     * @param paths the paths with which to create the configuration path.
     * @return
     */
    private String createConfigPath(String basePath, String... paths)
    {
        StringBuilder configPath = new StringBuilder();
        boolean first = true;

        if (basePath != null)
        {
            configPath.append(basePath);
            first = false;
        }

        for (String path : paths)
        {

            if (first)
            {
                first = false;
            }
            else
            {
                configPath.append(CONFIG_DIVIDER);
            }

            configPath.append(path);
        }

        return configPath.toString();
    }

    /**
     * Get the string configured value for the provided configuration paths
     *
     * @param allowNull a flag used to permit null values in the string.
     * @param paths
     * @return
     */
    public String getString(boolean allowNull, String... paths)
    {
        String value = null;
        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);
        if (suffixPath != null)
        {
            value = Configurator.getConfig().getString(suffixPath);
        }
        if (value == null)
        {
            value = Configurator.getConfig().getString(path);
            if (value == null && !allowNull)
            {
                throw new IllegalArgumentException("Could not find configuration property for: " + path);
            }
        }
        return value;
    }

    /**
     * Get a string array returned as a single string with the commas in tact
     *
     * @param allowNull a flag used to permit null values in the string.
     * @param paths
     * @return
     */
    public String getStringArrayAsString(boolean allowNull, String... paths)
    {

        String value = null;

        String[] valueArray = getStringArray(allowNull, paths);

        if (valueArray.length > 0)
        {
            StringBuilder valueBuilder = new StringBuilder();
            for (int i = 0; i < valueArray.length; i++)
            {
                if (i > 0)
                {
                    valueBuilder.append(",");
                }
                valueBuilder.append(valueArray[i]);
            }
            value = valueBuilder.toString();
        }

        return value;
    }

    /**
     * Get the string configured array of values for the provided configuration paths
     *
     * @param allowEmpty a flag used to permit empty values in the array.
     * @param paths
     * @return
     */
    public String[] getStringArray(boolean allowEmpty, String... paths)
    {
        String[] value = null;
        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);
        if (suffixPath != null)
        {
            value = Configurator.getConfig().getStringArray(suffixPath);
        }
        if (value == null || value.length == 0)
        {
            value = Configurator.getConfig().getStringArray(path);
            if ((value == null || value.length == 0) && !allowEmpty)
            {
                throw new IllegalArgumentException("Could not find configuration property or at least one value for: " + path);
            }
        }
        return value;
    }

    /**
     * Get the string configured set of values for the provided configuration paths
     *
     * @param allowEmpty a flag used to permit empty values in the set.
     * @param paths
     * @return
     */
    public Set<String> getStringSet(boolean allowEmpty, String... paths)
    {
        String[] stringArray = getStringArray(allowEmpty, paths);
        Set<String> stringSet = new HashSet<>();
        for (String value : stringArray)
        {
            stringSet.add(value);
        }
        return stringSet;
    }

    /**
     * Get the string configured set of values for the provided configuration paths in
     * the order they are configured
     *
     * @param allowEmpty a flag used to permit empty values in the set.
     * @param paths
     * @return
     */
    public Set<String> getOrderedStringSet(boolean allowEmpty, String... paths)
    {
        String[] stringArray = getStringArray(allowEmpty, paths);
        Set<String> stringSet = new LinkedHashSet<>();
        for (String value : stringArray)
        {
            stringSet.add(value);
        }
        return stringSet;
    }

    /**
     * Get the string configured set of values upper cased for the provided
     * configuration paths
     *
     * @param allowEmpty a flag used to permit empty values in the set.
     * @param paths
     * @return
     */
    public Set<String> getUpperCaseStringSet(boolean allowEmpty, String... paths)
    {
        String[] stringArray = getStringArray(allowEmpty, paths);
        Set<String> stringSet = new HashSet<>();
        for (String value : stringArray)
        {
            stringSet.add(value.toUpperCase());
        }
        return stringSet;
    }

    /**
     * Get the string configured value for the provided configuration paths, set to a
     * default value if not configured
     *
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths
     * @return
     */
    public String getStringWithDefault(String defaultValue, String... paths)
    {
        String value = null;
        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);
        if (suffixPath != null)
        {
            value = Configurator.getConfig().getString(suffixPath);
        }
        if (value == null)
        {
            value = Configurator.getConfig().getString(path, defaultValue);
        }
        return value;
    }

    /**
     * Get the integer configured value for the provided configuration paths
     *
     * @param paths
     * @return
     */
    public int getInt(String... paths)
    {
        return getIntegerHelper(false, null, paths);
    }

    /**
     * Get the integer configured value for the provided configuration paths returning
     * a default value if not found
     *
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths
     * @return
     */
    public Integer getIntegerWithDefault(Integer defaultValue, String... paths)
    {
        return getIntegerHelper(true, defaultValue, paths);
    }

    /**
     * Get integer helper method
     *
     * @param defaultMode
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths
     * @return
     */
    private Integer getIntegerHelper(boolean defaultMode, Integer defaultValue, String... paths)
    {
        Integer value = null;
        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);
        if (suffixPath != null)
        {
            value = Configurator.getConfig().getInteger(suffixPath, null);
        }
        if (value == null)
        {
            if (defaultMode)
            {
                value = Configurator.getConfig().getInteger(path, defaultValue);
            }
            else
            {
                value = Configurator.getConfig().getInt(path);
            }
        }
        return value;
    }

    /**
     * Get the long configured value for the provided configuration paths
     *
     * @param paths
     * @return
     */
    public long getLong(String... paths)
    {
        return getLongHelper(false, null, paths);
    }

    /**
     * Get the long configured value for the provided configuration paths returning a
     * default value if not found
     *
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths
     * @return
     */
    public Long getLongWithDefault(Long defaultValue, String... paths)
    {
        return getLongHelper(true, defaultValue, paths);
    }

    /**
     * Get long helper method
     *
     * @param defaultMode
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths
     * @return
     */
    private Long getLongHelper(boolean defaultMode, Long defaultValue, String... paths)
    {
        Long value = null;
        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);
        if (suffixPath != null)
        {
            value = Configurator.getConfig().getLong(suffixPath, null);
        }
        if (value == null)
        {
            if (defaultMode)
            {
                value = Configurator.getConfig().getLong(path, defaultValue);
            }
            else
            {
                value = Configurator.getConfig().getLong(path);
            }
        }
        return value;
    }

    /**
     * Get the boolean configured value for the provided configuration paths
     *
     * @param paths
     * @return
     */
    public boolean getBoolean(String... paths)
    {
        return getBooleanHelper(false, null, paths);
    }

    /**
     * Get the boolean configured value for the provided configuration paths returning
     * a default value if not found
     *
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths
     * @return
     */
    public Boolean getBooleanWithDefault(Boolean defaultValue, String... paths)
    {
        return getBooleanHelper(true, defaultValue, paths);
    }

    /**
     * Get boolean helper method
     *
     * @param defaultMode a flag to enable the default mode of the helper.
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths
     * @return
     */
    private Boolean getBooleanHelper(boolean defaultMode, Boolean defaultValue, String... paths)
    {
        Boolean value = null;
        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);
        if (suffixPath != null)
        {
            value = Configurator.getConfig().getBoolean(suffixPath, null);
        }
        if (value == null)
        {
            if (defaultMode)
            {
                value = Configurator.getConfig().getBoolean(path, defaultValue);
            }
            else
            {
                value = Boolean.valueOf(Configurator.getConfig().getBoolean(path));
            }
        }
        return value;
    }

    /**
     * Get the double configured value for the provided configuration paths
     *
     * @param paths the configuration paths from which to extract the double value.
     * @return
     */
    public double getDouble(String... paths)
    {
        return getDoubleHelper(false, null, paths);
    }

    /**
     * Get the double configured value for the provided configuration paths returning a
     * default value if one is not found
     *
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths the configuration paths from which to extract the double value.
     * @return
     */
    public Double getDoubleWithDefault(Double defaultValue, String... paths)
    {
        return getDoubleHelper(true, defaultValue, paths);
    }

    /**
     * Get double helper method
     *
     * @param defaultMode a flag to enable the default mode of the helper.
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths the configuration paths from which to extract the double value.
     * @return
     */
    private Double getDoubleHelper(boolean defaultMode, Double defaultValue, String... paths)
    {
        Double value = null;
        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);
        if (suffixPath != null)
        {
            value = Configurator.getConfig().getDouble(suffixPath, null);
        }
        if (value == null)
        {
            if (defaultMode)
            {
                value = Configurator.getConfig().getDouble(path, defaultValue);
            }
            else
            {
                value = Configurator.getConfig().getDouble(path);
            }
        }
        return value;
    }

    /**
     * Get the float configured value for the provided configuration paths
     *
     * @param paths the configuration paths from which to extract the float value.
     * @return
     */
    public float getFloat(String... paths)
    {
        return getFloatHelper(false, null, paths);
    }

    /**
     * Get the float configured value for the provided configuration paths returning a
     * default value if one is not found
     *
     * @param defaultValue
     * @param paths the configuration paths from which to extract the float value.
     * @return
     */
    public Float getFloatWithDefault(Float defaultValue, String... paths)
    {
        return getFloatHelper(true, defaultValue, paths);
    }

    /**
     * Get float helper method.
     *
     * @param defaultMode a flag to enable the default mode of the helper.
     * @param defaultValue the default value to use when no configuration item
     *            is found.
     * @param paths the configuration paths from which to extract the helper.
     * @return a Float initialized from configuration.
     */
    private Float getFloatHelper(boolean defaultMode, Float defaultValue, String... paths)
    {
        Float value = null;
        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);
        if (suffixPath != null)
        {
            value = Configurator.getConfig().getFloat(suffixPath, null);
        }
        if (value == null)
        {
            if (defaultMode)
            {
                value = Configurator.getConfig().getFloat(path, defaultValue);
            }
            else
            {
                value = Float.valueOf(Configurator.getConfig().getFloat(path));
            }
        }
        return value;
    }

    /**
     * Load a list of configuration values from a single configured value
     * separated by the delimiter.
     *
     * @param allowNull a flag used to permit null values.
     * @param delimiter the delimiter used to separate key and value pairs.
     * @param config the key to use in looking up the value in the map.
     * @return the list of delimiters encountered in the configuration.
     */
    public List<String> loadDelimiterList(boolean allowNull, String delimiter, String config)
    {
        List<String> list = new ArrayList<>();
        String value = getString(allowNull, config);
        if (value != null && !value.isEmpty())
        {
            String[] parts = value.split(delimiter);
            for (String part : parts)
            {
                list.add(part);
            }
        }
        return list;
    }

    /**
     * Load a set of configuration values from a single configured value
     * separated by the delimiter.
     *
     * @param allowNull a flag used to permit null values.
     * @param delimiter the delimiter used to separate key and value pairs.
     * @param config the key to use in looking up the value in the map.
     * @return the set of delimiters encountered in the configuration.
     */
    public Set<String> loadDelimiterSet(boolean allowNull, String delimiter, String config)
    {
        List<String> list = loadDelimiterList(allowNull, delimiter, config);
        Set<String> set = new HashSet<>();
        set.addAll(list);
        return set;
    }

    /**
     * Load a configuration set for the provided configuration String starting from 1 and
     * incrementing until not found.
     *
     * @param config the key to use in looking up the value in the map.
     * @return the set of configuration items found in the backing store.
     */
    public Set<String> loadConfigSet(String config)
    {
        Set<String> configSet = new HashSet<>();
        String error = null;
        int index = 1;
        while ((error = getString(true, config, Integer.toString(index++))) != null)
        {
            configSet.add(error);
        }
        if (configSet.isEmpty() && logger.isWarnEnabled())
        {
            logger.warn("No numbered configurations starting at 1 were found for config: " + getConfigPath(config));
        }
        return configSet;
    }

    /**
     * Load a configuration map for the provided configuration String starting from 1 and
     * incrementing until not found. Each configuration is split apart by the
     * delimiter and saved to the map.
     *
     * @param config the key to use in looking up the value in the map.
     * @param delimiter the delimiter used to separate keys and values.
     * @return the map of configuration items, read from the backing store.
     */
    public Map<String, String> loadConfigMap(String config, String delimiter)
    {
        Map<String, String> configMap = new HashMap<>();
        Set<String> configSet = loadConfigSet(config);
        for (String value : configSet)
        {
            String[] parts = value.split(delimiter);
            if (parts.length != 2)
            {
                throw new IllegalStateException("Configuration value must be split into two parts by the delimiter. Config: "
                        + config + ", Value: " + value + ", Delimiter: " + delimiter);
            }
            configMap.put(parts[0], parts[1]);
        }
        return configMap;
    }

    /**
     * Creates a map of properties to their values. A series of properties named
     * as follows:
     *
     * <base path>.<path>.<key 1>=<value 1> <base path>.<path>.<key 2>=<value 1>
     * ... <base path>.<path>.<key n>=<value n>
     *
     * will produce a <code>Map</code> of each key to it's value.
     *
     * @param paths Path to the map property
     * @return A <code>Map</code> or each key to it's value.
     */
    public Map<String, String> getMap(String... paths)
    {
        Map<String, String> map = new HashMap<>();

        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);

        Iterator<String> keys = null;

        if (suffixPath != null)
        {
            keys = Configurator.getConfig().getKeys(suffixPath);
        }
        else
        {
            keys = Configurator.getConfig().getKeys(path);
        }

        while (keys.hasNext())
        {
            String fullKey = keys.next();
            String key = fullKey.substring(fullKey.lastIndexOf(".") + 1);
            String propertyName = createConfigPath(path, key);
            String value = Configurator.getConfig().getString(propertyName);
            map.put(key, value);
        }

        return map;
    }

    /**
     * Creates a map of properties to their array of values. A series of
     * properties named as follows:
     *
     * <base path>.<path>.<key 1>=[<value 1A>,<value1B>,...] <base
     * path>.<path>.<key 2>=[<value 2B>,...] ... <base path>.<path>.<key
     * n>=[<value nA>,...]
     *
     * will produce a <code>Map</code> of each key to it's array of values.
     *
     * @param paths Path to the map property
     * @return A <code>Map</code> or each key to it's value array
     */
    public Map<String, String[]> getMapOfArrays(String... paths)
    {
        Map<String, String[]> map = new HashMap<>();

        String path = getConfigPath(paths);
        String suffixPath = getConfigPathSuffix(path);

        Iterator<String> keys = null;

        if (suffixPath != null)
        {
            keys = Configurator.getConfig().getKeys(suffixPath);
        }
        else
        {
            keys = Configurator.getConfig().getKeys(path);
        }

        while (keys.hasNext())
        {
            String fullKey = keys.next();
            String key = fullKey.substring(fullKey.lastIndexOf(".") + 1);
            String propertyName = createConfigPath(path, key);
            String[] values = Configurator.getConfig().getStringArray(propertyName);
            map.put(key, values);
        }

        return map;
    }
}
