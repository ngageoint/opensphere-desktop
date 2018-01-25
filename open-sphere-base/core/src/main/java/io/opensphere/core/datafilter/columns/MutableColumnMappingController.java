package io.opensphere.core.datafilter.columns;

/** Interface for column mappings. */
public interface MutableColumnMappingController extends ColumnMappingController
{
    /**
     * Adds a mapping.
     *
     * @param definedColumn the defined column
     * @param layerKey the layer key
     * @param layerColumn the layer column
     * @param overwrite whether to overwrite an existing value
     */
    void addMapping(String definedColumn, String layerKey, String layerColumn, boolean overwrite);

    /**
     * Checks whether the associate method (q.v.) would succeed without breaking any rules. Strictly speaking, this is not a
     * mutator method, but one would only call it if one intended to make changes.
     *
     * @param type1 the name of the first type to test.
     * @param col1 the name of the column for which to search.
     * @param type2 the name of the second type to test.
     * @param col2 the name of the column for which to search.
     * @return true if and only if the mapping is allowed
     */
    boolean canAssociate(String type1, String col1, String type2, String col2);

    /**
     * Adds an association between the two specified fields. A "definedColumn" is not provided because one or both of the fields
     * may already belong to a defined equivalence class, whose name(s) may not be known. If a new equivalence must be defined, it
     * is given the name <i>col1</i>. In case the fields belong to separate equivalence classes, the two will be merged. <br>
     * <br>
     * Note: this method succeeds forcibly, ignoring more subtle considerations such as a prohibition on having two fields from
     * the same type deemed equivalent. It is recommended to use the method canAssociate (q.v.) before calling this one to ensure
     * consistency.
     *
     * @param type1 the name of the first type to associate.
     * @param col1 the name of the column for to associate.
     * @param type2 the name of the second type to associate.
     * @param col2 the name of the column for to associate.
     * @param valType type to assign to a new equivalence, if necessary
     */
    void associate(String type1, String col1, String type2, String col2, String valType);

    /**
     * Renames a definedColumn.
     *
     * @param definedColumn the defined column
     * @param newColumn the new defined column
     */
    void rename(String definedColumn, String newColumn);

    /**
     * Sets the description of the given column.
     *
     * @param definedColumn the defined column
     * @param description the description
     * @param overwrite whether to overwrite an existing value
     */
    void setDescription(String definedColumn, String description, boolean overwrite);

    /**
     * Sets the data type of the given column.
     *
     * @param definedColumn the defined column
     * @param type the data type
     * @param overwrite whether to overwrite an existing value
     */
    void setType(String definedColumn, String type, boolean overwrite);

    /**
     * Clears all mappings for the given defined column.
     *
     * @param definedColumn the defined column
     */
    void clearMappings(String definedColumn);

    /**
     * Completely removes all data for the given defined column.
     *
     * @param definedColumn the defined column
     */
    void remove(String definedColumn);
}
