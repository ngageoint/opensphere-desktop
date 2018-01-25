package io.opensphere.controlpanels.roipanel.config.v1;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** Class to handle storage of regions of interest (Using an XML file). */
@XmlRootElement(name = "ROIConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class ROIConfig
{
    /** The list of all regions of interest. */
    @XmlElement(name = "RegionsOfInterest")
    private final Collection<RegionOfInterest> myRegionsOfInterest;

    /** The list of groupings of regions of interest. */
    @XmlElement(name = "ROIGroup")
    private Collection<ROIGroup> myROIGroups;

    /**
     * Default constructor.
     */
    public ROIConfig()
    {
        myROIGroups = new ArrayList<>();
        myRegionsOfInterest = new ArrayList<>();
    }

    /**
     * Get the current regions of interest.
     *
     * @return The regions of interest.
     */
    public Collection<RegionOfInterest> getROI()
    {
        return myRegionsOfInterest;
    }

    /**
     * Get the current region of interest groups.
     *
     * @return The regions of interest groups.
     */
    public Collection<ROIGroup> getROIGroups()
    {
        return myROIGroups;
    }

    /**
     * Set the regions of interest to given values. (Any previous are removed.)
     *
     * @param rois The new regions of interest.
     */
    public void setROI(Collection<RegionOfInterest> rois)
    {
        myRegionsOfInterest.clear();
        myRegionsOfInterest.addAll(rois);
    }

    /**
     * Set the regions of interest groups to given values. (Any previous are
     * removed.)
     *
     * @param roiGroups The new region of interest groups.
     */
    public void setROIGroups(Collection<ROIGroup> roiGroups)
    {
        myROIGroups.clear();
        myROIGroups = roiGroups;
    }
}
