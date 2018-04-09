//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.gml._311;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * This type is deprecated with GML 3 and shall not be used. It is included for
 * backwards compatibility with GML 2. Use MultiSurfacePropertyType instead.
 * 
 * A property that has a collection of polygons as its value domain can either
 * be an appropriate geometry element encapsulated in an element of this type or
 * an XLink reference to a remote geometry element (where remote includes
 * geometry elements located elsewhere in the same document). Either the
 * reference or the contained element must be given, but neither both nor none.
 * 
 * <p>
 * Java class for MultiPolygonPropertyType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="MultiPolygonPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element ref="{http://www.opengis.net/gml}MultiPolygon"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.opengis.net/gml}AssociationAttributeGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MultiPolygonPropertyType", propOrder = { "multiPolygon" })
public class MultiPolygonPropertyType
{

    @XmlElement(name = "MultiPolygon")
    protected MultiPolygonType multiPolygon;

    @XmlAttribute(name = "remoteSchema", namespace = "http://www.opengis.net/gml")
    @XmlSchemaType(name = "anyURI")
    protected String remoteSchema;

    @XmlAttribute(name = "type", namespace = "http://www.w3.org/1999/xlink")
    protected String type;

    @XmlAttribute(name = "href", namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    protected String href;

    @XmlAttribute(name = "role", namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    protected String role;

    @XmlAttribute(name = "arcrole", namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    protected String arcrole;

    @XmlAttribute(name = "title", namespace = "http://www.w3.org/1999/xlink")
    protected String title;

    @XmlAttribute(name = "show", namespace = "http://www.w3.org/1999/xlink")
    protected String show;

    @XmlAttribute(name = "actuate", namespace = "http://www.w3.org/1999/xlink")
    protected String actuate;

    /**
     * Gets the value of the multiPolygon property.
     * 
     * @return possible object is {@link MultiPolygonType }
     * 
     */
    public MultiPolygonType getMultiPolygon()
    {
        return multiPolygon;
    }

    /**
     * Sets the value of the multiPolygon property.
     * 
     * @param value allowed object is {@link MultiPolygonType }
     * 
     */
    public void setMultiPolygon(MultiPolygonType value)
    {
        this.multiPolygon = value;
    }

    /**
     * Gets the value of the remoteSchema property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getRemoteSchema()
    {
        return remoteSchema;
    }

    /**
     * Sets the value of the remoteSchema property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setRemoteSchema(String value)
    {
        this.remoteSchema = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getType()
    {
        if (type == null)
        {
            return "simple";
        }
        else
        {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setType(String value)
    {
        this.type = value;
    }

    /**
     * Gets the value of the href property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getHref()
    {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setHref(String value)
    {
        this.href = value;
    }

    /**
     * Gets the value of the role property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getRole()
    {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setRole(String value)
    {
        this.role = value;
    }

    /**
     * Gets the value of the arcrole property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getArcrole()
    {
        return arcrole;
    }

    /**
     * Sets the value of the arcrole property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setArcrole(String value)
    {
        this.arcrole = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setTitle(String value)
    {
        this.title = value;
    }

    /**
     * Gets the value of the show property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getShow()
    {
        return show;
    }

    /**
     * Sets the value of the show property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setShow(String value)
    {
        this.show = value;
    }

    /**
     * Gets the value of the actuate property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getActuate()
    {
        return actuate;
    }

    /**
     * Sets the value of the actuate property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setActuate(String value)
    {
        this.actuate = value;
    }

}
