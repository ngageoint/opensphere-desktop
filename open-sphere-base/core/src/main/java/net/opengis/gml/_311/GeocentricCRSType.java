//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.gml._311;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A 3D coordinate reference system with the origin at the approximate centre of
 * mass of the earth. A geocentric CRS deals with the earth's curvature by
 * taking a 3D spatial view, which obviates the need to model the earth's
 * curvature.
 * 
 * <p>
 * Java class for GeocentricCRSType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="GeocentricCRSType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractReferenceSystemType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{http://www.opengis.net/gml}usesCartesianCS"/>
 *           &lt;element ref="{http://www.opengis.net/gml}usesSphericalCS"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/gml}usesGeodeticDatum"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeocentricCRSType", propOrder = { "usesCartesianCS", "usesSphericalCS", "usesGeodeticDatum" })
public class GeocentricCRSType extends AbstractReferenceSystemType
{

    protected CartesianCSRefType usesCartesianCS;

    protected SphericalCSRefType usesSphericalCS;

    @XmlElement(required = true)
    protected GeodeticDatumRefType usesGeodeticDatum;

    /**
     * Gets the value of the usesCartesianCS property.
     * 
     * @return possible object is {@link CartesianCSRefType }
     * 
     */
    public CartesianCSRefType getUsesCartesianCS()
    {
        return usesCartesianCS;
    }

    /**
     * Sets the value of the usesCartesianCS property.
     * 
     * @param value allowed object is {@link CartesianCSRefType }
     * 
     */
    public void setUsesCartesianCS(CartesianCSRefType value)
    {
        this.usesCartesianCS = value;
    }

    /**
     * Gets the value of the usesSphericalCS property.
     * 
     * @return possible object is {@link SphericalCSRefType }
     * 
     */
    public SphericalCSRefType getUsesSphericalCS()
    {
        return usesSphericalCS;
    }

    /**
     * Sets the value of the usesSphericalCS property.
     * 
     * @param value allowed object is {@link SphericalCSRefType }
     * 
     */
    public void setUsesSphericalCS(SphericalCSRefType value)
    {
        this.usesSphericalCS = value;
    }

    /**
     * Gets the value of the usesGeodeticDatum property.
     * 
     * @return possible object is {@link GeodeticDatumRefType }
     * 
     */
    public GeodeticDatumRefType getUsesGeodeticDatum()
    {
        return usesGeodeticDatum;
    }

    /**
     * Sets the value of the usesGeodeticDatum property.
     * 
     * @param value allowed object is {@link GeodeticDatumRefType }
     * 
     */
    public void setUsesGeodeticDatum(GeodeticDatumRefType value)
    {
        this.usesGeodeticDatum = value;
    }

}
