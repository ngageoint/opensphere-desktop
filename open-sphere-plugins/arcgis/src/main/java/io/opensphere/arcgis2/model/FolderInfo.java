package io.opensphere.arcgis2.model;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import io.opensphere.core.util.collections.New;

/** Top-level information about a server folder. */
public class FolderInfo
{
    /** The description. */
    @JsonProperty("description")
    private String myDescription;

    /** The folders. */
    @JsonProperty("folders")
    private List<String> myFolders = New.list();

    /** The extent of the layer. */
    @JsonProperty("fullExtent")
    private Extent myFullExtent = new Extent();

    /** The layers. */
    @JsonProperty("layers")
    private List<Map<String, Object>> myLayers = New.list();

    /** The maximum scale of the layer. */
    private double myMaxScale;

    /** The minimum scale of the layer. */
    @JsonProperty("minScale")
    private double myMinScale;

    /** The service description. */
    @JsonProperty("serviceDescription")
    private String myServiceDescription;

    /** The services. */
    @JsonProperty("services")
    private List<Service> myServices = New.list();

    /** Indicates if this layer has cached tiles. */
    @JsonProperty("singleFusedMapCache")
    private boolean mySingleFusedMapCache;

    /** The tables. */
    @JsonProperty("tables")
    private List<Map<String, Object>> myTables = New.list();

    /** The tile info. */
    @JsonProperty("tileInfo")
    private TileInfo myTileInfo;

    /**
     * Contains other information about the map layer.
     */
    private DocumentInfo myDocumentInfo;

    /**
     * Get the description.
     *
     * @return The description.
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Get the document info.
     *
     * @return The document info.
     */
    public DocumentInfo getDocumentInfo()
    {
        return myDocumentInfo;
    }

    /**
     * Get the folders.
     *
     * @return The folders.
     */
    public List<String> getFolders()
    {
        return myFolders;
    }

    /**
     * Get the full extent of the layer.
     *
     * @return The full extent.
     */
    public Extent getFullExtent()
    {
        return myFullExtent;
    }

    /**
     * Get the layers.
     *
     * @return The layers.
     */
    public List<Map<String, Object>> getLayers()
    {
        return myLayers;
    }

    /**
     * Get the maximum scale of the layer.
     *
     * @return The max scale.
     */
    public double getMaxScale()
    {
        return myMaxScale;
    }

    /**
     * Get the minimum scale of the layer.
     *
     * @return The min scale.
     */
    public double getMinScale()
    {
        return myMinScale;
    }

    /**
     * Get the service description.
     *
     * @return The service description.
     */
    public String getServiceDescription()
    {
        return myServiceDescription;
    }

    /**
     * Get the services.
     *
     * @return The services.
     */
    public List<Service> getServices()
    {
        return myServices;
    }

    /**
     * Gets the tables.
     *
     * @return the tables
     */
    public List<Map<String, Object>> getTables()
    {
        return myTables;
    }

    /**
     * Get the tile info.
     *
     * @return The tile info.
     */
    public TileInfo getTileInfo()
    {
        return myTileInfo;
    }

    /**
     * Indicates if this layer has a tile cache.
     *
     * @return True if tiles are cached for this layer, false otherwise.
     */
    public boolean isSingleFusedMapCache()
    {
        return mySingleFusedMapCache;
    }

    /**
     * Set the description.
     *
     * @param description The description.
     */
    public void setDescription(String description)
    {
        myDescription = description;
    }

    /**
     * Sets the document info.
     *
     * @param documentInfo The document info.
     */
    public void setDocumentInfo(DocumentInfo documentInfo)
    {
        myDocumentInfo = documentInfo;
    }

    /**
     * Set the folders.
     *
     * @param folders The folders.
     */
    public void setFolders(List<String> folders)
    {
        myFolders = folders;
    }

    /**
     * Set the full extent of the layer.
     *
     * @param fullExtent The full extent.
     */
    public void setFullExtent(Extent fullExtent)
    {
        myFullExtent = fullExtent;
    }

    /**
     * Sets the layers.
     *
     * @param layers The layers.
     */
    public void setLayers(List<Map<String, Object>> layers)
    {
        myLayers = layers;
    }

    /**
     * Set the maximum scale of the layer.
     *
     * @param maxScale The max scale.
     */
    public void setMaxScale(double maxScale)
    {
        myMaxScale = maxScale;
    }

    /**
     * Set the minimum scale of the layer.
     *
     * @param minScale The min scale.
     */
    public void setMinScale(double minScale)
    {
        myMinScale = minScale;
    }

    /**
     * Set the service description.
     *
     * @param serviceDescription The service description.
     */
    public void setServiceDescription(String serviceDescription)
    {
        myServiceDescription = serviceDescription;
    }

    /**
     * Set the services.
     *
     * @param services The services.
     */
    public void setServices(List<Service> services)
    {
        myServices = services;
    }

    /**
     * Sets if this layer has a tile cache.
     *
     * @param singleFusedMapCache True if tiles are cached for this layer, false
     *            otherwise.
     */
    public void setSingleFusedMapCache(boolean singleFusedMapCache)
    {
        mySingleFusedMapCache = singleFusedMapCache;
    }

    /**
     * Sets the tables.
     *
     * @param tables the tables
     */
    public void setTables(List<Map<String, Object>> tables)
    {
        myTables = tables;
    }

    /**
     * Set the tile info.
     *
     * @param tileInfo The tile info.
     */
    public void setTileInfo(TileInfo tileInfo)
    {
        myTileInfo = tileInfo;
    }
}
