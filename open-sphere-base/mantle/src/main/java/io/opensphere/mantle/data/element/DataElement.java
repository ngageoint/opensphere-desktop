package io.opensphere.mantle.data.element;

import io.opensphere.core.api.Model;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.cache.DataElementCache;

/**
 * The Interface for a basic DataElement intended to be stored in the
 * DataRegistry and to be utilized on screen or by tools.
 */
public interface DataElement extends Model
{
    /** The Constant DATA_ELEMENT_ID_PROPERTY_NAME. */
    String DATA_ELEMENT_ORIGIN_ID_PROPERTY_NAME = "DataElelmentOrigin.ID";

    /** The Constant DATA_TYPE_INFO_KEY_PROPERTY_NAME. */
    String DATA_TYPE_INFO_KEY_PROPERTY_NAME = "DataTypeInfo.Key";

    /** The data type key property descriptor. */
    PropertyDescriptor<String> DATA_TYPE_KEY_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(DATA_TYPE_INFO_KEY_PROPERTY_NAME,
            String.class);

    /** The data element origin id property descriptor. */
    PropertyDescriptor<Long> DATA_ELEMENT_ORIGIN_ID_PROPERTY_DESCRIPTOR = new PropertyDescriptor<>(
            DATA_ELEMENT_ORIGIN_ID_PROPERTY_NAME, Long.class);

    /**
     * The Constant ID used for the element id if the attempted insert into the
     * system result in the element being filtered and not inserted.
     */
    long FILTERED = -2L;

    /** The FILTERED_AS_LONG. */
    Long FILTERED_AS_LONG = Long.valueOf(FILTERED);

    /**
     * Gets the id of the element within the {@link DataElementCache}.
     *
     * @return The id to use when interfacing with the {@link DataElementCache}.
     */
    long getIdInCache();

    /**
     * Gets the DataTypeInfo for this DataElement, could be null if it is not
     * attached to a DataTypeInfo.
     *
     * @return the DataTypeInfo or null if not attached.
     */
    DataTypeInfo getDataTypeInfo();

    /**
     * Gets the ID for this DataElement. Could be a hash code or a unique
     * number. Should not be generated on the fly and should not be mutable
     * depending on the contents of the DataElement
     *
     * Note: This is not the cache ID, it is an ID from the originating source.
     *
     * @return the id.
     */
    long getId();

    /**
     * Gets the MetaDataProvider for this {@link DataElement} if there is one.
     * May be null if there is no MetaDataProvider
     *
     * @return the {@link MetaDataProvider} or null if none
     */
    MetaDataProvider getMetaData();

    /**
     * Gets the {@link TimeSpan} for this DataElement.
     *
     * @return the {@link TimeSpan}
     */
    TimeSpan getTimeSpan();

    /**
     * Gets the visualization state.
     *
     * @return the visualization state
     */
    VisualizationState getVisualizationState();

    /**
     * Checks if is mappable.
     *
     * @return true, if is mappable
     */
    boolean isMappable();

    /**
     * Sets the id of the element within the {@link DataElementCache}.
     *
     * @param cacheId The id to use when interfacing with the
     *            {@link DataElementCache}.
     */
    void setIdInCache(long cacheId);

    /**
     * Creates a shallow copy of the data element, and sets the parent datatype
     * to the supplied value.
     *
     * @param datatype the datatype to use as the parent of the cloned instance.
     * @return a clone of the datatype.
     */
    DataElement cloneForDatatype(DataTypeInfo datatype);
}
