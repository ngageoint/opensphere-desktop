package io.opensphere.geopackage.importer;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.support.ConnectionSource;

import mil.nga.geopackage.extension.Extensions;
import mil.nga.geopackage.extension.ExtensionsDao;

/**
 * A {@link ExtensionsDao} used for testing.
 */
public class MockExtensionsDao extends ExtensionsDao
{
    /**
     * The list of extensions to return in the query.
     */
    private final List<Extensions> myExtensions;

    /**
     * Constructs a new test extensions dao.
     *
     * @param extensions The list of extensions to return in the query.
     * @param connectionSource A connection source.
     * @throws SQLException Bad sql.
     */
    public MockExtensionsDao(List<Extensions> extensions, ConnectionSource connectionSource) throws SQLException
    {
        super(connectionSource, Extensions.class);
        myExtensions = extensions;
    }

    @Override
    public List<Extensions> queryForAll() throws SQLException
    {
        return myExtensions;
    }

    @Override
    public boolean isTableExists() throws SQLException
    {
        return true;
    }
}
