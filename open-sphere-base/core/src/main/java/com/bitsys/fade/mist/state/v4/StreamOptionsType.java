//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.03.30 at 02:21:16 PM MDT 
//


package com.bitsys.fade.mist.state.v4;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *        Layer option related to streaming layers.
 *       
 * 
 * <p>Java class for StreamOptionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StreamOptionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="layer" type="{http://www.w3.org/2001/XMLSchema}token" minOccurs="0"/>
 *         &lt;element name="threshold" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="ageOffActive" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="ageOffTime" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *         &lt;element name="play" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="binSettings" type="{http://www.bit-sys.com/mist/state/v4}BinSettingsType" minOccurs="0"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StreamOptionsType", propOrder = {

})
public class StreamOptionsType {

    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String layer;
    protected BigInteger threshold;
    protected Boolean ageOffActive;
    protected BigInteger ageOffTime;
    protected Boolean play;
    protected BinSettingsType binSettings;

    /**
     * Gets the value of the layer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLayer() {
        return layer;
    }

    /**
     * Sets the value of the layer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLayer(String value) {
        this.layer = value;
    }

    public boolean isSetLayer() {
        return (this.layer!= null);
    }

    /**
     * Gets the value of the threshold property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getThreshold() {
        return threshold;
    }

    /**
     * Sets the value of the threshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setThreshold(BigInteger value) {
        this.threshold = value;
    }

    public boolean isSetThreshold() {
        return (this.threshold!= null);
    }

    /**
     * Gets the value of the ageOffActive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAgeOffActive() {
        return ageOffActive;
    }

    /**
     * Sets the value of the ageOffActive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAgeOffActive(Boolean value) {
        this.ageOffActive = value;
    }

    public boolean isSetAgeOffActive() {
        return (this.ageOffActive!= null);
    }

    /**
     * Gets the value of the ageOffTime property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getAgeOffTime() {
        return ageOffTime;
    }

    /**
     * Sets the value of the ageOffTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setAgeOffTime(BigInteger value) {
        this.ageOffTime = value;
    }

    public boolean isSetAgeOffTime() {
        return (this.ageOffTime!= null);
    }

    /**
     * Gets the value of the play property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPlay() {
        return play;
    }

    /**
     * Sets the value of the play property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPlay(Boolean value) {
        this.play = value;
    }

    public boolean isSetPlay() {
        return (this.play!= null);
    }

    /**
     * Gets the value of the binSettings property.
     * 
     * @return
     *     possible object is
     *     {@link BinSettingsType }
     *     
     */
    public BinSettingsType getBinSettings() {
        return binSettings;
    }

    /**
     * Sets the value of the binSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link BinSettingsType }
     *     
     */
    public void setBinSettings(BinSettingsType value) {
        this.binSettings = value;
    }

    public boolean isSetBinSettings() {
        return (this.binSettings!= null);
    }

}
