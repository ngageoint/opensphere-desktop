package io.opensphere.filterbuilder.filter;

/**
 * FilterComponentEvent.
 */
public class FilterComponentEvent
{
    /** The Constant GROUP_ADDED. */
    public static final String GROUP_ADDED = "Group.Added";

    /** The Constant COMMON_FIELD_GROUP_ADDED. */
    public static final String COMMON_FIELD_GROUP_ADDED = "Common.Field.Group.Added";

    /** The Constant GROUP_REMOVED. */
    public static final String GROUP_REMOVED = "Group.Removed";

    /** The Constant GROUP_NAME_CHANGED. */
    public static final String GROUP_NAME_CHANGED = "Group.Name.Changed";

    /** The Constant GROUP_OPERATOR_CHANGED. */
    public static final String GROUP_OPERATOR_CHANGED = "Group.Operator.Changed";

    /** The Constant CRITERIA_ADDED. */
    public static final String CRITERIA_ADDED = "Criteria.Added";

    /** The Constant CRITERIA_FIELD_CHANGED. */
    public static final String CRITERIA_FIELD_CHANGED = "Criteria.Field.Changed";

    /** The Constant CRITERIA_OPERATOR_CHANGED. */
    public static final String CRITERIA_OPERATOR_CHANGED = "Criteria.Operator.Changed";

    /** The Constant CRITERIA_VALUE_CHANGED. */
    public static final String CRITERIA_VALUE_CHANGED = "Criteria.Value.Changed";

    /** The Constant COMMON_FIELD_GROUP_OPERATOR_CHANGED. */
    public static final String COMMON_FIELD_GROUP_OPERATOR_CHANGED = "Common.Field.Group.Operator.Changed";

    /** The Constant COMMON_FIELD_GROUP_NAME_CHANGED. */
    public static final String COMMON_FIELD_GROUP_NAME_CHANGED = "Common.Field.Group.Name.Changed";

    /** The mySource of the event. */
    private final Object mySource;

    /** The property name of the event. */
    private final String myPropertyName;

    /**
     * Instantiates a new filter component event.
     *
     * @param pSource the mySource
     * @param pPropertyName the property name
     */
    public FilterComponentEvent(Object pSource, String pPropertyName)
    {
        super();
        mySource = pSource;
        myPropertyName = pPropertyName;
    }

    /**
     * Gets the property name.
     *
     * @return the myPropertyName
     */
    public String getPropertyName()
    {
        return myPropertyName;
    }

    /**
     * Gets the mySource.
     *
     * @return the mySource
     */
    public Object getSource()
    {
        return mySource;
    }
}
