package io.opensphere.mantle.icon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The Interface IconCache.
 */
public interface IconCache
{
    /**
     * Cache an icon from a byteArray source to the IconCache, returns the URL
     * for the icon in the cache location. Assigns a default name to the cached
     * file.
     *
     * @param byteArray the byte array
     * @return the cached ICON file {@link URL} in the cache.
     * @throws IOException Signals that an I/O exception has occurred while
     *             trying to cache the file.
     */
    URL cacheIcon(byte[] byteArray) throws IOException;

    /**
     * Cache an icon from a byteArray source to the IconCache, returns the URL
     * for the icon in the cache location.
     *
     * @param byteArray the byte array
     * @param destFileName the file name for the icon in the cache.
     * @param overwriteExisting the overwrite existing file if one exists.
     * @return the cached ICON file {@link URL} in the cache.
     * @throws IOException Signals that an I/O exception has occurred while
     *             trying to cache the file.
     */
    URL cacheIcon(byte[] byteArray, String destFileName, boolean overwriteExisting) throws IOException;

    /**
     * Cache an icon from a URL source to the IconCache, returns the URL for the
     * icon in the cache location. Assigns a default name to the cached file.
     *
     * @param source the source {@link File} to be cached.
     * @return the cached ICON file {@link URL} in the cache.
     * @throws IOException Signals that an I/O exception has occurred while
     *             trying to cache the file.
     */
    URL cacheIcon(File source) throws IOException;

    /**
     * Cache an icon from a URL source to the IconCache, returns the URL for the
     * icon in the cache location.
     *
     * @param source the source {@link File} to be cached.
     * @param destFileName the file name for the icon in the cache.
     * @param overwriteExisting the overwrite existing file if one exists.
     * @return the cached ICON file {@link URL} in the cache.
     * @throws IOException Signals that an I/O exception has occurred while
     *             trying to cache the file.
     */
    URL cacheIcon(File source, String destFileName, boolean overwriteExisting) throws IOException;

    /**
     * Cache an icon from a InputStream source to the IconCache, returns the URL
     * for the icon in the cache location. Assigns a default name to the cached
     * file.
     *
     * @param stream the stream
     * @return the cached ICON file {@link URL} in the cache.
     * @throws IOException Signals that an I/O exception has occurred while
     *             trying to cache the file.
     */
    URL cacheIcon(InputStream stream) throws IOException;

    /**
     * Cache an icon from a InputStream source to the IconCache, returns the URL
     * for the icon in the cache location.
     *
     * @param stream the stream
     * @param destFileName the file name for the icon in the cache.
     * @param overwriteExisting the overwrite existing file if one exists.
     * @return the cached ICON file {@link URL} in the cache.
     * @throws IOException Signals that an I/O exception has occurred while
     *             trying to cache the file.
     */
    URL cacheIcon(InputStream stream, String destFileName, boolean overwriteExisting) throws IOException;

    /**
     * Cache an icon from a URL source to the IconCache, returns the URL for the
     * icon in the cache location. Assigns a default name to the cached file.
     *
     * @param source the source {@link URL} to be cached.
     * @return the cached ICON file {@link URL} in the cache.
     * @throws IOException Signals that an I/O exception has occurred while
     *             trying to cache the file.
     */
    URL cacheIcon(URL source) throws IOException;

    /**
     * Cache an icon from a URL source to the IconCache, returns the URL for the
     * icon in the cache location.
     *
     * @param source the source {@link URL} to be cached.
     * @param destFileName the file name for the icon in the cache.
     * @param overwriteExisting the overwrite existing file if one exists.
     * @return the cached ICON file {@link URL} in the cache.
     * @throws IOException Signals that an I/O exception has occurred while
     *             trying to cache the file.
     */
    URL cacheIcon(URL source, String destFileName, boolean overwriteExisting) throws IOException;

    /**
     * Gets the icon cache location.
     *
     * @return the icon cache location
     */
    File getIconCacheLocation();

    /**
     * Removes the icon from the cache.
     *
     * @param cacheURL the cache url to be removed.
     * @return true, if successful
     */
    boolean removeIcon(URL cacheURL);
}
