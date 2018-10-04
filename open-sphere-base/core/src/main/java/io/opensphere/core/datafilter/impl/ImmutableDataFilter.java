package io.opensphere.core.datafilter.impl;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterGroup;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.ref.WeakReference;

/**
 * The Class ImmutableDataFilter.
 *
 * Creates an immutable deep copy of the provided DataFilter.
 */
@io.opensphere.core.util.Immutable
@net.jcip.annotations.Immutable
public class ImmutableDataFilter implements DataFilter
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The active. */
    private final boolean myActive;

    /** If this filter is from a state. */
    private final boolean myIsFromState;

    /** The Columns. */
    private final List<? extends String> myColumns;

    /**
     * The number of filters.
     */
    private final int myFilterCount;

    /** The filter description. */
    private final String myFilterDescription;

    /** The Filter group. */
    private final ImmutableDataFilterGroup myFilterGroup;

    /** The Filter source. */
    private final transient WeakReference<Object> myFilterSourceWR;

    /** The Name. */
    private final String myName;

    /** The server name. */
    private final String myServerName;

    /** The SQL like string. */
    private final String mySQLLikeString;

    /** The Type key. */
    private final String myTypeKey;

    /**
     * Instantiates a new immutable data filter.
     *
     * Copies all fields into an immutable deep copy.
     *
     * @param filter the filter
     * @param filterSource the filter source
     */
    public ImmutableDataFilter(DataFilter filter, Object filterSource)
    {
        Utilities.checkNull(filter, "filter");
        myFilterSourceWR = filterSource == null ? null : new WeakReference<Object>(filterSource);
        mySQLLikeString = filter.getSqlLikeString();
        myTypeKey = filter.getTypeKey();
        myColumns = filter.getColumns() == null ? null : New.unmodifiableList(filter.getColumns());
        myName = filter.getName();
        myFilterGroup = filter.getFilterGroup() == null ? null : new ImmutableDataFilterGroup(filter.getFilterGroup());
        myFilterCount = filter.getFilterCount();
        myActive = filter.isActive();
        myIsFromState = filter.isFromState();
        myServerName = filter.getServerName();
        myFilterDescription = filter.getFilterDescription();
    }

    /**
     * Instantiates a new immutable data filter.
     *
     * @param name the name
     * @param typeKey the type key
     * @param columns the columns
     * @param dfGroup the df group
     * @param filterSource the filter source
     * @param sqlLikeStr the sql like str
     */
    public ImmutableDataFilter(String name, String typeKey, List<? extends String> columns, DataFilterGroup dfGroup,
            Object filterSource, String sqlLikeStr)
    {
        myName = name;
        myTypeKey = typeKey;
        myColumns = columns == null ? null : New.unmodifiableList(columns);
        myFilterGroup = dfGroup == null ? null : new ImmutableDataFilterGroup(dfGroup);
        myFilterSourceWR = filterSource == null ? null : new WeakReference<Object>(filterSource);
        mySQLLikeString = sqlLikeStr;
        myFilterCount = 0;
        myActive = true;
        myIsFromState = false;
        myServerName = null;
        myFilterDescription = null;
    }

    @Override
    public DataFilter and(DataFilter filter)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataFilter applyFieldNameTransform(Function<String, String> transform)
    {
        ImmutableDataFilterGroup filterGroup = myFilterGroup.applyFieldNameTransform(transform);
        return new ImmutableDataFilter(myName, myTypeKey, myColumns, filterGroup, getFilterSource(), mySQLLikeString);
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
        ImmutableDataFilter other = (ImmutableDataFilter)obj;
        // @formatter:off
        return Objects.equals(myColumns, other.myColumns)
                && Objects.equals(myFilterGroup, other.myFilterGroup)
                && Objects.equals(myName, other.myName)
                && Objects.equals(mySQLLikeString, other.mySQLLikeString)
                && Objects.equals(myTypeKey, other.myTypeKey);
        // @formatter:on
    }

    @Override
    public List<? extends String> getColumns()
    {
        return myColumns;
    }

    @Override
    public int getFilterCount()
    {
        return myFilterCount;
    }

    @Override
    public String getFilterDescription()
    {
        return myFilterDescription;
    }

    @Override
    public DataFilterGroup getFilterGroup()
    {
        return myFilterGroup;
    }

    /**
     * Gets the filter source or null if none provide or weak reference has been
     * garbage collected.
     *
     * @return the filter source
     */
    public Object getFilterSource()
    {
        return myFilterSourceWR == null ? null : myFilterSourceWR.get();
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public String getServerName()
    {
        return myServerName;
    }

    @Override
    public String getSqlLikeString()
    {
        return mySQLLikeString;
    }

    @Override
    public String getTypeKey()
    {
        return myTypeKey;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myColumns);
        result = prime * result + HashCodeHelper.getHashCode(myFilterGroup);
        result = prime * result + HashCodeHelper.getHashCode(myName);
        result = prime * result + HashCodeHelper.getHashCode(mySQLLikeString);
        result = prime * result + HashCodeHelper.getHashCode(myTypeKey);
        return result;
    }

    @Override
    public boolean isActive()
    {
        return myActive;
    }

    @Override
    public boolean isFromState()
    {
        return myIsFromState;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append(this.getClass().getSimpleName());
        sb.append("\n  Name         : ").append(myName);
        sb.append("\n  TypeKey      : ").append(myTypeKey);
        sb.append("\n  SQLLikeString: ").append(mySQLLikeString);
        sb.append("\n  Columns      : ").append(myColumns == null ? "NULL" : myColumns.toString());
        sb.append("\n  Primary Group: ").append(myFilterGroup == null ? "NULL" : "").append('\n');
        if (myFilterGroup != null)
        {
            sb.append(myFilterGroup.toString());
        }
        return sb.toString();
    }
}
