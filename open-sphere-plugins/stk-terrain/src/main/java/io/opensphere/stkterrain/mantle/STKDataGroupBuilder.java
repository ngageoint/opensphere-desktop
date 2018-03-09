package io.opensphere.stkterrain.mantle;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.renderproperties.DefaultTileRenderProperties;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.order.OrderParticipantKey;
import io.opensphere.core.order.impl.DefaultOrderCategory;
import io.opensphere.core.order.impl.DefaultOrderParticipantKey;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.server.services.ServerMapVisualizationInfo;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.model.TileSetDataSource;
import io.opensphere.stkterrain.util.Constants;

/**
 * Given a {@link TileSet} this class will build the appropriate
 * {@link DataGroupInfo} and {@link DataTypeInfo} representing the tile set as a
 * layer.
 */
public class STKDataGroupBuilder
{
    /**
     * Listener for the data group's activaton property.
     */
    private final STKDataGroupActivationListener myActivationListener;

    /**
     * Used to manage zorders of the layers.
     */
    private final OrderManager myOrderManager;

    /**
     * The system {@link Toolbox}.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new {@link STKDataGroupBuilder}.
     *
     * @param toolbox The system toolbox.
     */
    public STKDataGroupBuilder(Toolbox toolbox)
    {
        myToolbox = toolbox;
        myActivationListener = new STKDataGroupActivationListener(toolbox.getDataRegistry());
        myOrderManager = toolbox.getOrderManagerRegistry().getOrderManager(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                DefaultOrderCategory.EARTH_ELEVATION_CATEGORY);
    }

    /**
     * Creates a new {@link DataGroupInfo} and {@link DataTypeInfo} representing
     * the specified {@link TileSet}. The {@link DataTypeInfo} will be a member
     * of the data group when returned.
     *
     * @param tileSet The {@link TileSet} to create data group and types for.
     * @param serverUrl The url of the server this tile set came from.
     * @return The newly created {@link DataGroupInfo}.
     */
    public DataGroupInfo createGroupAndType(TileSet tileSet, String serverUrl)
    {
        String id = serverUrl + tileSet.getName();
        DefaultDataGroupInfo dataGroup = new DefaultDataGroupInfo(false, myToolbox, Constants.PROVIDER_TYPE,
                serverUrl + tileSet.getName(), tileSet.getName());
        dataGroup.activationProperty().addListener(myActivationListener);
        dataGroup.setGroupDescription(buildDescription(tileSet));

        DefaultDataTypeInfo dataType =
                new DefaultDataTypeInfo(myToolbox, serverUrl, id, tileSet.getName(), tileSet.getName(), false);
        dataType.setUrl(serverUrl);
        OrderParticipantKey orderKey = new DefaultOrderParticipantKey(DefaultOrderCategory.DEFAULT_ELEVATION_FAMILY,
                DefaultOrderCategory.EARTH_ELEVATION_CATEGORY, id);
        dataType.setOrderKey(orderKey);
        int zOrder = myOrderManager.activateParticipant(orderKey);

        DefaultTileRenderProperties props = new DefaultTileRenderProperties(zOrder, true, false);
        ServerMapVisualizationInfo mapInfo = new ServerMapVisualizationInfo(MapVisualizationType.TERRAIN_TILE, props);
        dataType.setMapVisualizationInfo(mapInfo);

        dataGroup.addMember(dataType, this);

        return dataGroup;
    }

    /**
     * Builds a description string of the tile set.
     *
     * @param tileSet The tile set to build the description for.
     * @return The description.
     */
    private String buildDescription(TileSet tileSet)
    {
        StringBuilder builder = new StringBuilder(26);

        builder.append(tileSet.getDescription());
        builder.append("\\n\\nData Source:\\n\\n");

        int index = 0;
        for (TileSetDataSource dataSource : tileSet.getDataSources())
        {
            builder.append(dataSource.getName());

            if (index < tileSet.getDataSources().size() - 1)
            {
                builder.append(", ");
            }
            index++;
        }

        return builder.toString();
    }
}
