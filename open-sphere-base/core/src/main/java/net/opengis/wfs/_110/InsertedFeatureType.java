//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.wfs._110;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import net.opengis.ogc._110.FeatureIdType;

/**
 * <p>
 * Java class for InsertedFeatureType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="InsertedFeatureType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ogc}FeatureId" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="handle" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InsertedFeatureType", propOrder = { "featureId" })
public class InsertedFeatureType
{

    @XmlElement(name = "FeatureId", namespace = "http://www.opengis.net/ogc", required = true)
    protected List<FeatureIdType> featureId;

    @XmlAttribute(name = "handle")
    protected String handle;

    /**
     * 
     * This is the feature identifier for the newly created feature. The feature
     * identifier may be generated by the WFS or provided by the client
     * (depending on the value of the idgen attribute). In all cases of idgen
     * values, the feature id must be reported here. Gets the value of the
     * featureId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the featureId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getFeatureId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureIdType }
     * 
     * 
     */
    public List<FeatureIdType> getFeatureId()
    {
        if (featureId == null)
        {
            featureId = new ArrayList<FeatureIdType>();
        }
        return this.featureId;
    }

    /**
     * Gets the value of the handle property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getHandle()
    {
        return handle;
    }

    /**
     * Sets the value of the handle property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setHandle(String value)
    {
        this.handle = value;
    }

}
