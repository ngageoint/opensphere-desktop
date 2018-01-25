package io.opensphere.imagery;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;

import io.opensphere.core.util.JAXBContextHelper;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceConfig;

/**
 * The ImageryFilesConfig represents the configuration of all of the a list of
 * image files and their settings to be loaded. *
 * <ul>
 * <li>ImageryFileSource - each image files settings and location</li>
 * </ul>
 */
@XmlRootElement(name = "ImageryFilesConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImageryFilesConfig implements IDataSourceConfig
{
    /** Logger. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(ImageryFilesConfig.class);

    /** The Image groups. */
    @XmlElement(name = "ImageryGroup")
    private final List<ImagerySourceGroup> myImageGroups = new ArrayList<>();

    /**
     * Instantiates a new advanced image files config.
     */
    public ImageryFilesConfig()
    {
    }

    /**
     * Add a new source to the configuration. Any existing source with the same
     * name will be removed.
     *
     * @param source the source
     */
    public void addImageSourceGroup(ImagerySourceGroup source)
    {
        if (!myImageGroups.contains(source))
        {
            removeImageFileSource(source.getName());
            myImageGroups.add(source);
        }
    }

    @Override
    public boolean addSource(IDataSource source)
    {
        if (source instanceof ImagerySourceGroup)
        {
            addImageSourceGroup((ImagerySourceGroup)source);
        }
        return false;
    }

    /**
     * Retrieves the {@link ImageryFileSource} given the "pretty name" of the
     * image file.
     *
     * @param name the name
     * @return the {@link ImageryFileSource} or null if not found.
     */
    public ImagerySourceGroup getImageSourceGroup(String name)
    {
        for (ImagerySourceGroup l : myImageGroups)
        {
            if (l.getName().equals(name))
            {
                return l;
            }
        }
        return null;
    }

    /**
     * Gets the {@link List} of {@link ImagerySourceGroup}.
     *
     * @return the image file sources
     */
    public List<ImagerySourceGroup> getImageSourceGroups()
    {
        return myImageGroups;
    }

    @Override
    public List<IDataSource> getSourceList()
    {
        List<IDataSource> list = new ArrayList<IDataSource>(myImageGroups);
        return list;
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
            JAXBContext ctx = JAXBContextHelper.getCachedContext(ImageryFilesConfig.class);
            Marshaller m = ctx.createMarshaller();
            m.setProperty("jaxb.formatted.output", Boolean.TRUE);
            m.marshal(this, stream);
        }
        catch (JAXBException e)
        {
            LOGGER.error(e);
        }
    }

    /**
     * Removes the image file source.
     *
     * @param name the name
     * @return true, if successful
     */
    public boolean removeImageFileSource(String name)
    {
        Iterator<ImagerySourceGroup> iter = myImageGroups.iterator();
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
        return removeImageFileSource(source.getName());
    }

    /**
     * Sets the image file sources.
     *
     * @param dataLayers the new image file sources
     */
    public void setImageFileSources(Collection<ImagerySourceGroup> dataLayers)
    {
        myImageGroups.clear();
        myImageGroups.addAll(dataLayers);
    }

    /**
     * Update group source groups.
     */
    public void updateGroupSourceGroups()
    {
        for (ImagerySourceGroup l : myImageGroups)
        {
            l.updateSourceGroupReferences();
        }
    }

    @Override
    public void updateSource(IDataSource source)
    {
        removeSource(source);
        addSource(source);
    }
}
