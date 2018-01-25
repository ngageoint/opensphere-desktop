package io.opensphere.core.datafilter;

import java.util.List;

import io.opensphere.core.datafilter.DataFilterOperators.Logical;

/**
 * The Class DataFilterGroup.
 */
public interface DataFilterGroup extends DataFilterItem
{
    /**
     * Gets the criteria.
     *
     * @return the criteria
     */
    List<? extends DataFilterCriteria> getCriteria();

    /**
     * Gets the list of sub-groups.
     *
     * @return the groups
     */
    List<? extends DataFilterGroup> getGroups();

    /**
     * Gets the item at the specified index.
     *
     * @param idx the index.
     * @return the item at the index.
     * @throws IndexOutOfBoundsException the index out of bounds exception
     */
    DataFilterItem getItemAt(int idx) throws IndexOutOfBoundsException;

    /**
     * Gets the logic operator.
     *
     * @return the logic operator
     */
    Logical getLogicOperator();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Number of items. Total criteria + total groups.
     *
     * @return the number of criteria + groups.
     */
    int numItems();
}
