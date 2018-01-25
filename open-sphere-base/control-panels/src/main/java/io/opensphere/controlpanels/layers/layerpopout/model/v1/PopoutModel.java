package io.opensphere.controlpanels.layers.layerpopout.model.v1;

import java.util.Set;
import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.opensphere.core.util.collections.New;

/**
 * The model used for the layour popout tool.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PopoutModelType")
@XmlRootElement(name = "PopoutModel")
public class PopoutModel
{
    /**
     * The data group info.
     */
    @XmlElement(name = "dataGroupInfoKeys")
    private final Set<String> myDataGroupInfoKeys = New.set();

    /**
     * The views height.
     */
    @XmlElement(name = "height")
    private int myHeight;

    /**
     * The model id.
     */
    @XmlElement(name = "id", type = String.class)
    @XmlJavaTypeAdapter(UUIDConverter.class)
    private final UUID myId = UUID.randomUUID();

    /**
     * The title for the pop out view.
     */
    @XmlElement(name = "title")
    private String myTitle;

    /**
     * The views width.
     */
    @XmlElement(name = "width")
    private int myWidth;

    /**
     * The views x location.
     */
    @XmlElement(name = "x")
    private int myX;

    /**
     * The views y location.
     */
    @XmlElement(name = "y")
    private int myY;

    /**
     * Gets the set of data group info ids being displayed for the pop out
     * window.
     *
     * @return The set of the data group info keys.
     */
    public Set<String> getDataGroupInfoKeys()
    {
        return myDataGroupInfoKeys;
    }

    /**
     * Gets the height of the view.
     *
     * @return The height.
     */
    public int getHeight()
    {
        return myHeight;
    }

    /**
     * Gets the id of this pop out model.
     *
     * @return The model id.
     */
    public UUID getId()
    {
        return myId;
    }

    /**
     * Gets the title.
     *
     * @return The title for the popout window.
     */
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * Gets the width of the view.
     *
     * @return The width.
     */
    public int getWidth()
    {
        return myWidth;
    }

    /**
     * Gets the x location of the view.
     *
     * @return The x location.
     */
    public int getX()
    {
        return myX;
    }

    /**
     * Gets the y location.
     *
     * @return The y location.
     */
    public int getY()
    {
        return myY;
    }

    /**
     * Sets the height of my view.
     *
     * @param height The height.
     */
    public void setHeight(int height)
    {
        myHeight = height;
    }

    /**
     * Sets the title for the pop out view.
     *
     * @param title The title.
     */
    public void setTitle(String title)
    {
        myTitle = title;
    }

    /**
     * Sets the width of the view.
     *
     * @param width The width.
     */
    public void setWidth(int width)
    {
        myWidth = width;
    }

    /**
     * Sets the x location.
     *
     * @param x The x location.
     */
    public void setX(int x)
    {
        myX = x;
    }

    /**
     * Sets the y location.
     *
     * @param y The y location.
     */
    public void setY(int y)
    {
        myY = y;
    }
}
