package io.opensphere.wms.config.v1;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * Configuration entry for all settings related to a particular layer.
 */
@XmlRootElement(name = "LayerConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class WMSLayerConfig implements Cloneable, Serializable
{
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /** The Constant LAYER_SEPARATOR. */
    public static final String LAYERNAME_SEPARATOR = "!!";

    /**
     * Bounds allowed for this layer Provided in the GetCapabilities document.
     */
    @XmlTransient
    private WMSBoundingBoxConfig myBoundingBoxConfig;

    // TODO Add cache preservation time/no cache option

    /**
     * Image format in the tile cache.
     */
    @XmlElement(name = "CacheImageFormat")
    private String myCacheImageFormat;

    /**
     * Configuration related to how this layer is handled/rendered.
     */
    @XmlElement(name = "DisplayConfig")
    private WMSLayerDisplayConfig myDisplayConfig;

    /**
     * This layer provides tiles only in this pixel height Provided in the
     * GetCapabilities document.
     */
    @XmlElement(name = "FixedHeight")
    private Integer myFixedHeight;

    /**
     * This layer provides tiles only in this pixel width Provided in the
     * GetCapabilities document.
     */
    @XmlElement(name = "FixedWidth")
    private Integer myFixedWidth;

    /**
     * Configuration related to WMS GetMap requests.
     */
    @XmlElement(name = "GetMapConfig")
    private WMSLayerGetMapConfig myGetMapConfig;

    /**
     * The name of the layer Provided in the GetCapabilities document.
     */
    @XmlElement(name = "LayerName")
    private String myLayerName;

    /**
     * Transient field that holds the layer key.
     */
    @XmlElement(name = "LayerKey")
    private String myLayerKey;

    /** The Human-readable layer name provided in the GetCapabilities doc. */
    @XmlElement(name = "LayerTitle")
    private String myLayerTitle;

    /**
     * Type type of data provided by the layer.
     */
    @XmlElement(name = "LayerType")
    private LayerType myLayerType = LayerType.General;

    /**
     * This layer only provides a single image tile which can be displayed.
     * Provided in the GetCapabilities document.
     */
    @XmlElement(name = "NoSubsets")
    private boolean myNoSubsets;

    /**
     * Determines whether a layer supports GetFeatureInfo requests.
     */
    @XmlElement(name = "Queryable")
    private boolean myQueryable;

    /**
     * The times for which data is available on this layer. This field also
     * gives the periodicity for animation purposes. This is provided in the
     * GetCapabilities document and is dynamic, so should not be saved.
     */
    @XmlTransient
    private TimeSpan myTimeExtent;

    /** Flag indicating if the layers parameter will be added to the URL. */
    @XmlTransient
    private boolean myLayersParameterEnabled = true;

    @Override
    public WMSLayerConfig clone()
    {
        WMSLayerConfig clone;
        try
        {
            clone = (WMSLayerConfig)super.clone();

            clone.setBoundingBoxConfig(myBoundingBoxConfig.clone());
            if (myDisplayConfig != null)
            {
                clone.setDisplayConfig(myDisplayConfig.clone());
            }
            if (myGetMapConfig != null)
            {
                clone.setGetMapConfig(myGetMapConfig.clone());
            }
        }
        catch (CloneNotSupportedException e)
        {
            throw new AssertionError(e);
        }
        return clone;
    }

    /**
     * Test whether the user-configurable fields of this class are equal. This
     * will determine whether the configuration needs to be persisted between
     * sessions.
     *
     * NOTE: This is not the same as equals(). It will not compare fields that
     * are pulled from the server directly (e.g. myBoundingBoxConfig or
     * myTimeExtent) or are only set programmatically (e.g.
     * myLayersParameterEnabled).
     *
     * @param other another object of this type to compare against
     * @return true, if the user-configurable fields are all equal
     */
    public boolean configurableFieldsEqual(WMSLayerConfig other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null)
        {
            return false;
        }
        if (!EqualsHelper.equals(myGetMapConfig, other.myGetMapConfig, myLayerType, other.myLayerType))
        {
            return false;
        }

        // The display config could be either null or un-edited.
        WMSLayerDisplayConfig defaultDisplayConfig = new WMSLayerDisplayConfig();
        if (myDisplayConfig == null || myDisplayConfig.equals(defaultDisplayConfig))
        {
            return other.myDisplayConfig == null || other.myDisplayConfig.equals(defaultDisplayConfig);
        }
        return myDisplayConfig.equals(other.myDisplayConfig);
    }

    /**
     * Get the boundingBoxConfig.
     *
     * @return the boundingBoxConfig
     */
    public WMSBoundingBoxConfig getBoundingBoxConfig()
    {
        return myBoundingBoxConfig;
    }

    /**
     * Get the cacheImageFormat.
     *
     * @return the cacheImageFormat
     */
    public String getCacheImageFormat()
    {
        return myCacheImageFormat;
    }

    /**
     * Get displayConfig.
     *
     * @return the displayConfig
     */
    public WMSLayerDisplayConfig getDisplayConfig()
    {
        if (myDisplayConfig == null)
        {
            myDisplayConfig = new WMSLayerDisplayConfig();
        }
        return myDisplayConfig;
    }

    /**
     * Get the fixedHeight.
     *
     * @return the fixedHeight
     */
    public Integer getFixedHeight()
    {
        return myFixedHeight;
    }

    /**
     * Get the fixedWidth.
     *
     * @return the fixedWidth
     */
    public Integer getFixedWidth()
    {
        return myFixedWidth;
    }

    /**
     * Get getMapConfig.
     *
     * @return the getMapConfig
     */
    public WMSLayerGetMapConfig getGetMapConfig()
    {
        if (myGetMapConfig == null)
        {
            myGetMapConfig = new WMSLayerGetMapConfig();
        }
        return myGetMapConfig;
    }

    /**
     * Gets the layer key.
     *
     * @return the layer key
     */
    public String getLayerKey()
    {
        return myLayerKey;
    }

    /**
     * Get the layerName.
     *
     * @return the layerName
     */
    public String getLayerName()
    {
        return myLayerName;
    }

    /**
     * Get the layerTitle.
     *
     * @return the layerTitle
     */
    public String getLayerTitle()
    {
        return myLayerTitle;
    }

    /**
     * Get the layerType.
     *
     * @return the layerType
     */
    public LayerType getLayerType()
    {
        return myLayerType;
    }

    /**
     * Get the timeExtent.
     *
     * @return the timeExtent
     */
    public TimeSpan getTimeExtent()
    {
        if (myTimeExtent == null)
        {
            return TimeSpan.TIMELESS;
        }
        return myTimeExtent;
    }

    /**
     * Get if the layers parameter should be added to the URL.
     *
     * @return Flag indicating if the layers parameter is enabled.
     */
    public boolean isLayersParameterEnabled()
    {
        return myLayersParameterEnabled;
    }

    /**
     * Get the noSubsets.
     *
     * @return the noSubsets
     */
    public boolean isNoSubsets()
    {
        return myNoSubsets;
    }

    /**
     * Checks if this layer is queryable.
     *
     * @return true, if is queryable
     */
    public boolean isQueryable()
    {
        return myQueryable;
    }

    /**
     * Set the boundingBoxConfig.
     *
     * @param boundingBoxConfig the boundingBoxConfig to set
     */
    public void setBoundingBoxConfig(WMSBoundingBoxConfig boundingBoxConfig)
    {
        myBoundingBoxConfig = boundingBoxConfig;
    }

    /**
     * Set the cacheImageFormat.
     *
     * @param cacheImageFormat the cacheImageFormat to set
     */
    public void setCacheImageFormat(String cacheImageFormat)
    {
        myCacheImageFormat = cacheImageFormat;
    }

    /**
     * Set the displayConfig.
     *
     * @param displayConfig the displayConfig to set
     */
    public void setDisplayConfig(WMSLayerDisplayConfig displayConfig)
    {
        myDisplayConfig = displayConfig;
    }

    /**
     * Set the fixedHeight.
     *
     * @param fixedHeight the fixedHeight to set
     */
    public void setFixedHeight(Integer fixedHeight)
    {
        myFixedHeight = fixedHeight;
    }

    /**
     * Set the fixedWidth.
     *
     * @param fixedWidth the fixedWidth to set
     */
    public void setFixedWidth(Integer fixedWidth)
    {
        myFixedWidth = fixedWidth;
    }

    /**
     * Set the getMapConfig.
     *
     * @param getMapConfig the getMapConfig to set
     */
    public void setGetMapConfig(WMSLayerGetMapConfig getMapConfig)
    {
        myGetMapConfig = getMapConfig;
    }

    /**
     * Sets the layer key.
     *
     * @param layerKey the new layer key
     */
    public void setLayerKey(String layerKey)
    {
        myLayerKey = layerKey;
    }

    /**
     * Set the layerName.
     *
     * @param layerName the layerName to set
     */
    public void setLayerName(String layerName)
    {
        myLayerName = layerName;
    }

    /**
     * Set if the layers parameter should be added to the URL.
     *
     * @param enabled Flag indicating if the layers parameter is enabled.
     */
    public void setLayersParameterEnabled(boolean enabled)
    {
        myLayersParameterEnabled = enabled;
    }

    /**
     * Set the layerTitle.
     *
     * @param layerTitle the layerTitle to set
     */
    public void setLayerTitle(String layerTitle)
    {
        myLayerTitle = layerTitle;
    }

    /**
     * Set the layerType.
     *
     * @param layerType the layerType to set
     */
    public void setLayerType(LayerType layerType)
    {
        myLayerType = layerType;
    }

    /**
     * Set the noSubsets.
     *
     * @param noSubsets the noSubsets to set
     */
    public void setNoSubsets(boolean noSubsets)
    {
        myNoSubsets = noSubsets;
    }

    /**
     * Sets the flag indicating whether the layer supports GetFeatureInfo
     * requests.
     *
     * @param queryable the new queryable flag
     */
    public void setQueryable(boolean queryable)
    {
        myQueryable = queryable;
    }

    /**
     * Set the timeExtent.
     *
     * @param timeExtent the timeExtent to set
     */
    public void setTimeExtent(TimeSpan timeExtent)
    {
        myTimeExtent = timeExtent;
    }

    /** Available types for layers. */
    public enum LayerType
    {
        /** A regular WMS Layer. */
        General(MapVisualizationType.IMAGE_TILE),

        /**
         * A Layer which provides SRTM data. It is currently assumed that this
         * will be packed shorts in BIL format.
         */
        SRTM(MapVisualizationType.TERRAIN_TILE),

        /** A layer which provides Lidar data. */
        LIDAR(MapVisualizationType.TERRAIN_TILE);

        /** The map visualization type. */
        private final MapVisualizationType myMapVisualizationType;

        /**
         * Constructor.
         *
         * @param mapVisualizationType The map visualization type
         */
        private LayerType(MapVisualizationType mapVisualizationType)
        {
            myMapVisualizationType = mapVisualizationType;
        }

        /**
         * Gets the map visualization type.
         *
         * @return the map visualization type
         */
        public MapVisualizationType getMapVisualizationType()
        {
            return myMapVisualizationType;
        }
    }
}
