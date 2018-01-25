package io.opensphere.mantle.data.impl;

import java.awt.Component;
import java.awt.Dimension;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoAssistant;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoAssistant;

/**
 * The Class DefaultDataGroupInfoAssistant.
 */
public class DefaultDataGroupInfoAssistant implements DataGroupInfoAssistant
{
    /**
     * Instantiates a new default data group info assistant.
     */
    public DefaultDataGroupInfoAssistant()
    {
    }

    @Override
    public boolean canDeleteGroup(DataGroupInfo dgi)
    {
        return false;
    }

    @Override
    public boolean canReImport(DataGroupInfo dgi)
    {
        return false;
    }

    @Override
    public void close()
    {
    }

    @Override
    public void deleteGroup(DataGroupInfo dgi, Object source)
    {
    }

    @Override
    public Component getDebugUIComponent(Dimension preferredSize, DataGroupInfo dgi)
    {
        return null;
    }

    @Override
    public Component getLayerControlUIComponent(Dimension preferredSize, DataGroupInfo dataGroup, DataTypeInfo dataType)
    {
        DataTypeInfoAssistant assistant = dataType != null ? dataType.getAssistant() : null;
        return assistant != null ? assistant.getLayerControlUIComponent(dataType) : null;
    }

    @Override
    public Dimension getSettingsPreferredSize()
    {
        return null;
    }

    @Override
    public Component getSettingsUIComponent(Dimension preferredSize, DataGroupInfo dataGroup)
    {
        return null;
    }

    @Override
    public void reImport(DataGroupInfo dgi, Object source)
    {
    }
}
