package io.opensphere.shapefile.config.v1;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;

/**
 * The ShapeFilesConfig represents the configuration of all of the a list of
 * shape files and their settings to be loaded. *
 * <ul>
 * <li>ShapeFileSource - each shape files settings and location</li>
 * </ul>
 */
@XmlRootElement(name = "ShapeFilesConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class ShapeFilesConfig implements IDataSourceConfig
{
    /** The my logger. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(ShapeFilesConfig.class);

    /** The my shape files. */
    @XmlElement(name = "ShapeFile")
    private final List<ShapeFileSource> myShapeFiles = new ArrayList<>();

    /** Whether the config has been migrated. */
    @XmlAttribute(name = "migrated")
    private boolean myIsMigrated;

    /**
     * Instantiates a new shape files config.
     */
    public ShapeFilesConfig()
    {
    }

    /**
     * Add a new source to the configuration. Any existing source with the same
     * name will be removed.
     *
     * @param source the source
     */
    public void addShapeFileSource(ShapeFileSource source)
    {
        if (!myShapeFiles.contains(source) && !source.isTransient())
        {
            removeShapeFileSource(source.getName());
            myShapeFiles.add(source);
        }
    }

    @Override
    public boolean addSource(IDataSource source)
    {
        if (source instanceof ShapeFileSource)
        {
            addShapeFileSource((ShapeFileSource)source);
            return true;
        }
        return false;
    }

    /**
     * Retrieves the {@link ShapeFileSource} given the "pretty name" of the
     * shape file.
     *
     * @param name the name
     * @return the {@link ShapeFileSource} or null if not found.
     */
    public ShapeFileSource getShapeFileSource(String name)
    {
        for (ShapeFileSource l : myShapeFiles)
        {
            if (l.getName().equals(name))
            {
                return l;
            }
        }
        return null;
    }

    /**
     * Gets the shape file sources.
     *
     * @return the shape file sources
     */
    public List<ShapeFileSource> getShapeFileSources()
    {
        return myShapeFiles;
    }

    @Override
    public List<IDataSource> getSourceList()
    {
        List<IDataSource> srcList = new ArrayList<IDataSource>(myShapeFiles);
        return srcList;
    }

    /**
     * Prints the.
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
            LOGGER.warn(e.getMessage(), e);
        }
    }

    /**
     * Removes the shape file source.
     *
     * @param name the name
     * @return true, if successful
     */
    public boolean removeShapeFileSource(String name)
    {
        Iterator<ShapeFileSource> iter = myShapeFiles.iterator();
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
        return removeShapeFileSource(source.getName());
    }

    /**
     * Sets the shape file sources.
     *
     * @param dataLayers the new shape file sources
     */
    public void setShapeFileSources(Collection<ShapeFileSource> dataLayers)
    {
        myShapeFiles.clear();
        myShapeFiles.addAll(dataLayers);
    }

    @Override
    public void updateSource(IDataSource source)
    {
        removeSource(source);
        addSource(source);
    }

    /**
     * Checks if this config has been migrated.
     *
     * @return true, if config has been migrated
     */
    public boolean isMigrated()
    {
        return myIsMigrated;
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
}
