package io.opensphere.mantle.data;

/**
 * An interface to a {@link DataGroupInfo} whose members can be manually
 * refreshed by the user.
 */
@FunctionalInterface
public interface RefreshableDataGroupInfo
{
    /**
     * Refreshes all descendants nodes with up to date nodes.
     */
    void refresh();
}
