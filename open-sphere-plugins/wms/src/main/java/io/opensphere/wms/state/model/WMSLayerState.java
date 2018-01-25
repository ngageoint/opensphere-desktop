package io.opensphere.wms.state.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.opensphere.core.modulestate.TagList;
import io.opensphere.server.state.StateConstants;

/**
 * A state model class that contains specific values for a WMS layer that need
 * to be restored when loading a layer from a state.
 */
@XmlRootElement(name = StateConstants.LAYER_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
public class WMSLayerState
{
    /** The alpha channel for the tile color. */
    @XmlElement(name = "alpha")
    private float myAlpha = 1;

    /** The colorize style. */
    @XmlElement(name = "colorizeStyle")
    private String myColorizeStyle;

    /** Indicates if the image height is not editable. */
    @XmlElement(name = "fixedHeight")
    private boolean myFixedHeight;

    /** Indicates if the image width is not editable. */
    @XmlElement(name = "fixedWidth")
    private boolean myFixedWidth;

    /** The server get map url. */
    @XmlElement(name = "getMapUrl")
    private String myGetMapUrl;

    /** If preset the manually selected split level. */
    @XmlElement(name = "holdLevel")
    private Integer myHoldLevel;

    /** The layer id. */
    @XmlElement(name = "id")
    private String myId;

    /** True if the layer should load to the timeline, false if just static. */
    @XmlElement(name = "animate")
    private boolean myIsAnimate;

    /** The type of the layer. */
    @XmlAttribute(name = "type")
    private String myLayerType = StateConstants.WMS_LAYER_TYPE;

    /**
     * How large a tile is allowed to get before it is replaced with smaller
     * tile.
     */
    @XmlElement(name = "maxDisplaySize")
    private int myMaxDisplaySize;

    /**
     * How small a tile is allowed to get before it is replaced with a larger
     * tile.
     */
    @XmlElement(name = "minDisplaySize")
    private int myMinDisplaySize;

    /** Additional url parameters for the server. */
    @XmlElement(name = "params")
    private final Parameters myParameters = new Parameters();

    /** How many tile sizes are available. */
    @XmlElement(name = "splitLevels")
    private Integer mySplitLevels;

    /** Tags added by the user. */
    @XmlElement(name = "tags")
    private final TagList myTags = new TagList();

    /** The display name of this layer. */
    @XmlElement(name = "title")
    private String myTitle;

    /** General or terrain type. */
    @XmlElement(name = "type")
    private String myType;

    /** The server url. */
    @XmlElement(name = "url")
    private String myUrl;

    /** The layer visibility. */
    @XmlElement(name = "visible")
    private boolean myVisible;

    /**
     * Get the alpha.
     *
     * @return the alpha
     */
    public float getAlpha()
    {
        return myAlpha;
    }

    /**
     * Gets the colorize style.
     *
     * @return The colorize style.
     */
    public String getColorizeStyle()
    {
        return myColorizeStyle;
    }

    /**
     * Gets the server get map url.
     *
     * @return The get map url.
     */
    public String getGetMapUrl()
    {
        return myGetMapUrl;
    }

    /**
     * Gets the manually selected split level.
     *
     * @return The manually selected split level.
     */
    public Integer getHoldLevel()
    {
        return myHoldLevel;
    }

    /**
     * Gets the layer id.
     *
     * @return The layer id.
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the layer type.
     *
     * @return The layer type.
     */
    public String getLayerType()
    {
        return myLayerType;
    }

    /**
     * Gets how large a tile is allowed to get before it is replaced with
     * smaller tile.
     *
     * @return The maximum display size.
     */
    public int getMaxDisplaySize()
    {
        return myMaxDisplaySize;
    }

    /**
     * Gets how small a tile is allowed to get before it is replaced with a
     * larger tile.
     *
     * @return The minimum display size.
     */
    public int getMinDisplaySize()
    {
        return myMinDisplaySize;
    }

    /**
     * Gets the additional url parameters.
     *
     * @return The parameters.
     */
    public Parameters getParameters()
    {
        return myParameters;
    }

    /**
     * Gets how many tile sizes are available.
     *
     * @return The number of tile sizes available.
     */
    public Integer getSplitLevels()
    {
        return mySplitLevels;
    }

    /**
     * Gets the user added tags.
     *
     * @return The user added tags.
     */
    public List<String> getTags()
    {
        return myTags.getTags();
    }

    /**
     * Gets the layer title.
     *
     * @return The title.
     */
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * Gets either general type or terrain type.
     *
     * @return The layer type.
     */
    public String getType()
    {
        return myType;
    }

    /**
     * Gets the server url.
     *
     * @return The server url.
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Indicates if this layer should load in the timeline or not.
     *
     * @return True if it should load in the timeline, false otherwise.
     */
    public boolean isAnimate()
    {
        return myIsAnimate;
    }

    /**
     * Indicates if the image height is not editable.
     *
     * @return True if not editable, false if editable.
     */
    public boolean isFixedHeight()
    {
        return myFixedHeight;
    }

    /**
     * Indicates if the image width is not editable.
     *
     * @return True if not editable, false if editable.
     */
    public boolean isFixedWidth()
    {
        return myFixedWidth;
    }

    /**
     * Indicates if the layer is visible.
     *
     * @return True if visible, false otherwise.
     */
    public boolean isVisible()
    {
        return myVisible;
    }

    /**
     * Set the alpha.
     *
     * @param alpha the alpha to set
     */
    public void setAlpha(float alpha)
    {
        myAlpha = alpha;
    }

    /**
     * Sets the colorize style.
     *
     * @param colorizeStyle The colorize style.
     */
    public void setColorizeStyle(String colorizeStyle)
    {
        myColorizeStyle = colorizeStyle;
    }

    /**
     * Sets if the image height is not editable.
     *
     * @param fixedHeight True if not editable, false if editable.
     */
    public void setFixedHeight(boolean fixedHeight)
    {
        myFixedHeight = fixedHeight;
    }

    /**
     * Sets if the image width is not editable.
     *
     * @param fixedWidth True if not editable, false if editable.
     */
    public void setFixedWidth(boolean fixedWidth)
    {
        myFixedWidth = fixedWidth;
    }

    /**
     * Sets the get map url.
     *
     * @param getMapUrl The get map url.
     */
    public void setGetMapUrl(String getMapUrl)
    {
        myGetMapUrl = getMapUrl;
    }

    /**
     * Sets the manually selected split level.
     *
     * @param holdLevel The manually selected split level.
     */
    public void setHoldLevel(Integer holdLevel)
    {
        myHoldLevel = holdLevel;
    }

    /**
     * Sets the layer id.
     *
     * @param id The layer id.
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Sets if this layer should load to the timeline or not.
     *
     * @param isAnimate True if it loads to the timeline.
     */
    public void setIsAnimate(boolean isAnimate)
    {
        myIsAnimate = isAnimate;
    }

    /**
     * Sets the layer type.
     *
     * @param layerType The layer type.
     */
    public void setLayerType(String layerType)
    {
        myLayerType = layerType;
    }

    /**
     * Sets how large a tile is allowed to get before it is replaced with
     * smaller tile.
     *
     * @param maxDisplaySize The maximum display size.
     */
    public void setMaxDisplaySize(int maxDisplaySize)
    {
        myMaxDisplaySize = maxDisplaySize;
    }

    /**
     * Sets how small a tile is allowed to get before it is replaced with a
     * larger tile.
     *
     * @param minDisplaySize The minimum display size.
     */
    public void setMinDisplaySize(int minDisplaySize)
    {
        myMinDisplaySize = minDisplaySize;
    }

    /**
     * Sets how many tile sizes are available.
     *
     * @param splitLevels The number of tile sizes available.
     */
    public void setSplitLevels(Integer splitLevels)
    {
        mySplitLevels = splitLevels;
    }

    /**
     * Sets the layer title.
     *
     * @param title The title.
     */
    public void setTitle(String title)
    {
        myTitle = title;
    }

    /**
     * Sets the layer type.
     *
     * @param type The layer type.
     */
    public void setType(String type)
    {
        myType = type;
    }

    /**
     * Sets the server url.
     *
     * @param url The server url.
     */
    public void setUrl(String url)
    {
        myUrl = url;
    }

    /**
     * Sets the layer visibility.
     *
     * @param visible True if visible, false otherwise.
     */
    public void setVisible(boolean visible)
    {
        myVisible = visible;
    }
}
