package io.opensphere.kml.datasource.model.v1;

import java.util.Collection;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;

/**
 * Represents the configuration of all of the a list of KML files and their
 * settings to be loaded.
 */
@XmlRootElement(name = "KMLDataSourceConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class KMLDataSourceConfig implements IDataSourceConfig
{
    /** The logger. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(KMLDataSourceConfig.class);

    /** The list of KMLDataSource objects. */
    @GuardedBy("myKmlDataSources")
    @XmlElement(name = "KMLDataSource")
    private final Collection<KMLDataSource> myKmlDataSources = New.list();

    @Override
    public boolean addSource(IDataSource source)
    {
        boolean added = false;
        if (source instanceof KMLDataSource && !source.isTransient())
        {
            KMLDataSource kmlSource = (KMLDataSource)source;
            synchronized (myKmlDataSources)
            {
                if (!myKmlDataSources.contains(kmlSource))
                {
                    added = myKmlDataSources.add(kmlSource);
                }
            }
        }
        return added;
    }

    @Override
    public List<IDataSource> getSourceList()
    {
        synchronized (myKmlDataSources)
        {
            return New.list(myKmlDataSources);
        }
    }

    @Override
    public boolean removeSource(IDataSource source)
    {
        boolean removed = false;
        if (source instanceof KMLDataSource)
        {
            KMLDataSource kmlSource = (KMLDataSource)source;
            synchronized (myKmlDataSources)
            {
                removed = myKmlDataSources.remove(kmlSource);
            }
        }
        return removed;
    }

    @Override
    public void updateSource(IDataSource source)
    {
        if (!removeSource(source))
        {
            LOGGER.warn("Unable to remove source " + source.getName());
        }
        addSource(source);
    }

    /**
     * Gets the list of sources as KMLDataSource objects.
     *
     * @return the sources
     */
    public List<KMLDataSource> getKMLSourceList()
    {
        synchronized (myKmlDataSources)
        {
            return New.list(myKmlDataSources);
        }
    }
}
