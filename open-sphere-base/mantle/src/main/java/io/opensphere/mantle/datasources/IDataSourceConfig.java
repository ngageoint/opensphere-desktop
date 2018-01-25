package io.opensphere.mantle.datasources;

import java.util.List;

/**
 * The Interface IDataSourceConfig.
 */
public interface IDataSourceConfig
{
    /**
     * Adds a IFileDataSource to the config.
     *
     * @param source the source
     * @return true if added, false if not
     */
    boolean addSource(IDataSource source);

    /**
     * Returns the list of all {@link IDataSource} from the config.
     *
     * @return the {@link List} of {@link IDataSource}
     */
    List<IDataSource> getSourceList();

    /**
     * Removes an {@link IDataSource} from the config.
     *
     * @param source the source to remove
     * @return true if removed, false if not
     */
    boolean removeSource(IDataSource source);

    /**
     * Notifies the config that a source has been updated and needs to be
     * saved/persisted.
     *
     * @param source the source
     */
    void updateSource(IDataSource source);
}
