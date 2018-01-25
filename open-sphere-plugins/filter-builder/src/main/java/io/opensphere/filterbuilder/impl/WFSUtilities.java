package io.opensphere.filterbuilder.impl;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;

/** WFS utilities for filter builder. */
public final class WFSUtilities
{
    /**
     * Gets the conditional for property.
     *
     * @param property the property
     * @return the conditional for property
     */
    public static Conditional getConditionalForProperty(String property)
    {
        Conditional condition = null;
        if ("PropertyIsEqualTo".equalsIgnoreCase(property))
        {
            condition = Conditional.EQ;
        }
        else if ("PropertyIsLessThan".equalsIgnoreCase(property))
        {
            condition = Conditional.LT;
        }
        else if ("PropertyIsGreaterThan".equalsIgnoreCase(property))
        {
            condition = Conditional.GT;
        }
        else if ("PropertyIsLessThanOrEqualTo".equalsIgnoreCase(property))
        {
            condition = Conditional.LTE;
        }
        else if ("PropertyIsGreaterThanOrEqualTo".equalsIgnoreCase(property))
        {
            condition = Conditional.GTE;
        }
        else if ("PropertyIsNotEqualTo".equalsIgnoreCase(property))
        {
            condition = Conditional.NEQ;
        }
        return condition;
    }

    /** Private constructor. */
    private WFSUtilities()
    {
    }
}
