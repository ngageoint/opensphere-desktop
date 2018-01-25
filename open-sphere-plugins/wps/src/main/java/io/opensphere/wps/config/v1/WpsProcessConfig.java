package io.opensphere.wps.config.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;

/**
 * The Class WpsProcessConfig.
 */
@XmlRootElement(name = "WPSProcessConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class WpsProcessConfig implements IDataSourceConfig
{
    /** The Constant LOGGER reference. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(WpsProcessConfig.class);

    /** My list of WPS sources. */
    @XmlElement(name = "WPSProcess")
    private final List<WpsProcessSource> myWpsSources = new ArrayList<>();

    /**
     * Instantiates a new WPS process config.
     */
    public WpsProcessConfig()
    {
    }

    /**
     * Adds the source.
     *
     * @param source the source
     * @return true, if successful
     */
    @Override
    public boolean addSource(IDataSource source)
    {
        if (source instanceof WpsProcessSource)
        {
            addWpsSource((WpsProcessSource)source);
            return true;
        }
        return false;
    }

    /**
     * Add a new source to the configuration. Any existing source with the same
     * name will be removed.
     *
     * @param source the source
     */
    public void addWpsSource(WpsProcessSource source)
    {
        if (!myWpsSources.contains(source))
        {
            removeWpsSource(source.getName());
            myWpsSources.add(source);
        }
    }

    /**
     * Gets the source list.
     *
     * @return the source list
     */
    @Override
    public List<IDataSource> getSourceList()
    {
        List<IDataSource> srcList = new ArrayList<IDataSource>(myWpsSources);
        return srcList;
    }

    /**
     * Retrieves the {@link WpsProcessSource} given the "pretty name" of the WPS
     * process.
     *
     * @param name the name of the process to retrieve
     * @return the {@link WpsProcessSource} or null if not found.
     */
    public WpsProcessSource getWPSProcess(String name)
    {
        for (WpsProcessSource source : myWpsSources)
        {
            if (source.getName().equals(name))
            {
                return source;
            }
        }
        return null;
    }

    /**
     * Gets all the configured WPS sources.
     *
     * @return the WPS sources
     */
    public List<WpsProcessSource> getWpsSources()
    {
        return myWpsSources;
    }

    /**
     * Removes the source.
     *
     * @param source the source
     * @return true, if successful
     */
    @Override
    public boolean removeSource(IDataSource source)
    {
        return removeWpsDataSource(source.getName());
    }

    /**
     * Removes the WPS source.
     *
     * @param name the name of the source to remove
     * @return true, if successful
     */
    public boolean removeWpsSource(String name)
    {
        Iterator<WpsProcessSource> iter = myWpsSources.iterator();
        while (iter.hasNext())
        {
            if (iter.next().getName().equals(name))
            {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the WPS data sources.
     *
     * @param dataLayers the new WPS data sources
     */
    public void setWpsDataSources(Collection<WpsProcessSource> dataLayers)
    {
        myWpsSources.clear();
        myWpsSources.addAll(dataLayers);
    }

    /**
     * Update source.
     *
     * @param source the source
     */
    @Override
    public void updateSource(IDataSource source)
    {
        removeSource(source);
        addSource(source);
    }

    /**
     * Removes the WPS data source.
     *
     * @param name the name of the source to remove
     * @return true, if successful
     */
    private boolean removeWpsDataSource(String name)
    {
        Iterator<WpsProcessSource> iter = myWpsSources.iterator();
        while (iter.hasNext())
        {
            if (iter.next().getName().equals(name))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(" Removing WPS data source " + name);
                }
                iter.remove();
                return true;
            }
        }
        return false;
    }
}
