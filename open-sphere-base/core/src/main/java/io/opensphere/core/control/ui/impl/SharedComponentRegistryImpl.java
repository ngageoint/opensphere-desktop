package io.opensphere.core.control.ui.impl;

import java.awt.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.opensphere.core.control.ui.SharedComponentListener;
import io.opensphere.core.control.ui.SharedComponentRegistry;

/** Implementation of a SharedComponentRegistry. */
public class SharedComponentRegistryImpl implements SharedComponentRegistry
{
    /** Map of component names to components. */
    private final Map<String, Component> myComponents = new ConcurrentHashMap<>();

    /** Support for changes to shared components. */
    private final SharedComponentChangeSupport myChangeSupport = new SharedComponentChangeSupport();

    @Override
    public void addListener(SharedComponentListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    @Override
    public void deregisterComponent(String compName)
    {
        myComponents.remove(compName);
        myChangeSupport.notifyComponentListeners(compName, ComponentChangeType.REMOVED, null);
    }

    @Override
    public Component getComponent(String compName)
    {
        return myComponents.get(compName);
    }

    @Override
    public void registerComponent(String compName, Component component)
    {
        myComponents.put(compName, component);
        myChangeSupport.notifyComponentListeners(compName, ComponentChangeType.ADDED, null);
    }

    @Override
    public void removeListener(SharedComponentListener listener)
    {
        myChangeSupport.removeListener(listener);
    }
}
