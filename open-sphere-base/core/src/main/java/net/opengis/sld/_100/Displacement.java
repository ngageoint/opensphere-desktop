//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.10.06 at 03:53:52 PM EDT 
//

package net.opengis.sld._100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element ref="{http://www.opengis.net/sld}DisplacementX"/>
 *         &lt;element ref="{http://www.opengis.net/sld}DisplacementY"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "displacementX", "displacementY" })
@XmlRootElement(name = "Displacement")
public class Displacement
{

    @XmlElement(name = "DisplacementX", required = true)
    protected ParameterValueType displacementX;

    @XmlElement(name = "DisplacementY", required = true)
    protected ParameterValueType displacementY;

    /**
     * Gets the value of the displacementX property.
     * 
     * @return possible object is {@link ParameterValueType }
     * 
     */
    public ParameterValueType getDisplacementX()
    {
        return displacementX;
    }

    /**
     * Sets the value of the displacementX property.
     * 
     * @param value allowed object is {@link ParameterValueType }
     * 
     */
    public void setDisplacementX(ParameterValueType value)
    {
        this.displacementX = value;
    }

    /**
     * Gets the value of the displacementY property.
     * 
     * @return possible object is {@link ParameterValueType }
     * 
     */
    public ParameterValueType getDisplacementY()
    {
        return displacementY;
    }

    /**
     * Sets the value of the displacementY property.
     * 
     * @param value allowed object is {@link ParameterValueType }
     * 
     */
    public void setDisplacementY(ParameterValueType value)
    {
        this.displacementY = value;
    }

}
