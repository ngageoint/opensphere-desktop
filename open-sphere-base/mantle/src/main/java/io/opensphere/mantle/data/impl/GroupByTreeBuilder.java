package io.opensphere.mantle.data.impl;

import java.util.Comparator;
import java.util.function.Predicate;

import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.DataGroupInfoGroupByUtility.TreeOptions;

/**
 * The Interface GroupByTreeBuilder.
 */
public interface GroupByTreeBuilder
{
    /**
     * Gets the name of the type of group by this tree builder performs.
     *
     * @return The name of the group by.
     */
    String getGroupByName();

    /**
     * Gets the group categorizer.
     *
     * @return the group categorizer
     */
    GroupCategorizer getGroupCategorizer();

    /**
     * Gets the group comparator.
     *
     * @return the group comparator
     */
    Comparator<? super DataGroupInfo> getGroupComparator();

    /**
     * Sets the group comparator.
     *
     * @param comparator The group comparator.
     */
    void setGroupComparator(Comparator<? super DataGroupInfo> comparator);

    /**
     * Gets the predicate used to match data groups for the tree.
     *
     * @return the predicate used to match data groups for the tree.
     */
    Predicate<DataGroupInfo> getDataCategoryFilter();

    /**
     * Gets the group filter.
     *
     * @return the group filter
     */
    Predicate<DataGroupInfo> getGroupFilter();

    /**
     * Gets the tree options.
     *
     * @return the tree options
     */
    TreeOptions getTreeOptions();

    /**
     * Gets the group comparator.
     *
     * @return the group comparator
     */
    Comparator<? super DataTypeInfo> getTypeComparator();
}
