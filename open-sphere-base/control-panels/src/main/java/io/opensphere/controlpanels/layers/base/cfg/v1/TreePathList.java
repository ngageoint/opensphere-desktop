package io.opensphere.controlpanels.layers.base.cfg.v1;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.New;

/**
 * The Class TreePathList.
 */
@XmlRootElement(name = "TreePathList")
@XmlAccessorType(XmlAccessType.FIELD)
public class TreePathList
{
    /** The Tree paths. */
    @XmlElement(name = "Path", required = true)
    private List<String> myTreePaths;

    /** The View by type. */
    @XmlAttribute(name = "viewByType")
    private String myViewByType;

    /**
     * Instantiates a new tree path list.
     */
    public TreePathList()
    {
        myTreePaths = New.list();
    }

    /**
     * Instantiates a new tree path list.
     *
     * @param viewByType the view by type
     */
    public TreePathList(String viewByType)
    {
        myViewByType = viewByType;
        myTreePaths = New.list();
    }

    /**
     * Instantiates a new tree path list.
     *
     * @param viewByType the view by type
     * @param treePaths the tree paths
     */
    public TreePathList(String viewByType, Collection<String> treePaths)
    {
        myViewByType = viewByType;
        myTreePaths = New.list();
        if (treePaths != null)
        {
            myTreePaths.addAll(treePaths);
            Collections.sort(myTreePaths);
        }
    }

    /**
     * Gets the tree paths.
     *
     * @return the tree paths
     */
    public List<String> getTreePaths()
    {
        return myTreePaths;
    }

    /**
     * Gets the view by type.
     *
     * @return the view by type
     */
    public String getViewByType()
    {
        return myViewByType;
    }

    /**
     * Sets the tree paths.
     *
     * @param treePaths the new tree paths
     */
    public void setTreePaths(List<String> treePaths)
    {
        myTreePaths = treePaths;
    }

    /**
     * Sets the view by type.
     *
     * @param viewByType the new view by type
     */
    public void setViewByType(String viewByType)
    {
        myViewByType = viewByType;
    }
}
