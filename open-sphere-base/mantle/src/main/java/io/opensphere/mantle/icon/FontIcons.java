package io.opensphere.mantle.icon;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.google.common.collect.Streams;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import io.opensphere.core.util.AwesomeIcon;
import io.opensphere.core.util.GovIcon;
import io.opensphere.core.util.MilitaryRankIcon;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.swing.FontAwesomeIcon;
import io.opensphere.core.util.swing.FontIcon;
import io.opensphere.core.util.swing.GovIconFontIcon;
import io.opensphere.core.util.swing.MilitaryRankFontIcon;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.icon.impl.IconProviderFactory;

/** Way to access KML icons. */
public final class FontIcons
{
    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(FontIcons.class);

    /** The System Properties map. */
    private static final Map<String, String> ourSystemPropertiesMap = New.map();

    /** Map for storage of default Google Earth icons. */
    private static Map<String, URL> ourIconNameToUrlMap;

    /**
     * Gets the icon map.
     *
     * @param mantleToolbox Reference to the MantleToolbox
     * @return the icon map
     */
    public static synchronized Map<String, URL> getIconMap(MantleToolbox mantleToolbox)
    {
        if (ourSystemPropertiesMap.isEmpty())
        {
            buildPropertiesMap();
        }
        if (ourIconNameToUrlMap == null)
        {
            ourIconNameToUrlMap = Collections.unmodifiableMap(loadIcons(mantleToolbox));
        }
        return ourIconNameToUrlMap;
    }

    /**
     * Initializes the system properties map. We only care about the runtime
     * path value.
     */
    private static void buildPropertiesMap()
    {
        Properties p = System.getProperties();
        String pathProperty = "opensphere.path.runtime";
        String pathValue = StringUtilities.expandProperties(p.getProperty(pathProperty), p);

        ourSystemPropertiesMap.put(pathProperty, pathValue);
    }

    /**
     * Initialize the default icons.
     *
     * @param mantleToolbox Reference to the MantleToolbox
     * @return the map
     */
    private static Map<String, URL> loadIcons(MantleToolbox mantleToolbox)
    {
        List<IconRecord> records = new LinkedList<>(getIconsFromRegistry(mantleToolbox));

        Map<String, Integer> nameToIdMap = records.stream()
                .collect(Collectors.toMap(r -> r.getName(), r -> Integer.valueOf(r.getId()), (v1, v2) -> v2));

        removeOldRecords(mantleToolbox, records);

        if (records.isEmpty())
        {
            records = readIconsFromFile(mantleToolbox, nameToIdMap);
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
        return mantleToolbox.getIconRegistry().getIconRecords(r -> "Font Icons".equals(r.getCollectionName()));
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
            TIntList removedIds = new TIntArrayList();
            for (Iterator<IconRecord> iter = records.iterator(); iter.hasNext();)
            {
                IconRecord record = iter.next();

                iter.remove();
                removedIds.add(record.getId());
            }

            if (!removedIds.isEmpty())
            {
                mantleToolbox.getIconRegistry().removeIcons(removedIds, FontIcons.class);
            }
        }
    }

    /**
     * Reads icons from the file, into the icon registry.
     *
     * @param mantleToolbox the mantle toolbox
     * @param nameToIdMap map of icon name to icon ID (to keep IDs consistent)
     * @return the icon records
     */
    private static List<IconRecord> readIconsFromFile(MantleToolbox mantleToolbox, Map<String, Integer> nameToIdMap)
    {
        List<IconProvider> iconProviders;

        Stream<IconProvider> fontawesomeStream = Arrays.stream(AwesomeIcon.values()).map(FontIcons::getResource)
                .filter(Objects::nonNull).map(url -> IconProviderFactory.create(url, "Font Icons", "FontAwesome", "fontawesome"))
                .filter(Objects::nonNull);
        Stream<IconProvider> goviconStream = Arrays.stream(GovIcon.values()).map(FontIcons::getResource).filter(Objects::nonNull)
                .map(url -> IconProviderFactory.create(url, "Font Icons", "GovIcons", "govicons.io")).filter(Objects::nonNull);
        Stream<IconProvider> rankStream = Arrays.stream(MilitaryRankIcon.values()).map(FontIcons::getResource)
                .filter(Objects::nonNull)
                .map(url -> IconProviderFactory.create(url, "Font Icons", "Military Ranks", "militaryranks"))
                .filter(Objects::nonNull);

        iconProviders = Streams.concat(fontawesomeStream, goviconStream, rankStream).collect(Collectors.toCollection(New::list));

        List<Integer> ids = iconProviders.stream().map(p -> nameToIdMap.get(getIconName(p))).collect(Collectors.toList());

        return mantleToolbox.getIconRegistry().addIcons(iconProviders, ids, FontIcons.class);
    }

    /**
     * Gets the name of the IconRecord linked to the IconProvider.
     *
     * @param provider the icon provider
     * @return the name
     */
    private static String getIconName(IconProvider provider)
    {
        String urlStr = provider.getIconURL().toString();
        String nameStr = urlStr;
        int lastIndexOfSlash = urlStr.lastIndexOf('\\');
        if (lastIndexOfSlash == -1)
        {
            lastIndexOfSlash = urlStr.lastIndexOf('/');
        }
        if (lastIndexOfSlash != -1)
        {
            nameStr = urlStr.substring(lastIndexOfSlash + 1);
        }
        return nameStr;
    }

    /**
     * Creates a map of real-world URL to icon location URL.
     *
     * @param records the icon records
     * @return the map
     */
    private static Map<String, URL> createMap(Collection<? extends IconRecord> records)
    {
        Map<String, URL> map = New.map(records.size());
        for (IconRecord record : records)
        {
            map.put(record.getName(), record.getImageURL());
        }
        return map;
    }

    /**
     * Gets the URL for the icon image.
     *
     * @param icon the AwesomeIcon enum
     * @return the URL
     */
    private static URL getResource(AwesomeIcon icon)
    {
        return getResource("/images/awesomeicon/" + icon.name() + ".png", new FontAwesomeIcon(icon, Color.WHITE, 100));
    }

    /**
     * Gets the URL for the icon image.
     *
     * @param icon the GovIcon enum
     * @return the URL
     */
    private static URL getResource(GovIcon icon)
    {
        return getResource("/images/govicons/" + icon.name() + ".png", new GovIconFontIcon(icon, Color.WHITE, 100));
    }

    /**
     * Gets the URL for the icon image.
     *
     * @param icon the GovIcon enum
     * @return the URL
     */
    private static URL getResource(MilitaryRankIcon icon)
    {
        return getResource("/images/militaryranks/" + icon.name() + ".png", new MilitaryRankFontIcon(icon, Color.WHITE, 100));
    }

    /**
     * Gets the URL for the icon image. Creates one if it doesn't exist.
     *
     * @param iconFile the file name & type
     * @param icon the FontIcon object representing the icon
     * @return the URL
     */
    private static URL getResource(String iconFile, FontIcon icon)
    {
        String imgPath = ourSystemPropertiesMap.get("opensphere.path.runtime") + iconFile;
        URL file = null;

        Path folderPath = Paths.get(imgPath);
        if (!Files.exists(folderPath))
        {
            try
            {
                Files.createDirectories(folderPath.getParent());

                File filePath = folderPath.toFile();
                if (filePath.createNewFile())
                {
                    ImageIO.write(icon.getImage(), "PNG", filePath);
                }

                file = filePath.toURI().toURL();
            }
            catch (IOException e)
            {
                LOGGER.error("Unable to write new file", e);
            }
        }
        else
        {
            try
            {
                file = folderPath.toUri().toURL();
            }
            catch (MalformedURLException e)
            {
                LOGGER.error(e, e);
            }
        }

        return file;
    }

    /** Private constructor. */
    private FontIcons()
    {
    }
}
