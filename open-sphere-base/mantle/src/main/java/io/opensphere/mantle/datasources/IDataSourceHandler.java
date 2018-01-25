package io.opensphere.mantle.datasources;

/**
 * The Interface IDataSourceHandler.
 */
public interface IDataSourceHandler
{
    /**
     * Add a new user selected data source to the tool.
     *
     * @param pSource the source
     * @return true, if successful
     */
    boolean addDataSource(IDataSource pSource);

    /**
     * Removes a data source from the tool and the corresponding config file.
     *
     * @param pSource the source
     */
    void removeDataSource(IDataSource pSource);

    /**
     * Makes necessary changes to a data source layer.
     *
     * @param pSource the source
     * @return true, if successful
     */
    boolean updateDataSource(IDataSource pSource);
}
