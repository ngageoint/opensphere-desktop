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
 * Closeness of reported coordinate values to values accepted as or being true.
 * 
 * <p>
 * Java class for AbsoluteExternalPositionalAccuracyType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="AbsoluteExternalPositionalAccuracyType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractPositionalAccuracyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}result"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbsoluteExternalPositionalAccuracyType", propOrder = { "result" })
public class AbsoluteExternalPositionalAccuracyType extends AbstractPositionalAccuracyType
{

    @XmlElement(required = true)
    protected MeasureType result;

    /**
     * Gets the value of the result property.
     * 
     * @return possible object is {@link MeasureType }
     * 
     */
    public MeasureType getResult()
    {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value allowed object is {@link MeasureType }
     * 
     */
    public void setResult(MeasureType value)
    {
        this.result = value;
    }

}
