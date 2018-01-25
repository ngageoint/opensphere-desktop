package io.opensphere.core.pipeline.processor;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.pipeline.renderer.GeometryRenderer;
import io.opensphere.core.pipeline.renderer.GeometryRenderer.Factory;
import io.opensphere.core.pipeline.util.DisposalHelper;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;

/**
 * Set of geometry renderers.
 */
public class GeometryRendererSet
{
    /** Map of geometry types to renderers. */
    private final Map<Class<? extends Geometry>, List<GeometryRenderer.Factory<? extends Geometry>>> myMap;

    /**
     * Constructor.
     *
     * @param map A map of geometry types to lists of renderer factories.
     */
    public GeometryRendererSet(Map<Class<? extends Geometry>, List<Factory<? extends Geometry>>> map)
    {
        myMap = New.map(map.size());
        for (Entry<Class<? extends Geometry>, List<Factory<? extends Geometry>>> entry : map.entrySet())
        {
            myMap.put(entry.getKey(), New.list(entry.getValue()));
        }
    }

    /**
     * Disable the current renderer for a geometry type.
     *
     * @param <T> The geometry type.
     * @param type The geometry type.
     * @return The disabled renderer factory.
     */
    @SuppressWarnings("unchecked")
    public <T extends Geometry> Factory<? super T> disableRenderer(Class<T> type)
    {
        List<Factory<? extends Geometry>> list;
        Class<? super T> checkType = type;
        do
        {
            list = myMap.get(checkType);
            if (list == null)
            {
                checkType = checkType.getSuperclass();
            }
            else
            {
                break;
            }
        }
        while (checkType != null);

        return (Factory<? super T>)(list == null ? null : list.remove(0));
    }

    /**
     * Get the set of capabilities supported by my renderers.
     *
     * @return The capabilities.
     */
    public Set<? extends String> getCapabilities()
    {
        Set<String> result = New.set();
        for (List<Factory<? extends Geometry>> list : myMap.values())
        {
            if (!list.isEmpty())
            {
                Factory<? extends Geometry> factory = list.get(0);
                result.addAll(factory.getCapabilities());
            }
        }

        return result;
    }

    /**
     * Get the disposal helpers for the viable renderers.
     *
     * @return The disposal helpers.
     */
    public Set<DisposalHelper> getDisposalHelpers()
    {
        Set<DisposalHelper> disposalHelpers = New.set();
        for (List<Factory<? extends Geometry>> list : myMap.values())
        {
            for (Factory<? extends Geometry> factory : list)
            {
                disposalHelpers.addAll(factory.getDisposalHelpers());
            }
        }
        return disposalHelpers;
    }

    /**
     * Get the renderer for a type of geometry.
     *
     * @param <T> the geometry type
     * @param type the geometry type
     * @return a geometry renderer
     */
    public <T extends Geometry> GeometryRenderer<? super T> getRenderer(Class<T> type)
    {
        Factory<? super T> factory = get(type);
        return factory == null ? null : factory.createRenderer();
    }

    /**
     * Search my map to find a renderer factory for a geometry type.
     *
     * @param <T> the geometry type
     * @param type the geometry type
     * @return a geometry renderer
     */
    @SuppressWarnings("unchecked")
    private <T extends Geometry> GeometryRenderer.Factory<? super T> get(Class<T> type)
    {
        GeometryRenderer.Factory<? super T> factory;
        Class<? super T> checkType = type;
        do
        {
            List<Factory<? extends Geometry>> list = myMap.get(checkType);
            factory = CollectionUtilities.hasContent(list) ? (GeometryRenderer.Factory<? super T>)list.get(0) : null;
            if (factory == null)
            {
                checkType = checkType.getSuperclass();
            }
            else
            {
                if (!checkType.equals(type))
                {
                    CollectionUtilities.multiMapAdd(myMap, (Class<? extends Geometry>)checkType, factory, false);
                }
                break;
            }
        }
        while (checkType != null);

        return factory;
    }
}
