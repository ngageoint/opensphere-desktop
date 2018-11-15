package io.opensphere.mantle.icon;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The Interface IconProvider. Instances of this interface provide a single icon
 * from either a local or remote source.
 */
public interface IconProvider
{
    /**
     * Gets the collection name.
     *
     * @return the collection name
     */
    String getCollectionName();

    /**
     * Gets the image url.
     *
     * @return the image url
     */
    URL getIconURL();

    /**
     * Gets the data comprising the icon image. Note that implementations should
     * take care to not store icon data in member fields wherever possible, as
     * this could seriously bloat runtime memory needs.
     *
     * @return the data comprising the icon image.
     * @throws IOException if the data cannot be read.
     */
    InputStream getIconImageData() throws IOException;

    /**
     * Gets the source key for the source.
     *
     * @return the source
     */
    String getSourceKey();

    /**
     * Gets the sub category for the icon.
     *
     * @return the sub category
     */
    String getSubCategory();
}
