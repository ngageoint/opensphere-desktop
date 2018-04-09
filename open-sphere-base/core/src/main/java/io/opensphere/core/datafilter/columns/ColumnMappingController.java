package io.opensphere.core.datafilter.columns;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import io.opensphere.core.util.lang.Pair;

/** Interface for column mappings. */
public interface ColumnMappingController
{
    /** The all layers string. */
    String ALL_LAYERS = "All Layers";

    /**
     * Checks to see if the specified set of columns in <i>fromType</i> can be
     * mapped to corresponding columns in each of <i>toTypes</i>.
     *
     * @param fromType the typeKey of the layer where <i>cols</i> are defined
     * @param cols the names of the columns that must be mapped
     * @param toTypes the typeKeys of the layers that must be supported
     * @return true if and only if all required mappings are supported
     */
    boolean supportsTypes(String fromType, Set<String> cols, List<String> toTypes);

    /**
     * Gets the columns that are mapped.
     *
     * @return the columns
     */
    Collection<String> getDefinedColumns();

    /**
     * Gets any shared mappings the given layers have with each other.
     *
     * @param layers The layer type keys, paired with a list of their columns,
     *            to check for mappings.
     * @return A map of the layers mapped to their original column names whose
     *         value is the defined column name.
     */
    Map<String, Map<String, String>> getDefinedColumns(List<Pair<String, List<String>>> layers);

    /**
     * Gets the defined column for the layer and layer column.
     *
     * @param layerKey the layer key
     * @param layerColumn the layer column
     * @return the defined column or null
     */
    String getDefinedColumn(String layerKey, String layerColumn);

    /**
     * Gets the layer column for the layer and defined column.
     *
     * @param layerKey the layer key
     * @param definedColumn the defined column
     * @return the layer column or null
     */
    String getLayerColumn(String layerKey, String definedColumn);

    /**
     * Gets the mapped column for the given source column and from/to data
     * types.
     *
     * @param sourceColumn the source column
     * @param sourceLayerKey the layer key of the source
     * @param targetLayerKey the layer key of the target
     * @return the mapped column
     */
    String getMappedColumn(String sourceColumn, String sourceLayerKey, String targetLayerKey);

    /**
     * Gets the mappings for the given defined column.
     *
     * @param definedColumn the defined column
     * @return the mappings
     */
    List<ColumnMapping> getMappings(String definedColumn);

    /**
     * Gets the description of the given column.
     *
     * @param definedColumn the defined column
     * @return the description, or null
     */
    String getDescription(String definedColumn);

    /**
     * Gets the data type of the given column.
     *
     * @param definedColumn the defined column
     * @return the data type, or null
     */
    String getType(String definedColumn);

    /**
     * Adds a listener for mapping changes.
     *
     * @param listener the listener
     */
    void addListener(Consumer<Void> listener);

    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    void removeListener(Consumer<Void> listener);
}
