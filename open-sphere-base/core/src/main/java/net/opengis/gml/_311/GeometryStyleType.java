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
import javax.xml.bind.annotation.XmlType;

/**
 * [complexType of] The style descriptor for geometries of a feature.
 * 
 * <p>
 * Java class for GeometryStyleType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="GeometryStyleType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}BaseStyleDescriptorType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{http://www.opengis.net/gml}symbol"/>
 *           &lt;element name="style" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/gml}labelStyle" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="geometryProperty" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="geometryType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeometryStyleType", propOrder = { "symbol", "style", "labelStyle" })
public class GeometryStyleType extends BaseStyleDescriptorType
{

    protected SymbolType symbol;

    protected String style;

    protected LabelStylePropertyType labelStyle;

    @XmlAttribute(name = "geometryProperty")
    protected String geometryProperty;

    @XmlAttribute(name = "geometryType")
    protected String geometryType;

    /**
     * Gets the value of the symbol property.
     * 
     * @return possible object is {@link SymbolType }
     * 
     */
    public SymbolType getSymbol()
    {
        return symbol;
    }

    /**
     * Sets the value of the symbol property.
     * 
     * @param value allowed object is {@link SymbolType }
     * 
     */
    public void setSymbol(SymbolType value)
    {
        this.symbol = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getStyle()
    {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setStyle(String value)
    {
        this.style = value;
    }

    /**
     * Gets the value of the labelStyle property.
     * 
     * @return possible object is {@link LabelStylePropertyType }
     * 
     */
    public LabelStylePropertyType getLabelStyle()
    {
        return labelStyle;
    }

    /**
     * Sets the value of the labelStyle property.
     * 
     * @param value allowed object is {@link LabelStylePropertyType }
     * 
     */
    public void setLabelStyle(LabelStylePropertyType value)
    {
        this.labelStyle = value;
    }

    /**
     * Gets the value of the geometryProperty property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getGeometryProperty()
    {
        return geometryProperty;
    }

    /**
     * Sets the value of the geometryProperty property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setGeometryProperty(String value)
    {
        this.geometryProperty = value;
    }

    /**
     * Gets the value of the geometryType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getGeometryType()
    {
        return geometryType;
    }

    /**
     * Sets the value of the geometryType property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setGeometryType(String value)
    {
        this.geometryType = value;
    }

}
