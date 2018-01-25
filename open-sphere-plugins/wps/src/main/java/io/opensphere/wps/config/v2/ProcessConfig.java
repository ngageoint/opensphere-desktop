package io.opensphere.wps.config.v2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** A process config entry. */
@XmlRootElement(name = "process")
@XmlAccessorType(XmlAccessType.NONE)
public class ProcessConfig
{
    /** The identifier. */
    @XmlAttribute(name = "identifier")
    private String myIdentifier;

    /** The list of UI elements. */
    @XmlElement(name = "uiElement")
    private final List<UiElement> myUiElements = new ArrayList<>();

    /**
     * Gets the identifier.
     *
     * @return the identifier
     */
    public String getIdentifier()
    {
        return myIdentifier;
    }

    /**
     * Sets the identifier.
     *
     * @param identifier the identifier
     */
    public void setIdentifier(String identifier)
    {
        myIdentifier = identifier;
    }

    /**
     * Gets the uiElements.
     *
     * @return the uiElements
     */
    public List<UiElement> getUiElements()
    {
        return myUiElements;
    }
}
