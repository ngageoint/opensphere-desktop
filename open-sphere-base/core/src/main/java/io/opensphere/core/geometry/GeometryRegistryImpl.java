package io.opensphere.core.geometry;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.messaging.DefaultGenericPublisher;
import io.opensphere.core.messaging.GenericSubscriber;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.registry.GenericRegistry;

/**
 * Facility that keeps track of a set of geometries and provides notifications
 * when geometries are added or removed.
 */
@SuppressWarnings("PMD.GodClass")
public class GeometryRegistryImpl extends DefaultGenericPublisher<Geometry> implements GeometryRegistry
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(GeometryRegistryImpl.class);

    /**
     * A mapping of the data model id to the geometries which are backed by that
     * data model. The value may either be a {@link Geometry} or an array of
     * {@link Geometry}s.
     */
    private final TLongObjectHashMap<Object> myDataModelMap = new TLongObjectHashMap<>();

    /** Data retriever executor. */
    private final ExecutorService myDataRetrieverExecutor;

    /** Registry implementation. */
    private final GenericRegistry<Geometry> myRegistry = new GenericRegistry<>();

    /** The rendering capabilities. */
    private RenderingCapabilities myRenderingCapabilities;

    /**
     * Construct the geometry registry.
     *
     * @param dataRetrieverExecutor The executor to use for retrieving geometry
     *            data.
     */
    public GeometryRegistryImpl(ExecutorService dataRetrieverExecutor)
    {
        myDataRetrieverExecutor = dataRetrieverExecutor;
    }

    @Override
    public void addGeometriesForSource(Object source, Collection<? extends Geometry> geometries)
    {
        setDataRetriverExecutorInGeometries(geometries);
        myRegistry.addObjectsForSource(source, geometries);
        for (Geometry geom : geometries)
        {
            addToDataModelMap(geom);
        }
    }

    @Override
    public void addSubscriber(GenericSubscriber<Geometry> subscriber)
    {
        myRegistry.addSubscriber(subscriber);
    }

    @Override
    public void cancelAllImageRetrievals()
    {
        for (ImageProvidingGeometry<?> geom : getGeometriesPlusDescendantsAssignableToClass(ImageProvidingGeometry.class))
        {
            ImageManager imageManager = geom.getImageManager();
            if (imageManager != null)
            {
                imageManager.cancelRequest(true);
            }
        }
        LOGGER.info("Cancelled all image retrievals.");
    }

    @Override
    public ExecutorService getDataRetrieverExecutor()
    {
        return myDataRetrieverExecutor;
    }

    @Override
    public Collection<Geometry> getGeometries()
    {
        return myRegistry.getObjects();
    }

    /**
     * Get the geometries in this registry that are associated with a data
     * model.
     *
     * @param dataModelId The data model id.
     * @return The geometries.
     */
    @Override
    public List<? extends Geometry> getGeometriesForDataModel(long dataModelId)
    {
        synchronized (myDataModelMap)
        {
            Object obj = myDataModelMap.get(dataModelId);
            if (obj == null)
            {
                return Collections.emptyList();
            }
            else if (obj instanceof Geometry)
            {
                return Collections.singletonList((Geometry)obj);
            }
            else
            {
                return New.unmodifiableList((Geometry[])obj);
            }
        }
    }

    /**
     * Get the geometries in this registry that are associated with some data
     * models.
     *
     * @param dataModelIds The data model ids.
     * @return The geometries.
     */
    @Override
    public List<Geometry> getGeometriesForDataModels(long[] dataModelIds)
    {
        List<Geometry> result = New.list(dataModelIds.length);
        synchronized (myDataModelMap)
        {
            for (long dataModelId : dataModelIds)
            {
                Object obj = myDataModelMap.get(dataModelId);
                if (obj instanceof Geometry)
                {
                    result.add((Geometry)obj);
                }
                else if (obj instanceof Geometry[])
                {
                    result.addAll(Arrays.asList((Geometry[])obj));
                }
            }
        }
        return result;
    }

    /**
     * Get the geometries that have been added associated with a particular
     * source.
     *
     * @param source The source of the geometries.
     * @return The collection of geometries in the registry associated with the
     *         source.
     */
    public Collection<Geometry> getGeometriesForSource(Object source)
    {
        return myRegistry.getObjectsForSource(source);
    }

    @Override
    public <T extends Geometry> Collection<T> getGeometriesForSource(Object source, Class<T> type)
    {
        return myRegistry.getObjectsForSource(source, type);
    }

    /**
     * {@inheritDoc}
     *
     * @see io.opensphere.core.geometry.GeometryRegistry#getGeometriesOfClass(java.lang.Class)
     */
    @Override
    public <T extends Geometry> Collection<T> getGeometriesOfClass(Class<T> cl)
    {
        return myRegistry.getObjectsOfClass(cl);
    }

    /**
     * Get the top-level geometry plus any children that exist for the top-level
     * geometries that are assignable to the specified class.
     *
     * @param <T> The class.
     * @param cl The class.
     * @return The geometries.
     */
    public <T extends Geometry> Collection<? extends T> getGeometriesPlusDescendantsAssignableToClass(Class<T> cl)
    {
        Collection<T> result = New.collection();
        for (T obj : myRegistry.getObjectsAssignableToClass(cl))
        {
            result.add(obj);
            if (obj instanceof HierarchicalGeometry)
            {
                @SuppressWarnings("unchecked")
                HierarchicalGeometry<T> cast = (HierarchicalGeometry<T>)obj;
                cast.getDescendants(result);
            }
        }
        return result;
    }

    @Override
    public RenderingCapabilities getRenderingCapabilities()
    {
        return myRenderingCapabilities;
    }

    @Override
    public void receiveObjects(Object source, Collection<? extends Geometry> adds, Collection<? extends Geometry> removes)
    {
        for (Geometry geom : adds)
        {
            if (geom instanceof DataRequestingGeometry)
            {
                ((DataRequestingGeometry)geom).getDataRequestAgent().setDataRetrieverExecutor(myDataRetrieverExecutor);
            }

            addToDataModelMap(geom);
        }

        for (Geometry geom : removes)
        {
            removeFromDataModelMap(geom);
        }
        myRegistry.receiveObjects(source, adds, removes);
    }

    @Override
    public void removeGeometriesForDataModels(long[] dataModelIds, Collection<? super Geometry> removed)
    {
        synchronized (myDataModelMap)
        {
            for (long dataModelId : dataModelIds)
            {
                Object obj = myDataModelMap.remove(dataModelId);
                if (removed != null)
                {
                    if (obj instanceof Geometry)
                    {
                        removed.add((Geometry)obj);
                    }
                    else if (obj instanceof Geometry[])
                    {
                        removed.addAll(Arrays.asList((Geometry[])obj));
                    }
                }
            }
        }
    }

    @Override
    public Collection<Geometry> removeGeometriesForSource(Object source)
    {
        Collection<Geometry> removes = myRegistry.removeObjectsForSource(source);
        for (Geometry geom : removes)
        {
            removeFromDataModelMap(geom);
        }
        return removes;
    }

    @Override
    public <T extends Geometry> Collection<T> removeGeometriesForSource(Object source, Class<T> type)
    {
        Collection<T> removes = myRegistry.removeObjectsForSource(source, type);
        for (Geometry geom : removes)
        {
            removeFromDataModelMap(geom);
        }
        return removes;
    }

    @Override
    public boolean removeGeometriesForSource(Object source, Collection<? extends Geometry> geometries)
    {
        for (Geometry geom : geometries)
        {
            removeFromDataModelMap(geom);
        }
        return myRegistry.removeObjectsForSource(source, geometries);
    }

    @Override
    public void setRenderingCapabilities(RenderingCapabilities caps)
    {
        if (myRenderingCapabilities == null)
        {
            myRenderingCapabilities = caps;
        }
        else
        {
            throw new IllegalStateException("Cannot set rendering capabilities more than once.");
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        for (Geometry geom : getGeometries())
        {
            sb.append(geom).append(StringUtilities.LINE_SEP);
        }

        return sb.toString();
    }

    /**
     * Add a geometry to the data model map.
     *
     * @param geom The geometry.
     */
    private void addToDataModelMap(Geometry geom)
    {
        if (geom.getDataModelId() != -1)
        {
            synchronized (myDataModelMap)
            {
                Object obj = myDataModelMap.get(geom.getDataModelId());
                if (obj == null)
                {
                    myDataModelMap.put(geom.getDataModelId(), geom);
                }
                else if (obj instanceof Geometry)
                {
                    Geometry[] arr = new Geometry[] { (Geometry)obj, geom };
                    myDataModelMap.put(geom.getDataModelId(), arr);
                }
                else
                {
                    Geometry[] arr = (Geometry[])obj;
                    arr = Arrays.copyOf(arr, arr.length + 1);
                    arr[arr.length - 1] = geom;
                    myDataModelMap.put(geom.getDataModelId(), arr);
                }
            }
        }
    }

    /**
     * Remove a geometry from the data model map.
     *
     * @param geom The geometry.
     */
    private void removeFromDataModelMap(Geometry geom)
    {
        if (geom.getDataModelId() != -1)
        {
            synchronized (myDataModelMap)
            {
                Object obj = myDataModelMap.get(geom.getDataModelId());
                if (obj != null)
                {
                    if (obj instanceof Geometry)
                    {
                        if (Utilities.sameInstance(obj, geom))
                        {
                            myDataModelMap.remove(geom.getDataModelId());
                        }
                    }
                    else if (((Geometry[])obj).length == 1)
                    {
                        if (Utilities.sameInstance(((Geometry[])obj)[0], geom))
                        {
                            myDataModelMap.remove(geom.getDataModelId());
                        }
                    }
                    else
                    {
                        myDataModelMap.put(geom.getDataModelId(), Utilities.removeFromArray((Geometry[])obj, geom));
                    }
                }
            }
        }
    }

    /**
     * Set the data retriever executor in a set of geometries.
     *
     * @param geometries The geometries.
     */
    private void setDataRetriverExecutorInGeometries(Iterable<? extends Geometry> geometries)
    {
        for (Geometry geom : geometries)
        {
            if (geom instanceof DataRequestingGeometry)
            {
                ((DataRequestingGeometry)geom).getDataRequestAgent().setDataRetrieverExecutor(myDataRetrieverExecutor);
            }
        }
    }
}
