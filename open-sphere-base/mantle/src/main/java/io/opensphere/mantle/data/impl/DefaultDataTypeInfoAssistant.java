package io.opensphere.mantle.data.impl;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.Icon;

import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.DataTypeInfoAssistant;

/** Default {@link DataTypeInfoAssistant}. */
public class DefaultDataTypeInfoAssistant implements DataTypeInfoAssistant
{
    /** The layer control UI component. */
    private Component myLayerControlUIComponent;

    /** The layer icons. */
    private List<Icon> myLayerIcons;

    /** The layer labels. */
    private List<String> myLayerLabels;

    @Override
    public Component getLayerControlUIComponent(DataTypeInfo dataType)
    {
        return myLayerControlUIComponent;
    }

    @Override
    public synchronized List<Icon> getLayerIcons()
    {
        if (myLayerIcons == null)
        {
            myLayerIcons = new CopyOnWriteArrayList<>();
        }
        return myLayerIcons;
    }

    @Override
    public synchronized List<String> getLayerLabels()
    {
        if (myLayerLabels == null)
        {
            myLayerLabels = new CopyOnWriteArrayList<>();
        }
        return myLayerLabels;
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
}
