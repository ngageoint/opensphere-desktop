package io.opensphere.mantle.datasources;

import java.util.List;

/**
 * The Interface IDataSourceCreator.
 */
public interface IDataSourceCreator
{
    /**
     * Source created.
     *
     * @param successful the successful
     * @param source the source
     */
    void sourceCreated(boolean successful, IDataSource source);

    /**
     * Sources created.
     *
     * @param successful the successful
     * @param sources the sources
     */
    void sourcesCreated(boolean successful, List<IDataSource> sources);
}
