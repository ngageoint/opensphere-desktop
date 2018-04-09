//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.gml._311;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * Curve segment defines a homogeneous segment of a curve.
 * 
 * <p>
 * Java class for AbstractCurveSegmentType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractCurveSegmentType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="numDerivativesAtStart" type="{http://www.w3.org/2001/XMLSchema}integer" default="0" />
 *       &lt;attribute name="numDerivativesAtEnd" type="{http://www.w3.org/2001/XMLSchema}integer" default="0" />
 *       &lt;attribute name="numDerivativeInterior" type="{http://www.w3.org/2001/XMLSchema}integer" default="0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractCurveSegmentType")
@XmlSeeAlso({ GeodesicStringType.class, LineStringSegmentType.class, ArcStringType.class, BSplineType.class,
    OffsetCurveType.class, ClothoidType.class, CubicSplineType.class, ArcByCenterPointType.class, ArcStringByBulgeType.class })
public abstract class AbstractCurveSegmentType
{

    @XmlAttribute(name = "numDerivativesAtStart")
    protected BigInteger numDerivativesAtStart;

    @XmlAttribute(name = "numDerivativesAtEnd")
    protected BigInteger numDerivativesAtEnd;

    @XmlAttribute(name = "numDerivativeInterior")
    protected BigInteger numDerivativeInterior;

    /**
     * Gets the value of the numDerivativesAtStart property.
     * 
     * @return possible object is {@link BigInteger }
     * 
     */
    public BigInteger getNumDerivativesAtStart()
    {
        if (numDerivativesAtStart == null)
        {
            return new BigInteger("0");
        }
        else
        {
            return numDerivativesAtStart;
        }
    }

    /**
     * Sets the value of the numDerivativesAtStart property.
     * 
     * @param value allowed object is {@link BigInteger }
     * 
     */
    public void setNumDerivativesAtStart(BigInteger value)
    {
        this.numDerivativesAtStart = value;
    }

    /**
     * Gets the value of the numDerivativesAtEnd property.
     * 
     * @return possible object is {@link BigInteger }
     * 
     */
    public BigInteger getNumDerivativesAtEnd()
    {
        if (numDerivativesAtEnd == null)
        {
            return new BigInteger("0");
        }
        else
        {
            return numDerivativesAtEnd;
        }
    }

    /**
     * Sets the value of the numDerivativesAtEnd property.
     * 
     * @param value allowed object is {@link BigInteger }
     * 
     */
    public void setNumDerivativesAtEnd(BigInteger value)
    {
        this.numDerivativesAtEnd = value;
    }

    /**
     * Gets the value of the numDerivativeInterior property.
     * 
     * @return possible object is {@link BigInteger }
     * 
     */
    public BigInteger getNumDerivativeInterior()
    {
        if (numDerivativeInterior == null)
        {
            return new BigInteger("0");
        }
        else
        {
            return numDerivativeInterior;
        }
    }

    /**
     * Sets the value of the numDerivativeInterior property.
     * 
     * @param value allowed object is {@link BigInteger }
     * 
     */
    public void setNumDerivativeInterior(BigInteger value)
    {
        this.numDerivativeInterior = value;
    }

}
