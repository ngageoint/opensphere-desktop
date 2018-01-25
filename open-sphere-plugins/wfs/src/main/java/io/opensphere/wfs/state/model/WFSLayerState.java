package io.opensphere.wfs.state.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.modulestate.TagList;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.server.state.StateConstants;
import io.opensphere.wfs.util.WFSConstants;

/**
 * A state model class that contains specific values for a WFS layer that need
 * to be restored when loading a layer from a state.
 */
@XmlRootElement(name = StateConstants.LAYER_NAME)
@XmlAccessorType(XmlAccessType.FIELD)
public class WFSLayerState
{
    /** Indicates whether or not this layer can participate in animations. */
    @XmlElement(name = "animate")
    private boolean myAnimate;

    /** The Basic feature style. */
    @XmlElement(name = "basicFeatureStyle")
    private BasicFeatureStyle myBasicFeatureStyle;

    /** The set of disabled columns. */
    @XmlElement(name = "disabledColumns")
    private List<String> myDisabledColumns = New.list();

    /** If empty columns are disabled. */
    @XmlElement(name = "disabledEmptyColumns")
    private boolean myDisableEmptyColumns;

    /** The Display name. */
    @XmlElement(name = "title")
    private String myDisplayName;

    /** The Ellipse style. */
    @XmlElement(name = "ellipseStyle")
    private EllipseStyle myEllipseStyle;

    /** The Icon style. */
    @XmlElement(name = "iconStyle")
    private IconStyle myIconStyle;

    /** The id. */
    @XmlElement(name = "id")
    private String myId;

    /** The Line of bearing style. */
    @XmlElement(name = "lobStyle")
    private LineOfBearingStyle myLineOfBearingStyle;

    /** The Server id. */
    @XmlElement(name = "serverId")
    private String myServerId;

    /** Tags added by the user. */
    @XmlElement(name = "tags")
    private TagList myTags = new TagList();

    /** The layer type. */
    @XmlAttribute(name = "type")
    private String myType = StateConstants.WFS_LAYER_TYPE;

    /** The server URL. */
    @XmlElement(name = "url")
    private String myUrl;

    /** The visibility flag. */
    @XmlElement(name = "visible")
    private boolean myVisible = true;

    /** The WFS parameters. */
    @XmlElement(name = "params")
    private WFSStateParameters myWFSParameters;

    /** The loads to. */
    @XmlTransient
    private LoadsTo myLoadsTo;

    // TODO: Add spike style

    /**
     * Gets the basic feature style.
     *
     * @return the basic feature style
     */
    public BasicFeatureStyle getBasicFeatureStyle()
    {
        return myBasicFeatureStyle;
    }

    /**
     * Gets the disabled columns.
     *
     * @return the disabled columns
     */
    public List<String> getDisabledColumns()
    {
        return myDisabledColumns;
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    public String getDisplayName()
    {
        return myDisplayName;
    }

    /**
     * Gets the ellipse style.
     *
     * @return the ellipse style
     */
    public EllipseStyle getEllipseStyle()
    {
        return myEllipseStyle;
    }

    /**
     * Gets the icon style.
     *
     * @return the icon style
     */
    public IconStyle getIconStyle()
    {
        return myIconStyle;
    }

    /**
     * Gets the id.
     *
     * @return The id.
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the line of bearing style.
     *
     * @return the line of bearing style
     */
    public LineOfBearingStyle getLineOfBearingStyle()
    {
        return myLineOfBearingStyle;
    }

    /**
     * Gets the server id.
     *
     * @return the server id
     */
    public String getServerId()
    {
        return myServerId;
    }

    /**
     * Gets the tags.
     *
     * @return the tags
     */
    public TagList getTags()
    {
        return myTags;
    }

    /**
     * Gets the layer type.
     *
     * @return The layer type.
     */
    public String getType()
    {
        return myType;
    }

    /**
     * Gets the type key.
     *
     * @return the type key
     */
    public String getTypeKey()
    {
        String typeName = getWFSParameters().getTypeName();
        String[] typeNameTok = typeName.split(":");
        if (typeNameTok.length > 1)
        {
            typeName = typeNameTok[typeNameTok.length - 1];
        }
        String url = getUrl();

        return url + WFSConstants.LAYERNAME_SEPARATOR + typeName;
    }

    /**
     * Gets the URL.
     *
     * @return the URL
     */
    public String getUrl()
    {
        return myUrl.replace("wps", "wfs");
    }

    /**
     * Gets the URL that will be serialized.
     *
     * @return The serialized URL.
     */
    public String getUrlUnmodified()
    {
        return myUrl;
    }

    /**
     * Gets the wFS parameters.
     *
     * @return the wFS parameters
     */
    public WFSStateParameters getWFSParameters()
    {
        return myWFSParameters;
    }

    /**
     * Checks if is animate.
     *
     * @return true, if is animate
     */
    public boolean isAnimate()
    {
        return myAnimate;
    }

    /**
     * Checks if is disable empty columns.
     *
     * @return true, if is disable empty columns
     */
    public boolean isDisableEmptyColumns()
    {
        return myDisableEmptyColumns;
    }

    /**
     * Checks if is empty columns disabled.
     *
     * @return true, if is empty columns disabled
     */
    public boolean isEmptyColumnsDisabled()
    {
        return myDisableEmptyColumns;
    }

    /**
     * Checks if is visible.
     *
     * @return true, if is visible
     */
    public boolean isVisible()
    {
        return myVisible;
    }

    /**
     * Sets the animate.
     *
     * @param animate the new animate
     */
    public void setAnimate(boolean animate)
    {
        myAnimate = animate;
    }

    /**
     * Sets the basic feature style.
     *
     * @param style the new basic feature style
     */
    public void setBasicFeatureStyle(BasicFeatureStyle style)
    {
        myBasicFeatureStyle = style;
    }

    /**
     * Sets the disabled columns.
     *
     * @param disabledColumns the new disabled columns
     */
    public void setDisabledColumns(List<String> disabledColumns)
    {
        myDisabledColumns = disabledColumns;
    }

    /**
     * Sets the disable empty columns.
     *
     * @param enabled the new disable empty columns
     */
    public void setDisableEmptyColumns(boolean enabled)
    {
        myDisableEmptyColumns = enabled;
    }

    /**
     * Sets the display name.
     *
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName)
    {
        myDisplayName = displayName;
    }

    /**
     * Sets the ellipse style.
     *
     * @param ellipseStyle the new ellipse style
     */
    public void setEllipseStyle(EllipseStyle ellipseStyle)
    {
        myEllipseStyle = ellipseStyle;
    }

    /**
     * Sets the icon style.
     *
     * @param iconStyle the new icon style
     */
    public void setIconStyle(IconStyle iconStyle)
    {
        myIconStyle = iconStyle;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id)
    {
        myId = id;
    }

    /**
     * Sets the line of bearing style.
     *
     * @param lineOfBearingStyle the new line of bearing style
     */
    public void setLineOfBearingStyle(LineOfBearingStyle lineOfBearingStyle)
    {
        myLineOfBearingStyle = lineOfBearingStyle;
    }

    /**
     * Sets the server id.
     *
     * @param id the new server id
     */
    public void setServerId(String id)
    {
        myServerId = id;
    }

    /**
     * Sets the tags.
     *
     * @param tags the new tags
     */
    public void setTags(TagList tags)
    {
        myTags = tags;
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
     * Sets the URL.
     *
     * @param url the new URL
     */
    public void setUrl(String url)
    {
        myUrl = url;
    }

    /**
     * Sets the visible.
     *
     * @param visible the new visible
     */
    public void setVisible(boolean visible)
    {
        myVisible = visible;
    }

    /**
     * Sets the wFS parameters.
     *
     * @param params the new wFS parameters
     */
    public void setWFSParameters(WFSStateParameters params)
    {
        myWFSParameters = params;
    }

    /**
     * Gets the loadsTo.
     *
     * @return the loadsTo
     */
    public LoadsTo getLoadsTo()
    {
        return myLoadsTo;
    }

    /**
     * Sets the loadsTo.
     *
     * @param loadsTo the loadsTo
     */
    public void setLoadsTo(LoadsTo loadsTo)
    {
        myLoadsTo = loadsTo;
    }
}
