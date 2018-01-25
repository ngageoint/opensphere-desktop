package io.opensphere.core.collada.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * A COLLADA library effect.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class Effect
{
    /** The ID. */
    @XmlAttribute(name = "id")
    private String myId;

    /** The profile common. */
    @XmlElement(name = "profile_COMMON")
    private ProfileCommon myProfileCommon;

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the profileCommon.
     *
     * @return the profileCommon
     */
    public ProfileCommon getProfileCommon()
    {
        return myProfileCommon;
    }
}
