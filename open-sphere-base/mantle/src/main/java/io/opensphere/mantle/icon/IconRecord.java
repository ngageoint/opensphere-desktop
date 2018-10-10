package io.opensphere.mantle.icon;

import java.net.URL;

/**
 * The Interface IconRecord.
 */
public interface IconRecord
{
    /** The Constant DEFAULT_COLLECTION. */
    String DEFAULT_COLLECTION = "Default";

    /** The Constant FAVORITES_COLLECTION. */
    String FAVORITES_COLLECTION = "Favorites";

    /** The Constant USER_ADDED_COLLECTION. */
    String USER_ADDED_COLLECTION = "User Added";

    /**
     * Gets the collection name.
     *
     * @return the collection name
     */
    String getCollectionName();

    /**
     * Gets the id.
     *
     * @return the id
     */
    int getId();

    /**
     * Gets the image provider.
     *
     * @return the image provider
     */
    URL getImageURL();

    /**
     * Gets the source key.
     *
     * @return the source key
     */
    String getSourceKey();

    /**
     * Gets the sub category.
     *
     * @return the sub category
     */
    String getSubCategory();

    /**
     * Gets the name of the record (based on the URL).
     *
     * @return the name
     */
    default String getName()
    {
        String urlStr = getImageURL().toString();
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
}
