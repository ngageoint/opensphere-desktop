package io.opensphere.geopackage.importer;

import java.sql.Connection;

import mil.nga.geopackage.db.GeoPackageConnection;

/**
 * Used for tests.
 */
public class MockGeoPackageConnection extends GeoPackageConnection
{
    /**
     * Constructs a new mock.
     *
     * @param mockedConnection A mocked connection.
     */
    public MockGeoPackageConnection(Connection mockedConnection)
    {
        super(null, mockedConnection, null);
    }
}
