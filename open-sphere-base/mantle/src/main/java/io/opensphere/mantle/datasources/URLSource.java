package io.opensphere.mantle.datasources;

/** Interface for data sources that have a URL. */
public interface URLSource extends IDataSource
{
    /**
     * Get the URL string.
     *
     * @return the URL string
     */
    String getURLString();
}
