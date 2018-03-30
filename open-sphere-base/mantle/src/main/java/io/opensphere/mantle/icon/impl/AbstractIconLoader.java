package io.opensphere.mantle.icon.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRegistry;

/** Way to access KML icons. */
public abstract class AbstractIconLoader
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(AbstractIconLoader.class);

    /** The images file path. */
    private final String myImageList;

    /** The icon collection. */
    private final String myCollectionName;

    /** The collection subcategory. */
    private final String mySubCategoryName;

    /** The icon record source. */
    private final String mySourceKey;

    /** Map for storage of default Google Earth icons. */
    private Map<String, URL> myIconIdentifierToUrlMap;

    /**
     * Constructs an IconLoader
     *
     * @param pImageList the filepath of the list of images this will use
     * @param pCollectionName the name of the icon collection
     * @param pSubCategoryName the name of the collection subcategory
     * @param pSourceKey the source key for records
     */
    public AbstractIconLoader(String pImageList, String pCollectionName, String pSubCategoryName, String pSourceKey)
    {
        myImageList = pImageList;
        myCollectionName = pCollectionName;
        mySubCategoryName = pSubCategoryName;
        mySourceKey = pSourceKey;
    }

    /**
     * Gets the icon map.
     *
     * @param iconRegistry the icon registry
     * @return the icon map
     */
    public Map<String, URL> getIconMap(IconRegistry iconRegistry)
    {
        if (myIconIdentifierToUrlMap == null)
        {
            myIconIdentifierToUrlMap = Collections.unmodifiableMap(loadIconBuilder(iconRegistry));
        }
        return myIconIdentifierToUrlMap;
    }

    /**
     * Initialize the default icons.
     *
     * @param iconRegistry the icon registry
     * @return the map
     */
    protected Map<String, URL> loadIconBuilder(IconRegistry iconRegistry)
    {
        List<IconRecord> records = new LinkedList<>(getIconsFromRegistry(iconRegistry));

        Map<String, Integer> publicUrlToIdMap = records.stream()
                .collect(Collectors.toMap(r -> getPublicUrl(r.getImageURL()), r -> Integer.valueOf(r.getId()), (v1, v2) -> v2));

        removeOldRecords(iconRegistry, records);

        if (records.isEmpty())
        {
            records = readIconsFromFile(iconRegistry, publicUrlToIdMap);
        }

        return createMap(records);
    }

    /**
     * Gets the KML icon records from the icon registry.
     *
     * @param iconRegistry the icon registry
     * @return the icon records
     */
    protected List<IconRecord> getIconsFromRegistry(IconRegistry iconRegistry)
    {
        return iconRegistry.getIconRecords(
                r -> myCollectionName.equals(r.getCollectionName()) && mySubCategoryName.equals(r.getSubCategory()));
    }

    /**
     * Removes old records from the list and the icon registry.
     *
     * @param iconRegistry the icon registry
     * @param records the icon records
     */
    protected void removeOldRecords(IconRegistry iconRegistry, Collection<IconRecord> records)
    {
        if (!records.isEmpty())
        {
            URL url = getClass().getResource(myImageList);
            if (url != null)
            {
                String currentDirectory = getDirectory(url);

                TIntList removedIds = new TIntArrayList();
                for (Iterator<IconRecord> iter = records.iterator(); iter.hasNext();)
                {
                    IconRecord record = iter.next();

                    String directory = getDirectory(record.getImageURL());
                    boolean isCurrent = currentDirectory.equals(directory);
                    if (!isCurrent)
                    {
                        iter.remove();
                        removedIds.add(record.getId());
                    }
                }

                if (!removedIds.isEmpty())
                {
                    iconRegistry.removeIcons(removedIds, getClass());
                }
            }
        }
    }

    /**
     * Reads icons from the file, into the icon registry.
     *
     * @param iconRegistry the icon registry
     * @param publicUrlToIdMap map of public URL to icon ID (to keep IDs
     *            consistent)
     * @return the icon records
     */
    protected List<IconRecord> readIconsFromFile(IconRegistry iconRegistry, Map<String, Integer> publicUrlToIdMap)
    {
        List<IconProvider> iconProviders = readFile().stream().map(this::getResource).filter(Objects::nonNull)
                .map(url -> IconProviderFactory.create(url, myCollectionName, mySubCategoryName, mySourceKey))
                .filter(Objects::nonNull).collect(Collectors.toList());

        List<Integer> ids = iconProviders.stream().map(p -> publicUrlToIdMap.get(getPublicUrl(p.getIconURL())))
                .collect(Collectors.toList());

        return iconRegistry.addIcons(iconProviders, ids, getClass());
    }

    /**
     * Creates a map of real-world URL to icon location URL.
     *
     * @param records the icon records
     * @return the map
     */
    protected Map<String, URL> createMap(Collection<? extends IconRecord> records)
    {
        Map<String, URL> map = New.map(records.size());
        for (IconRecord record : records)
        {
            map.put(getPublicUrl(record.getImageURL()), record.getImageURL());
        }
        return map;
    }

    /**
     * Gets the root directory for an icon URL.
     *
     * @param url the icon URL
     * @return the root directory
     */
    protected abstract String getDirectory(URL url);

    /**
     * Gets the public facing URL for an icon URL.
     *
     * @param url the icon URL
     * @return the public facing URL
     */
    protected abstract String getPublicUrl(URL url);

    /**
     * Reads the lines of the images file.
     *
     * @return the lines
     */
    protected List<String> readFile()
    {
        // Base size 620 for Google KML icons. This might be slow for larger
        // sets.
        List<String> lines = New.list(620);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(myImageList), StringUtilities.DEFAULT_CHARSET)))
        {
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                lines.add(line);
            }
        }
        catch (IOException e)
        {
            LOGGER.warn(e.getMessage());
        }
        return lines;
    }

    /**
     * Gets the URL for the filename.
     *
     * @param filename the filename
     * @return the URL, or null
     */
    protected URL getResource(String filename)
    {
        URL iconURL = getClass().getResource(filename);
        if (iconURL == null)
        {
            LOGGER.warn("Icon not found: " + filename);
        }
        return iconURL;
    }
}
