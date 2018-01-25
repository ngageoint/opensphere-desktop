package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA color base tag.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ColorBase
{
    /** The color. */
    @XmlElement(name = "color")
    private String myColor;

    /**
     * Gets the color.
     *
     * @return the color
     */
    public String getColor()
    {
        return myColor;
    }
}
