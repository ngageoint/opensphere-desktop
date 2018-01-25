package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * COLLADA images.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Images
{
    /** The images. */
    @XmlElement(name = "image")
    private List<Image> myImages;

    /**
     * Gets the images.
     *
     * @return the images
     */
    public List<Image> getImages()
    {
        return myImages;
    }
}
