package io.opensphere.mantle.datasources.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;

/**
 * Config for UrlDataSource objects.
 */
@XmlRootElement(name = "config")
@XmlAccessorType(XmlAccessType.FIELD)
public class UrlSourceConfig implements IDataSourceConfig
{
    /** The sources. */
    @XmlElement(name = "source")
    private final List<UrlDataSource> mySources = New.list();

    @Override
    public synchronized boolean addSource(IDataSource source)
    {
        if (source instanceof UrlDataSource)
        {
            return mySources.add((UrlDataSource)source);
        }
        return false;
    }

    @Override
    public synchronized List<IDataSource> getSourceList()
    {
        return New.list(mySources);
    }

    @Override
    public synchronized boolean removeSource(IDataSource source)
    {
        return mySources.remove(source);
    }

    @Override
    public synchronized void updateSource(IDataSource source)
    {
        removeSource(source);
        addSource(source);
    }

    /**
     * Determines if the source is present in the config.
     *
     * @param source the source
     * @return whether the source is present
     */
    public synchronized boolean hasSource(IDataSource source)
    {
        return mySources.contains(source);
    }
}
