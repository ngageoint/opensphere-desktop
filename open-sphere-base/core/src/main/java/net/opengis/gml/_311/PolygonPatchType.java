//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//


package net.opengis.gml._311;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;


/**
 * A PolygonPatch is a surface patch that is defined by a set of boundary curves and an underlying surface to which these curves adhere. The curves are coplanar and the polygon uses planar interpolation in its interior. Implements GM_Polygon of ISO 19107.
 * 
 * <p>Java class for PolygonPatchType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PolygonPatchType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractSurfacePatchType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}exterior" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}interior" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="interpolation" type="{http://www.opengis.net/gml}SurfaceInterpolationType" fixed="planar" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolygonPatchType", propOrder = {
    "exterior",
    "interior"
})
public class PolygonPatchType
    extends AbstractSurfacePatchType
{

    @XmlElementRef(name = "exterior", namespace = "http://www.opengis.net/gml", type = JAXBElement.class)
    protected JAXBElement<AbstractRingPropertyType> exterior;
    @XmlElementRef(name = "interior", namespace = "http://www.opengis.net/gml", type = JAXBElement.class)
    protected List<JAXBElement<AbstractRingPropertyType>> interior;
    @XmlAttribute(name = "interpolation")
    protected SurfaceInterpolationType interpolation;

    /**
     * Gets the value of the exterior property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}
     *     
     */
    public JAXBElement<AbstractRingPropertyType> getExterior() {
        return exterior;
    }

    /**
     * Sets the value of the exterior property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}
     *     {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}
     *     
     */
    public void setExterior(JAXBElement<AbstractRingPropertyType> value) {
        this.exterior = (value);
    }

    /**
     * Gets the value of the interior property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the interior property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInterior().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractRingPropertyType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<AbstractRingPropertyType>> getInterior() {
        if (interior == null) {
            interior = new ArrayList<JAXBElement<AbstractRingPropertyType>>();
        }
        return this.interior;
    }

    /**
     * Gets the value of the interpolation property.
     * 
     * @return
     *     possible object is
     *     {@link SurfaceInterpolationType }
     *     
     */
    public SurfaceInterpolationType getInterpolation() {
        if (interpolation == null) {
            return SurfaceInterpolationType.PLANAR;
        } else {
            return interpolation;
        }
    }

    /**
     * Sets the value of the interpolation property.
     * 
     * @param value
     *     allowed object is
     *     {@link SurfaceInterpolationType }
     *     
     */
    public void setInterpolation(SurfaceInterpolationType value) {
        this.interpolation = value;
    }

}
