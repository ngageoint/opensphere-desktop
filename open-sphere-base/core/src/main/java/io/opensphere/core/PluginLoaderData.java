package io.opensphere.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB-generated file for unmarshalling a plugin-specific block in
 * pluginLoader.xml.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "myId", "myEnabled", "myClass", "myAppVersion", "myPluginVersion", "myDescription", "mySummary",
    "myAuthor", "myLanguage", "myRequiredPluginDependency", "myOptionalPluginDependency", "myPluginProperty" })
public class PluginLoaderData
{
    /** The id of the plug-in. */
    @XmlElement(name = "id")
    private String myId;

    /** The main plug-in class. */
    @XmlElement(name = "class")
    private String myClass;

    /** Version of the application that this plug-in is compatible with. */
    @XmlElement(name = "appVersion")
    private String myAppVersion;

    /** Version identifier of the plug-in. */
    @XmlElement(name = "pluginVersion")
    private String myPluginVersion;

    /** Description of this plug-in. */
    @XmlElement(name = "description")
    private String myDescription;

    /** Short summary of the plug-in's purpose. */
    @XmlElement(name = "summary")
    private String mySummary;

    /** Author of the plug-in. */
    @XmlElement(name = "author")
    private String myAuthor;

    /** Language used by the plug-in. */
    @XmlElement(name = "language")
    private String myLanguage;

    /** Identifiers of other plug-ins that this plug-in requires. */
    @XmlElement(name = "requiredPluginDependency")
    private List<String> myRequiredPluginDependency;

    /**
     * Identifiers of other plug-ins that this plug-in can use but does not
     * require.
     */
    @XmlElement(name = "optionalPluginDependency")
    private List<String> myOptionalPluginDependency;

    /** Configuration properties for the plug-in. */
    @XmlElement(name = "pluginProperty")
    private List<PluginProperty> myPluginProperty;

    /** Flag indicating if the plugin is enabled. */
    @XmlElement(name = "enabled")
    private boolean myEnabled = true;

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        PluginLoaderData other = (PluginLoaderData)obj;
        return Objects.equals(myId, other.myId);
    }

    /**
     * Gets the value of the appVersion property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAppVersion()
    {
        return myAppVersion;
    }

    /**
     * Gets the value of the author property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAuthor()
    {
        return myAuthor;
    }

    /**
     * Gets the value of the clazz property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getClazz()
    {
        return myClass;
    }

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getDescription()
    {
        return myDescription;
    }

    /**
     * Gets the value of the id property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getId()
    {
        return myId;
    }

    /**
     * Gets the value of the language property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getLanguage()
    {
        return myLanguage;
    }

    /**
     * Gets the value of the optionalPluginDependency property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the optionalPluginDependency property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getOptionalPluginDependency().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     *
     * @return The optional plug-ins.
     *
     */
    public List<String> getOptionalPluginDependency()
    {
        if (myOptionalPluginDependency == null)
        {
            myOptionalPluginDependency = new ArrayList<>();
        }
        return myOptionalPluginDependency;
    }

    /**
     * Gets the value of the pluginProperty property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the pluginProperty property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getPluginProperty().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PluginProperty }
     *
     * @return The plug-in properties.
     */
    public List<PluginProperty> getPluginProperty()
    {
        if (myPluginProperty == null)
        {
            myPluginProperty = new ArrayList<>();
        }
        return myPluginProperty;
    }

    /**
     * Gets the value of the pluginVersion property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPluginVersion()
    {
        return myPluginVersion;
    }

    /**
     * Gets the value of the requiredPluginDependency property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the requiredPluginDependency property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getRequiredPluginDependency().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     *
     * @return The required plug-ins.
     */
    public List<String> getRequiredPluginDependency()
    {
        if (myRequiredPluginDependency == null)
        {
            myRequiredPluginDependency = new ArrayList<>();
        }
        return myRequiredPluginDependency;
    }

    /**
     * Gets the value of the summary property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSummary()
    {
        return mySummary;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myId == null ? 0 : myId.hashCode());
        return result;
    }

    /**
     * Get if the plugin is enabled.
     *
     * @return If the plugin is enabled.
     */
    public boolean isEnabled()
    {
        return myEnabled;
    }

    /**
     * Sets the value of the appVersion property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAppVersion(String value)
    {
        myAppVersion = value;
    }

    /**
     * Sets the value of the author property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAuthor(String value)
    {
        myAuthor = value;
    }

    /**
     * Sets the value of the clazz property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setClazz(String value)
    {
        myClass = value;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setDescription(String value)
    {
        myDescription = value;
    }

    /**
     * Sets the plugin enabled.
     *
     * @param enabled The enabled flag.
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled = enabled;
    }

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setId(String value)
    {
        myId = value;
    }

    /**
     * Sets the value of the language property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setLanguage(String value)
    {
        myLanguage = value;
    }

    /**
     * Sets the value of the pluginVersion property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setPluginVersion(String value)
    {
        myPluginVersion = value;
    }

    /**
     * Sets the value of the summary property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setSummary(String value)
    {
        mySummary = value;
    }

    @Override
    public String toString()
    {
        return myId;
    }
}
