package io.opensphere.core.appl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.opensphere.core.PluginToolbox;
import io.opensphere.core.PluginToolboxRegistry;

/**
 * Implementation of a {@link PluginToolboxRegistry}.
 */
public class PluginToolboxRegistryImpl implements PluginToolboxRegistry
{
    /** Map to relate toolbox class to toolbox. */
    private final ConcurrentMap<Class<? extends PluginToolbox>, PluginToolbox> myToolboxClassToToolboxMap;

    /**
     * Instantiates a new plugin toolbox registry impl.
     */
    public PluginToolboxRegistryImpl()
    {
        myToolboxClassToToolboxMap = new ConcurrentHashMap<>();
    }

    @Override
    public Set<Class<? extends PluginToolbox>> getAvailablePluginToolboxTypes()
    {
        return Collections.unmodifiableSet(myToolboxClassToToolboxMap.keySet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PluginToolbox> T getPluginToolbox(Class<T> toolboxClass)
    {
        T ptb = (T)myToolboxClassToToolboxMap.get(toolboxClass);
        if (ptb == null)
        {
            for (Map.Entry<Class<? extends PluginToolbox>, PluginToolbox> entry : myToolboxClassToToolboxMap.entrySet())
            {
                if (toolboxClass.isAssignableFrom(entry.getKey()))
                {
                    ptb = (T)entry.getValue();
                    break;
                }
            }
        }

        return ptb;
    }

    @Override
    public void registerPluginToolbox(PluginToolbox toolbox)
    {
        myToolboxClassToToolboxMap.put(toolbox.getClass(), toolbox);
    }

    @Override
    public boolean removePluginToolbox(PluginToolbox toolbox)
    {
        return myToolboxClassToToolboxMap.remove(toolbox.getClass()) != null;
    }
}
