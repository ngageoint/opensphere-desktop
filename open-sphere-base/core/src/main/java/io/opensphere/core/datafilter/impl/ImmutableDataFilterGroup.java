package io.opensphere.core.datafilter.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.opensphere.core.datafilter.DataFilterCriteria;
import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.datafilter.DataFilterItem;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.HashCodeHelper;

/**
 * The Class ImmutableDataFilterGroup.
 *
 * Wraps a DataFilterGroup and makes any returned lists immutable and returns
 * only immutable sub-types.
 */
public class ImmutableDataFilterGroup implements DataFilterGroup
{
    /** The Constant BREAKSTR. */
    private static final String BREAKSTR = "-------------------------------------------\n";

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The Criteria. */
    private final List<? extends ImmutableDataFilterCriteria> myCriteria;

    /** The Groups. */
    private final List<? extends ImmutableDataFilterGroup> myGroups;

    /** The Logical operator. */
    private final Logical myLogicalOperator;

    /** The Name. */
    private final String myName;

    /** The Num items. */
    private final int myNumItems;

    /** The Sql like string. */
    private final String mySqlLikeString;

    /**
     * Copy criteria list.
     *
     * @param dfcList the dfc list
     * @return the list
     */
    private static List<ImmutableDataFilterCriteria> copyCriteriaList(List<? extends DataFilterCriteria> dfcList)
    {
        List<ImmutableDataFilterCriteria> result;
        if (dfcList == null)
        {
            result = null;
        }
        else if (dfcList.isEmpty())
        {
            result = Collections.emptyList();
        }
        else
        {
            result = New.list(dfcList.size());
            for (DataFilterCriteria c : dfcList)
            {
                if (c != null)
                {
                    result.add(new ImmutableDataFilterCriteria(c));
                }
            }
            result = Collections.unmodifiableList(result);
        }
        return result;
    }

    /**
     * Copy group list.
     *
     * @param dfgList the dfg list
     * @return the list
     */
    private static List<ImmutableDataFilterGroup> copyGroupList(List<? extends DataFilterGroup> dfgList)
    {
        List<ImmutableDataFilterGroup> result;
        if (dfgList == null)
        {
            result = null;
        }
        else if (dfgList.isEmpty())
        {
            result = Collections.emptyList();
        }
        else
        {
            result = New.list(dfgList.size());
            for (DataFilterGroup g : dfgList)
            {
                if (g != null)
                {
                    result.add(new ImmutableDataFilterGroup(g));
                }
            }
            result = Collections.unmodifiableList(result);
        }
        return result;
    }

    /**
     * Instantiates a new immutable data filter group.
     *
     * @param dfg the dfg
     */
    public ImmutableDataFilterGroup(DataFilterGroup dfg)
    {
        this(dfg.getName(), dfg.getLogicOperator(), dfg.getCriteria(), dfg.getGroups(), dfg.getSqlLikeString());
    }

    /**
     * Instantiates a new immutable data filter group.
     *
     * @param name the name
     * @param operator the operator
     * @param criteriaList the criteria list
     * @param subgroups the subgroups
     * @param sqlLikeString the sql like string
     */
    public ImmutableDataFilterGroup(String name, Logical operator, List<? extends DataFilterCriteria> criteriaList,
            List<? extends DataFilterGroup> subgroups, String sqlLikeString)
    {
        Utilities.checkNull(operator, "operator");
        mySqlLikeString = sqlLikeString;
        myName = name;
        myLogicalOperator = operator;
        myGroups = copyGroupList(subgroups);
        myCriteria = copyCriteriaList(criteriaList);
        myNumItems = (myCriteria == null ? 0 : myCriteria.size()) + (myGroups == null ? 0 : myGroups.size());
        if (myNumItems == 0)
        {
            throw new IllegalArgumentException(
                    "DataFilterGroup \"" + myName + "\" has no evaluateable items items criteria or subgroups.");
        }
        if (myLogicalOperator == Logical.NOT && myNumItems > 1)
        {
            throw new IllegalArgumentException("DataFilterGroup \"" + myName
                    + "\" has a NOT operator but has more more than one evaluatable item (criteria + subgroups=\"" + myNumItems
                    + "\").");
        }
    }

    /**
     * Clone me, applying a function to the field names in my criteria.
     *
     * @param transform The field name transform.
     * @return The new group with the transform applied.
     */
    public ImmutableDataFilterGroup applyFieldNameTransform(Function<String, String> transform)
    {
        List<ImmutableDataFilterCriteria> criteriaList = New.list(myCriteria.size());
        for (ImmutableDataFilterCriteria criteria : myCriteria)
        {
            criteriaList.add(criteria.applyFieldNameTransform(transform));
        }
        List<ImmutableDataFilterGroup> groups = New.list(myGroups.size());
        for (ImmutableDataFilterGroup group : myGroups)
        {
            groups.add(group.applyFieldNameTransform(transform));
        }
        return new ImmutableDataFilterGroup(myName, myLogicalOperator, criteriaList, groups, mySqlLikeString);
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
        ImmutableDataFilterGroup other = (ImmutableDataFilterGroup)obj;
        // @formatter:off
        return Objects.equals(myCriteria, other.myCriteria)
                && Objects.equals(myGroups, other.myGroups)
                && Objects.equals(myLogicalOperator, other.myLogicalOperator)
                && Objects.equals(myName, other.myName)
                && Objects.equals(mySqlLikeString, other.mySqlLikeString);
        // @formatter:on
    }

    @Override
    public List<? extends DataFilterCriteria> getCriteria()
    {
        return myCriteria;
    }

    @Override
    public List<? extends DataFilterGroup> getGroups()
    {
        return myGroups;
    }

    @Override
    public DataFilterItem getItemAt(int idx) throws IndexOutOfBoundsException
    {
        if (idx < 0 || idx >= myNumItems)
        {
            throw new IndexOutOfBoundsException("Index " + idx + " is out of bounds.");
        }
        if (idx < myCriteria.size())
        {
            return myCriteria.get(idx);
        }
        else if (idx - myCriteria.size() < myGroups.size())
        {
            return myGroups.get(idx - myCriteria.size());
        }
        else
        {
            return null;
        }
    }

    @Override
    public Logical getLogicOperator()
    {
        return myLogicalOperator;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public String getSqlLikeString()
    {
        return mySqlLikeString;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myCriteria);
        result = prime * result + HashCodeHelper.getHashCode(myGroups);
        result = prime * result + HashCodeHelper.getHashCode(myLogicalOperator);
        result = prime * result + HashCodeHelper.getHashCode(myName);
        result = prime * result + HashCodeHelper.getHashCode(mySqlLikeString);
        return result;
    }

    @Override
    public int numItems()
    {
        return myGroups.size() + myCriteria.size();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(this.getClass().getSimpleName());
        sb.append("\n  Name     : ").append(myName);
        sb.append("\n  Operator : ").append(myLogicalOperator == null ? "NULL" : myLogicalOperator.toString());
        sb.append("\n  NumItems : ").append(myNumItems);
        sb.append("\n  SQLString: ").append(mySqlLikeString);
        sb.append("\n  Criteria : ").append(myCriteria == null ? "NULL" : Integer.toString(myCriteria.size())).append('\n');
        if (myCriteria != null && !myCriteria.isEmpty())
        {
            sb.append(BREAKSTR);
            int counter = 0;
            for (DataFilterCriteria group : myCriteria)
            {
                sb.append("Criteria[").append(counter).append("]\n");
                sb.append(group.toString());
                counter++;
            }
            sb.append(BREAKSTR);
        }
        sb.append("  Groups: ").append(myGroups == null ? "NULL" : Integer.toString(myGroups.size())).append('\n');
        if (myGroups != null && !myGroups.isEmpty())
        {
            sb.append(BREAKSTR);
            int counter = 0;
            for (DataFilterGroup group : myGroups)
            {
                sb.append("Group[").append(counter).append("]\n");
                sb.append(group.toString());
                counter++;
            }
            sb.append(BREAKSTR);
        }
        return sb.toString();
    }
}
