//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.30 at 02:21:16 PM MDT 
//


package net.opengis.gml._212;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         A geometry collection must include one or more geometries, referenced 
 *         through geometryMember elements. User-defined geometry collections 
 *         that accept GML geometry classes as members must instantiate--or 
 *         derive from--this type.
 *       
 * 
 * <p>Java class for GeometryCollectionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GeometryCollectionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractGeometryCollectionBaseType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element ref="{http://www.opengis.net/gml}geometryMember"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeometryCollectionType", propOrder = {
    "geometryMember"
})
@XmlSeeAlso({
    MultiPointType.class,
    MultiLineStringType.class,
    MultiPolygonType.class
})
public class GeometryCollectionType
    extends AbstractGeometryCollectionBaseType
{

    @XmlElementRef(name = "geometryMember", namespace = "http://www.opengis.net/gml", type = JAXBElement.class)
    protected List<JAXBElement<? extends GeometryAssociationType>> geometryMember;

    /**
     * Gets the value of the geometryMember property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the geometryMember property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGeometryMember().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link GeometryAssociationType }{@code >}
     * {@link JAXBElement }{@code <}{@link PolygonMemberType }{@code >}
     * {@link JAXBElement }{@code <}{@link PointMemberType }{@code >}
     * {@link JAXBElement }{@code <}{@link LineStringMemberType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends GeometryAssociationType>> getGeometryMember() {
        if (geometryMember == null) {
            geometryMember = new ArrayList<JAXBElement<? extends GeometryAssociationType>>();
        }
        return this.geometryMember;
    }

    public boolean isSetGeometryMember() {
        return ((this.geometryMember!= null)&&(!this.geometryMember.isEmpty()));
    }

    public void unsetGeometryMember() {
        this.geometryMember = null;
    }

}
