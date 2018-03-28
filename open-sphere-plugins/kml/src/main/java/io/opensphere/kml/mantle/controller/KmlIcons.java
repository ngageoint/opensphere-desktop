package io.opensphere.kml.mantle.controller;

import java.net.URL;
import java.util.Map;

import io.opensphere.mantle.controller.AbstractIconLoader;
import io.opensphere.mantle.icon.IconRegistry;

/** Way to access KML icons. */
public final class KmlIcons extends AbstractIconLoader
{
    /** The static KmlIcons instance. */
    private static KmlIcons iconLoader;

    /**
     * Generates a static KmlIcons loader and returns its IconMap.
     *
     * @param iconRegistry the icon registry
     * @return the icon map
     */
    public static synchronized Map<String, URL> getKmlIconMap(IconRegistry iconRegistry)
    {
        if (iconLoader == null)
        {
            iconLoader = new KmlIcons("/images/maps.google.com/imageList.txt", "GoogleEarth", "KML Plugin", "maps.google.com");
        }

        return iconLoader.getIconMap(iconRegistry);
    }

    /**
     * Constructs an IconLoader
     *
     * @param pImageList the filepath of the list of images this will use
     * @param pCollectionName the name of the icon collection
     * @param pSubCategoryName the name of the collection subcategory
     * @param pSourceKey the source key for records
     */
    public KmlIcons(String pImageList, String pCollectionName, String pSubCategoryName, String pSourceKey)
    {
        super(pImageList, pCollectionName, pSubCategoryName, pSourceKey);
    }

    /**
     * Gets the root directory for an icon URL.
     *
     * @param url the icon URL
     * @return the root directory
     * @override
     */
    @Override
    protected String getDirectory(URL url)
    {
        String urlString = url.toString();
        return urlString.substring(0, urlString.indexOf("maps.google.com"));
    }

    /**
     * Gets the public facing URL for an icon URL.
     *
     * @param url the icon URL
     * @return the public facing URL
     * @override
     */
    @Override
    protected String getPublicUrl(URL url)
    {
        String urlString = url.toString();
        return "http://" + urlString.substring(urlString.indexOf("maps.google.com"));
    }
}
