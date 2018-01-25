package io.opensphere.mantle.crust;

import java.util.function.Consumer;

import io.opensphere.core.Toolbox;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfo;
import io.opensphere.mantle.data.impl.DefaultDataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDeletableDataGroupInfo;
import io.opensphere.mantle.data.impl.DeletableDataGroupInfoAssistant;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Creates {@link DefaultDataGroupInfo} and {@link DefaultDataTypeInfo}.
 */
public class DefaultDataGroupAndTypeFactory implements DataGroupAndTypeFactory
{
    @Override
    public DefaultDataGroupInfo createGroup(Toolbox toolbox, String providerType, String folderName,
            Consumer<DataGroupInfo> deleteListener)
    {
        DefaultDataGroupInfo group;

        if (deleteListener != null)
        {
            group = new DefaultDeletableDataGroupInfo(false, toolbox, providerType, folderName);
            group.setAssistant(new DeletableDataGroupInfoAssistant(MantleToolboxUtils.getMantleToolbox(toolbox), null, null,
                    deleteListener));
        }
        else
        {
            group = new DefaultDataGroupInfo(false, toolbox, providerType, folderName);
        }

        return group;
    }

    @Override
    public DefaultDataTypeInfo createType(Toolbox toolbox, String providerType, String id, String typeName, String layerName)
    {
        return new DefaultDataTypeInfo(toolbox, providerType, id, typeName, layerName, false);
    }
}
