package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * A reference to a COLLADA visual_scene.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class InstanceVisualScene
{
    /** The url for the visual_scene. */
    @XmlAttribute(name = "url")
    private String myUrl;

    /**
     * Get the url.
     *
     * @return The url.
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Set the url.
     *
     * @param url The url.
     */
    public void setUrl(String url)
    {
        myUrl = url;
    }
}
