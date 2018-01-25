package io.opensphere.wms.config.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.Utilities;

/**
 * Entry which is a folder.
 */
@XmlRootElement(name = "LayerFolder")
@XmlAccessorType(XmlAccessType.FIELD)
@Deprecated
public class WMSLayerFolder implements Cloneable, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     * Layers provided by this server.
     */
    @XmlElement(name = "LayerConfig")
    private WMSLayerConfig myLayer;

    /**
     * Layers folders provided by this server.
     */
    @XmlElement(name = "LayerFolder")
    private List<WMSLayerFolder> myLayerFolders;

    /**
     * The title (for display purposes) of the layer Provided in the
     * GetCapabilities document.
     */
    @XmlElement(name = "Title")
    private String myTitle;

    /**
     * Add a folder for this configuration.
     *
     * @param fld folder to add
     */
    public void addFolder(WMSLayerFolder fld)
    {
        if (myLayerFolders == null)
        {
            myLayerFolders = new ArrayList<>();
        }
        myLayerFolders.add(fld);
    }

    @Override
    public WMSLayerFolder clone() throws CloneNotSupportedException
    {
        WMSLayerFolder clone = (WMSLayerFolder)super.clone();
        if (myLayer != null)
        {
            clone.setLayer(myLayer.clone());
        }
        return clone;
    }

    /**
     * Get the folder whose title matches the given title.
     *
     * @param title title for which to search
     * @return Matching folder or <code>null</code> if not found
     */
    public WMSLayerFolder getFolderByTitle(String title)
    {
        if (title == null || myLayerFolders == null)
        {
            return null;
        }

        for (WMSLayerFolder folder : myLayerFolders)
        {
            if (title.equals(folder.getTitle()))
            {
                return folder;
            }
        }

        return null;
    }

    /**
     * Find The Folder for the layer with the given name.
     *
     * @param layerName the layer name of the associated folder.
     * @return the folder for the layer or <code>null</code> if not found.
     */
    public WMSLayerFolder getFolderForLayer(String layerName)
    {
        if (myLayer != null && myLayer.getLayerName().equals(layerName))
        {
            return this;
        }

        if (myLayerFolders != null)
        {
            for (WMSLayerFolder folder : myLayerFolders)
            {
                WMSLayerFolder folderForName = folder.getFolderForLayer(layerName);
                if (folderForName != null)
                {
                    return folderForName;
                }
            }
        }

        return null;
    }

    /**
     * Get the layer.
     *
     * @return the layer
     */
    public WMSLayerConfig getLayer()
    {
        return myLayer;
    }

    /**
     * Get the layerFolders.
     *
     * @return the layerFolders
     */
    public List<WMSLayerFolder> getLayerFolders()
    {
        return myLayerFolders;
    }

    /**
     * Get the title.
     *
     * @return the title
     */
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * Set the layer.
     *
     * @param layer the layer to set
     */
    public void setLayer(WMSLayerConfig layer)
    {
        myLayer = layer;
    }

    /**
     * Set the layerFolders.
     *
     * @param layerFolders the layerFolders to set
     */
    public void setLayerFolders(List<WMSLayerFolder> layerFolders)
    {
        myLayerFolders = layerFolders;
    }

    /**
     * Set the title.
     *
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        myTitle = title;
    }

    /**
     * Replace the old configuration with the given one.
     *
     * @param oldConfig The original configuration.
     * @param newConfig The updated configuration.
     */
    public void updateLayerConfig(WMSLayerConfig oldConfig, WMSLayerConfig newConfig)
    {
        if (Utilities.sameInstance(myLayer, oldConfig))
        {
            myLayer = newConfig;
            return;
        }

        if (myLayerFolders != null)
        {
            for (WMSLayerFolder child : myLayerFolders)
            {
                child.updateLayerConfig(oldConfig, newConfig);
            }
        }
    }
}
