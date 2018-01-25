package io.opensphere.server.filter.config.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.server.util.ServerConstants;

/**
 * The Class ServerFilter.
 */
@XmlRootElement(name = "Filter")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerFilter
{
    /** My filter name. */
    @XmlAttribute(name = "filterName")
    private String myFilterNameStr;

    /** My server name. */
    @XmlAttribute(name = "serverName")
    private String myServerNameStr;

    /** My WMS URL. */
    @XmlAttribute(name = "serverWMSURL")
    private String myServerWMSURL;

    /** My WFS URL. */
    @XmlAttribute(name = "serverWFSURL")
    private String myServerWFSURL;

    /** The active state. */
    @XmlAttribute(name = "active")
    private boolean myIsActive;

    /** My filter type. */
    @XmlAttribute(name = "filterType")
    private FilterType myFilterType;

    /** Whether the filter has been migrated. */
    @XmlAttribute(name = "migrated")
    private boolean myIsMigrated;

    /** The server filter folder. */
    @XmlElement(name = "Folder")
    private ServerFilterFolder myServerFilterFolder;

    /**
     * Default constructor.
     */
    public ServerFilter()
    {
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
        ServerFilter other = (ServerFilter)obj;
        return EqualsHelper.equals(myFilterNameStr, other.myFilterNameStr, myFilterType, other.myFilterType, myServerNameStr,
                other.myServerNameStr, myServerWFSURL, other.myServerWFSURL, myServerWMSURL, other.myServerWMSURL);
    }

    /**
     * Gets the filter name.
     *
     * @return the filter name
     */
    public String getFilterName()
    {
        return myFilterNameStr;
    }

    /**
     * Gets the filter type.
     *
     * @return the filter type
     */
    public FilterType getFilterType()
    {
        return myFilterType;
    }

    /**
     * Gets the layer paths.
     *
     * @return the layer paths
     */
    public List<String[]> getLayerPaths()
    {
        if (myServerFilterFolder != null)
        {
            return myServerFilterFolder.getLayerPaths();
        }
        else
        {
            return null;
        }
    }

    /**
     * Gets the layer paths as strings.
     *
     * @return the layer paths as strings
     */
    public synchronized List<String> getLayerPathsAsStrings()
    {
        // Convert the String[] paths to strings which are used to compare
        // against layer keys
        List<String> pathStrSet = null;

        List<String[]> allPaths = getLayerPaths();
        if (allPaths != null)
        {
            pathStrSet = new ArrayList<>();
            for (String[] entry : allPaths)
            {
                StringBuilder entryStr = new StringBuilder();
                for (int i = 0; i < entry.length; i++)
                {
                    if (i > 0)
                    {
                        entryStr.append(ServerConstants.LAYERNAME_SEPARATOR);
                    }
                    entryStr.append(entry[i]);
                }
                pathStrSet.add(entryStr.toString());
            }
        }
        return pathStrSet;
    }

    /**
     * Gets the server filter folder.
     *
     * @return the server filter folder
     */
    public ServerFilterFolder getServerFilterFolder()
    {
        return myServerFilterFolder;
    }

    /**
     * Gets the server name.
     *
     * @return the server name
     */
    public String getServerName()
    {
        return myServerNameStr;
    }

    /**
     * Gets the WFS url.
     *
     * @return the WFS url
     */
    public String getServerWFSURL()
    {
        return myServerWFSURL;
    }

    /**
     * Gets the WMS url.
     *
     * @return the WMS url
     */
    public String getServerWMSURL()
    {
        return myServerWMSURL;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myFilterNameStr == null ? 0 : myFilterNameStr.hashCode());
        result = prime * result + (myFilterType == null ? 0 : myFilterType.hashCode());
        result = prime * result + (myServerNameStr == null ? 0 : myServerNameStr.hashCode());
        result = prime * result + (myServerWFSURL == null ? 0 : myServerWFSURL.hashCode());
        result = prime * result + (myServerWMSURL == null ? 0 : myServerWMSURL.hashCode());
        return result;
    }

    /**
     * Checks if this filter is active.
     *
     * @return true, if filter is active
     */
    public boolean isActive()
    {
        return myIsActive;
    }

    /**
     * Checks if this filter has been migrated.
     *
     * @return true, if filter has been migrated
     */
    public boolean isMigrated()
    {
        return myIsMigrated;
    }

    /**
     * Sets the active state.
     *
     * @param isActive the new state
     */
    public void setActive(boolean isActive)
    {
        myIsActive = isActive;
    }

    /**
     * Sets the filter type.
     *
     * @param filterType the new filter type
     */
    public void setFilterType(FilterType filterType)
    {
        myFilterType = filterType;
    }

    /**
     * Sets the migrated state.
     *
     * @param isMigrated the new state
     */
    public void setMigrated(boolean isMigrated)
    {
        myIsMigrated = isMigrated;
    }

    /**
     * Sets the server name.
     *
     * @param pServerName the new server name
     */
    public void setServerName(String pServerName)
    {
        myServerNameStr = pServerName;
    }

    /**
     * Sets the WFS url.
     *
     * @param serverWFSURL the new WFS url
     */
    public void setServerWFSURL(String serverWFSURL)
    {
        myServerWFSURL = serverWFSURL;
    }

    /**
     * Sets the WMS url.
     *
     * @param serverWMSURL the new WMS url
     */
    public void setServerWMSURL(String serverWMSURL)
    {
        myServerWMSURL = serverWMSURL;
    }

    /**
     * The FilterType enum.
     */
    public enum FilterType
    {
        /** Base Layer Filter. */
        BASELAYER,

        /** Data Layer Filter. */
        DATALAYER,
    }
}
