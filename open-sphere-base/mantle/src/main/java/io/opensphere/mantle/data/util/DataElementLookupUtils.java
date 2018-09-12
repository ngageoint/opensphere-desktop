package io.opensphere.mantle.data.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import gnu.trove.map.hash.TLongObjectHashMap;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.util.rangeset.RangedLongSet;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.data.element.MetaDataProvider;
import io.opensphere.mantle.data.element.VisualizationState;
import io.opensphere.mantle.data.geom.MapGeometrySupport;

/**
 * Assistant utility for finding components for a DataElement within a registry.
 */
public interface DataElementLookupUtils
{
    /**
     * Looks up the times ( if provided) for each of the specified data element
     * ids and filters them for overlap with the time span of interest.
     *
     * Note: any timeless data elements will match any timespan, and if
     * {@link TimeSpan}.Timeless is provided all data elements will match.
     *
     * @param tsOfInterest array of {@link TimeSpan}'s of interest to check
     * @param cacheIds the set of ids to be filtered
     * @return the filtered set of ids of elements that overlapped the desired
     *         timespan.
     */
    List<Long> filterIdsByTimeOfInterest(TimeSpan[] tsOfInterest, Collection<? extends Long> cacheIds);

    /**
     * Looks up the times ( if provided) for each of the specified data element
     * ids and filters them for overlap with the time span of interest.
     *
     * Note: any timeless data elements will match any timespan, and if
     * {@link TimeSpan}.Timeless is provided all data elements will match.
     *
     * @param tsOfInterest {@link TimeSpanList} of {@link TimeSpan}'s of
     *            interest to check
     * @param cacheIds the set of ids to be filtered
     * @return the filtered set of ids of elements that overlapped the desired
     *         timespan.
     */
    List<Long> filterIdsByTimeOfInterest(TimeSpanList tsOfInterest, Collection<? extends Long> cacheIds);

    /**
     * Retrieves all the parts of a DataElement and reconstitutes them into an
     * actual DataElement. The dtiHint and dataTypeInfoKeyHint can help prevent
     * multiple queries from running against the data model. They are used in
     * the dtiHint first, then the dataTypeInfoKeyHint second. If neither hint
     * is provided then they will be queried first so that the remainder of the
     * element can be retrieved and reformed.
     *
     * @param dataElementId the internal registry id of the element
     * @param dtiHint the {@link DataTypeInfo} for the point if known ( null if
     *            not known is okay )
     * @param dataTypeInfoKeyHint the key for the DataTypeInfo if known ( null
     *            if not known is okay )
     * @return the data element
     */
    DataElement getDataElement(long dataElementId, DataTypeInfo dtiHint, String dataTypeInfoKeyHint);

    /**
     * Gets the {@link DataElement} or {@link MapDataElement} data registry ids
     * for the given DataTypeInfo.
     *
     * @param dti the DataTypeInfo
     * @param tsOfInterest the {@link TimeSpan}'s of interest ( or null if no
     *            time filtering is desired ), if provided only those elements
     *            that overlap the span of interest will be returned. Note: that
     *            use of this parameter will slow down the lookup significantly.
     * @return the data element ids or empty array if none are found.
     */
    List<Long> getDataElementCacheIds(DataTypeInfo dti, TimeSpan... tsOfInterest);

    /**
     * Gets the {@link DataElement} or {@link MapDataElement} data registry ids
     * for the given DataTypeInfo.
     *
     * @param dti the DataTypeInfo
     * @param tsOfInterest the {@link TimeSpanList}'s of interest ( or null if
     *            no time filtering is desired ), if provided only those
     *            elements that overlap the span of interest will be returned.
     *            Note: that use of this parameter will slow down the lookup
     *            significantly.
     * @return the data element ids or empty array if none are found.
     */
    List<Long> getDataElementCacheIds(DataTypeInfo dti, TimeSpanList tsOfInterest);

    /**
     * Gets the {@link DataElement} or {@link MapDataElement} data registry ids
     * for the given DataTypeInfo key.
     *
     * @param dtiKey the DataTypeInfo key
     * @param tsOfInterest the {@link TimeSpan}'s of interest ( or null if no
     *            time filtering is desired ). Note: that use of this parameter
     *            will slow down the lookup significantly.
     * @return the data element ids
     */
    List<Long> getDataElementCacheIds(String dtiKey, TimeSpan... tsOfInterest);

    /**
     * Gets the {@link DataElement} or {@link MapDataElement} data registry ids
     * for the given DataTypeInfo key.
     *
     * @param dtiKey the DataTypeInfo key
     * @param tsOfInterest the {@link TimeSpanList}'s of interest ( or null if
     *            no time filtering is desired ). Note: that use of this
     *            parameter will slow down the lookup significantly.
     * @return the data element ids
     */
    List<Long> getDataElementCacheIds(String dtiKey, TimeSpanList tsOfInterest);

    /**
     * Retrieves the DataElements by id and adds them to the provided list. The
     * dtiHint and dataTypeInfoKeyHint can help prevent multiple queries from
     * running against the data model. They are used in the dtiHint first, then
     * the dataTypeInfoKeyHint second. If neither hint is provided then they
     * will be queried first so that the remainder of the element can be
     * retrieved and reformed.
     *
     * All of the id's requested must be of the same data type or an exception
     * will be generated.
     *
     * @param dataElementIds the data element ids to lookup
     * @param dtiHint the {@link DataTypeInfo} for the point if known ( null if
     *            not known is okay )
     * @param dataTypeInfoKeyHint the key for the DataTypeInfo if known ( null
     *            if not known is okay )
     * @param ignoreMapGeometrySupport the ignore map data elements map geometry
     *            support ( don't get the extra MGS parts )
     * @return the number of elements added to the list.
     * @throws DataElementLookupException if the dtiHint or dataTypeInfoKeyHint
     *             are the wrong type for any of the ids provided, or if the
     *             types retrieved are of different data types, or if the data
     *             type cannot be determined, or if all the ids cannot be
     *             retrieved.
     */
    List<DataElement> getDataElements(List<Long> dataElementIds, DataTypeInfo dtiHint, String dataTypeInfoKeyHint,
            boolean ignoreMapGeometrySupport)
        throws DataElementLookupException;

    /**
     * Gets all data elements for the given data type.
     *
     * @param type the data type
     * @return the data elements
     */
    List<DataElement> getDataElements(DataTypeInfo type);

    /**
     * Gets the DataTypeInfo for a given DataElement.
     *
     * @param dataElementId the internal registry id of the element
     * @return the DataTypeInfo or null id not found or no DataTypeInfo
     *         available.
     */
    DataTypeInfo getDataTypeInfo(long dataElementId);

    /**
     * Gets the DataTypeInfo for a given DataElement.
     *
     * @param dataElementId the internal registry id of the element
     * @param dtiKeyHint the data type info key ( if available, otherwise will
     *            be queried).
     * @return the DataTypeInfo or null id not found or no DataTypeInfo
     *         available.
     */
    DataTypeInfo getDataTypeInfo(long dataElementId, String dtiKeyHint);

    /**
     * Gets the DataTypeInfo key for the .
     *
     * @param dataElementId the internal registry id of the element
     * @return the data type info key or null if not available or id not found.
     */
    String getDataTypeInfoKey(long dataElementId);

    /**
     * Gets the DataTypeInfo keys for the requested ids.
     *
     * Will return a list with one to one correspondence between the
     * dataElementIds and the type keys, null will be inserted for ids with no
     * associated type.
     *
     * @param dataElementIds the data element ids
     * @return the data type info key
     */
    List<String> getDataTypeInfoKeys(List<Long> dataElementIds);

    /**
     * Gets the unique set of DataTypeInfo keys for the set of data element ids.
     *
     * @param dataElementIds the data element ids
     * @return the unique set of DataTypeInfo keys
     */
    Set<String> getDataTypeInfoKeySet(List<Long> dataElementIds);

    /**
     * Retrieves all the parts of a MapDataElement and reconstitutes them into
     * an actual MapDataElement. The dtiHint and dataTypeInfoKeyHint can help
     * prevent multiple queries from running against the data model. They are
     * used in the dtiHint first, then the dataTypeInfoKeyHint second. If
     * neither hint is provided then they will be queried first so that the
     * remainder of the element can be retrieved and reformed.
     *
     * @param dataElementId the internal registry id of the element
     * @param dtiHint the {@link DataTypeInfo} for the point if known ( null if
     *            not known is okay )
     * @param dataTypeInfoKeyHint the key for the DataTypeInfo if known ( null
     *            if not known is okay )
     * @param ignoreMapGeometrySupport the ignore map data elements map geometry
     *            support ( don't get the extra MGS parts )
     * @return the map data element
     */
    MapDataElement getMapDataElement(long dataElementId, DataTypeInfo dtiHint, String dataTypeInfoKeyHint,
            boolean ignoreMapGeometrySupport);

    /**
     * Retrieves all the parts of a MapDataElement and reconstitutes them into
     * an actual MapDataElement. The dtiHint and dataTypeInfoKeyHint can help
     * prevent multiple queries from running against the data model. They are
     * used in the dtiHint first, then the dataTypeInfoKeyHint second. If
     * neither hint is provided then they will be queried first so that the
     * remainder of the element can be retrieved and reformed.
     *
     * @param dataElementIds the internal registry ids of the elements to
     *            retrieve.
     * @param dtiHint the {@link DataTypeInfo} for the point if known ( null if
     *            not known is okay )
     * @param dataTypeInfoKeyHint the key for the DataTypeInfo if known ( null
     *            if not known is okay )
     * @param ignoreMapGeometrySupport the ignore map data elements map geometry
     *            support ( don't get the extra MGS parts )
     * @return the map data elements.
     * @throws DataElementLookupException if the dtiHint or dataTypeInfoKeyHint
     *             are the wrong type for any of the ids provided, or if the
     *             types retrieved are of different data types, or if the data
     *             type cannot be determined, or if all the ids cannot be
     *             retrieved.
     */
    List<MapDataElement> getMapDataElements(List<Long> dataElementIds, DataTypeInfo dtiHint, String dataTypeInfoKeyHint,
            boolean ignoreMapGeometrySupport)
        throws DataElementLookupException;

    /**
     * Gets the MapGeometrySupport for given list DataElements.
     *
     * @param dataElementIds the internal registry ids of the elements
     * @return the MapGeometrySupport or null if not found.
     */
    List<MapGeometrySupport> getMapGeometrySupport(List<Long> dataElementIds);

    /**
     * Gets the MapGeometrySupport for a given DataElement.
     *
     * @param dataElementId the internal registry id of the element
     * @return the MapGeometrySupport or null if not found.
     */
    MapGeometrySupport getMapGeometrySupport(long dataElementId);

    /**
     * Gets the MapGeometrySupport for given list DataElements.
     *
     * @param dataElementIds the internal registry ids of the elements
     * @return the MapGeometrySupport or null if not found.
     */
    List<MapGeometrySupport> getMapGeometrySupport(RangedLongSet dataElementIds);

    /**
     * Gets the meta data list for a given DataElement.
     *
     * @param dataElementId the internal registry id of the element
     * @return the meta data List or null if id not found.
     */
    List<Object> getMetaData(long dataElementId);

    /**
     * Retrieve meta data property values for a key name of a specific type of
     * data element.
     *
     * @param keyName the key name
     * @param dtiHint the {@link DataTypeInfo} for the point if known ( null if
     *            not known is okay )
     * @param dataTypeInfoKeyHint the key for the DataTypeInfo if known ( null
     *            if not known is okay )
     * @param maxSamples the max samples desired from the data type.
     * @param maxToQuery the max to query, the maximum number of elements to
     *            check ( -1 if no limit )
     * @return the list of property value samples
     * @throws DataElementLookupException if there is a problem determining the
     *             type or if the keyName is invalid.
     */
    List<Object> getMetaDataPropertySamples(String keyName, DataTypeInfo dtiHint, String dataTypeInfoKeyHint, int maxSamples,
            int maxToQuery)
        throws DataElementLookupException;

    /**
     * Retrieve meta data property values for a key name of a specific type of
     * data element.
     *
     * @param dataElementIds the data element ids to lookup
     * @param keyName the key name
     * @param dtiHint the {@link DataTypeInfo} for the point if known ( null if
     *            not known is okay )
     * @param dataTypeInfoKeyHint the key for the DataTypeInfo if known ( null
     *            if not known is okay )
     * @return the list of property values
     * @throws DataElementLookupException if there is a problem determining the
     *             type or if the keyName is invalid.
     */
    List<Object> getMetaDataPropertyValues(List<Long> dataElementIds, String keyName, DataTypeInfo dtiHint,
            String dataTypeInfoKeyHint)
        throws DataElementLookupException;

    /**
     * Gets a MetaDataProvider for a given DataElement.
     *
     * @param dataElementId the internal registry id of the element
     * @return the MetaDataProvider or null if the id is not in the registry or
     *         the DataTypeInfo cannot be located for the data element.
     */
    MetaDataProvider getMetaDataProvider(long dataElementId);

    /**
     * Gets the origin ID for the specified data element.
     *
     * @param dataElementId the internal registry id of the element
     * @return the origin id or -1 if not found.
     */
    Long getOriginId(long dataElementId);

    /**
     * Gets a map of cache ids to origin ids for the requested DataElements. The
     * map will contain null values for all un-found registry ids.
     *
     * If the query fails the map will be empty.
     *
     * @param dataElementIds the data element ids to use to get the origin ids
     * @return the Map of cache id to origin ids.
     */
    TLongObjectHashMap<Long> getOriginIds(List<Long> dataElementIds);

    /**
     * Gets the {@link TimeSpan} for the specified data element.
     *
     * @param dataElementId internal registry id of the element
     * @return the {@link TimeSpan} or null if not found or it has no timespan.
     */
    TimeSpan getTimespan(long dataElementId);

    /**
     * Gets the {@link TimeSpan}s for the specified data elements.
     *
     * @param dataElementIds internal registry ids of the elements
     * @return the list of {@link TimeSpan} elements with no time span will be
     *         null entries in the map.
     */
    List<TimeSpan> getTimespans(Collection<? extends Long> dataElementIds);

    /**
     * Gets the {@link VisualizationState} for a given DataElement.
     *
     * @param dataElementId the internal registry id of the element
     * @return the VisualizationState or null if not found.
     */
    VisualizationState getVisualizationState(long dataElementId);

    /**
     * Gets the VisualizationState for given list DataElements.
     *
     * @param dataElementIds the internal registry ids of the elements
     * @return the VisualizationState or null if not found.
     */
    List<VisualizationState> getVisualizationStates(List<Long> dataElementIds);

    /**
     * Gets the VisualizationState for given list DataElements.
     *
     * @param dataElementIds the internal registry ids of the elements
     * @return the VisualizationState or null if not found.
     */
    List<VisualizationState> getVisualizationStates(long[] dataElementIds);

    /**
     * Gets the VisualizationState for given list DataElements.
     *
     * @param dataElementIds the internal registry ids of the elements
     * @return the VisualizationState or null if not found.
     */
    List<VisualizationState> getVisualizationStates(RangedLongSet dataElementIds);
}
