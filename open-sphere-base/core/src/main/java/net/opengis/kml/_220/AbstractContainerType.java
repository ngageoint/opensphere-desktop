//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.02.22 at 10:22:41 AM MST 
//

package net.opengis.kml._220;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for AbstractContainerType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractContainerType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractFeatureType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractContainerSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}AbstractContainerObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractContainerType")
@XmlSeeAlso({ DocumentType.class, FolderType.class })
public abstract class AbstractContainerType extends AbstractFeatureType
{

}
