//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.gml._311;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a coordinate tuple in one, two, or three dimensions. Deprecated
 * with GML 3.0 and replaced by DirectPositionType.
 * 
 * <p>
 * Java class for CoordType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CoordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="X" type="{http://www.w3.org/2001/XMLSchema}decimal"/>
 *         &lt;element name="Y" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *         &lt;element name="Z" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoordType", propOrder = { "x", "y", "z" })
public class CoordType
{

    @XmlElement(name = "X", required = true)
    protected BigDecimal x;

    @XmlElement(name = "Y")
    protected BigDecimal y;

    @XmlElement(name = "Z")
    protected BigDecimal z;

    /**
     * Gets the value of the x property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getX()
    {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setX(BigDecimal value)
    {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getY()
    {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setY(BigDecimal value)
    {
        this.y = value;
    }

    /**
     * Gets the value of the z property.
     * 
     * @return possible object is {@link BigDecimal }
     * 
     */
    public BigDecimal getZ()
    {
        return z;
    }

    /**
     * Sets the value of the z property.
     * 
     * @param value allowed object is {@link BigDecimal }
     * 
     */
    public void setZ(BigDecimal value)
    {
        this.z = value;
    }

}
