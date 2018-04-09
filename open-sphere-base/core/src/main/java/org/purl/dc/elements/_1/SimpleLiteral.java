//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.22 at 01:49:34 PM MST 
//

package org.purl.dc.elements._1;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * This is the default type for all of the DC elements. It defines a complexType
 * SimpleLiteral which permits mixed content but disallows child elements by use
 * of minOcccurs/maxOccurs. However, this complexType does permit the derivation
 * of other types which would permit child elements. The scheme attribute may be
 * used as a qualifier to reference an encoding scheme that describes the value
 * domain for a given property.
 * 
 * <p>
 * Java class for SimpleLiteral complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="SimpleLiteral">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any processContents='lax' maxOccurs="0" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="scheme" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimpleLiteral", propOrder = { "content" })
public class SimpleLiteral
{

    @XmlMixed
    protected List<String> content;

    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    protected String scheme;

    /**
     * This is the default type for all of the DC elements. It defines a
     * complexType SimpleLiteral which permits mixed content but disallows child
     * elements by use of minOcccurs/maxOccurs. However, this complexType does
     * permit the derivation of other types which would permit child elements.
     * The scheme attribute may be used as a qualifier to reference an encoding
     * scheme that describes the value domain for a given property.Gets the
     * value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     * 
     * 
     */
    public List<String> getContent()
    {
        if (content == null)
        {
            content = new ArrayList<String>();
        }
        return this.content;
    }

    /**
     * Gets the value of the scheme property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getScheme()
    {
        return scheme;
    }

    /**
     * Sets the value of the scheme property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setScheme(String value)
    {
        this.scheme = value;
    }

}
