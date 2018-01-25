package io.opensphere.arcgis.config.v1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;

/**
 * Collection of {@link ArcGISServerSource}s.
 */
@XmlRootElement(name = "ArcGISServerConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class ArcGISServerConfig implements IDataSourceConfig
{
    /** The my servers files. */
    @XmlElement(name = "ArcGISServerSource")
    private final List<ArcGISServerSource> myServerSources = new ArrayList<>();

    /**
     * Instantiates a new server source config.
     */
    public ArcGISServerConfig()
    {
    }

    /**
     * Add a new source to the configuration. Any existing source with the same
     * name will be removed.
     *
     * @param source the source
     */
    public void addServerSource(ArcGISServerSource source)
    {
        if (!myServerSources.contains(source))
        {
            removeServerSource(source.getName());
            myServerSources.add(source);
        }
    }

    @Override
    public boolean addSource(IDataSource source)
    {
        if (source instanceof ArcGISServerSource)
        {
            addServerSource((ArcGISServerSource)source);
        }
        return false;
    }

    /**
     * Retrieves the {@link ArcGISServerSource} given the "pretty name" of the
     * server file.
     *
     * @param name the name
     * @return the {@link ArcGISServerSource} or null if not found.
     */
    public ArcGISServerSource getServerSource(String name)
    {
        for (ArcGISServerSource source : myServerSources)
        {
            if (source.getName().equals(name))
            {
                return source;
            }
        }
        return null;
    }

    /**
     * Gets the server sources.
     *
     * @return the server sources
     */
    public List<ArcGISServerSource> getServerSources()
    {
        return myServerSources;
    }

    @Override
    public List<IDataSource> getSourceList()
    {
        return New.<IDataSource>list(myServerSources);
    }

    /**
     * Removes the server source.
     *
     * @param name the name
     * @return true, if successful
     */
    public boolean removeServerSource(String name)
    {
        Iterator<ArcGISServerSource> iter = myServerSources.iterator();
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

    @Override
    public boolean removeSource(IDataSource source)
    {
        return removeServerSource(source.getName());
    }

    /**
     * Sets the server sources.
     *
     * @param sources the new server sources
     */
    public void setServerSources(Collection<ArcGISServerSource> sources)
    {
        myServerSources.clear();
        myServerSources.addAll(sources);
    }

    @Override
    public void updateSource(IDataSource source)
    {
        removeServerSource(source.getName());
        addSource(source);
    }
}
