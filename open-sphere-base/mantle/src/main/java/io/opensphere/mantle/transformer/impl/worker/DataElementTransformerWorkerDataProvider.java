package io.opensphere.mantle.transformer.impl.worker;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import io.opensphere.core.Toolbox;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.transformer.TransformerGeomRegistryUpdateTaskActivity;

/**
 * The Interface DataElementTransformerWorkerDataProvider.
 */
public interface DataElementTransformerWorkerDataProvider
{
    /**
     * Gets the data model id from geometry id bit mask.
     *
     * @return the data model id from geometry id bit mask
     */
    long getDataModelIdFromGeometryIdBitMask();

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    DataTypeInfo getDataType();

    /**
     * Gets the geometry set.
     *
     * @return the geometry set
     */
    Set<Geometry> getGeometrySet();

    /**
     * Gets the geometry set lock.
     *
     * @return the geometry set lock
     */
    ReentrantLock getGeometrySetLock();

    /**
     * Gets the hidden geometry set.
     *
     * @return the hidden geometry set
     */
    Set<Geometry> getHiddenGeometrySet();

    /**
     * Gets the id set.
     *
     * @return the id set
     */
    Set<Long> getIdSet();

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    Toolbox getToolbox();

    /**
     * Gets the update source.
     *
     * @return the update source
     */
    Object getUpdateSource();

    /**
     * Gets the update task activity.
     *
     * @return the update task activity
     */
    TransformerGeomRegistryUpdateTaskActivity getUpdateTaskActivity();

    /**
     * Gets the publish updates to geometry registry.
     *
     * @return the publish updates to geometry registry
     */
    boolean isPublishUpdatesToGeometryRegistry();
}
