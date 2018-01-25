package io.opensphere.filterbuilder.filter.v1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.concurrent.GuardedBy;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import io.opensphere.core.datafilter.DataFilter;
import io.opensphere.core.datafilter.DataFilterOperators.Logical;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.filterbuilder.FilterBuilderPlugin;
import io.opensphere.filterbuilder.filter.FilterComponentEvent;
import io.opensphere.filterbuilder.filter.FilterComponentListener;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * Filter: an object to represent a data filter. Used by the {@link FilterBuilderPlugin} plugin.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Filter", namespace = "3.6.6", propOrder = { "myActive", "myFilterDescription", "mySource", "myOtherSources",
    "myName", "myGroup" })
@SuppressWarnings("PMD.GodClass")
public class Filter extends FilterItem implements DataFilter, Comparable<Filter>, FilterComponentListener
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The active flag. */
    @XmlAttribute(name = "active", required = true)
    private boolean myActive;

    /** The columns used in the creation of the filter. */
    @SuppressWarnings("PMD.LooseCoupling")
    private ArrayList<String> myColumns;

    /** The filter change listeners. */
    private transient List<FilterChangeListener> myFilterChangeListeners;

    /**
     * The number of filters that comprise this filter.
     */
    private int myFilterCount = 1;

    /** The Filter description. */
    @XmlAttribute(name = "description", required = false)
    private String myFilterDescription;

    /**
     * The primary group of the filter. Every {@link Filter} must have one. It cannot be deleted!
     */
    @XmlElement(name = "group", required = true)
    private Group myGroup;

    /**
     * Whether this filter matches any(or) or all(and) other filters. TRUE is match all
     */
    @XmlAttribute(name = "match", required = true)
    private String myMatch = "AND";

    /** The name of the filter. */
    @XmlAttribute(name = "name", required = true)
    private String myName;

    /** The source of the data. */
    @XmlElement(name = "source", required = true)
    private Source mySource;

    /** The other sources of the data. */
    @GuardedBy("this")
    @XmlElement(name = "otherSource")
    private final List<Source> myOtherSources = new LinkedList<>();

    /** Whether the filter is virtual. */
    private volatile boolean myVirtual;

    /**
     * The parent filter of this instance.
     */
    private volatile Filter myParent;

    /**
     * Instantiates a new filter.
     */
    public Filter()
    {
        this(null, null, null, false);
    }

    /**
     * Instantiates a new filter.
     *
     * @param pSource the source
     */
    public Filter(DataTypeInfo pSource)
    {
        this(null, Source.fromDataType(pSource), null, false);
        myOtherSources.add(mySource);
    }

    /**
     * Instantiates a new filter.
     *
     * @param pName the name
     * @param pSource the source
     */
    public Filter(String pName, DataTypeInfo pSource)
    {
        this(pName, Source.fromDataType(pSource), null, false);
        myOtherSources.add(mySource);
    }

    /**
     * Instantiates a new filter.
     *
     * @param pName the name
     * @param pSource the source
     */
    public Filter(String pName, Source pSource)
    {
        this(pName, pSource, null, false);
    }

    /**
     * Instantiates a new filter.
     *
     * @param pName the name
     * @param pSource the source
     * @param pGroup the group
     * @param pActive the active
     */
    public Filter(String pName, Source pSource, Group pGroup, boolean pActive)
    {
        myName = pName != null ? pName : "Filter_" + Calendar.getInstance().getTimeInMillis();
        myGroup = pGroup != null ? pGroup : new Group(myName);
        mySource = pSource == null ? new Source() : pSource;
        myActive = pActive;
        init();
    }

    /**
     * Gets the value of the {@link #myParent} field.
     *
     * @return the value stored in the {@link #myParent} field.
     */
    public Filter getParent()
    {
        return myParent;
    }

    /**
     * Sets the value of the {@link #myParent} field.
     *
     * @param pParent the value to store in the {@link #myParent} field.
     */
    public void setParent(Filter pParent)
    {
        myParent = pParent;
    }

    /**
     * Removes this filter instance from the parent, if the parent has been supplied.
     */
    public void removeFromParent()
    {
        if (myParent == null)
        {
            return;
        }
        String t = mySource.getTypeKey();
        myParent.myOtherSources.removeIf(s -> s.getTypeKey().equals(t));
    }

    /**
     * Adds the filter change listener.
     *
     * @param pFilterChangeListener the filter change listener
     */
    public void addFilterChangeListener(FilterChangeListener pFilterChangeListener)
    {
        synchronized (myFilterChangeListeners)
        {
            if (!myFilterChangeListeners.contains(pFilterChangeListener))
            {
                myFilterChangeListeners.add(pFilterChangeListener);
            }
        }
    }

    @Override
    public DataFilter and(DataFilter filter)
    {
        Filter combinedFilter = new Filter(getSource().getTypeKey() + " Master Filter", getSource());
        combinedFilter.getFilterGroup().setLogicOperator(Logical.AND);
        combinedFilter.getFilterGroup().addFilterGroup(getFilterGroup());
        combinedFilter.getFilterGroup().addFilterGroup(((Filter)filter).getFilterGroup());
        combinedFilter.setFilterCount(getFilterCount() + filter.getFilterCount());

        return combinedFilter;
    }

    @Override
    public DataFilter applyFieldNameTransform(Function<String, String> transform)
    {
        Filter f = copy();

        // apply transform to columns
        if (f.myColumns != null)
        {
            List<String> oldCols = f.myColumns;
            f.myColumns = new ArrayList<>();
            oldCols.forEach(c -> f.myColumns.add(transform.apply(c)));
        }

        // apply transform to the criteria
        fieldTransform(f.myGroup, transform);

        return f;
    }

    /**
     * Transforms the supplied filter group using the supplied transform function.
     *
     * @param g the group to transform.
     * @param tf the function to apply to the supplied group.
     */
    private static void fieldTransform(Group g, Function<String, String> tf)
    {
        g.getCriteria().forEach(c -> c.setField(tf.apply(c.getField())));
        g.getCommonFieldGroups().forEach(c -> c.setField(tf.apply(c.getField())));
        g.getGroups().forEach(subG -> fieldTransform((Group)subG, tf));
    }

    /**
     * Generates a deep copy of this instance.
     *
     * @return a deep copy of this instance.
     */
    private Filter copy()
    {
        Filter f = new Filter();
        f.myActive = myActive;
        f.myName = myName;
        f.myFilterDescription = myFilterDescription;
        f.myFilterCount = myFilterCount;
        f.myMatch = myMatch;
        f.mySource = mySource.copy();
        myOtherSources.stream().forEach(s -> f.myOtherSources.add(s.copy()));
        if (myColumns != null)
        {
            f.myColumns = new ArrayList<>(myColumns);
        }
        f.myGroup = myGroup.clone();
        return f;
    }

    @Override
    public int compareTo(Filter o)
    {
        return getName().compareTo(o.getName());
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
        Filter other = (Filter)obj;
        //@formatter:off
        return Objects.equals(myGroup, other.myGroup)
                && Objects.equals(myName, other.myName)
                && Objects.equals(mySource, other.mySource);
        //@formatter:on
    }

    @Override
    public void filterComponentChanged(FilterComponentEvent pEvent)
    {
        fireFilterChangeEvent(
                new FilterChangeEvent(this, pEvent.getSource(), FilterChangeEvent.STRUCTURE_CHANGED, pEvent.getPropertyName()));
    }

    @Override
    public List<String> getColumns()
    {
        if (myColumns == null)
        {
            myColumns = new ArrayList<>();
        }
        return myColumns;
    }

    /**
     * Gets the data type display name.
     *
     * @return the data type display name
     */
    public String getDataTypeDisplayName()
    {
        return mySource.getTypeDisplayName();
    }

    @Override
    public int getFilterCount()
    {
        return myFilterCount;
    }

    /**
     * Gets the filter description.
     *
     * @return the filter description
     */
    @Override
    public String getFilterDescription()
    {
        return myFilterDescription;
    }

    @Override
    public Group getFilterGroup()
    {
        return myGroup;
    }

    /**
     * Gets the fields defined in this filter.
     *
     * @return the fields defined in this filter.
     */
    public Set<String> getFields()
    {
        Set<String> fields = new TreeSet<>();
        recurseFields(myGroup, fields);
        return fields;
    }

    /**
     * Recurses over the children defined in the supplied group, adding the fields from the group to the supplied set.
     *
     * @param g the group over which to traverse.
     * @param fields the container in which to collect the field names.
     */
    private static void recurseFields(Group g, Set<String> fields)
    {
        g.getCriteria().stream().forEach(cr -> fields.add(cr.getField()));
        g.getCommonFieldGroups().stream().forEach(cf -> fields.add(cf.getField()));
        g.getStdGroups().stream().forEach(grp -> recurseFields(grp, fields));
    }

    /**
     * Gets the match.
     *
     * @return the match
     */
    public String getMatch()
    {
        return myMatch;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the other sources.
     *
     * @return the other sources
     */
    public synchronized List<Source> getOtherSources()
    {
        return myOtherSources;
    }

    /**
     * Find the resident Source matching the specified typeKey, if it exists.
     *
     * @param typeKey the desired data type key
     * @return the Source that was found, if any, or null
     */
    public Source srcForType(String typeKey)
    {
        for (Source s : myOtherSources)
        {
            if (typeKey.equals(s.getTypeKey()))
            {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets the set of types supported by this filter.
     *
     * @return the set of types supported by this filter.
     */
    public List<String> getSupportedTypes()
    {
        return myOtherSources.stream().map(s -> s.getTypeKey()).collect(Collectors.toList());
    }

    /**
     * Gets the server name.
     *
     * @return the server name
     */
    @Override
    public String getServerName()
    {
        return mySource.getServerName();
    }

    /**
     * Gets the source.
     *
     * @return the source
     */
    public Source getSource()
    {
        return mySource;
    }

    @Override
    public String getSqlLikeString()
    {
        String output = myGroup.getSqlLikeString();
        return output;
    }

    @Override
    public String getTypeKey()
    {
        return mySource.getTypeKey();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myGroup);
        result = prime * result + HashCodeHelper.getHashCode(myName);
        result = prime * result + HashCodeHelper.getHashCode(mySource);
        return result;
    }

    /**
     * Checks if is active.
     *
     * @return true, if is active
     */
    @Override
    public boolean isActive()
    {
        return myActive;
    }

    /**
     * Checks to see if the filter is a valid filter. In order for the filter to be valid its main {@link Group} must be valid.
     *
     * @see Group#isValid()
     * @return true, if is valid
     */
    public boolean isValid()
    {
        return myGroup.isValid();
    }

    /**
     * Gets the virtual.
     *
     * @return the virtual
     */
    public boolean isVirtual()
    {
        return myVirtual;
    }

    /**
     * Removes the filter change listener.
     *
     * @param pFilterChangeListener the filter change listener
     */
    public void removeFilterChangeListener(FilterChangeListener pFilterChangeListener)
    {
        synchronized (myFilterChangeListeners)
        {
            myFilterChangeListeners.remove(pFilterChangeListener);
        }
    }

    /**
     * Sets the active. Really? What idiot writes something like "Sets the active", anyway?
     *
     * @param active the set active
     * @param pSource the source
     */
    public void setActive(boolean active, Object pSource)
    {
        if (myVirtual)
        {
            mySource.setActive(active);
        }

        if (myActive != active)
        {
            myActive = active;
            fireFilterChangeEvent(new FilterChangeEvent(this, pSource, FilterChangeEvent.ACTIVE_STATE));
        }
    }

    /**
     * Sets the columns.
     *
     * @param pColumns the new columns
     */
    public void setColumns(Collection<String> pColumns)
    {
        if (myColumns == null)
        {
            myColumns = new ArrayList<>(pColumns);
        }
        else
        {
            myColumns.clear();
            myColumns.addAll(pColumns);
            myColumns.trimToSize();
        }
        fireFilterChangeEvent(new FilterChangeEvent(this, this, FilterChangeEvent.STRUCTURE_CHANGED));
    }

    /**
     * Sets the number of filters that comprise this filter.
     *
     * @param count The filter count.
     */
    public void setFilterCount(int count)
    {
        myFilterCount = count;
    }

    /**
     * Sets the filter description.
     *
     * @param description the new filter description
     */
    public void setFilterDescription(String description)
    {
        myFilterDescription = description;
        fireFilterChangeEvent(new FilterChangeEvent(this, this, FilterChangeEvent.FILTER_DESCRIPTION_CHANGE));
    }

    /**
     * Sets the filter group.
     *
     * @param grp the new filter group
     */
    public void setFilterGroup(Group grp)
    {
        myGroup.setFilterComponentListener(null);
        myGroup = grp;
        myGroup.setFilterComponentListener(this);
        fireFilterChangeEvent(new FilterChangeEvent(this, this, FilterChangeEvent.STRUCTURE_CHANGED));
    }

    /**
     * Sets the match.
     *
     * @param match the new match
     */
    public void setMatch(String match)
    {
        myMatch = match;
    }

    /**
     * Sets the name.
     *
     * @param pName the new name
     */
    public void setName(String pName)
    {
        myName = pName;
        fireFilterChangeEvent(new FilterChangeEvent(this, this, FilterChangeEvent.NAME_CHANGE));
    }

    /**
     * Sets the virtual.
     *
     * @param virtual the virtual
     */
    public void setVirtual(boolean virtual)
    {
        myVirtual = virtual;
    }

    @Override
    public String toString()
    {
        return myName;
    }

    /**
     * Fire filter change event.
     *
     * @param pFilterChangeEvent the filter change event
     */
    private void fireFilterChangeEvent(FilterChangeEvent pFilterChangeEvent)
    {
        for (FilterChangeListener fcl : myFilterChangeListeners)
        {
            fcl.filterChanged(pFilterChangeEvent);
        }
    }

    /**
     * Initializes fields.
     */
    private void init()
    {
        myGroup.setFilterComponentListener(this);
        myFilterChangeListeners = new ArrayList<>();
    }
}
