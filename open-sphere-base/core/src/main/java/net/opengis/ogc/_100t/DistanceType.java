//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.02.22 at 10:22:41 AM MST 
//

package net.opengis.ogc._100t;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * <p>
 * Java class for DistanceType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="DistanceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="units" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DistanceType", propOrder = { "content" })
public class DistanceType
{

    @XmlValue
    protected String content;

    @XmlAttribute(name = "units", required = true)
    protected String units;

    /**
     * Gets the value of the content property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getContent()
    {
        return content;
    }

    /**
     * Sets the value of the content property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setContent(String value)
    {
        this.content = value;
    }

    public boolean isSetContent()
    {
        return (this.content != null);
    }

    /**
     * Gets the value of the units property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getUnits()
    {
        return units;
    }

    /**
     * Sets the value of the units property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setUnits(String value)
    {
        this.units = value;
    }

    public boolean isSetUnits()
    {
        return (this.units != null);
    }

}
