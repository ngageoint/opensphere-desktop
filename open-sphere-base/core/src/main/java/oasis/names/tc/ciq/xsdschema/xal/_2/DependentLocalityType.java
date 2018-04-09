//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.02.22 at 10:22:41 AM MST 
//

package oasis.names.tc.ciq.xsdschema.xal._2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 * <p>
 * Java class for DependentLocalityType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="DependentLocalityType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}AddressLine" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="DependentLocalityName" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attGroup ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}grPostal"/>
 *                 &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *                 &lt;anyAttribute namespace='##other'/>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="DependentLocalityNumber" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attGroup ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}grPostal"/>
 *                 &lt;attribute name="NameNumberOccurrence">
 *                   &lt;simpleType>
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *                       &lt;enumeration value="Before"/>
 *                       &lt;enumeration value="After"/>
 *                     &lt;/restriction>
 *                   &lt;/simpleType>
 *                 &lt;/attribute>
 *                 &lt;anyAttribute namespace='##other'/>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostBox"/>
 *           &lt;element name="LargeMailUser" type="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}LargeMailUserType"/>
 *           &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostOffice"/>
 *           &lt;element name="PostalRoute" type="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostalRouteType"/>
 *         &lt;/choice>
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}Thoroughfare" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}Premise" minOccurs="0"/>
 *         &lt;element name="DependentLocality" type="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}DependentLocalityType" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostalCode" minOccurs="0"/>
 *         &lt;any namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="UsageType" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="Connector" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="Indicator" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;anyAttribute namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DependentLocalityType", propOrder = { "addressLine", "dependentLocalityName", "dependentLocalityNumber",
    "postBox", "largeMailUser", "postOffice", "postalRoute", "thoroughfare", "premise", "dependentLocality", "postalCode",
    "any" })
public class DependentLocalityType
{

    @XmlElement(name = "AddressLine")
    protected List<AddressLine> addressLine;

    @XmlElement(name = "DependentLocalityName")
    protected List<DependentLocalityType.DependentLocalityName> dependentLocalityName;

    @XmlElement(name = "DependentLocalityNumber")
    protected DependentLocalityType.DependentLocalityNumber dependentLocalityNumber;

    @XmlElement(name = "PostBox")
    protected PostBox postBox;

    @XmlElement(name = "LargeMailUser")
    protected LargeMailUserType largeMailUser;

    @XmlElement(name = "PostOffice")
    protected PostOffice postOffice;

    @XmlElement(name = "PostalRoute")
    protected PostalRouteType postalRoute;

    @XmlElement(name = "Thoroughfare")
    protected Thoroughfare thoroughfare;

    @XmlElement(name = "Premise")
    protected Premise premise;

    @XmlElement(name = "DependentLocality")
    protected DependentLocalityType dependentLocality;

    @XmlElement(name = "PostalCode")
    protected PostalCode postalCode;

    @XmlAnyElement(lax = true)
    protected List<Object> any;

    @XmlAttribute(name = "Type")
    @XmlSchemaType(name = "anySimpleType")
    protected String type;

    @XmlAttribute(name = "UsageType")
    @XmlSchemaType(name = "anySimpleType")
    protected String usageType;

    @XmlAttribute(name = "Connector")
    @XmlSchemaType(name = "anySimpleType")
    protected String connector;

    @XmlAttribute(name = "Indicator")
    @XmlSchemaType(name = "anySimpleType")
    protected String indicator;

    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the addressLine property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the addressLine property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getAddressLine().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AddressLine }
     * 
     * 
     */
    public List<AddressLine> getAddressLine()
    {
        if (addressLine == null)
        {
            addressLine = new ArrayList<AddressLine>();
        }
        return this.addressLine;
    }

    public boolean isSetAddressLine()
    {
        return ((this.addressLine != null) && (!this.addressLine.isEmpty()));
    }

    public void unsetAddressLine()
    {
        this.addressLine = null;
    }

    /**
     * Gets the value of the dependentLocalityName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the dependentLocalityName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getDependentLocalityName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DependentLocalityType.DependentLocalityName }
     * 
     * 
     */
    public List<DependentLocalityType.DependentLocalityName> getDependentLocalityName()
    {
        if (dependentLocalityName == null)
        {
            dependentLocalityName = new ArrayList<DependentLocalityType.DependentLocalityName>();
        }
        return this.dependentLocalityName;
    }

    public boolean isSetDependentLocalityName()
    {
        return ((this.dependentLocalityName != null) && (!this.dependentLocalityName.isEmpty()));
    }

    public void unsetDependentLocalityName()
    {
        this.dependentLocalityName = null;
    }

    /**
     * Gets the value of the dependentLocalityNumber property.
     * 
     * @return possible object is
     *         {@link DependentLocalityType.DependentLocalityNumber }
     * 
     */
    public DependentLocalityType.DependentLocalityNumber getDependentLocalityNumber()
    {
        return dependentLocalityNumber;
    }

    /**
     * Sets the value of the dependentLocalityNumber property.
     * 
     * @param value allowed object is
     *            {@link DependentLocalityType.DependentLocalityNumber }
     * 
     */
    public void setDependentLocalityNumber(DependentLocalityType.DependentLocalityNumber value)
    {
        this.dependentLocalityNumber = value;
    }

    public boolean isSetDependentLocalityNumber()
    {
        return (this.dependentLocalityNumber != null);
    }

    /**
     * Gets the value of the postBox property.
     * 
     * @return possible object is {@link PostBox }
     * 
     */
    public PostBox getPostBox()
    {
        return postBox;
    }

    /**
     * Sets the value of the postBox property.
     * 
     * @param value allowed object is {@link PostBox }
     * 
     */
    public void setPostBox(PostBox value)
    {
        this.postBox = value;
    }

    public boolean isSetPostBox()
    {
        return (this.postBox != null);
    }

    /**
     * Gets the value of the largeMailUser property.
     * 
     * @return possible object is {@link LargeMailUserType }
     * 
     */
    public LargeMailUserType getLargeMailUser()
    {
        return largeMailUser;
    }

    /**
     * Sets the value of the largeMailUser property.
     * 
     * @param value allowed object is {@link LargeMailUserType }
     * 
     */
    public void setLargeMailUser(LargeMailUserType value)
    {
        this.largeMailUser = value;
    }

    public boolean isSetLargeMailUser()
    {
        return (this.largeMailUser != null);
    }

    /**
     * Gets the value of the postOffice property.
     * 
     * @return possible object is {@link PostOffice }
     * 
     */
    public PostOffice getPostOffice()
    {
        return postOffice;
    }

    /**
     * Sets the value of the postOffice property.
     * 
     * @param value allowed object is {@link PostOffice }
     * 
     */
    public void setPostOffice(PostOffice value)
    {
        this.postOffice = value;
    }

    public boolean isSetPostOffice()
    {
        return (this.postOffice != null);
    }

    /**
     * Gets the value of the postalRoute property.
     * 
     * @return possible object is {@link PostalRouteType }
     * 
     */
    public PostalRouteType getPostalRoute()
    {
        return postalRoute;
    }

    /**
     * Sets the value of the postalRoute property.
     * 
     * @param value allowed object is {@link PostalRouteType }
     * 
     */
    public void setPostalRoute(PostalRouteType value)
    {
        this.postalRoute = value;
    }

    public boolean isSetPostalRoute()
    {
        return (this.postalRoute != null);
    }

    /**
     * Gets the value of the thoroughfare property.
     * 
     * @return possible object is {@link Thoroughfare }
     * 
     */
    public Thoroughfare getThoroughfare()
    {
        return thoroughfare;
    }

    /**
     * Sets the value of the thoroughfare property.
     * 
     * @param value allowed object is {@link Thoroughfare }
     * 
     */
    public void setThoroughfare(Thoroughfare value)
    {
        this.thoroughfare = value;
    }

    public boolean isSetThoroughfare()
    {
        return (this.thoroughfare != null);
    }

    /**
     * Gets the value of the premise property.
     * 
     * @return possible object is {@link Premise }
     * 
     */
    public Premise getPremise()
    {
        return premise;
    }

    /**
     * Sets the value of the premise property.
     * 
     * @param value allowed object is {@link Premise }
     * 
     */
    public void setPremise(Premise value)
    {
        this.premise = value;
    }

    public boolean isSetPremise()
    {
        return (this.premise != null);
    }

    /**
     * Gets the value of the dependentLocality property.
     * 
     * @return possible object is {@link DependentLocalityType }
     * 
     */
    public DependentLocalityType getDependentLocality()
    {
        return dependentLocality;
    }

    /**
     * Sets the value of the dependentLocality property.
     * 
     * @param value allowed object is {@link DependentLocalityType }
     * 
     */
    public void setDependentLocality(DependentLocalityType value)
    {
        this.dependentLocality = value;
    }

    public boolean isSetDependentLocality()
    {
        return (this.dependentLocality != null);
    }

    /**
     * Gets the value of the postalCode property.
     * 
     * @return possible object is {@link PostalCode }
     * 
     */
    public PostalCode getPostalCode()
    {
        return postalCode;
    }

    /**
     * Sets the value of the postalCode property.
     * 
     * @param value allowed object is {@link PostalCode }
     * 
     */
    public void setPostalCode(PostalCode value)
    {
        this.postalCode = value;
    }

    public boolean isSetPostalCode()
    {
        return (this.postalCode != null);
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Object }
     * 
     * 
     */
    public List<Object> getAny()
    {
        if (any == null)
        {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    public boolean isSetAny()
    {
        return ((this.any != null) && (!this.any.isEmpty()));
    }

    public void unsetAny()
    {
        this.any = null;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getType()
    {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setType(String value)
    {
        this.type = value;
    }

    public boolean isSetType()
    {
        return (this.type != null);
    }

    /**
     * Gets the value of the usageType property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getUsageType()
    {
        return usageType;
    }

    /**
     * Sets the value of the usageType property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setUsageType(String value)
    {
        this.usageType = value;
    }

    public boolean isSetUsageType()
    {
        return (this.usageType != null);
    }

    /**
     * Gets the value of the connector property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getConnector()
    {
        return connector;
    }

    /**
     * Sets the value of the connector property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setConnector(String value)
    {
        this.connector = value;
    }

    public boolean isSetConnector()
    {
        return (this.connector != null);
    }

    /**
     * Gets the value of the indicator property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getIndicator()
    {
        return indicator;
    }

    /**
     * Sets the value of the indicator property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setIndicator(String value)
    {
        this.indicator = value;
    }

    public boolean isSetIndicator()
    {
        return (this.indicator != null);
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed
     * property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and the value is the string
     * value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute by
     * updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes()
    {
        return otherAttributes;
    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attGroup ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}grPostal"/>
     *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *       &lt;anyAttribute namespace='##other'/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "content" })
    public static class DependentLocalityName
    {

        @XmlValue
        protected String content;

        @XmlAttribute(name = "Type")
        @XmlSchemaType(name = "anySimpleType")
        protected String type;

        @XmlAttribute(name = "Code")
        @XmlSchemaType(name = "anySimpleType")
        protected String code;

        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the content property.
         * 
         * @return possible object is {@link String }
         * 
         */
        public String getContent()
        {
            return content;
        }

        /**
         * Sets the value of the content property.
         * 
         * @param value allowed object is {@link String }
         * 
         */
        public void setContent(String value)
        {
            this.content = value;
        }

        public boolean isSetContent()
        {
            return (this.content != null);
        }

        /**
         * Gets the value of the type property.
         * 
         * @return possible object is {@link String }
         * 
         */
        public String getType()
        {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value allowed object is {@link String }
         * 
         */
        public void setType(String value)
        {
            this.type = value;
        }

        public boolean isSetType()
        {
            return (this.type != null);
        }

        /**
         * Gets the value of the code property.
         * 
         * @return possible object is {@link String }
         * 
         */
        public String getCode()
        {
            return code;
        }

        /**
         * Sets the value of the code property.
         * 
         * @param value allowed object is {@link String }
         * 
         */
        public void setCode(String value)
        {
            this.code = value;
        }

        public boolean isSetCode()
        {
            return (this.code != null);
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed
         * property on this class.
         * 
         * <p>
         * the map is keyed by the name of the attribute and the value is the
         * string value of the attribute.
         * 
         * the map returned by this method is live, and you can add new
         * attribute by updating the map directly. Because of this design,
         * there's no setter.
         * 
         * 
         * @return always non-null
         */
        public Map<QName, String> getOtherAttributes()
        {
            return otherAttributes;
        }

    }

    /**
     * <p>
     * Java class for anonymous complex type.
     * 
     * <p>
     * The following schema fragment specifies the expected content contained
     * within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attGroup ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}grPostal"/>
     *       &lt;attribute name="NameNumberOccurrence">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
     *             &lt;enumeration value="Before"/>
     *             &lt;enumeration value="After"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *       &lt;anyAttribute namespace='##other'/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "content" })
    public static class DependentLocalityNumber
    {

        @XmlValue
        protected String content;

        @XmlAttribute(name = "NameNumberOccurrence")
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String nameNumberOccurrence;

        @XmlAttribute(name = "Code")
        @XmlSchemaType(name = "anySimpleType")
        protected String code;

        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the content property.
         * 
         * @return possible object is {@link String }
         * 
         */
        public String getContent()
        {
            return content;
        }

        /**
         * Sets the value of the content property.
         * 
         * @param value allowed object is {@link String }
         * 
         */
        public void setContent(String value)
        {
            this.content = value;
        }

        public boolean isSetContent()
        {
            return (this.content != null);
        }

        /**
         * Gets the value of the nameNumberOccurrence property.
         * 
         * @return possible object is {@link String }
         * 
         */
        public String getNameNumberOccurrence()
        {
            return nameNumberOccurrence;
        }

        /**
         * Sets the value of the nameNumberOccurrence property.
         * 
         * @param value allowed object is {@link String }
         * 
         */
        public void setNameNumberOccurrence(String value)
        {
            this.nameNumberOccurrence = value;
        }

        public boolean isSetNameNumberOccurrence()
        {
            return (this.nameNumberOccurrence != null);
        }

        /**
         * Gets the value of the code property.
         * 
         * @return possible object is {@link String }
         * 
         */
        public String getCode()
        {
            return code;
        }

        /**
         * Sets the value of the code property.
         * 
         * @param value allowed object is {@link String }
         * 
         */
        public void setCode(String value)
        {
            this.code = value;
        }

        public boolean isSetCode()
        {
            return (this.code != null);
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed
         * property on this class.
         * 
         * <p>
         * the map is keyed by the name of the attribute and the value is the
         * string value of the attribute.
         * 
         * the map returned by this method is live, and you can add new
         * attribute by updating the map directly. Because of this design,
         * there's no setter.
         * 
         * 
         * @return always non-null
         */
        public Map<QName, String> getOtherAttributes()
        {
            return otherAttributes;
        }

    }

}
