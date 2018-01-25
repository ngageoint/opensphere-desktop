package io.opensphere.core.datafilter;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;

/**
 * The Class Criteria.
 */
public interface DataFilterCriteria extends DataFilterItem
{
    /**
     * Gets the comparison operator.
     *
     * @return the comparison operator
     */
    Conditional getComparisonOperator();

    /**
     * Gets the field.
     *
     * @return the field
     */
    String getField();

    /**
     * Gets the value.
     *
     * @return the value
     */
    String getValue();
}
