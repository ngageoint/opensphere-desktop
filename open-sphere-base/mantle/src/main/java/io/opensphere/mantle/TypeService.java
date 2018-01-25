package io.opensphere.mantle;

import io.opensphere.core.Toolbox;
import io.opensphere.core.order.OrderManager;
import io.opensphere.core.util.Service;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/** Service that creates/destroys a data type. */
public class TypeService implements Service
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The parent group. */
    private final DataGroupInfo myGroup;

    /** The type managed by this service. */
    private final DataTypeInfo myType;

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     * @param group the group to add the type to
     * @param type The data type
     */
    public TypeService(Toolbox toolbox, DataGroupInfo group, DataTypeInfo type)
    {
        myToolbox = toolbox;
        myGroup = group;
        myType = type;
    }

    @Override
    public void open()
    {
        OrderManager manager = myToolbox.getOrderManagerRegistry().getOrderManager(myType.getOrderKey());
        int zorder = manager.activateParticipant(myType.getOrderKey());
        myType.getMapVisualizationInfo().setZOrder(zorder, null);

        myGroup.addMember(myType, this);

        if (myType.getMetaDataInfo() != null)
        {
            MantleToolbox mantleToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
            mantleToolbox.getDataTypeController().addDataType(myGroup.getProviderType(), "mantle", myType, this);
        }
    }

    @Override
    public void close()
    {
        if (myType.getMetaDataInfo() != null)
        {
            MantleToolbox mantleToolbox = myToolbox.getPluginToolboxRegistry().getPluginToolbox(MantleToolbox.class);
            mantleToolbox.getDataTypeController().removeDataType(myType, this);
        }

//        myGroup.removeMember(myType, false, this);

        OrderManager manager = myToolbox.getOrderManagerRegistry().getOrderManager(myType.getOrderKey());
        manager.deactivateParticipant(myType.getOrderKey());
    }

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    public DataTypeInfo getType()
    {
        return myType;
    }
}
