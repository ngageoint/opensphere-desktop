//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 12:20:41 PM MST 
//

package net.opengis.wms_130;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element ref="{http://www.opengis.net/wms}ContactPersonPrimary" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}ContactPosition" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}ContactAddress" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}ContactVoiceTelephone" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}ContactFacsimileTelephone" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wms}ContactElectronicMailAddress" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "contactPersonPrimary", "contactPosition", "contactAddress", "contactVoiceTelephone",
    "contactFacsimileTelephone", "contactElectronicMailAddress" })
@XmlRootElement(name = "ContactInformation")
public class ContactInformation
{

    @XmlElement(name = "ContactPersonPrimary")
    protected ContactPersonPrimary contactPersonPrimary;

    @XmlElement(name = "ContactPosition")
    protected String contactPosition;

    @XmlElement(name = "ContactAddress")
    protected ContactAddress contactAddress;

    @XmlElement(name = "ContactVoiceTelephone")
    protected String contactVoiceTelephone;

    @XmlElement(name = "ContactFacsimileTelephone")
    protected String contactFacsimileTelephone;

    @XmlElement(name = "ContactElectronicMailAddress")
    protected String contactElectronicMailAddress;

    /**
     * Gets the value of the contactPersonPrimary property.
     * 
     * @return possible object is {@link ContactPersonPrimary }
     * 
     */
    public ContactPersonPrimary getContactPersonPrimary()
    {
        return contactPersonPrimary;
    }

    /**
     * Sets the value of the contactPersonPrimary property.
     * 
     * @param value allowed object is {@link ContactPersonPrimary }
     * 
     */
    public void setContactPersonPrimary(ContactPersonPrimary value)
    {
        this.contactPersonPrimary = value;
    }

    /**
     * Gets the value of the contactPosition property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getContactPosition()
    {
        return contactPosition;
    }

    /**
     * Sets the value of the contactPosition property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setContactPosition(String value)
    {
        this.contactPosition = value;
    }

    /**
     * Gets the value of the contactAddress property.
     * 
     * @return possible object is {@link ContactAddress }
     * 
     */
    public ContactAddress getContactAddress()
    {
        return contactAddress;
    }

    /**
     * Sets the value of the contactAddress property.
     * 
     * @param value allowed object is {@link ContactAddress }
     * 
     */
    public void setContactAddress(ContactAddress value)
    {
        this.contactAddress = value;
    }

    /**
     * Gets the value of the contactVoiceTelephone property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getContactVoiceTelephone()
    {
        return contactVoiceTelephone;
    }

    /**
     * Sets the value of the contactVoiceTelephone property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setContactVoiceTelephone(String value)
    {
        this.contactVoiceTelephone = value;
    }

    /**
     * Gets the value of the contactFacsimileTelephone property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getContactFacsimileTelephone()
    {
        return contactFacsimileTelephone;
    }

    /**
     * Sets the value of the contactFacsimileTelephone property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setContactFacsimileTelephone(String value)
    {
        this.contactFacsimileTelephone = value;
    }

    /**
     * Gets the value of the contactElectronicMailAddress property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getContactElectronicMailAddress()
    {
        return contactElectronicMailAddress;
    }

    /**
     * Sets the value of the contactElectronicMailAddress property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setContactElectronicMailAddress(String value)
    {
        this.contactElectronicMailAddress = value;
    }

}
