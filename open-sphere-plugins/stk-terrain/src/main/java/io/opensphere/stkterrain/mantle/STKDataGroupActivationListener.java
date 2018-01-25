package io.opensphere.stkterrain.mantle;

import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.core.util.lang.PhasedTaskCanceller;
import io.opensphere.mantle.data.AbstractActivationListener;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.stkterrain.model.TileSetMetadata;
import io.opensphere.stkterrain.util.Constants;

/**
 * This is the activation listener attached to an STK {@link DataGroupInfo} and
 * reacts to its activation state change by going out and doing a
 * {@link TileSetMetadata} query on activation, and deletes the
 * {@link TileSetMetadata} on deactivation.
 */
public class STKDataGroupActivationListener extends AbstractActivationListener
{
    /**
     * Used to query for {@link TileSetMetadata}.
     */
    private final DataRegistry myDataRegistry;

    /**
     * Constructs a new activation listener.
     *
     * @param dataRegistry Used to query for {@link TileSetMetadata}.
     */
    public STKDataGroupActivationListener(DataRegistry dataRegistry)
    {
        myDataRegistry = dataRegistry;
    }

    @Override
    public void handleCommit(boolean active, DataGroupInfo dgi, PhasedTaskCanceller canceller)
    {
        super.handleCommit(active, dgi, canceller);
        if (active)
        {
            handleActivated(dgi);
        }
        else
        {
            handleDeactivated(dgi);
        }
    }

    /**
     * Queries for the {@link TileSetMetadata} for the activated data group.
     *
     * @param dgi The data group to query for metadata.
     */
    private void handleActivated(DataGroupInfo dgi)
    {
        String serverUrl = dgi.getMembers(false).iterator().next().getSourcePrefix();
        SimpleQuery<TileSetMetadata> query = new SimpleQuery<>(
                new DataModelCategory(serverUrl, TileSetMetadata.class.getName(), dgi.getDisplayName()),
                Constants.TILESET_METADATA_PROPERTY_DESCRIPTOR);
        myDataRegistry.submitQuery(query);
    }

    /**
     * Removes the previously queried {@link TileSetMetadata} from the data
     * registry.
     *
     * @param dgi The data group to remove metadata for.
     */
    private void handleDeactivated(DataGroupInfo dgi)
    {
        String serverUrl = dgi.getMembers(false).iterator().next().getSourcePrefix();
        myDataRegistry.removeModels(new DataModelCategory(serverUrl, TileSetMetadata.class.getName(), dgi.getDisplayName()),
                false);
    }
}
