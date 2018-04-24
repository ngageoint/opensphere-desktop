//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.30 at 02:21:16 PM MDT 
//


package net.opengis.kml._220;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PolyStyleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PolyStyleType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractColorStyleType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}fill" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}outline" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PolyStyleSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}PolyStyleObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PolyStyleType", propOrder = {
    "fill",
    "outline",
    "polyStyleSimpleExtensionGroup",
    "polyStyleObjectExtensionGroup"
})
public class PolyStyleType
    extends AbstractColorStyleType
{

    @XmlElement(defaultValue = "1")
    protected Boolean fill;
    @XmlElement(defaultValue = "1")
    protected Boolean outline;
    @XmlElement(name = "PolyStyleSimpleExtensionGroup")
    protected List<Object> polyStyleSimpleExtensionGroup;
    @XmlElement(name = "PolyStyleObjectExtensionGroup")
    protected List<AbstractObjectType> polyStyleObjectExtensionGroup;

    /**
     * Gets the value of the fill property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFill() {
        return fill;
    }

    /**
     * Sets the value of the fill property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFill(Boolean value) {
        this.fill = value;
    }

    public boolean isSetFill() {
        return (this.fill!= null);
    }

    /**
     * Gets the value of the outline property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isOutline() {
        return outline;
    }

    /**
     * Sets the value of the outline property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setOutline(Boolean value) {
        this.outline = value;
    }

    public boolean isSetOutline() {
        return (this.outline!= null);
    }

    /**
     * Gets the value of the polyStyleSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the polyStyleSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolyStyleSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getPolyStyleSimpleExtensionGroup() {
        if (polyStyleSimpleExtensionGroup == null) {
            polyStyleSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.polyStyleSimpleExtensionGroup;
    }

    public boolean isSetPolyStyleSimpleExtensionGroup() {
        return ((this.polyStyleSimpleExtensionGroup!= null)&&(!this.polyStyleSimpleExtensionGroup.isEmpty()));
    }

    public void unsetPolyStyleSimpleExtensionGroup() {
        this.polyStyleSimpleExtensionGroup = null;
    }

    /**
     * Gets the value of the polyStyleObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the polyStyleObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPolyStyleObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getPolyStyleObjectExtensionGroup() {
        if (polyStyleObjectExtensionGroup == null) {
            polyStyleObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.polyStyleObjectExtensionGroup;
    }

    public boolean isSetPolyStyleObjectExtensionGroup() {
        return ((this.polyStyleObjectExtensionGroup!= null)&&(!this.polyStyleObjectExtensionGroup.isEmpty()));
    }

    public void unsetPolyStyleObjectExtensionGroup() {
        this.polyStyleObjectExtensionGroup = null;
    }

}
