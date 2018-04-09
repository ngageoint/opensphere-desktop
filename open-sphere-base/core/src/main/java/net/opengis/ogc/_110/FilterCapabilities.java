//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.ogc._110;

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
 *         &lt;element name="Spatial_Capabilities" type="{http://www.opengis.net/ogc}Spatial_CapabilitiesType"/>
 *         &lt;element name="Scalar_Capabilities" type="{http://www.opengis.net/ogc}Scalar_CapabilitiesType"/>
 *         &lt;element name="Id_Capabilities" type="{http://www.opengis.net/ogc}Id_CapabilitiesType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "spatialCapabilities", "scalarCapabilities", "idCapabilities" })
@XmlRootElement(name = "Filter_Capabilities")
public class FilterCapabilities
{

    @XmlElement(name = "Spatial_Capabilities", required = true)
    protected SpatialCapabilitiesType spatialCapabilities;

    @XmlElement(name = "Scalar_Capabilities", required = true)
    protected ScalarCapabilitiesType scalarCapabilities;

    @XmlElement(name = "Id_Capabilities", required = true)
    protected IdCapabilitiesType idCapabilities;

    /**
     * Gets the value of the spatialCapabilities property.
     * 
     * @return possible object is {@link SpatialCapabilitiesType }
     * 
     */
    public SpatialCapabilitiesType getSpatialCapabilities()
    {
        return spatialCapabilities;
    }

    /**
     * Sets the value of the spatialCapabilities property.
     * 
     * @param value allowed object is {@link SpatialCapabilitiesType }
     * 
     */
    public void setSpatialCapabilities(SpatialCapabilitiesType value)
    {
        this.spatialCapabilities = value;
    }

    /**
     * Gets the value of the scalarCapabilities property.
     * 
     * @return possible object is {@link ScalarCapabilitiesType }
     * 
     */
    public ScalarCapabilitiesType getScalarCapabilities()
    {
        return scalarCapabilities;
    }

    /**
     * Sets the value of the scalarCapabilities property.
     * 
     * @param value allowed object is {@link ScalarCapabilitiesType }
     * 
     */
    public void setScalarCapabilities(ScalarCapabilitiesType value)
    {
        this.scalarCapabilities = value;
    }

    /**
     * Gets the value of the idCapabilities property.
     * 
     * @return possible object is {@link IdCapabilitiesType }
     * 
     */
    public IdCapabilitiesType getIdCapabilities()
    {
        return idCapabilities;
    }

    /**
     * Sets the value of the idCapabilities property.
     * 
     * @param value allowed object is {@link IdCapabilitiesType }
     * 
     */
    public void setIdCapabilities(IdCapabilitiesType value)
    {
        this.idCapabilities = value;
    }

}
