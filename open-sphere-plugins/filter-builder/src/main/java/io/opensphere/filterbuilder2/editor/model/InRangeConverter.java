package io.opensphere.filterbuilder2.editor.model;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.filterbuilder.filter.v1.CommonFieldGroup;
import io.opensphere.filterbuilder.filter.v1.Criteria;

/**
 * Deals with converting between a range criterion and the underlying filter
 * model.
 */
public class InRangeConverter extends SpecialCriterionConverter
{
    /** The regex to split the values. */
    public static final String SPLIT_REGEX = " - ";

    /** The separator to append the values. */
    public static final String SEPARATOR = " - ";

    /**
     * Constructor.
     */
    public InRangeConverter()
    {
        super(Conditional.BETWEEN, Logical.AND, null, SPLIT_REGEX, SEPARATOR);
    }

    @Override
    public boolean accepts(CommonFieldGroup group)
    {
        return group.getLogicOperator() == getGroupLogicalOperator() && (group.getCriteria().isEmpty()
                || group.getCriteria().size() == 2 && group.getCriteria().get(0).getComparisonOperator() == Conditional.GTE
                        && group.getCriteria().get(1).getComparisonOperator() == Conditional.LTE);
    }

    @Override
    public void addFilterCriteria(CommonFieldGroup group, String[] tokens)
    {
        if (tokens.length == 2)
        {
            group.addFilterCriteria(new Criteria(group.getField(), Conditional.GTE, tokens[0]));
            group.addFilterCriteria(new Criteria(group.getField(), Conditional.LTE, tokens[1]));
        }
    }
}
