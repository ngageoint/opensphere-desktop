package io.opensphere.filterbuilder2.editor.model;

import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.filterbuilder.filter.v1.CommonFieldGroup;
import io.opensphere.filterbuilder.filter.v1.Criteria;

/**
 * Deals with converting between a special criterion (e.g. list, range) and the
 * underlying filter model.
 */
public abstract class SpecialCriterionConverter
{
    /** The operator that uses this model. */
    private Conditional myOperator;

    /** The logical operator for the group. */
    private Logical myGroupLogicalOperator;

    /** The comparison operator for the group. */
    private Conditional myGroupComparisonOperator;

    /** The regex to split the values. */
    private String mySplitRegex;

    /** The separator between values. */
    private String mySeparator;

    /** Create a subclass. */
    protected SpecialCriterionConverter()
    {
    }

    /**
     * Install values for instance variables.
     * @param op The operator that uses this model
     * @param logic The logical operator for the group
     * @param comp The comparison operator for the group
     * @param rex The regex to split the values
     * @param sep The separator between values
     */
    protected void setup (Conditional op, Logical logic, Conditional comp, String rex, String sep)
    {
        myOperator = op;
        myGroupLogicalOperator = logic;
        myGroupComparisonOperator = comp;
        mySplitRegex = rex;
        mySeparator = sep;
    }

    /**
     * Constructor.
     *
     * @param op The operator that uses this model
     * @param logic The logical operator for the group
     * @param comp The comparison operator for the group
     * @param rex The regex to split the values
     * @param sep The separator between values
     */
    public SpecialCriterionConverter(Conditional op, Logical logic, Conditional comp, String rex, String sep)
    {
        setup(op, logic, comp, rex, sep);
    }

    /**
     * Determines whether the given group can be handled.
     *
     * @param group the group
     * @return whether the group can be handled
     */
    public boolean accepts(CommonFieldGroup group)
    {
        return group.getLogicOperator() == myGroupLogicalOperator
                && group.getFieldComparisonOperator() == myGroupComparisonOperator;
    }

    /**
     * Adds filter criteria to the given group based on the tokens.
     *
     * @param group the common field group
     * @param tokens the value tokens
     */
    public abstract void addFilterCriteria(CommonFieldGroup group, String[] tokens);

    /**
     * Gets a Criteria from a CommonFieldGroup.
     *
     * @param group the group
     * @return the Criteria
     */
    public Criteria getCriteria(CommonFieldGroup group)
    {
        StringBuilder value = new StringBuilder();
        boolean isFirst = true;
        for (Criteria criterion : group.getCriteria())
        {
            if (!isFirst)
            {
                value.append(getSeparator());
            }
            isFirst = false;
            value.append(criterion.getValue());
        }

        return new Criteria(group.getField(), getOperator(), value.toString());
    }

    /**
     * Gets a CommonFieldGroup from a Criteria.
     *
     * @param criteria the criterion
     * @return the CommonFieldGroup
     */
    public CommonFieldGroup getGroup(Criteria criteria)
    {
        String field = criteria.getField();
        String value = criteria.getValue();
        String name = StringUtilities.concat(field, " ", value);

        CommonFieldGroup group = new CommonFieldGroup(getGroupLogicalOperator(), name);
        group.setField(field);
        group.setFieldComparisonOperator(getGroupComparisonOperator());
        addFilterCriteria(group, value.split(getSplitRegex()));
        return group;
    }

    /**
     * Gets the group comparison operator.
     *
     * @return the group comparison operator
     */
    public Conditional getGroupComparisonOperator()
    {
        return myGroupComparisonOperator;
    }

    /**
     * Gets the group logical operator.
     *
     * @return the group logical operator
     */
    public Logical getGroupLogicalOperator()
    {
        return myGroupLogicalOperator;
    }

    /**
     * Gets the operator.
     *
     * @return the operator
     */
    public Conditional getOperator()
    {
        return myOperator;
    }

    /**
     * Gets the separator.
     *
     * @return the separator
     */
    public String getSeparator()
    {
        return mySeparator;
    }

    /**
     * Gets the split regex.
     *
     * @return the split regex
     */
    public String getSplitRegex()
    {
        return mySplitRegex;
    }
}
