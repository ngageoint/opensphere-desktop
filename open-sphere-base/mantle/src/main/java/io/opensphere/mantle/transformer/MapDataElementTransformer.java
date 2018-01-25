package io.opensphere.mantle.transformer;

import java.util.Collection;

import gnu.trove.map.hash.TLongLongHashMap;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.MapDataElement;

/**
 * This interface helps transform MapDataElement geometry support information
 * into Geometry for the Geometry Registry.
 */
public interface MapDataElementTransformer
{
    /**
     * Adds the map data elements.
     *
     * @param dataElements the data elements
     * @param ids the ids
     */
    void addMapDataElements(Collection<? extends MapDataElement> dataElements, long[] ids);

    /**
     * Gets the element id for a given geometry id, if this transformer manages
     * the geometry, or -1 if not.
     *
     * Note: This is not an efficient call as it may have to do exhaustive
     * searching, do not call repeatedly or when time is critical.
     *
     * @param geomId the geometry id to test.
     * @return the element id or -1 if not found.
     */
    long getDataModelIdFromGeometryId(long geomId);

    /**
     * Gets the data type.
     *
     * @return the data type
     */
    DataTypeInfo getDataType();

    /**
     * Given a list of geometry ids, returns a map of geometry id to element id
     * for those gometryIds in the list that are managed by this transformer.
     * Geometry ids not managed by this transformer will not occur in the
     * resultant map.
     *
     * Note: This is not an efficient call as it may have to do exhaustive
     * searching, do not call repeatedly or when time is critical.
     *
     * @param geometryIds the list of geometry ids to check
     * @return a {@link TLongLongHashMap} of the found geometry ids to element
     *         ids.
     */
    TLongLongHashMap getElementIdsForGeometryIds(long[] geometryIds);

    /**
     * Checks to see if a particular this transformer is managing the provide
     * data element.
     *
     * @param cacheId the cache data model id to check.
     * @return true, if this transformer manages this cache id.
     */
    boolean hasGeometryForDataModelId(long cacheId);

    /**
     * Checks to see if a particular geometry id is managed by this transformer.
     *
     *
     * Note: This is not an efficient calls as it may have to do exhaustive
     * searching, do not call repeatedly or when time is critical.
     *
     * @param geomId the geometry id to check.
     * @return true, if this transformer manages this geometry id.
     */
    boolean hasGeometryId(long geomId);

    /**
     * Removes the map data elements that belong to this transformer.
     *
     * @param ids the ids
     */
    void removeMapDataElements(long[] ids);

    /**
     * Removes all geometries from the geometry registry for which this
     * transformer is responsible.
     */
    void shutdown();
}
