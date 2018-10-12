package io.opensphere.core.modulestate;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import net.jcip.annotations.GuardedBy;

/**
 * Abstract state controller for layers.
 *
 * @param <T> the type of the activated resources
 */
public abstract class AbstractLayerStateController<T> extends AbstractModuleStateController
{
    /** The map of state ID to activated resources. */
    @GuardedBy("myActivedResources")
    private final Map<String, List<T>> myActivedResources = New.map();

    @Override
    public void deactivateState(String id, Node node)
    {
        List<T> resources = removeResources(id);
        if (CollectionUtilities.hasContent(resources))
        {
            for (T resource : resources)
            {
                if (resource != null)
                {
                    deactivate(resource);
                }
            }
        }
    }

    @Override
    public void deactivateState(String id, StateType state) throws InterruptedException
    {
        deactivateState(id, (Node)null);
    }

    /**
     * Deactivates the resource.
     *
     * @param resource the resource
     */
    protected abstract void deactivate(T resource);

    /**
     * Adds the resource to the map.
     *
     * @param resource the resource
     * @param id the state ID
     */
    protected void addResource(String id, T resource)
    {
        synchronized (myActivedResources)
        {
            myActivedResources.computeIfAbsent(id, k -> New.list()).add(resource);
        }
    }

    /**
     * Adds the resources to the map.
     *
     * @param resources the resources
     * @param id the state ID
     */
    protected void addResources(String id, Collection<? extends T> resources)
    {
        synchronized (myActivedResources)
        {
            myActivedResources.computeIfAbsent(id, k -> New.list()).addAll(resources);
        }
    }

    /**
     * Removes the resources from the map.
     *
     * @param id the state ID
     * @return the removed resources, or an empty list
     */
    protected List<T> removeResources(String id)
    {
        List<T> resources;
        synchronized (myActivedResources)
        {
            resources = myActivedResources.remove(id);
        }
        return resources;
    }

    /**
     * Removes a file prefix from the URL if there is one.
     *
     * @param urlString the URL
     * @return the path without the file prefix
     */
    protected static String removeFilePrefix(String urlString)
    {
        String url = StringUtilities.removePrefix(urlString, "file://");
        url = StringUtilities.removePrefix(urlString, "file:");
        return url;
    }
}
