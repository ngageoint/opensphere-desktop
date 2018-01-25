package io.opensphere.merge.controller;

import java.util.List;

import io.opensphere.core.Toolbox;
import io.opensphere.core.cache.SimpleSessionOnlyCacheDeposit;
import io.opensphere.core.cache.util.PropertyDescriptor;
import io.opensphere.core.data.util.DataModelCategory;
import io.opensphere.core.data.util.SimpleQuery;
import io.opensphere.merge.model.MergedDataRow;

/**
 * Contains constants and utility methods used to interact with the data
 * registry.
 */
public final class DataRegistryUtils
{
    /**
     * The property descriptor for merged data.
     */
    public static final PropertyDescriptor<MergedDataRow> MERGED_PROP_DESCRIPTOR = new PropertyDescriptor<>("mergedRow",
            MergedDataRow.class);

    /**
     * The instance of this class.
     */
    private static final DataRegistryUtils ourInstance = new DataRegistryUtils();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static DataRegistryUtils getInstance()
    {
        return ourInstance;
    }

    /**
     * Not constructible.
     */
    private DataRegistryUtils()
    {
    }

    /**
     * The data model category to use when depositing or querying
     * {@link MergedDataRow} for the given layer id.
     *
     * @param layerId The id of the merged layer.
     * @return The category to retrieve or deposit {@link MergedDataRow} for the
     *         given layer.
     */
    public DataModelCategory getMergeDataCategory(String layerId)
    {
        return mergeCat(layerId);
    }

    /**
     * Same as getMergeDataCategory (q.v.), but statically accessible.
     *
     * @param layerId the id of the merged layer
     * @return the DataRegistry category
     */
    public static DataModelCategory mergeCat(String layerId)
    {
        return new DataModelCategory("merged", MergedDataRow.class.getName(), layerId);
    }

    /**
     * Specific method for depositing the MergedDataRow type.
     *
     * @param tools the system Toolbox
     * @param layerId the name of the layer
     * @param data the data to be deposited
     */
    public static void deposit(Toolbox tools, String layerId, List<MergedDataRow> data)
    {
        deposit(tools, mergeCat(layerId), MERGED_PROP_DESCRIPTOR, data);
    }

    /**
     * Specific method for retrieving the MergedDataRow type.
     *
     * @param tools the system Toolbox
     * @param layerId the name of the layer
     * @return the retrieved data
     */
    public static List<MergedDataRow> query(Toolbox tools, String layerId)
    {
        return query(tools, mergeCat(layerId), MERGED_PROP_DESCRIPTOR);
    }

    /**
     * Specific method for removing data associated with merged layers.
     *
     * @param tools the system Toolbox
     * @param layerId the name of the layer
     */
    public static void delete(Toolbox tools, String layerId)
    {
        delete(tools, mergeCat(layerId));
    }

    /**
     * Generic method for inserting (session only) stuff into the DataRegistry.
     *
     * @param tools the system Toolbox
     * @param cat DataModelCategory
     * @param prop PropertyDescriptor
     * @param data the data to be deposited
     */
    public static <R> void deposit(Toolbox tools, DataModelCategory cat, PropertyDescriptor<R> prop, List<R> data)
    {
        tools.getDataRegistry().addModels(new SimpleSessionOnlyCacheDeposit<>(cat, prop, data));
    }

    /**
     * Generic method for retrieving stuff from the DataRegistry (local cache).
     *
     * @param tools the system Toolbox
     * @param cat DataModelCategory
     * @param prop PropertyDescriptor
     * @return the retrieved data
     */
    public static <R> List<R> query(Toolbox tools, DataModelCategory cat, PropertyDescriptor<R> prop)
    {
        SimpleQuery<R> q = new SimpleQuery<>(cat, prop);
        tools.getDataRegistry().performLocalQuery(q);
        return q.getResults();
    }

    /**
     * Generic method for removing stuff from the DataRegistry.
     *
     * @param tools the system Toolbox
     * @param cat DataModelCategory
     */
    public static void delete(Toolbox tools, DataModelCategory cat)
    {
        tools.getDataRegistry().removeModels(cat, false);
    }
}
