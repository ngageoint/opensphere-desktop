package io.opensphere.mantle.data.impl;

import java.awt.Component;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoAssistant;

/** Default {@link DataTypeInfoAssistant}. */
public class DefaultDataTypeInfoAssistant implements DataTypeInfoAssistant
{
    /** The layer control UI component. */
    private Component myLayerControlUIComponent;

    /** The layer icons. */
    private List<Icon> myLayerIcons = Collections.emptyList();

    @Override
    public Component getLayerControlUIComponent(DataTypeInfo dataType)
    {
        return myLayerControlUIComponent;
    }

    @Override
    public List<Icon> getLayerIcons()
    {
        return myLayerIcons;
    }

    /**
     * Sets the layer control UI component.
     *
     * @param layerControlUIComponent the layer control UI component
     */
    public void setLayerControlUIComponent(Component layerControlUIComponent)
    {
        myLayerControlUIComponent = layerControlUIComponent;
    }

    /**
     * Sets the layer icons.
     *
     * @param layerIcons the layer icons
     */
    public void setLayerIcons(List<Icon> layerIcons)
    {
        myLayerIcons = layerIcons;
    }
}
