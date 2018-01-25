package io.opensphere.mantle.data.impl;

import java.util.List;
import java.util.Set;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Interface GroupCategorizer.
 */
public interface GroupCategorizer
{
    /**
     * Gets the all categories produced by this categorizer. This will not be
     * called until all the individual data groups have been categorized so it
     * may be generated dynamically.
     *
     * @return the all categories
     */
    List<String> getAllCategories();

    /**
     * Gets the one or more group categories for the data group info.
     *
     * @param dgi the {@link DataGroupInfo}
     * @return the {@link Set} of categories or (null or empty set for no
     *         category).
     */
    Set<String> getGroupCategories(DataGroupInfo dgi);

    /**
     * Gets one or more type categories for the data type info..
     *
     * @param dti the {@link DataTypeInfo}
     * @return the the {@link Set} of categories or (null or empty set for no
     *         category).
     */
    Set<String> getTypeCategories(DataTypeInfo dti);
}
