//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 12:20:41 PM MST 
//

package net.opengis.wms_130;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wms}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Title"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Abstract" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}KeywordList" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}CRS" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}EX_GeographicBoundingBox" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}BoundingBox" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Dimension" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Attribution" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}AuthorityURL" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Identifier" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}MetadataURL" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}DataURL" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}FeatureListURL" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Style" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}MinScaleDenominator" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}MaxScaleDenominator" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}Layer" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="queryable" type="{http://www.w3.org/2001/XMLSchema}boolean" default="0" />
 *       &lt;attribute name="cascaded" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="opaque" type="{http://www.w3.org/2001/XMLSchema}boolean" default="0" />
 *       &lt;attribute name="noSubsets" type="{http://www.w3.org/2001/XMLSchema}boolean" default="0" />
 *       &lt;attribute name="fixedWidth" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="fixedHeight" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "name", "title", "_abstract", "keywordList", "crs", "exGeographicBoundingBox", "boundingBox",
    "dimension", "attribution", "authorityURL", "identifier", "metadataURL", "dataURL", "featureListURL", "style",
    "minScaleDenominator", "maxScaleDenominator", "layer" })
@XmlRootElement(name = "Layer")
public class Layer
{

    @XmlElement(name = "Name")
    protected String name;

    @XmlElement(name = "Title", required = true)
    protected String title;

    @XmlElement(name = "Abstract")
    protected String _abstract;

    @XmlElement(name = "KeywordList")
    protected KeywordList keywordList;

    @XmlElement(name = "CRS")
    protected List<String> crs;

    @XmlElement(name = "EX_GeographicBoundingBox")
    protected EXGeographicBoundingBox exGeographicBoundingBox;

    @XmlElement(name = "BoundingBox")
    protected List<BoundingBox> boundingBox;

    @XmlElement(name = "Dimension")
    protected List<Dimension> dimension;

    @XmlElement(name = "Attribution")
    protected Attribution attribution;

    @XmlElement(name = "AuthorityURL")
    protected List<AuthorityURL> authorityURL;

    @XmlElement(name = "Identifier")
    protected List<Identifier> identifier;

    @XmlElement(name = "MetadataURL")
    protected List<MetadataURL> metadataURL;

    @XmlElement(name = "DataURL")
    protected List<DataURL> dataURL;

    @XmlElement(name = "FeatureListURL")
    protected List<FeatureListURL> featureListURL;

    @XmlElement(name = "Style")
    protected List<Style> style;

    @XmlElement(name = "MinScaleDenominator")
    protected Double minScaleDenominator;

    @XmlElement(name = "MaxScaleDenominator")
    protected Double maxScaleDenominator;

    @XmlElement(name = "Layer")
    protected List<Layer> layer;

    @XmlAttribute
    protected Boolean queryable;

    @XmlAttribute
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger cascaded;

    @XmlAttribute
    protected Boolean opaque;

    @XmlAttribute
    protected Boolean noSubsets;

    @XmlAttribute
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger fixedWidth;

    @XmlAttribute
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger fixedHeight;

    /**
     * Gets the value of the name property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setName(String value)
    {
        this.name = value;
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
     * Gets the value of the abstract property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getAbstract()
    {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setAbstract(String value)
    {
        this._abstract = value;
    }

    /**
     * Gets the value of the keywordList property.
     * 
     * @return possible object is {@link KeywordList }
     * 
     */
    public KeywordList getKeywordList()
    {
        return keywordList;
    }

    /**
     * Sets the value of the keywordList property.
     * 
     * @param value allowed object is {@link KeywordList }
     * 
     */
    public void setKeywordList(KeywordList value)
    {
        this.keywordList = value;
    }

    /**
     * Gets the value of the crs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the crs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getCRS().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link String }
     * 
     * 
     */
    public List<String> getCRS()
    {
        if (crs == null)
        {
            crs = new ArrayList<String>();
        }
        return this.crs;
    }

    /**
     * Gets the value of the exGeographicBoundingBox property.
     * 
     * @return possible object is {@link EXGeographicBoundingBox }
     * 
     */
    public EXGeographicBoundingBox getEXGeographicBoundingBox()
    {
        return exGeographicBoundingBox;
    }

    /**
     * Sets the value of the exGeographicBoundingBox property.
     * 
     * @param value allowed object is {@link EXGeographicBoundingBox }
     * 
     */
    public void setEXGeographicBoundingBox(EXGeographicBoundingBox value)
    {
        this.exGeographicBoundingBox = value;
    }

    /**
     * Gets the value of the boundingBox property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the boundingBox property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getBoundingBox().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BoundingBox }
     * 
     * 
     */
    public List<BoundingBox> getBoundingBox()
    {
        if (boundingBox == null)
        {
            boundingBox = new ArrayList<BoundingBox>();
        }
        return this.boundingBox;
    }

    /**
     * Gets the value of the dimension property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the dimension property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDimension().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Dimension
     * }
     * 
     * 
     */
    public List<Dimension> getDimension()
    {
        if (dimension == null)
        {
            dimension = new ArrayList<Dimension>();
        }
        return this.dimension;
    }

    /**
     * Gets the value of the attribution property.
     * 
     * @return possible object is {@link Attribution }
     * 
     */
    public Attribution getAttribution()
    {
        return attribution;
    }

    /**
     * Sets the value of the attribution property.
     * 
     * @param value allowed object is {@link Attribution }
     * 
     */
    public void setAttribution(Attribution value)
    {
        this.attribution = value;
    }

    /**
     * Gets the value of the authorityURL property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the authorityURL property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getAuthorityURL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AuthorityURL }
     * 
     * 
     */
    public List<AuthorityURL> getAuthorityURL()
    {
        if (authorityURL == null)
        {
            authorityURL = new ArrayList<AuthorityURL>();
        }
        return this.authorityURL;
    }

    /**
     * Gets the value of the identifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the identifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getIdentifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Identifier }
     * 
     * 
     */
    public List<Identifier> getIdentifier()
    {
        if (identifier == null)
        {
            identifier = new ArrayList<Identifier>();
        }
        return this.identifier;
    }

    /**
     * Gets the value of the metadataURL property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the metadataURL property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getMetadataURL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MetadataURL }
     * 
     * 
     */
    public List<MetadataURL> getMetadataURL()
    {
        if (metadataURL == null)
        {
            metadataURL = new ArrayList<MetadataURL>();
        }
        return this.metadataURL;
    }

    /**
     * Gets the value of the dataURL property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the dataURL property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDataURL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link DataURL }
     * 
     * 
     */
    public List<DataURL> getDataURL()
    {
        if (dataURL == null)
        {
            dataURL = new ArrayList<DataURL>();
        }
        return this.dataURL;
    }

    /**
     * Gets the value of the featureListURL property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the featureListURL property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getFeatureListURL().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link FeatureListURL }
     * 
     * 
     */
    public List<FeatureListURL> getFeatureListURL()
    {
        if (featureListURL == null)
        {
            featureListURL = new ArrayList<FeatureListURL>();
        }
        return this.featureListURL;
    }

    /**
     * Gets the value of the style property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the style property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Style }
     * 
     * 
     */
    public List<Style> getStyle()
    {
        if (style == null)
        {
            style = new ArrayList<Style>();
        }
        return this.style;
    }

    /**
     * Gets the value of the minScaleDenominator property.
     * 
     * @return possible object is {@link Double }
     * 
     */
    public Double getMinScaleDenominator()
    {
        return minScaleDenominator;
    }

    /**
     * Sets the value of the minScaleDenominator property.
     * 
     * @param value allowed object is {@link Double }
     * 
     */
    public void setMinScaleDenominator(Double value)
    {
        this.minScaleDenominator = value;
    }

    /**
     * Gets the value of the maxScaleDenominator property.
     * 
     * @return possible object is {@link Double }
     * 
     */
    public Double getMaxScaleDenominator()
    {
        return maxScaleDenominator;
    }

    /**
     * Sets the value of the maxScaleDenominator property.
     * 
     * @param value allowed object is {@link Double }
     * 
     */
    public void setMaxScaleDenominator(Double value)
    {
        this.maxScaleDenominator = value;
    }

    /**
     * Gets the value of the layer property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the layer property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getLayer().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Layer }
     * 
     * 
     */
    public List<Layer> getLayer()
    {
        if (layer == null)
        {
            layer = new ArrayList<Layer>();
        }
        return this.layer;
    }

    /**
     * Gets the value of the queryable property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public boolean isQueryable()
    {
        if (queryable == null)
        {
            return false;
        }
        else
        {
            return queryable;
        }
    }

    /**
     * Sets the value of the queryable property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setQueryable(Boolean value)
    {
        this.queryable = value;
    }

    /**
     * Gets the value of the cascaded property.
     * 
     * @return possible object is {@link BigInteger }
     * 
     */
    public BigInteger getCascaded()
    {
        return cascaded;
    }

    /**
     * Sets the value of the cascaded property.
     * 
     * @param value allowed object is {@link BigInteger }
     * 
     */
    public void setCascaded(BigInteger value)
    {
        this.cascaded = value;
    }

    /**
     * Gets the value of the opaque property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public boolean isOpaque()
    {
        if (opaque == null)
        {
            return false;
        }
        else
        {
            return opaque;
        }
    }

    /**
     * Sets the value of the opaque property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setOpaque(Boolean value)
    {
        this.opaque = value;
    }

    /**
     * Gets the value of the noSubsets property.
     * 
     * @return possible object is {@link Boolean }
     * 
     */
    public boolean isNoSubsets()
    {
        if (noSubsets == null)
        {
            return false;
        }
        else
        {
            return noSubsets;
        }
    }

    /**
     * Sets the value of the noSubsets property.
     * 
     * @param value allowed object is {@link Boolean }
     * 
     */
    public void setNoSubsets(Boolean value)
    {
        this.noSubsets = value;
    }

    /**
     * Gets the value of the fixedWidth property.
     * 
     * @return possible object is {@link BigInteger }
     * 
     */
    public BigInteger getFixedWidth()
    {
        return fixedWidth;
    }

    /**
     * Sets the value of the fixedWidth property.
     * 
     * @param value allowed object is {@link BigInteger }
     * 
     */
    public void setFixedWidth(BigInteger value)
    {
        this.fixedWidth = value;
    }

    /**
     * Gets the value of the fixedHeight property.
     * 
     * @return possible object is {@link BigInteger }
     * 
     */
    public BigInteger getFixedHeight()
    {
        return fixedHeight;
    }

    /**
     * Sets the value of the fixedHeight property.
     * 
     * @param value allowed object is {@link BigInteger }
     * 
     */
    public void setFixedHeight(BigInteger value)
    {
        this.fixedHeight = value;
    }

}
