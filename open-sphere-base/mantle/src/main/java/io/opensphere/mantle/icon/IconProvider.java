package io.opensphere.mantle.icon;

import java.net.URL;

/**
 * The Interface IconProvider.
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
