package io.opensphere.xyztile.mantle;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Set;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DefaultDataGroupInfoAssistant;
import io.opensphere.xyztile.model.XYZDataTypeInfo;
import io.opensphere.xyztile.model.XYZTileLayerInfo;

/**
 * Class responsible for providing the Settings UI for xyz layers.
 */
public class XYZDataGroupInfoAssistant extends DefaultDataGroupInfoAssistant
{
    /**
     * Used to save and read settings stored in the system.
     */
    private final SettingsBroker myBroker;

    /**
     * Constructs a new assistant.
     *
     * @param broker Used to save and read settings stored in the system.
     */
    public XYZDataGroupInfoAssistant(SettingsBroker broker)
    {
        myBroker = broker;
    }

    @Override
    public Component getSettingsUIComponent(Dimension preferredSize, DataGroupInfo dataGroup)
    {
        Component settingsUI = null;

        Set<DataTypeInfo> members = dataGroup.getMembers(false);
        for (DataTypeInfo member : members)
        {
            if (member instanceof XYZDataTypeInfo)
            {
                XYZDataTypeInfo xyzType = (XYZDataTypeInfo)member;
                XYZTileLayerInfo layer = xyzType.getLayerInfo();
                settingsUI = new XYZSettingsUI(layer, myBroker);
                break;
            }
        }

        return settingsUI;
    }
}
