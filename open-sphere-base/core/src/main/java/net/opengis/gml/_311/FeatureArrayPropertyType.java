//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.gml._311;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;

/**
 * Container for features - follow gml:ArrayAssociationType pattern.
 * 
 * <p>
 * Java class for FeatureArrayPropertyType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="FeatureArrayPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}_Feature" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FeatureArrayPropertyType", propOrder = { "feature" })
public class FeatureArrayPropertyType
{

    @XmlElementRef(name = "_Feature", namespace = "http://www.opengis.net/gml", type = JAXBElement.class)
    protected List<JAXBElement<? extends AbstractFeatureType>> feature;

    /**
     * Gets the value of the feature property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the feature property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getFeature().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link AbstractCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractDiscreteCoverageType
     * }{@code >} {@link JAXBElement }{@code <}{@link MultiCurveCoverageType
     * }{@code >} {@link JAXBElement }{@code <}{@link MultiSolidCoverageType
     * }{@code >} {@link JAXBElement
     * }{@code <}{@link AbstractFeatureCollectionType }{@code >}
     * {@link JAXBElement }{@code <}{@link ObservationType }{@code >}
     * {@link JAXBElement }{@code <}{@link DirectedObservationType }{@code >}
     * {@link JAXBElement }{@code <}{@link RectifiedGridCoverageType }{@code >}
     * {@link JAXBElement
     * }{@code <}{@link net.opengis.wfs._110.FeatureCollectionType }{@code >}
     * {@link JAXBElement
     * }{@code <}{@link net.opengis.gml._311.FeatureCollectionType }{@code >}
     * {@link JAXBElement }{@code <}{@link AbstractFeatureType }{@code >}
     * {@link JAXBElement }{@code <}{@link GridCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiPointCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link DirectedObservationAtDistanceType
     * }{@code >} {@link JAXBElement
     * }{@code <}{@link AbstractContinuousCoverageType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiSurfaceCoverageType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends AbstractFeatureType>> getFeature()
    {
        if (feature == null)
        {
            feature = new ArrayList<JAXBElement<? extends AbstractFeatureType>>();
        }
        return this.feature;
    }

}
