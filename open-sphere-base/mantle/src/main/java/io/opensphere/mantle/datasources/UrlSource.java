package io.opensphere.mantle.datasources;

/** Interface for data sources that have a URL. */
public interface UrlSource extends IDataSource
{
    /**
     * Get the URL string.
     *
     * @return the URL string
     */
    String getURL();
}
