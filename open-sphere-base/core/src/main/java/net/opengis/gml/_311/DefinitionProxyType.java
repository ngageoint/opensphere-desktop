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
 * A proxy entry in a dictionary of definitions. An element of this type
 * contains a reference to a remote definition object. This entry is expected to
 * be convenient in allowing multiple elements in one XML document to contain
 * short (abbreviated XPointer) references, which are resolved to an external
 * definition provided in a Dictionary element in the same XML document.
 * 
 * <p>
 * Java class for DefinitionProxyType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="DefinitionProxyType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}DefinitionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}definitionRef"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DefinitionProxyType", propOrder = { "definitionRef" })
public class DefinitionProxyType extends DefinitionType
{

    @XmlElement(required = true)
    protected ReferenceType definitionRef;

    /**
     * A reference to a remote entry in this dictionary, used when this
     * dictionary entry is identified to allow external references to this
     * specific entry. The remote entry referenced can be in a dictionary in the
     * same or different XML document.
     * 
     * @return possible object is {@link ReferenceType }
     * 
     */
    public ReferenceType getDefinitionRef()
    {
        return definitionRef;
    }

    /**
     * Sets the value of the definitionRef property.
     * 
     * @param value allowed object is {@link ReferenceType }
     * 
     */
    public void setDefinitionRef(ReferenceType value)
    {
        this.definitionRef = value;
    }

}
