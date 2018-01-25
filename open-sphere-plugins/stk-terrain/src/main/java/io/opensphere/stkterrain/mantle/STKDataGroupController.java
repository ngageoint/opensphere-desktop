package io.opensphere.stkterrain.mantle;

import io.opensphere.core.Toolbox;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.data.DataRegistryListenerAdapter;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.mantle.MantleToolbox;
import io.opensphere.mantle.controller.DataGroupController;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.util.MantleToolboxUtils;
import io.opensphere.stkterrain.model.TileSet;
import io.opensphere.stkterrain.util.Constants;

/**
 * This class pays attention to when {@link TileSet} are added/removed to the
 * {@link DataRegistry}. When added to the registry it will go and build out the
 * {@link DataGroupInfo} and {@link DataTypeInfo} representing each tile set as
 * a layer. When they are removed from the registry it will remove the
 * {@link DataGroupInfo} and {@link DataTypeInfo} from the system as well.
 */
public class STKDataGroupController extends DataRegistryListenerAdapter<TileSet>
{
    /**
     * Builds the data group and data type for a {@link TileSet}.
     */
    private final STKDataGroupBuilder myBuilder;

    /**
     * Used to listen for added {@link TileSet}.
     */
    private final DataRegistry myDataRegistry;

    /**
     * The mantle's {@link DataGroupController} used to add the root group to
     * the system.
     */
    private final DataGroupController myGroupController;

    /**
     * The root group representing the server.
     */
    private final DataGroupInfo myRootGroup;

    /**
     * Constructs a new data group controller.
     *
     * @param toolbox The system toolbox.
     * @param serverName The name of the server this data group controller will
     *            control layers for.
     * @param serverUrl The url to the server.
     */
    public STKDataGroupController(Toolbox toolbox, String serverName, String serverUrl)
    {
        myBuilder = new STKDataGroupBuilder(toolbox);
        myRootGroup = new DefaultDataGroupInfo(true, toolbox, "STK Terrain", serverUrl, serverName);
        myDataRegistry = toolbox.getDataRegistry();
        MantleToolbox mantleToolbox = MantleToolboxUtils.getMantleToolbox(toolbox);
        myGroupController = mantleToolbox.getDataGroupController();
        myGroupController.addRootDataGroupInfo(myRootGroup, this);
        myDataRegistry.addChangeListener(this, new DataModelCategory(serverUrl, TileSet.class.getName(), null),
                Constants.TILESET_PROPERTY_DESCRIPTOR);
    }

    /**
     * Stops this controller from listening to events, also removes any data
     * groups this controller has created from the system.
     */
    public void close()
    {
        myDataRegistry.removeChangeListener(this);
        myGroupController.removeDataGroupInfo(myRootGroup, this);
    }

    @Override
    public boolean isIdArrayNeeded()
    {
        return false;
    }

    @Override
    public void valuesAdded(DataModelCategory dataModelCategory, long[] ids, Iterable<? extends TileSet> newValues, Object source)
    {
        String serverUrl = dataModelCategory.getSource();

        for (TileSet tileSet : newValues)
        {
            DataGroupInfo dataGroup = myBuilder.createGroupAndType(tileSet, serverUrl);
            myRootGroup.addChild(dataGroup, this);
        }
    }
}
