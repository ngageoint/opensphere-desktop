package io.opensphere.kml.mantle.controller;

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
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.IconProviderFactory;

/** Way to access KML icons. */
public final class KmlIcons
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(KmlIcons.class);

    /** The images file path. */
    private static final String IMAGES_FILE_PATH = "/images/maps.google.com/imageList.txt";

    /** Map for storage of default Google Earth icons. */
    private static Map<String, URL> ourGoogleEarthIconHrefToURLMap;

    /**
     * Gets the icon map.
     *
     * @param mantleToolbox Reference to the MantleToolbox
     * @return the icon map
     */
    public static synchronized Map<String, URL> getIconMap(MantleToolbox mantleToolbox)
    {
        if (ourGoogleEarthIconHrefToURLMap == null)
        {
            ourGoogleEarthIconHrefToURLMap = Collections.unmodifiableMap(loadKmlIcons(mantleToolbox));
        }
        return ourGoogleEarthIconHrefToURLMap;
    }

    /**
     * Initialize the default icons.
     *
     * @param mantleToolbox Reference to the MantleToolbox
     * @return the map
     */
    private static Map<String, URL> loadKmlIcons(MantleToolbox mantleToolbox)
    {
        List<IconRecord> records = new LinkedList<>(getIconsFromRegistry(mantleToolbox));

        Map<String, Integer> publicUrlToIdMap = records.stream()
                .collect(Collectors.toMap(r -> getPublicUrl(r.getImageURL()), r -> Integer.valueOf(r.getId()), (v1, v2) -> v2));

        removeOldRecords(mantleToolbox, records);

        if (records.isEmpty())
        {
            records = readIconsFromFile(mantleToolbox, publicUrlToIdMap);
        }

        return createMap(records);
    }

    /**
     * Gets the KML icon records from the icon registry.
     *
     * @param mantleToolbox the mantle toolbox
     * @return the icon records
     */
    private static List<IconRecord> getIconsFromRegistry(MantleToolbox mantleToolbox)
    {
        return mantleToolbox.getIconRegistry()
                .getIconRecords(r -> "GoogleEarth".equals(r.getCollectionName()) && "KML Plugin".equals(r.getSubCategory()));
    }

    /**
     * Removes old records from the list and the icon registry.
     *
     * @param mantleToolbox the mantle toolbox
     * @param records the icon records
     */
    private static void removeOldRecords(MantleToolbox mantleToolbox, Collection<IconRecord> records)
    {
        if (!records.isEmpty())
        {
            URL url = KmlIcons.class.getResource(IMAGES_FILE_PATH);
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
                    mantleToolbox.getIconRegistry().removeIcons(removedIds, KmlIcons.class);
                }
            }
        }
    }

    /**
     * Reads icons from the file, into the icon registry.
     *
     * @param mantleToolbox the mantle toolbox
     * @param publicUrlToIdMap map of public URL to icon ID (to keep IDs consistent)
     * @return the icon records
     */
    private static List<IconRecord> readIconsFromFile(MantleToolbox mantleToolbox, Map<String, Integer> publicUrlToIdMap)
    {
        List<IconProvider> iconProviders = readFile().stream().map(KmlIcons::getResource).filter(Objects::nonNull)
                .map(url -> IconProviderFactory.create(url, "GoogleEarth", "KML Plugin", "maps.google.com"))
                .filter(Objects::nonNull).collect(Collectors.toList());

        List<Integer> ids = iconProviders.stream().map(p -> publicUrlToIdMap.get(getPublicUrl(p.getIconURL())))
                .collect(Collectors.toList());

        return mantleToolbox.getIconRegistry().addIcons(iconProviders, ids, KmlIcons.class);
    }

    /**
     * Creates a map of real-world URL to icon location URL.
     *
     * @param records the icon records
     * @return the map
     */
    private static Map<String, URL> createMap(Collection<? extends IconRecord> records)
    {
        Map<String, URL> map = New.map(900);
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
    private static String getDirectory(URL url)
    {
        String urlString = url.toString();
        return urlString.substring(0, urlString.indexOf("maps.google.com"));
    }

    /**
     * Gets the public facing URL for an icon URL.
     *
     * @param url the icon URL
     * @return the public facing URL
     */
    private static String getPublicUrl(URL url)
    {
        String urlString = url.toString();
        return "http://" + urlString.substring(urlString.indexOf("maps.google.com"));
    }

    /**
     * Reads the lines of the images file.
     *
     * @return the lines
     */
    private static List<String> readFile()
    {
        List<String> lines = New.list(620);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(KmlIcons.class.getResourceAsStream(IMAGES_FILE_PATH), StringUtilities.DEFAULT_CHARSET)))
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
    private static URL getResource(String filename)
    {
        URL iconURL = KmlIcons.class.getResource(filename);
        if (iconURL == null)
        {
            LOGGER.warn("Icon not found: " + filename);
        }
        return iconURL;
    }

    /** Private constructor. */
    private KmlIcons()
    {
    }
}
