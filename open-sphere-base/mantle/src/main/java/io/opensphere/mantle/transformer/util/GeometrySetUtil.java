package io.opensphere.mantle.transformer.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.geometry.AbstractRenderableGeometry;
import io.opensphere.core.geometry.Geometry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.rangeset.RangedLongSet;

/**
 * The Class GeometrySetUtil.
 */
public final class GeometrySetUtil
{
    /** The Constant ALL_BITS_MASK. */
    public static final long ALL_BITS_MASK = -1;

    /**
     * Find geometries with ids.
     *
     * @param geomSet the geom set
     * @param geomSetLock the geom set lock
     * @param idSet the id set
     * @param dmGeomIdMask the dm geom id mask
     * @return the map
     */
    public static TLongObjectHashMap<AbstractRenderableGeometry> findGeometriesWithIds(Set<AbstractRenderableGeometry> geomSet,
            ReentrantLock geomSetLock, RangedLongSet idSet, long dmGeomIdMask)
    {
        return findGeometriesWithIds(geomSet, geomSetLock, idSet, null, dmGeomIdMask);
    }

    /**
     * Find geometries with ids.
     *
     * @param geomSet the geom set
     * @param geomSetLock the geom set lock
     * @param idSet the id set
     * @param foundIdSet the found id set ( if passed in not null any found ids
     *            will be added to this set )
     * @param dmGeomIdMask the dm geom id mask
     * @return the map
     */
    public static TLongObjectHashMap<AbstractRenderableGeometry> findGeometriesWithIds(Set<AbstractRenderableGeometry> geomSet,
            ReentrantLock geomSetLock, RangedLongSet idSet, Set<Long> foundIdSet, long dmGeomIdMask)
    {
        TLongObjectHashMap<AbstractRenderableGeometry> foundSet = new TLongObjectHashMap<>();
        geomSetLock.lock();
        try
        {
            for (AbstractRenderableGeometry geom : geomSet)
            {
                if (idSet.contains(Long.valueOf(geom.getDataModelId() & dmGeomIdMask)))
                {
                    if (foundIdSet != null)
                    {
                        foundIdSet.add(Long.valueOf(geom.getDataModelId() & dmGeomIdMask));
                    }
                    foundSet.put(geom.getDataModelId() & dmGeomIdMask, geom);
                }
            }
        }
        finally
        {
            geomSetLock.unlock();
        }

        return foundSet;
    }

    /**
     * Find geometry set with ids.
     *
     * @param geomSet the geom set
     * @param geomSetLock the geom set lock
     * @param idList the id list
     * @param dmGeomIdMask the dm geom id mask
     * @return the sets the
     */
    public static Set<Geometry> findGeometrySetWithIds(Set<Geometry> geomSet, ReentrantLock geomSetLock, List<Long> idList,
            long dmGeomIdMask)
    {
        return findGeometrySetWithIds(geomSet, geomSetLock, idList, null, dmGeomIdMask);
    }

    /**
     * Find geometry set with ids.
     *
     * @param geomSet the geom set
     * @param geomSetLock the geom set lock
     * @param idList the id list of cache ids.
     * @param foundIdSet the found id set ( if passed in not null any found ids
     *            will be added to this set )
     * @param dmGeomIdMask the dm geom id mask
     * @return the sets the
     */
    public static Set<Geometry> findGeometrySetWithIds(Set<Geometry> geomSet, ReentrantLock geomSetLock, List<Long> idList,
            Set<Long> foundIdSet, long dmGeomIdMask)
    {
        Set<Long> idSet = New.set(idList);
        Set<Geometry> foundSet = New.set();
        geomSetLock.lock();
        try
        {
            for (Geometry geom : geomSet)
            {
                if (idSet.contains(Long.valueOf(geom.getDataModelId() & dmGeomIdMask)))
                {
                    if (foundIdSet != null)
                    {
                        foundIdSet.add(Long.valueOf(geom.getDataModelId() & dmGeomIdMask));
                    }
                    foundSet.add(geom);
                }
            }
        }
        finally
        {
            geomSetLock.unlock();
        }

        return foundSet;
    }

    /**
     * Find geometry with id.
     *
     * @param geomSet the geom set
     * @param geomSetLock the geom set lock
     * @param cacheId the id
     * @param dmGeomIdMask the dm geom id mask
     * @return the geometry
     */
    public static Geometry findGeometryWithId(Set<Geometry> geomSet, ReentrantLock geomSetLock, long cacheId, long dmGeomIdMask)
    {
        Geometry found = null;
        geomSetLock.lock();
        try
        {
            for (Geometry geom : geomSet)
            {
                if ((geom.getDataModelId() & dmGeomIdMask) == cacheId)
                {
                    found = geom;
                    break;
                }
            }
        }
        finally
        {
            geomSetLock.unlock();
        }
        return found;
    }

    /**
     * Instantiates a new geometry set util.
     */
    private GeometrySetUtil()
    {
        // Disallow
    }
}
