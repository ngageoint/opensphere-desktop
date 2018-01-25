package io.opensphere.core.collada.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA profile common.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileCommon
{
    /** The new params. */
    @XmlElement(name = "newparam")
    private List<NewParam> myNewParams;

    /** The technique. */
    @XmlElement(name = "technique")
    private Technique myTechnique;

    /**
     * Gets the new params.
     *
     * @return the new params
     */
    public List<NewParam> getNewParams()
    {
        return myNewParams;
    }

    /**
     * Gets the technique.
     *
     * @return the technique
     */
    public Technique getTechnique()
    {
        return myTechnique;
    }
}
