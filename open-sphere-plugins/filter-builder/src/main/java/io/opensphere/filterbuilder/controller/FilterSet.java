package io.opensphere.filterbuilder.controller;

import java.util.LinkedList;
import java.util.List;

import io.opensphere.core.datafilter.DataFilterOperators;
import io.opensphere.filterbuilder.filter.v1.Filter;

/**
 * This class is a relatively compact way of keeping a set of Filters together with some other useful fields. Refer to
 * FilterBuilderService for uses of this class.
 */
public class FilterSet
{
    /**
     * The name of the type key used in the filter set.
     */
    private final String myTypeKey;

    /**
     * The filters managed by the filter set.
     */
    private final List<Filter> myFilters = new LinkedList<>();

    /**
     * The operation applied to the filters in the set.
     */
    private DataFilterOperators.Logical myFilterOp;

    /**
     * Construct a FilterSet for the specified type key.
     *
     * @param t the type key associated with this FilterSet's relevant layer
     */
    public FilterSet(String t)
    {
        myTypeKey = t;
    }

    /**
     * Get the type key associated with this FilterSet.
     *
     * @return the type key
     */
    public String getTypeKey()
    {
        return myTypeKey;
    }

    /**
     * Retrieve a reference to the list of members of this set.
     *
     * @return the list of Filters
     */
    public List<Filter> getFilters()
    {
        return myFilters;
    }

    /**
     * Get the logical operator that will be applied to the members of the set for purposes of filtering data.
     *
     * @return the logical operator
     */
    public DataFilterOperators.Logical getLogicOp()
    {
        return myFilterOp;
    }

    /**
     * Set the logical operator that will be applied to the members of the set for purposes of filtering data. Only the
     * associative operators "AND" and "OR" are allowed. This method throws an IllegalArgumentException if the argument is unary
     * "NOT".
     *
     * @param op the new logical operator
     */
    public void setFilterOp(DataFilterOperators.Logical op)
    {
        if (op == DataFilterOperators.Logical.NOT)
        {
            throw new IllegalArgumentException("only AND or OR is allowed");
        }
        myFilterOp = op;
    }
}
