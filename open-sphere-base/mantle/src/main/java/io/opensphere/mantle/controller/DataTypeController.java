package io.opensphere.mantle.controller;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.element.DataElement;
import io.opensphere.mantle.data.element.DataElementProvider;
import io.opensphere.mantle.data.element.MapDataElement;
import io.opensphere.mantle.transformer.MapDataElementTransformer;

/**
 * The Interface DataTypeController.
 */
public interface DataTypeController
{
    /**
     * Adds the map data elements ( or data elements, can be either or both from
     * provider ) to the mantle.
     *
     * @param dep the {@link DataElementProvider}
     * @param boundingRegion - the bounding region for the insert ( or null if
     *            not available or geospatial indexing is not desired )
     * @param overallTimespan - the overall time span for all the elements being
     *            inserted ( or null if no temporal indexing is desired )
     * @param originator the requester of the add
     * @return the cache ids for the added elements ( note that if the value -1
     *         occurs within the array that the corresponding element was not
     *         added due to error, if -2 it means that the element was
     *         filtered.)
     * @throws IllegalStateException the illegal state exception
     */
    List<Long> addDataElements(DataElementProvider dep, Geometry boundingRegion, TimeSpan overallTimespan, Object originator)
        throws IllegalStateException;

    /**
     * Adds the data elements to the mantle.
     *
     * @param dti - the data type info
     * @param overallTimespan - the overall time span for all the elements being
     *            inserted ( or null if no temporal indexing is desired )
     * @param dataElements - the map data elements to add
     * @return the array of registry ids for the added elements ( note that if
     *         the value -1 occurs within the array that the corresponding
     *         element was not added due to error, if -2 it means that the
     *         element was filtered.)
     * @param originator the requester of the add
     * @throws IllegalStateException if the DataTypeInfo is not already in the
     *             controller.
     */
    long[] addDataElements(DataTypeInfo dti, TimeSpan overallTimespan, Collection<DataElement> dataElements, Object originator)
        throws IllegalStateException;

    /**
     * Adds the data type to the Mantle.
     *
     * @param source the source to add
     * @param category the category to add
     * @param dti the dti
     * @param originator the requester of the add
     */
    void addDataType(String source, String category, DataTypeInfo dti, Object originator);

    /**
     * Adds the map data elements to the mantle.
     *
     * @param dti - the data type info
     * @param boundingRegion - the bounding region for the insert ( or null if
     *            not available or geospatial indexing is not desired )
     * @param overallTimespan - the overall time span for all the elements being
     *            inserted ( or null if no temporal indexing is desired )
     * @param dataElements - the map data elements to add
     * @param originator the requester of the add
     * @return the array of registry ids for the added elements ( note that if
     *         the values -1 occurs within the array that the corresponding
     *         element was not added due to error, if -2 it means that the
     *         element was filtered.)
     * @throws IllegalStateException if the DataTypeInfo is not already in the
     *             controller.
     */
    long[] addMapDataElements(DataTypeInfo dti, Geometry boundingRegion, TimeSpan overallTimespan,
            Collection<? extends MapDataElement> dataElements, Object originator)
        throws IllegalStateException;

    /**
     * Gets the all data type info keys.
     *
     * @return the all data type info keys
     */
    Set<String> getAllDataTypeInfoKeys();

    /**
     * Gets the current or active {@link DataTypeInfo}.
     *
     * @return the current {@link DataTypeInfo} or null if none is set.
     */
    DataTypeInfo getCurrentDataType();

    /**
     * Gets the {@link DataTypeInfo} managed by the controller.
     *
     * @return the list of {@link DataTypeInfo}
     */
    List<DataTypeInfo> getDataTypeInfo();

    /**
     * Retrieves the DataTypeInfo for a geometry id, if that geometry is owned
     * by one of the data types managed by this controller.
     *
     * @param geomId the geometry id
     * @return the data type info for geometry id or null if not found.
     */
    DataTypeInfo getDataTypeInfoForGeometryId(long geomId);

    /**
     * Gets the data type info for type.
     *
     * @param dtiKey the data type info key
     * @return the data type info for type
     */
    DataTypeInfo getDataTypeInfoForType(String dtiKey);

    /**
     * Gets the element id for a given geometry id provided that geometry is
     * managed by this controller.
     *
     * Note: This is not an efficient operation and should not be performed
     * repeatedly.
     *
     * @param geomId the geometry id.
     * @return the element id for geometry id or -1 if not found.
     */
    long getElementIdForGeometryId(long geomId);

    /**
     * Gets the class responsible for drawing the elements on the globe for the
     * specified type.
     *
     * @param dtiKey The id of the type.
     * @return The transformer for the type.
     */
    MapDataElementTransformer getTransformerForType(String dtiKey);

    /**
     * Checks to see if the {@link DataTypeController} has the
     * {@link DataTypeInfo} for the specified data type info key..
     *
     * @param dtiKey data type info key
     * @return true, if contains the {@link DataTypeInfo}
     */
    boolean hasDataTypeInfoForTypeKey(String dtiKey);

    /**
     * Removes the data elements from the Mantle.
     *
     * @param dtiHint the DataTypeInfo if this is a single type being removed.
     * @param ids - the ids to remove or null for all.
     */
    void removeDataElements(DataTypeInfo dtiHint, long[] ids);

    /**
     * Removes the data type and all associated data elements from the Mantle.
     *
     * @param dti the dti
     * @param originator the requester of the add
     * @return true if the type was removed, false if not found.
     */
    boolean removeDataType(DataTypeInfo dti, Object originator);

    /**
     * Removes the data type and all associated data elements from the Mantle.
     *
     * @param dataTypeInfoKey the data type info key
     * @param originator the requester of the add
     * @return true if the type was removed, false if not found.
     */
    boolean removeDataType(String dataTypeInfoKey, Object originator);

    /**
     * Sets the current or active {@link DataTypeInfo}.
     *
     * @param dti the new current data type
     * @param source the originator of the request
     */
    void setCurrentDataType(DataTypeInfo dti, Object source);
}
