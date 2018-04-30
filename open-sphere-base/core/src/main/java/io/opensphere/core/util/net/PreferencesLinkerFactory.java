package io.opensphere.core.util.net;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;

/**
 * Factory that builds {@link Linker}s based on preferences.
 */
public final class PreferencesLinkerFactory
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PreferencesLinkerFactory.class);

    /**
     * Get a linker for the given data type.
     *
     * @param typeKey the type key
     * @param prefsRegistry the preferences registry
     * @return The linker.
     */
    public static DefaultLinker getLinker(String typeKey, PreferencesRegistry prefsRegistry)
    {
        // Build the map of column name links
        Collection<LinkPattern> linkPatterns = New.collection();
        Preferences prefs = prefsRegistry.getPreferences(DefaultLinker.class);
        Map<String, String> columnNamesToLinkProviders = prefs.getStringMap("columnNamesToLinkProviders", null);
        if (columnNamesToLinkProviders != null)
        {
            Map<String, String> linkProviders = prefs.getStringMap("linkProviders", null);
            Map<String, String> transformPrefs = prefs.getStringMap("transforms", null);

            for (Entry<String, String> entry : columnNamesToLinkProviders.entrySet())
            {
                String key = entry.getKey();
                int splitIndex = key.indexOf("$^");
                String colPattern;
                if (splitIndex != -1)
                {
                    String typeRegex = key.substring(0, splitIndex + 1);
                    if (Pattern.matches(typeRegex, typeKey))
                    {
                        colPattern = key.substring(splitIndex + 1);
                    }
                    else
                    {
                        colPattern = null;
                    }
                }
                else
                {
                    colPattern = key;
                }

                if (colPattern != null)
                {
                    String urlPattern = linkProviders.get(entry.getValue());
                    if (urlPattern != null)
                    {
                        splitIndex = colPattern.indexOf("$^");
                        String valuePattern = null;
                        if (splitIndex != -1)
                        {
                            valuePattern = colPattern.substring(splitIndex + 1);
                            colPattern = colPattern.substring(0, splitIndex + 1);
                        }
                        LinkPattern linkPattern = new LinkPattern(entry.getValue(), colPattern, valuePattern, urlPattern);
                        linkPattern.setTransforms(getTransforms(entry.getValue(), transformPrefs));
                        linkPatterns.add(linkPattern);
                    }
                }
            }
        }

        // Create the linker
        DefaultLinker linker;
        if (!linkPatterns.isEmpty())
        {
            linker = new DefaultLinker();
            linker.addPatterns(linkPatterns);
        }
        else
        {
            linker = null;
        }
        return linker;
    }

    /**
     * Gets the transforms for the given key.
     *
     * @param key the key
     * @param transformPrefs the transform preferences map
     * @return the transforms
     */
    @SuppressWarnings("unchecked")
    private static Collection<Function<String, String>> getTransforms(String key, Map<String, String> transformPrefs)
    {
        Collection<Function<String, String>> transforms = New.list();
        String transformPref = transformPrefs == null ? null : transformPrefs.get(key);
        if (transformPref != null)
        {
            String[] transformClassNames = transformPref.split(",");
            for (String transformClassName : transformClassNames)
            {
                try
                {
                    Class<?> transformClass = Class.forName(transformClassName);
                    Object transformObject = transformClass.getDeclaredConstructor().newInstance();
                    if (transformObject instanceof Function)
                    {
                        transforms.add((Function<String, String>)transformObject);
                    }
                }
                catch (ReflectiveOperationException e)
                {
                    LOGGER.error(e, e);
                }
            }
        }
        return transforms;
    }

    /** Private constructor. */
    private PreferencesLinkerFactory()
    {
    }
}
