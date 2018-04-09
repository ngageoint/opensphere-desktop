//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 12:20:41 PM MST 
//

package net.opengis.wms_130;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wms}Service"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Capability"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.3.0" />
 *       &lt;attribute name="updateSequence" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "service", "capability" })
@XmlRootElement(name = "WMS_Capabilities")
public class WMSCapabilities
{

    @XmlElement(name = "Service", required = true)
    protected Service service;

    @XmlElement(name = "Capability", required = true)
    protected Capability capability;

    @XmlAttribute
    protected String version;

    @XmlAttribute
    protected String updateSequence;

    /**
     * Gets the value of the service property.
     * 
     * @return possible object is {@link Service }
     * 
     */
    public Service getService()
    {
        return service;
    }

    /**
     * Sets the value of the service property.
     * 
     * @param value allowed object is {@link Service }
     * 
     */
    public void setService(Service value)
    {
        this.service = value;
    }

    /**
     * Gets the value of the capability property.
     * 
     * @return possible object is {@link Capability }
     * 
     */
    public Capability getCapability()
    {
        return capability;
    }

    /**
     * Sets the value of the capability property.
     * 
     * @param value allowed object is {@link Capability }
     * 
     */
    public void setCapability(Capability value)
    {
        this.capability = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getVersion()
    {
        if (version == null)
        {
            return "1.3.0";
        }
        else
        {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setVersion(String value)
    {
        this.version = value;
    }

    /**
     * Gets the value of the updateSequence property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getUpdateSequence()
    {
        return updateSequence;
    }

    /**
     * Sets the value of the updateSequence property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setUpdateSequence(String value)
    {
        this.updateSequence = value;
    }

}
