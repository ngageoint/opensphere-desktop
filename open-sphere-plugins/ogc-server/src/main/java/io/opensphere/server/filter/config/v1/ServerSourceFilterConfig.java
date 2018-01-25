package io.opensphere.server.filter.config.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.server.filter.config.v1.ServerFilter.FilterType;

/**
 * The Class ServerSourceFilterConfig.
 */
@XmlRootElement(name = "ServerFilters")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerSourceFilterConfig
{
    /** My filters. */
    @XmlElement(name = "Filter")
    private final List<ServerFilter> myFilters = new ArrayList<>();

    /** Default Constructor. */
    public ServerSourceFilterConfig()
    {
    }

    /**
     * Gets the active filter by server and type.
     *
     * @param serverName the server name
     * @param pType the type
     * @return the active filter
     */
    public ServerFilter getActiveFilter(String serverName, FilterType pType)
    {
        ServerFilter toReturn = null;
        ArrayList<ServerFilter> list = (ArrayList<ServerFilter>)getServerFilters(serverName);
        for (ServerFilter sf : list)
        {
            if (sf != null && sf.isActive() && sf.getFilterType().equals(pType))
            {
                toReturn = sf;
            }
        }
        return toReturn;
    }

    /**
     * Gets the default base layer filters.
     *
     * @return the default base layer filters
     */
    public List<ServerFilter> getDefaultBaseLayerFilters()
    {
        List<ServerFilter> defaults = new ArrayList<>();
        if (myFilters != null && !myFilters.isEmpty())
        {
            for (ServerFilter sf : myFilters)
            {
                if (sf.getFilterName().equals("Default"))
                {
                    defaults.add(sf);
                }
            }
        }
        return defaults;
    }

    /**
     * Gets the server filters by server name.
     *
     * @param serverName the server name
     * @return the server filters
     */
    public List<ServerFilter> getServerFilters(String serverName)
    {
        ArrayList<ServerFilter> filterList = new ArrayList<>();
        if (myFilters != null && !myFilters.isEmpty())
        {
            for (ServerFilter sf : myFilters)
            {
                if (sf != null && sf.getServerName().equals(serverName))
                {
                    filterList.add(sf);
                }
            }
        }
        return filterList;
    }

    /**
     * Returns whether the config is empty.
     *
     * @return whether the config is empty
     */
    public boolean isEmpty()
    {
        return myFilters == null || myFilters.isEmpty();
    }
}
