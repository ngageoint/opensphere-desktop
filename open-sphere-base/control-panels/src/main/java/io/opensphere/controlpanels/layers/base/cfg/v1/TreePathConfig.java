package io.opensphere.controlpanels.layers.base.cfg.v1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The Class TreePathConfig.
 */
@XmlRootElement(name = "TreePathConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class TreePathConfig
{
    /** The list of tree paths. */
    @XmlElement(name = "PathList")
    private final List<TreePathList> myPathLists = new ArrayList<>();

    /**
     * Instantiates a new tree path config.
     */
    public TreePathConfig()
    {
    }

    /**
     * Gets the path lists.
     *
     * @return the path lists
     */
    public List<TreePathList> getPathLists()
    {
        return myPathLists;
    }
}
