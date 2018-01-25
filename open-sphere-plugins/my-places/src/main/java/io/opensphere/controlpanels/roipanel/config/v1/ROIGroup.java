package io.opensphere.controlpanels.roipanel.config.v1;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
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

/** Class to handle storage of region of interest groups (Using an XML file). */
@XmlRootElement(name = "ROIGroup")
@XmlAccessorType(XmlAccessType.FIELD)
public class ROIGroup
{
    /** Logger used. */
    @XmlTransient
    private static final Logger LOGGER = Logger.getLogger(ROIGroup.class);

    /** The name. */
    @XmlAttribute
    private String myName;

    /** The list of regions of interest. */
    @XmlElement(name = "ROI")
    private final List<String> myRegionsOfInterest;

    /**
     * Default constructor.
     */
    public ROIGroup()
    {
        myRegionsOfInterest = new ArrayList<>();
    }

    /**
     * Constructor.
     *
     * @param name The name of group.
     */
    public ROIGroup(String name)
    {
        myName = name;
        myRegionsOfInterest = new ArrayList<>();
    }

    /**
     * Add a new region of interest to the list.
     *
     * @param roi The region of interest.
     */
    public void addRegionOfInterest(String roi)
    {
        if (roi != null)
        {
            myRegionsOfInterest.add(roi);
        }
    }

    /**
     * Returns true if this config contains at least one ROI with the specified
     * name.
     *
     * @param name The name to check for.
     * @return True if in ROI config, false otherwise.
     */
    public boolean containsRegionOfInterest(String name)
    {
        return getRegionOfInterest(name) != null;
    }

    /**
     * Standard getter.
     *
     * @return The name.
     */
    public String getName()
    {
        return myName;
    }

    /**
     * Retrieves the first RegionOfInterest in the file with the specified name.
     *
     * @param name The name to retrieve.
     * @return The {@link RegionOfInterest} or null if not found.
     */
    public String getRegionOfInterest(String name)
    {
        String roi = null;
        for (String aRoi : myRegionsOfInterest)
        {
            if (aRoi.equals(name))
            {
                roi = aRoi;
                break;
            }
        }

        return roi;
    }

    /**
     * Standard getter.
     *
     * @return The list of RegionOfInterest.
     */
    public List<String> getRegionsOfInterest()
    {
        return myRegionsOfInterest;
    }

    /**
     * Prints this ROI as XML to the specified stream.
     *
     * @param ps The stream to print to.
     */
    public void print(PrintStream ps)
    {
        try
        {
            XMLUtilities.writeXMLObject(this, ps);
        }
        catch (JAXBException e)
        {
            LOGGER.error(e.getStackTrace());
        }
    }

    /**
     * Removes all {@link RegionOfInterest} from the config that have the
     * specified name.
     *
     * @param name - the name of the ROI to remove
     * @return true if at least one was removed
     */
    public boolean removeRegionOfInterest(String name)
    {
        return myRegionsOfInterest.remove(name);
    }

    /**
     * Standard setter.
     *
     * @param name The name.
     */
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Clears and replaces all the regions of interest.
     *
     * @param rois The regions of interest to replace with.
     */
    public void setRegionsOfInterest(Collection<String> rois)
    {
        myRegionsOfInterest.clear();
        myRegionsOfInterest.addAll(rois);
    }
}
