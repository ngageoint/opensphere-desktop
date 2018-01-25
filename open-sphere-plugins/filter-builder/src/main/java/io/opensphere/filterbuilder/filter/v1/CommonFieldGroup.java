package io.opensphere.filterbuilder.filter.v1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterItem;
import io.opensphere.core.datafilter.DataFilterOperators.Conditional;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.filterbuilder.filter.FilterComponentEvent;
import io.opensphere.filterbuilder.filter.FilterComponentListener;

/**
 * The Class CommonFieldGroup.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "commonFieldGroup", propOrder = { "myCriteria", "myFieldComparisonOperator", "myField", "myGroupOperator",
    "myName" })
@SuppressWarnings("PMD.GodClass")
public class CommonFieldGroup extends FilterItem implements DataFilterGroup
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Field comparison operator. */
    @XmlAttribute(name = "criteriaOperator", required = true)
    private Conditional myFieldComparisonOperator;

    /** The Field. */
    @XmlAttribute(name = "field", required = true)
    private String myField;

    /** The Name. */
    @XmlAttribute(name = "name", required = true)
    private String myName;

    /** The Groups. */
    private List<CommonFieldGroup> myGroups;

    /** The Criteria. */
    @XmlElement(name = "criteria", required = true)
    private List<Criteria> myCriteria;

    /** The Group operator. */
    @XmlAttribute(name = "intraGroupOperator", required = true)
    private Logical myGroupOperator;

    /** The Filter component listener. */
    private FilterComponentListener myFilterCompListener;

    /**
     * Instantiates a new group.
     */
    public CommonFieldGroup()
    {
        this(Logical.OR);
    }

    /**
     * Instantiates a new group.
     *
     * @param pOper the operator
     * @param pName the name
     */
    public CommonFieldGroup(Logical pOper, String pName)
    {
        myName = pName;
        myGroups = new ArrayList<>();
        myCriteria = new ArrayList<>();
        setLogicOperator(pOper);

        myFieldComparisonOperator = Conditional.EQ;
    }

    /**
     * Instantiates a new group.
     *
     * @param oper the operator
     */
    private CommonFieldGroup(Logical oper)
    {
        this(oper, "Group");
    }

    /**
     * Adds the filter criteria.
     *
     * @param crit the criteria
     */
    public void addFilterCriteria(Criteria crit)
    {
        crit.setFilterComponentListener(myFilterCompListener);
        myCriteria.add(crit);
        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.CRITERIA_ADDED));
    }

    @Override
    public CommonFieldGroup clone()
    {
        CommonFieldGroup clone = (CommonFieldGroup)super.clone();
        clone.myGroups = StreamUtilities.map(myGroups, group -> group.clone());
        clone.myCriteria = StreamUtilities.map(myCriteria, criteria -> criteria.clone());
        myFilterCompListener = null;
        return clone;
    }

    @Override
    public List<Criteria> getCriteria()
    {
        return myCriteria;
    }

    /**
     * Gets the field.
     *
     * @return the field
     */
    public String getField()
    {
        return myField;
    }

    /**
     * Gets the field comparison operator.
     *
     * @return the field comparison operator
     */
    public Conditional getFieldComparisonOperator()
    {
        return myFieldComparisonOperator;
    }

    /**
     * Gets the field values.
     *
     * @return the field values
     */
    public String getFieldValues()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<Criteria> iter = getCriteria().iterator();
        while (iter.hasNext())
        {
            sb.append(iter.next().getValue());
            if (iter.hasNext())
            {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    @Override
    public List<CommonFieldGroup> getGroups()
    {
        return myGroups;
    }

    @Override
    public DataFilterItem getItemAt(int pIdx) throws IndexOutOfBoundsException
    {
        return myCriteria.get(pIdx);
    }

    @Override
    public Logical getLogicOperator()
    {
        return myGroupOperator;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public String getSqlLikeString()
    {
        StringBuilder sb = new StringBuilder();

        if (getGroups().size() + myCriteria.size() > 0)
        {
            boolean addParens = addParens();
            if (myGroupOperator.equals(Logical.NOT))
            {
                if (hasSqlOperationError())
                {
                    sb.append(myGroupOperator.name());
                    sb.append("( SQL ERROR )");
                }
                else
                {
                    if (addParens)
                    {
                        sb.append(myGroupOperator.name());
                        sb.append('(');
                        if (!myCriteria.isEmpty())
                        {
                            sb.append(myCriteria.get(0).getSqlLikeString());
                        }
                        sb.append(')');
                    }
                    else
                    {
                        sb.append(myGroupOperator.name());
                        if (!myCriteria.isEmpty())
                        {
                            sb.append(myCriteria.get(0).getSqlLikeString());
                        }
                    }
                }
            }
            else
            {
                if (addParens)
                {
                    sb.append("( ");
                }
                if (!myCriteria.isEmpty())
                {
                    sb.append(myCriteria.get(0).getSqlLikeString());
                }

                for (int i = 1; i < myCriteria.size(); i++)
                {
                    if (myCriteria.get(i).getSqlLikeString().length() > 0)
                    {
                        sb.append(' ');
                        sb.append(myGroupOperator.name());
                        sb.append(' ');
                        sb.append(myCriteria.get(i).getSqlLikeString());
                    }
                }

                if (addParens)
                {
                    sb.append(" )");
                }
            }
        }
        return sb.toString();
    }

    /**
     * Checks if is valid.
     *
     * @return true, if is valid
     */
    public boolean isValid()
    {
        if (getCriteria().isEmpty())
        {
            return false;
        }

        if (StringUtils.isBlank(myField))
        {
            return false;
        }

        if (myFieldComparisonOperator == null)
        {
            return false;
        }

        if (StringUtils.isBlank(getName()))
        {
            return false;
        }

        for (Criteria c : getCriteria())
        {
            if (!c.getComparisonOperator().equals(myFieldComparisonOperator))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public int numItems()
    {
        return myCriteria.size();
    }

    /**
     * Sets the field.
     *
     * @param pField the new field
     */
    public void setField(String pField)
    {
        myField = pField;
        for (Criteria c : getCriteria())
        {
            c.setField(pField);
        }
    }

    /**
     * Sets the field comparison operator.
     *
     * @param pOperator the new field comparison operator
     */
    public void setFieldComparisonOperator(Conditional pOperator)
    {
        myFieldComparisonOperator = pOperator;
        for (Criteria c : getCriteria())
        {
            c.setComparisonOperator(pOperator);
        }
    }

    /**
     * Sets the filter component listener.
     *
     * @param pListener the new filter component listener
     */
    public void setFilterComponentListener(FilterComponentListener pListener)
    {
        myFilterCompListener = pListener;
        for (Criteria c : myCriteria)
        {
            c.setFilterComponentListener(pListener);
        }
    }

    /**
     * Sets the logic operator.
     *
     * @param operator the new logic operator
     */
    public final void setLogicOperator(Logical operator)
    {
        myGroupOperator = operator;
        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.COMMON_FIELD_GROUP_OPERATOR_CHANGED));
    }

    /**
     * Sets the name.
     *
     * @param pName the new name
     */
    public void setName(String pName)
    {
        myName = pName;
        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.COMMON_FIELD_GROUP_NAME_CHANGED));
    }

    @Override
    public String toString()
    {
        if (!StringUtils.isBlank(myName))
        {
            return getName() + " (" + getLogicOperator() + ")";
        }
        return getClass().getSimpleName() + "@" + hashCode();
    }

    /**
     * Adds the parenthesis.
     *
     * @return true, if successful
     */
    private boolean addParens()
    {
        if (myGroupOperator.equals(Logical.NOT))
        {
            if (!hasSqlOperationError())
            {
                if (!myCriteria.isEmpty())
                {
                    return true;
                }
                return !getGroups().get(0).getSqlLikeString().startsWith("(");
            }
        }
        else
        {
            if (getGroups().size() + myCriteria.size() > 1)
            {
                return true;
            }
        }
        return true;
    }

    /**
     * Fire filter component event.
     *
     * @param pEvent the event
     */
    private void fireFilterComponentEvent(FilterComponentEvent pEvent)
    {
        if (myFilterCompListener != null)
        {
            myFilterCompListener.filterComponentChanged(pEvent);
        }
    }

    /**
     * Checks for sql operation error.
     *
     * @return true, if successful
     */
    private boolean hasSqlOperationError()
    {
        return myGroupOperator.equals(Logical.NOT) && myCriteria.size() + getGroups().size() > 1;
    }
}
