//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.01.26 at 02:04:22 PM MST 
//

package net.opengis.wfs._110;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * The TransactionType defines the Transaction operation. A Transaction element
 * contains one or more Insert, Update Delete and Native elements that allow a
 * client application to create, modify or remove feature instances from the
 * feature repository that a Web Feature Service controls.
 * 
 * 
 * <p>
 * Java class for TransactionType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="TransactionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wfs}BaseRequestType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wfs}LockId" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/wfs}Insert"/>
 *           &lt;element ref="{http://www.opengis.net/wfs}Update"/>
 *           &lt;element ref="{http://www.opengis.net/wfs}Delete"/>
 *           &lt;element ref="{http://www.opengis.net/wfs}Native"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="releaseAction" type="{http://www.opengis.net/wfs}AllSomeType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionType", propOrder = { "lockId", "insertOrUpdateOrDelete" })
public class TransactionType extends BaseRequestType
{

    @XmlElement(name = "LockId")
    protected String lockId;

    @XmlElements({ @XmlElement(name = "Update", type = UpdateElementType.class),
        @XmlElement(name = "Insert", type = InsertElementType.class), @XmlElement(name = "Native", type = NativeType.class),
        @XmlElement(name = "Delete", type = DeleteElementType.class) })
    protected List<Object> insertOrUpdateOrDelete;

    @XmlAttribute(name = "releaseAction")
    protected AllSomeType releaseAction;

    /**
     * 
     * In order for a client application to operate upon locked feature
     * instances, the Transaction request must include the LockId element. The
     * content of this element must be the lock identifier the client
     * application obtained from a previous GetFeatureWithLock or LockFeature
     * operation.
     * 
     * If the correct lock identifier is specified the Web Feature Service knows
     * that the client application may operate upon the locked feature
     * instances.
     * 
     * No LockId element needs to be specified to operate upon unlocked
     * features.
     * 
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getLockId()
    {
        return lockId;
    }

    /**
     * Sets the value of the lockId property.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setLockId(String value)
    {
        this.lockId = value;
    }

    /**
     * Gets the value of the insertOrUpdateOrDelete property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the insertOrUpdateOrDelete property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getInsertOrUpdateOrDelete().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UpdateElementType } {@link InsertElementType } {@link NativeType }
     * {@link DeleteElementType }
     * 
     * 
     */
    public List<Object> getInsertOrUpdateOrDelete()
    {
        if (insertOrUpdateOrDelete == null)
        {
            insertOrUpdateOrDelete = new ArrayList<Object>();
        }
        return this.insertOrUpdateOrDelete;
    }

    /**
     * Gets the value of the releaseAction property.
     * 
     * @return possible object is {@link AllSomeType }
     * 
     */
    public AllSomeType getReleaseAction()
    {
        return releaseAction;
    }

    /**
     * Sets the value of the releaseAction property.
     * 
     * @param value allowed object is {@link AllSomeType }
     * 
     */
    public void setReleaseAction(AllSomeType value)
    {
        this.releaseAction = value;
    }

}
