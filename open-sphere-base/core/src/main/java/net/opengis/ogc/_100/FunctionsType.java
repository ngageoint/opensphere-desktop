//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 12:24:08 PM MST 
//

package net.opengis.ogc._100;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for FunctionsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="FunctionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Function_Names" type="{http://www.opengis.net/ogc}Function_NamesType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FunctionsType", propOrder = { "functionNames" })
public class FunctionsType
{

    @XmlElement(name = "Function_Names", required = true)
    protected FunctionNamesType functionNames;

    /**
     * Gets the value of the functionNames property.
     * 
     * @return possible object is {@link FunctionNamesType }
     * 
     */
    public FunctionNamesType getFunctionNames()
    {
        return functionNames;
    }

    /**
     * Sets the value of the functionNames property.
     * 
     * @param value allowed object is {@link FunctionNamesType }
     * 
     */
    public void setFunctionNames(FunctionNamesType value)
    {
        this.functionNames = value;
    }

}
