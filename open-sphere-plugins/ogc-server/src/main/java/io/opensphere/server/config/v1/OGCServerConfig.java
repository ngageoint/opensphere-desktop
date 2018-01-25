package io.opensphere.server.config.v1;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;
import io.opensphere.server.source.OGCServerSource;

/**
 * The CSVFilesConfig represents the configuration of all of the a list of CSV
 * files and their settings to be loaded.
 * <ul>
 * <li>ShapeFileSource - each shape files settings and location</li>
 * </ul>
 *
 *
 */
@XmlRootElement(name = "OGCServerConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class OGCServerConfig implements IDataSourceConfig
{
    /** The Constant myLogger. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(OGCServerConfig.class);

    /** The my servers files. */
    @XmlElement(name = "OGCServerSource")
    private final List<OGCServerSource> myServerSources = new ArrayList<>();

    /**
     * Instantiates a new server source config.
     */
    public OGCServerConfig()
    {
    }

    /**
     * Add a new source to the configuration. Any existing source with the same
     * name will be removed.
     *
     * @param source the source
     */
    public void addOGCServerSource(OGCServerSource source)
    {
        if (!myServerSources.contains(source))
        {
            removeOGCServerSource(source.getName());
            myServerSources.add(source);
        }
    }

    @Override
    public boolean addSource(IDataSource source)
    {
        if (source instanceof OGCServerSource)
        {
            addOGCServerSource((OGCServerSource)source);
        }
        return false;
    }

    /**
     * Retrieves the {@link OGCServerSource} given the "pretty name" of the
     * server file.
     *
     * @param name the name
     * @return the {@link OGCServerSource} or null if not found.
     */
    public OGCServerSource getOGCServerSource(String name)
    {
        for (OGCServerSource source : myServerSources)
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
    public List<OGCServerSource> getServerSources()
    {
        return myServerSources;
    }

    @Override
    public List<IDataSource> getSourceList()
    {
        return New.<IDataSource>list(myServerSources);
    }

    /**
     * Prints this class to the stream.
     *
     * @param stream The stream to which to print.
     */
    public void print(PrintStream stream)
    {
        try
        {
            XMLUtilities.writeXMLObject(this, stream);
        }
        catch (JAXBException e)
        {
            LOGGER.warn("Failed to marshal Server Source configuration file: " + e, e);
        }
    }

    /**
     * Removes the server source.
     *
     * @param name the name
     * @return true, if successful
     */
    public boolean removeOGCServerSource(String name)
    {
        Iterator<OGCServerSource> iter = myServerSources.iterator();
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
        return removeOGCServerSource(source.getName());
    }

    /**
     * Sets the server sources.
     *
     * @param sources the new server sources
     */
    public void setOGCServerSources(Collection<OGCServerSource> sources)
    {
        myServerSources.clear();
        myServerSources.addAll(sources);
    }

    @Override
    public void updateSource(IDataSource source)
    {
        removeOGCServerSource(source.getName());
        addSource(source);
    }
}
