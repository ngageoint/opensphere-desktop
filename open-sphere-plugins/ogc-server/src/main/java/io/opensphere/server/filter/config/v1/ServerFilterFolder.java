package io.opensphere.server.filter.config.v1;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;

/**
 * The Class ServerFilterFolder.
 */
@XmlRootElement(name = "Folder")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerFilterFolder // implements Cloneable
{
    /** The layer folders. */
    @XmlElement(name = "Folder")
    private final List<ServerFilterFolder> myLayerFolders = New.list();

    /** The folder title. */
    @XmlAttribute(name = "title")
    private String myFolderTitle;

    /** The data layers. */
    @XmlElement(name = "Layer")
    private final Set<ServerFilterLayer> myDataLayers = New.set();

    /**
     * Default Constructor.
     */
    public ServerFilterFolder()
    {
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        ServerFilterFolder other = (ServerFilterFolder)obj;
        return EqualsHelper.equals(myFolderTitle, other.myFolderTitle, myLayerFolders, other.myLayerFolders, myDataLayers,
                other.myDataLayers);
    }

    /**
     * Begin the recursive search for ServerFilterLayers in this
     * ServerFilterFolder.
     *
     * @return - the set of ServerFilterLayers
     */
    public List<ServerFilterLayer> getAllLayers()
    {
        return getAllServerFilterLayersLayers(this);
    }

    /**
     * Gets the data layers.
     *
     * @return the data layers
     */
    public Set<ServerFilterLayer> getDataLayers()
    {
        return myDataLayers;
    }

    /**
     * Gets the folder title.
     *
     * @return the folder title
     */
    public String getFolderTitle()
    {
        return myFolderTitle;
    }

    /**
     * Gets the layer folders.
     *
     * @return the layer folders
     */
    public List<ServerFilterFolder> getLayerFolders()
    {
        return myLayerFolders;
    }

//    @Override
//    public ServerFilterFolder clone()
//    {
//        ServerFilterFolder myClone = copy(this);
//        return myClone;
//    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myFolderTitle == null ? 0 : myFolderTitle.hashCode());
        if (CollectionUtilities.hasContent(myLayerFolders))
        {
            for (ServerFilterFolder folder : myLayerFolders)
            {
                result = prime * result + folder.hashCode();
            }
        }
        if (CollectionUtilities.hasContent(myDataLayers))
        {
            for (ServerFilterLayer layer : myDataLayers)
            {
                result = prime * result + layer.hashCode();
            }
        }
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(" [Title=\"").append(myFolderTitle).append("\" Folders=[");
        if (myLayerFolders == null)
        {
            sb.append("null");
        }
        else if (myLayerFolders.isEmpty())
        {
            sb.append("empty");
        }
        else
        {
            for (ServerFilterFolder folder : myLayerFolders)
            {
                sb.append(folder.toString());
            }
        }
        sb.append("] Layers=[");
        if (myDataLayers == null)
        {
            sb.append("null");
        }
        else if (myDataLayers.isEmpty())
        {
            sb.append("empty");
        }
        else
        {
            for (ServerFilterLayer layer : myDataLayers)
            {
                sb.append(layer.toString());
            }
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Get the Layer paths.
     *
     * @return the Layer paths
     */
    protected List<String[]> getLayerPaths()
    {
        return getLayerSelectionPaths(this, New.<String>list());
    }

    /**
     * Recursively search this ServerFilterFolder for ServerFilterLayers and
     * keep track of them.
     *
     * @param pFolder - the ServerFilterFolder to start searching in
     * @return - the set of ServerFilterLayers
     */
    private List<ServerFilterLayer> getAllServerFilterLayersLayers(ServerFilterFolder pFolder)
    {
        List<ServerFilterLayer> allLayers = New.list();
        for (ServerFilterFolder aFolder : pFolder.getLayerFolders())
        {
            allLayers.addAll(getAllServerFilterLayersLayers(aFolder));
        }

        if (pFolder.getDataLayers().size() > 0)
        {
            for (ServerFilterLayer aLayer : pFolder.getDataLayers())
            {
                if (!allLayers.contains(aLayer))
                {
                    allLayers.add(aLayer);
                }
            }
        }
        return allLayers;
    }

    /**
     * Recursive method that steps through the ServerFilterFolder structure and
     * returns paths to the layers in the form of string arrays.
     *
     * @param aFolder - the ServerFilterFolder to check
     * @param pPath - the paths to check for
     * @return the Layer paths
     */
    private List<String[]> getLayerSelectionPaths(ServerFilterFolder aFolder, List<String> pPath)
    {
        List<String[]> selectionPaths = New.list();
        if (CollectionUtilities.hasContent(aFolder.getLayerFolders()))
        {
            pPath.add(aFolder.getFolderTitle());
            for (ServerFilterFolder sf : aFolder.getLayerFolders())
            {
                selectionPaths.addAll(getLayerSelectionPaths(sf, pPath));
                pPath.remove(sf.getFolderTitle());
            }
        }
        else
        {
            pPath.add(aFolder.getFolderTitle());
        }

        Set<ServerFilterLayer> layers = aFolder.getDataLayers();
        if (layers != null && !layers.isEmpty())
        {
            for (ServerFilterLayer layer : layers)
            {
                List<String> fullPath = New.list();
                for (String str : pPath)
                {
                    fullPath.add(str);
                }
                fullPath.add(layer.getLayerTitle());

                String[] pathArray = new String[fullPath.size()];
                selectionPaths.add(fullPath.toArray(pathArray));
            }
        }
        return selectionPaths;
    }
}
