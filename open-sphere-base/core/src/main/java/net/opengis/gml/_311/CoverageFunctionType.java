//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.gml._311;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

/**
 * The function or rule which defines the map from members of the domainSet to
 * the range. More functions will be added to this list
 * 
 * <p>
 * Java class for CoverageFunctionType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="CoverageFunctionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/gml}MappingRule"/>
 *         &lt;element ref="{http://www.opengis.net/gml}GridFunction"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoverageFunctionType", propOrder = { "mappingRule", "gridFunction" })
public class CoverageFunctionType
{

    @XmlElement(name = "MappingRule")
    protected StringOrRefType mappingRule;

    @XmlElementRef(name = "GridFunction", namespace = "http://www.opengis.net/gml", type = JAXBElement.class)
    protected JAXBElement<? extends GridFunctionType> gridFunction;

    /**
     * Gets the value of the mappingRule property.
     * 
     * @return possible object is {@link StringOrRefType }
     * 
     */
    public StringOrRefType getMappingRule()
    {
        return mappingRule;
    }

    /**
     * Sets the value of the mappingRule property.
     * 
     * @param value allowed object is {@link StringOrRefType }
     * 
     */
    public void setMappingRule(StringOrRefType value)
    {
        this.mappingRule = value;
    }

    /**
     * Gets the value of the gridFunction property.
     * 
     * @return possible object is {@link JAXBElement
     *         }{@code <}{@link IndexMapType }{@code >} {@link JAXBElement
     *         }{@code <}{@link GridFunctionType }{@code >}
     * 
     */
    public JAXBElement<? extends GridFunctionType> getGridFunction()
    {
        return gridFunction;
    }

    /**
     * Sets the value of the gridFunction property.
     * 
     * @param value allowed object is {@link JAXBElement
     *            }{@code <}{@link IndexMapType }{@code >} {@link JAXBElement
     *            }{@code <}{@link GridFunctionType }{@code >}
     * 
     */
    public void setGridFunction(JAXBElement<? extends GridFunctionType> value)
    {
        this.gridFunction = (value);
    }

}
