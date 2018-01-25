package io.opensphere.controlpanels.animation.state;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The live mode save state jaxb object.
 */
@XmlRootElement(name = "time")
@XmlAccessorType(XmlAccessType.FIELD)
public class LiveState
{
    /**
     * True if live, false otherwise.
     */
    @XmlElement(name = "live")
    private boolean myLive;

    /**
     * Gets the live mode state value.
     *
     * @return True if live.
     */
    public boolean isLive()
    {
        return myLive;
    }

    /**
     * Sets the live mode state value.
     *
     * @param live True if live.
     */
    public void setLive(boolean live)
    {
        myLive = live;
    }
}
