//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.ogc._110;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for UpperBoundaryType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="UpperBoundaryType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ogc}expression"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpperBoundaryType", propOrder = { "expression" })
public class UpperBoundaryType
{

    @XmlElementRef(name = "expression", namespace = "http://www.opengis.net/ogc", type = JAXBElement.class)
    protected JAXBElement<?> expression;

    /**
     * Gets the value of the expression property.
     * 
     * @return possible object is {@link JAXBElement
     *         }{@code <}{@link ExpressionType }{@code >} {@link JAXBElement
     *         }{@code <}{@link LiteralType }{@code >} {@link JAXBElement
     *         }{@code <}{@link PropertyNameType }{@code >} {@link JAXBElement
     *         }{@code <}{@link BinaryOperatorType }{@code >} {@link JAXBElement
     *         }{@code <}{@link FunctionType }{@code >} {@link JAXBElement
     *         }{@code <}{@link BinaryOperatorType }{@code >} {@link JAXBElement
     *         }{@code <}{@link BinaryOperatorType }{@code >} {@link JAXBElement
     *         }{@code <}{@link BinaryOperatorType }{@code >}
     * 
     */
    public JAXBElement<?> getExpression()
    {
        return expression;
    }

    /**
     * Sets the value of the expression property.
     * 
     * @param value allowed object is {@link JAXBElement
     *            }{@code <}{@link ExpressionType }{@code >} {@link JAXBElement
     *            }{@code <}{@link LiteralType }{@code >} {@link JAXBElement
     *            }{@code <}{@link PropertyNameType }{@code >}
     *            {@link JAXBElement }{@code <}{@link BinaryOperatorType
     *            }{@code >} {@link JAXBElement }{@code <}{@link FunctionType
     *            }{@code >} {@link JAXBElement
     *            }{@code <}{@link BinaryOperatorType }{@code >}
     *            {@link JAXBElement }{@code <}{@link BinaryOperatorType
     *            }{@code >} {@link JAXBElement
     *            }{@code <}{@link BinaryOperatorType }{@code >}
     * 
     */
    public void setExpression(JAXBElement<?> value)
    {
        this.expression = (value);
    }

}
