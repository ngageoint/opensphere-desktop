package io.opensphere.filterbuilder2.editor.model;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.filterbuilder.filter.v1.CommonFieldGroup;
import io.opensphere.filterbuilder.filter.v1.Criteria;

/**
 * Deals with converting between a list criterion and the underlying filter
 * model.
 */
public final class InListConverter extends SpecialCriterionConverter
{
    /** The regex to split the values. */
    public static final String SPLIT_REGEX = "\\s*,\\s*";

    /** The separator to append the values. */
    public static final String SEPARATOR = ", ";

    /** Instance for testing inclusion in a list. */
    public static final InListConverter IN_LIST = new InListConverter();

    /** Instance for testing exclusion in a list. */
    public static final InListConverter NOT_IN_LIST = new InListConverter();

    /** Instance for testing if something is like a list. */
    public static final InListConverter LIKE_LIST = new InListConverter();

    /** Instance for testing if something is not like a list. */
    public static final InListConverter NOT_LIKE_LIST = new InListConverter();

    static
    {
        IN_LIST.setup(Conditional.IN_LIST, Logical.OR, Conditional.EQ, SPLIT_REGEX, SEPARATOR);
        NOT_IN_LIST.setup(Conditional.NOT_IN_LIST, Logical.AND, Conditional.NEQ, SPLIT_REGEX, SEPARATOR);

        LIKE_LIST.setup(Conditional.LIKE_LIST, Logical.OR, Conditional.LIKE, SPLIT_REGEX, SEPARATOR);
        NOT_LIKE_LIST.setup(Conditional.NOT_LIKE_LIST, Logical.AND, Conditional.NOT_LIKE, SPLIT_REGEX, SEPARATOR);
    }

    /** Create. */
    private InListConverter()
    {
    }

    @Override
    public void addFilterCriteria(CommonFieldGroup group, String[] tokens)
    {
        for (String token : tokens)
        {
            group.addFilterCriteria(new Criteria(group.getField(), getGroupComparisonOperator(), token));
        }
    }
}
