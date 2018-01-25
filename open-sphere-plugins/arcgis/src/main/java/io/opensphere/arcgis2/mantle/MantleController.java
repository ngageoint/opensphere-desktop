package io.opensphere.arcgis2.mantle;

import io.opensphere.mantle.data.DataGroupInfo;

/**
 * Interface to an object that knows how to add {@link DataGroupInfo} to the
 * system.
 */
public interface MantleController
{
    /**
     * Adds the server group to the system.
     *
     * @param serverName The user designated name of the server.
     * @param baseUrl The server's base url.
     * @param dataGroup The data group to add.
     */
    void addServerGroup(String serverName, String baseUrl, DataGroupInfo dataGroup);
}
