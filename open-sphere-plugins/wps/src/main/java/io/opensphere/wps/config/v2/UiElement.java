package io.opensphere.wps.config.v2;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** A UI element. */
@XmlRootElement(name = "uiElement")
@XmlAccessorType(XmlAccessType.NONE)
public class UiElement
{
    /** The UI component. */
    @XmlElement(name = "uiComponent")
    private String myUiComponent;

    /** The identifier. */
    @XmlElement(name = "identifier")
    private final List<String> myIdentifiers = new ArrayList<>(2);

    /** The title (display name). */
    @XmlElement(name = "title")
    private String myTitle;

    /** The abstract (tooltip). */
    @XmlElement(name = "abstract")
    private String myAbstract;

    /** The default value. */
    @XmlElement(name = "defaultValue")
    private String myDefaultValue;

    /** The units. */
    @XmlElement(name = "units")
    private String myUnits;

    /**
     * Gets the uiComponent.
     *
     * @return the uiComponent
     */
    public String getUiComponent()
    {
        return myUiComponent;
    }

    /**
     * Sets the uiComponent.
     *
     * @param uiComponent the uiComponent
     */
    public void setUiComponent(String uiComponent)
    {
        myUiComponent = uiComponent;
    }

    /**
     * Gets the identifier.
     *
     * @return the identifier
     */
    public String getIdentifier()
    {
        return myIdentifiers.get(0);
    }

    /**
     * Gets the identifiers.
     *
     * @return the identifiers
     */
    public List<String> getIdentifiers()
    {
        return myIdentifiers;
    }

    /**
     * Sets the identifier.
     *
     * @param identifier the identifier
     */
    public void setIdentifier(String identifier)
    {
        myIdentifiers.clear();
        myIdentifiers.add(identifier);
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle()
    {
        return myTitle;
    }

    /**
     * Sets the title.
     *
     * @param title the title
     */
    public void setTitle(String title)
    {
        myTitle = title;
    }

    /**
     * Gets the abstract.
     *
     * @return the abstract
     */
    public String getAbstract()
    {
        return myAbstract;
    }

    /**
     * Sets the abstract.
     *
     * @param abstract1 the abstract
     */
    public void setAbstract(String abstract1)
    {
        myAbstract = abstract1;
    }

    /**
     * Gets the defaultValue.
     *
     * @return the defaultValue
     */
    public String getDefaultValue()
    {
        return myDefaultValue;
    }

    /**
     * Sets the defaultValue.
     *
     * @param defaultValue the defaultValue
     */
    public void setDefaultValue(String defaultValue)
    {
        myDefaultValue = defaultValue;
    }

    /**
     * Gets the units.
     *
     * @return the units
     */
    public String getUnits()
    {
        return myUnits;
    }

    /**
     * Sets the units.
     *
     * @param units the units
     */
    public void setUnits(String units)
    {
        myUnits = units;
    }
}
