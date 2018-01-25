package io.opensphere.core.iconlegend.impl;

import javax.swing.Icon;

import io.opensphere.core.iconlegend.IconLegendListener;
import io.opensphere.core.iconlegend.IconLegendRegistry;
import io.opensphere.core.util.WeakChangeSupport;

/**
 * The Class IconLegendRegistryImpl. Registry that allows adding icons to the
 * icon legend.
 */
public class IconLegendRegistryImpl implements IconLegendRegistry
{
    /** The Change support. */
    private final WeakChangeSupport<IconLegendListener> myChangeSupport;

    /**
     * Instantiates a new icon legend registry impl.
     */
    public IconLegendRegistryImpl()
    {
        myChangeSupport = new WeakChangeSupport<>();
    }

    @Override
    public void addIconLegendListener(IconLegendListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void addIconToLegend(final Icon icon, final String iconName, final String description)
    {
        myChangeSupport.notifyListeners(l -> l.iconLegendIconAdded(icon, iconName, description), null);
    }

    @Override
    public void removeIconLegendListener(IconLegendListener listener)
    {
        myChangeSupport.removeListener(listener);
    }
}
