package io.opensphere.mantle.data.element.mdfilter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.opengis.ogc._110.FilterType;

/**
 * A custom {@link FilterType} to match geospatial filters within a state file.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CustomFilterType")
@XmlRootElement(name = "filter")
public class CustomFilterType extends FilterType implements CustomFilter
{
    /** The Filter description. */
    @XmlAttribute(name = "description", required = false)
    private String myFilterDescription;

    /** The id of the filter. */
    @XmlAttribute(name = "id", required = false)
    private String myFilterId;

    /** The Filter type. */
    @XmlAttribute(name = "filterType", required = false)
    private String myFilterType;

    /** The Is active. */
    @XmlAttribute(name = "active", required = false)
    private boolean myIsActive;

    /** If the filter is from a state. */
    @XmlAttribute(name = "fromState")
    private boolean myIsFromState;

    /**
     * Whether this filter matches any(or) or all(and) other filters. TRUE is
     * match all
     */
    @XmlAttribute(name = "match", required = true)
    private String myMatch = "AND";

    /** The Server name. */
    @XmlAttribute(name = "serverName", required = false)
    private String myServerName;

    /** The title. */
    @XmlAttribute(name = "title", required = false)
    private String myTitle;

    /** The Url key. */
    @XmlAttribute(name = "type", required = false)
    private String myUrlKey;

    /**
     * Default constructor.
     */
    public CustomFilterType()
    {
    }

    /**
     * Constructs a custom filter type from an exisitng filter type.
     *
     * @param filterType The filter type to shallow copy values from.
     */
    public CustomFilterType(FilterType filterType)
    {
        setComparisonOps(filterType.getComparisonOps());
        setLogicOps(filterType.getLogicOps());
        setSpatialOps(filterType.getSpatialOps());
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
    public String getFilterId()
    {
        return myFilterId;
    }

    /**
     * Gets the filter type.
     *
     * @return the filter type
     */
    @Override
    public String getFilterType()
    {
        return myFilterType;
    }

    /**
     * Checks the match state. TRUE is match all(filters are anded).
     *
     * @return true, if is match
     */
    @Override
    public String getMatch()
    {
        return myMatch;
    }

    /**
     * Gets the server name.
     *
     * @return the server name
     */
    @Override
    public String getServerName()
    {
        return myServerName;
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    @Override
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * Gets the url key.
     *
     * @return the url key
     */
    @Override
    public String getUrlKey()
    {
        return myUrlKey;
    }

    @Override
    public boolean isFromState()
    {
        return myIsFromState;
    }

    @Override
    public void setFromState(boolean isFromState)
    {
        myIsFromState = isFromState;
    }

    /**
     * Checks if is active.
     *
     * @return true, if is active
     */
    @Override
    public boolean isActive()
    {
        return myIsActive;
    }

    /**
     * Sets the active.
     *
     * @param isActive the new active
     */
    @Override
    public void setActive(boolean isActive)
    {
        myIsActive = isActive;
    }

    /**
     * Sets the filter description.
     *
     * @param filterDescription the new filter description
     */
    @Override
    public void setFilterDescription(String filterDescription)
    {
        myFilterDescription = filterDescription;
    }

    /**
     * Mutator for the filterId.
     *
     * @param filterId The filterId to set.
     */
    public void setFilterId(String filterId)
    {
        myFilterId = filterId;
    }

    /**
     * Sets the filter type.
     *
     * @param filterType the new filter type
     */
    @Override
    public void setFilterType(String filterType)
    {
        myFilterType = filterType;
    }

    /**
     * Sets the match. TRUE is match all(filters are anded).
     *
     * @param match the new match
     */
    @Override
    public void setMatch(String match)
    {
        myMatch = match;
    }

    /**
     * Sets the server name.
     *
     * @param serverName the new server name
     */
    @Override
    public void setServerName(String serverName)
    {
        myServerName = serverName;
    }

    /**
     * Sets the title.
     *
     * @param title the new title
     */
    @Override
    public void setTitle(String title)
    {
        myTitle = title;
    }

    /**
     * Sets the url key.
     *
     * @param urlKey the new url key
     */
    @Override
    public void setUrlKey(String urlKey)
    {
        myUrlKey = urlKey;
    }
}
