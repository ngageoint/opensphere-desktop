package io.opensphere.mantle.data.cache;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.mantle.controller.DataTypeController;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.dynmeta.DynamicDataElementMetadataManager;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.VisualizationState;

/**
 * The Interface to the DataElementCache, a cache that stores.
 *
 * {@link DataElement} and {@link MapDataElement}s in a memory pool and may (
 * depending on configuration ) store elements to disk, the registry, or another
 * storage mechanism. Users can utilize this interface to query information from
 * the cache. The cache is populated by the {@link DataTypeController}, where
 * all data additions and removals can be made.
 */
public interface DataElementCache
{
    /**
     * Gets the all the cache element ids as array.
     *
     * @return the all ids as array
     */
    long[] getAllElementIdsAsArray();

    /**
     * Gets the all the cache element ids as list.
     *
     * @return the all ids as list
     */
    List<Long> getAllElementIdsAsList();

    /**
     * Gets the all the cache element ids as {@link RangedLongSet}.
     *
     * @return the all ids as ranged long set
     */
    RangedLongSet getAllElementIdsAsRangedLongSet();

    /**
     * Gets the DataTypeInfo key for the requested element.
     *
     * @param elementId the the element cache ids
     * @return the data type key or null if not found.
     */
    String getDataTypeInfoKey(long elementId);

    /**
     * Gets the DataTypeInfo keys for the requested elements.
     *
     * @param elementIds the element cache ids
     * @return the DataTypeInfo keys in a list with null values for ids not
     *         found
     */
    List<String> getDataTypeInfoKeys(List<Long> elementIds);

    /**
     * Gets the DataTypeInfo keys for the requested elements.
     *
     * @param elementIds the element cache ids
     * @return the DataTypeInfo keys in a list with null values for ids not
     *         found
     */
    List<String> getDataTypeInfoKeys(long[] elementIds);

    /**
     * Gets the set of DataTypeInfo keys for the requested elements.
     *
     * @param elementIds the element ids
     * @return the set of unique {@link DataTypeInfo} keys.
     */
    Set<String> getDataTypeInfoKeySet(List<Long> elementIds);

    /**
     * Gets a new direct access retriever for the specified data type. Note: Use
     * with extreme caution, this was put in here for the ListTool to provide
     * un-warmed retrieves of data for an entity that will need fast random
     * access to "select" records less frequently where the overhead of a query
     * is too high.
     *
     * @param type the type
     * @return the direct access retriever
     */
    DirectAccessRetriever getDirectAccessRetriever(DataTypeInfo type);

    /**
     * Gets the dynamic column manager.
     *
     * @return the dynamic column manager
     */
    DynamicDataElementMetadataManager getDynamicColumnManager();

    /**
     * Gets the count of elements for a specific data type.
     *
     * @param type the {@link DataTypeInfo} of interest
     * @return the element count for type
     */
    int getElementCountForType(DataTypeInfo type);

    /**
     * Gets all the cache element ids for a specified data type.
     *
     * @param type the {@link DataTypeInfo} of interest
     * @return the cache element ids as an array of long.
     */
    long[] getElementIdsForTypeAsArray(DataTypeInfo type);

    /**
     * Gets the all the cache element ids for a specific data type.
     *
     * @param type the {@link DataTypeInfo} of interest
     * @return the cache element ids as a {@link List} of {@link Long}
     */
    List<Long> getElementIdsForTypeAsList(DataTypeInfo type);

    /**
     * Gets all the cache element ids for a specified data type as a.
     *
     * @param type the {@link DataTypeInfo} of interest
     * @return the cache element ids as a {@link RangedLongSet}
     *         {@link RangedLongSet}.
     */
    RangedLongSet getElementIdsForTypeAsRangedLongSet(DataTypeInfo type);

    /**
     * Gets the preferred insert block size.
     *
     * @return the preferred insert block size, or -1 if no preference.
     */
    int getPreferredInsertBlockSize();

    /**
     * Gets the {@link TimeSpan} for the requested element.
     *
     * @param elementId the element cache id
     * @return the time span or null if not found.
     */
    TimeSpan getTimeSpan(long elementId);

    /**
     * Gets the {@link TimeSpan}s for the requested elements.
     *
     * @param elementIds the element cache ids
     * @return the TimeSpans as a list, with null values for ids not found.
     */
    List<TimeSpan> getTimeSpans(Collection<? extends Long> elementIds);

    /**
     * Gets the {@link TimeSpan}s for the requested elements.
     *
     * @param elementIds the element cache ids
     * @return the TimeSpans as a list, with null values for ids not found.
     */
    List<TimeSpan> getTimeSpans(long[] elementIds);

    /**
     * Gets the types with elements.
     *
     * @return the types with elements
     */
    Set<DataTypeInfo> getTypesWithElements();

    /**
     * Gets the {@link VisualizationState} for the requested element.
     *
     * @param elementId the element cache id
     * @return the {@link VisualizationState} or null if not found.
     */
    VisualizationState getVisualizationState(long elementId);

    /**
     * Gets the VisualizationStates.
     *
     * @param elementIds the element cache ids
     * @return the VisualizationStates with null values if id is not found
     */
    List<VisualizationState> getVisualizationStates(List<Long> elementIds);

    /**
     * Gets the VisualizationStates.
     *
     * @param elementIds the element cache ids
     * @return the VisualizationStates with null values if ids not found
     */
    List<VisualizationState> getVisualizationStates(long[] elementIds);

    /**
     * Query the cache with a set of data types as a filter.
     *
     * @param dtQuery the data type query.
     */
    void query(CacheDataTypeQuery dtQuery);

    /**
     * Query the cache with a set of element cache ids.
     *
     * @param idQuery the query to run
     */
    void query(CacheIdQuery idQuery);

    /**
     * Queries the cache contents.
     *
     * @param query the query to run
     */
    void query(CacheQuery query);
}
