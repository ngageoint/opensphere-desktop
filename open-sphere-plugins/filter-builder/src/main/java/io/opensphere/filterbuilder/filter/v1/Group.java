package io.opensphere.filterbuilder.filter.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.filterbuilder.filter.FilterComponentEvent;
import io.opensphere.filterbuilder.filter.FilterComponentListener;

/**
 * Group: A filter group can be thought of as the contents of a set of
 * parenthesis in an SQL like statement.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "group", propOrder = { "myName", "myOperator", "myCriteria", "myCommonFieldGroups", "myStdGroups" })
@SuppressWarnings("PMD.GodClass")
public class Group extends FilterItem implements DataFilterGroup
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The groups. */
    @XmlElement(name = "group", required = true)
    private List<Group> myStdGroups;

    /** The Common field groups. */
    @XmlElement(name = "commonFieldGroup")
    private List<CommonFieldGroup> myCommonFieldGroups;

    /** The criteria. */
    @XmlElement(name = "criteria", required = true)
    private List<Criteria> myCriteria;

    /** The Operator. */
    @XmlAttribute(name = "intraGroupOperator", required = true)
    private Logical myOperator;

    /** The name. */
    @XmlAttribute(name = "name", required = true)
    private String myName;

    /** The Filter component listener. */
    private FilterComponentListener myFilterCompListener;

    /**
     * Instantiates a new group.
     */
    public Group()
    {
        this(Logical.AND);
    }

    /**
     * Instantiates a new group.
     *
     * @param oper the operator
     */
    public Group(Logical oper)
    {
        this(oper, "Group");
    }

    /**
     * Instantiates a new group.
     *
     * @param pName the name
     */
    public Group(String pName)
    {
        this(Logical.AND, pName);
    }

    /**
     * Instantiates a new group.
     *
     * @param oper the operator
     * @param pName the name
     */
    private Group(Logical oper, String pName)
    {
        myStdGroups = new ArrayList<>();
        myCriteria = new ArrayList<>();
        myCommonFieldGroups = new ArrayList<>();

        setLogicOperator(oper);
        myName = pName;
    }

    /**
     * Adds the common field filter group.
     *
     * @param pGroup the group
     */
    public void addCommonFieldFilterGroup(CommonFieldGroup pGroup)
    {
        pGroup.setFilterComponentListener(myFilterCompListener);
        myCommonFieldGroups.add(pGroup);

        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.COMMON_FIELD_GROUP_ADDED));
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

    /**
     * Adds the filter group.
     *
     * @param grp the group
     */
    public void addFilterGroup(Group grp)
    {
        grp.setFilterComponentListener(myFilterCompListener);
        myStdGroups.add(grp);

        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.GROUP_ADDED));
    }

    @Override
    public Group clone()
    {
        Group clone = (Group)super.clone();
        clone.myStdGroups = StreamUtilities.map(myStdGroups, group -> group.clone());
        clone.myCommonFieldGroups = StreamUtilities.map(myCommonFieldGroups, commonFieldGroup -> commonFieldGroup.clone());
        clone.myCriteria = StreamUtilities.map(myCriteria, criteria -> criteria.clone());
        clone.myFilterCompListener = null;
        return clone;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Group other = (Group)obj;
        // @formatter:off
        return EqualsHelper.equals(
                myCriteria, other.myCriteria,
                myStdGroups, other.myStdGroups,
                myCommonFieldGroups, other.myCommonFieldGroups,
                myOperator, other.myOperator,
                myName, other.myName);
        // @formatter:on
    }

    /**
     * Gets the common field groups.
     *
     * @return the common field groups
     */
    public List<CommonFieldGroup> getCommonFieldGroups()
    {
        return myCommonFieldGroups;
    }

    @Override
    public List<Criteria> getCriteria()
    {
        return myCriteria;
    }

    @Override
    public List<DataFilterGroup> getGroups()
    {
        List<DataFilterGroup> result = new ArrayList<>();
        result.addAll(myStdGroups);
        result.addAll(myCommonFieldGroups);
        return result;
    }

    @Override
    public FilterItem getItemAt(int idx) throws IndexOutOfBoundsException
    {
        if (idx < myCriteria.size())
        {
            return myCriteria.get(idx);
        }
        else if (idx - myCriteria.size() < getGroups().size())
        {
            return (FilterItem)getGroups().get(idx - myCriteria.size());
        }
        else
        {
            return null;
        }
    }

    @Override
    public Logical getLogicOperator()
    {
        return myOperator;
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

        if (numItems() > 0)
        {
            boolean addParens = addParens();
            if (myOperator.equals(Logical.NOT))
            {
                if (hasSqlOperationError())
                {
                    sb.append(myOperator.name());
                    sb.append("( SQL ERROR )");
                }
                else
                {
                    if (addParens)
                    {
                        sb.append(myOperator.name());
                        sb.append('(');
                        if (!myCriteria.isEmpty())
                        {
                            sb.append(myCriteria.get(0).getSqlLikeString());
                        }
                        else
                        {
                            sb.append(getGroups().get(0).getSqlLikeString());
                        }
                        sb.append(')');
                    }
                    else
                    {
                        sb.append(myOperator.name());
                        if (!myCriteria.isEmpty())
                        {
                            sb.append(myCriteria.get(0).getSqlLikeString());
                        }
                        else
                        {
                            sb.append(getGroups().get(0).getSqlLikeString());
                        }
                    }
                }
            }
            else
            {
                int grpStartIdx = 0;
                if (addParens)
                {
                    sb.append("( ");
                }
                if (!myCriteria.isEmpty())
                {
                    sb.append(myCriteria.get(0).getSqlLikeString());
                }
                else
                {
                    sb.append(getGroups().get(0).getSqlLikeString());
                    grpStartIdx = 1;
                }

                for (int i = 1; i < myCriteria.size(); i++)
                {
                    if (myCriteria.get(i).getSqlLikeString().length() > 0)
                    {
                        sb.append(' ');
                        sb.append(myOperator.name());
                        sb.append(' ');
                        sb.append(myCriteria.get(i).getSqlLikeString());
                    }
                }

                for (int i = grpStartIdx; i < getGroups().size(); i++)
                {
                    if (getGroups().get(i).getSqlLikeString().length() > 0)
                    {
                        sb.append(' ');
                        sb.append(myOperator.name());
                        sb.append(' ');
                        sb.append(getGroups().get(i).getSqlLikeString());
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
     * Gets the standard groups.
     *
     * @return the standard groups
     */
    public List<Group> getStdGroups()
    {
        return myStdGroups;
    }

    @Override
    public int hashCode()
    {
        /* this doesn't really accomplish anything, but there are issues with
         * creating a more robust hashCode method. Some of them are related to
         * how the tree handles drag drop events and data. */
        final int prime = 31;
        int result = 1;
        result = prime * result;
        return result;
    }

    /**
     * Checks to see if this {@link Group} is meets the minimum requirements to
     * be valid. The minimum requirements are that it contains one or more
     * children. The children can be all {@link Group}s, all {@link Criteria},
     * or some combination. Additionally each of the children must be valid.
     *
     * @see Group#isValid()
     * @see Criteria#isValid()
     * @return true, if is valid
     */
    public boolean isValid()
    {
        if (numItems() < 1)
        {
            return false;
        }

        for (Group g : myStdGroups)
        {
            if (!g.isValid())
            {
                return false;
            }
        }

        for (CommonFieldGroup cfg : myCommonFieldGroups)
        {
            if (!cfg.isValid())
            {
                return false;
            }
        }

        for (Criteria c : myCriteria)
        {
            if (!c.isValid())
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public int numItems()
    {
        return myCriteria.size() + getGroups().size();
    }

    /**
     * Removes the filter group.
     *
     * @param grp the group
     */
    public void removeFilterGroup(Group grp)
    {
        grp.setFilterComponentListener(null);
        myStdGroups.remove(grp);
        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.GROUP_REMOVED));
    }

    /**
     * Sets the filter component listener.
     *
     * @param pListener the new filter component listener
     */
    public void setFilterComponentListener(FilterComponentListener pListener)
    {
        setMyFilterComponentListener(pListener);

        for (Group g : myStdGroups)
        {
            g.setFilterComponentListener(myFilterCompListener);
        }

        for (Criteria c : myCriteria)
        {
            c.setFilterComponentListener(myFilterCompListener);
        }

        for (CommonFieldGroup cfg : myCommonFieldGroups)
        {
            cfg.setFilterComponentListener(myFilterCompListener);
        }
    }

    /**
     * Sets the logic operator.
     *
     * @param operator the new logic operator
     */
    public final void setLogicOperator(Logical operator)
    {
        myOperator = operator;
        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.GROUP_OPERATOR_CHANGED));
    }

    /**
     * Sets the name.
     *
     * @param pName the new name
     */
    public void setName(String pName)
    {
        myName = pName;
        fireFilterComponentEvent(new FilterComponentEvent(this, FilterComponentEvent.GROUP_NAME_CHANGED));
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
     * Determines if the SQL string needs parentheses.
     *
     * @return true, if parentheses are needed
     */
    private boolean addParens()
    {
        if (myOperator.equals(Logical.NOT))
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
            if (numItems() > 1)
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
        return myOperator.equals(Logical.NOT) && numItems() > 1;
    }

    /**
     * Sets the my filter component listener.
     *
     * @param pListener the new my filter component listener
     */
    private void setMyFilterComponentListener(FilterComponentListener pListener)
    {
        myFilterCompListener = pListener;
    }
}
