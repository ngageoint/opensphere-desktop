package io.opensphere.osm.util;

import io.opensphere.core.model.ZYXImageKey;

/**
 * Utility class for Open Street Map.
 */
public final class OSMUtil
{
    /**
     * The provider type.
     */
    public static final String PROVIDER = "OSM";

    /**
     * The instance of this class.
     */
    private static final OSMUtil ourInstance = new OSMUtil();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static OSMUtil getInstance()
    {
        return ourInstance;
    }

    /**
     * Not constructible.
     */
    private OSMUtil()
    {
    }

    /**
     * Builds a url to get the specified tile's image.
     *
     * @param url The full url containing {z}, {x}, {y} to replace.
     * @param key The tile to get the image for.
     * @return The url string to the tile's image.
     */
    public String buildImageUrlString(String url, ZYXImageKey key)
    {
        String urlString = url;
        urlString = urlString.replace("{z}", String.valueOf(key.getZ()));
        urlString = urlString.replace("{y}", String.valueOf(key.getY()));
        urlString = urlString.replace("{x}", String.valueOf(key.getX()));
        return urlString;
    }
}
