package io.opensphere.core.image.exception;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * An XML Tag in which a service exception is wrapped.
 */
@XmlType(name = "ServiceException")
public class ServiceExceptionType
{
    /**
     * The error code assigned to the exception.
     */
    private String myCode;

    /**
     * The value of the XML tag.
     */
    private String myValue;

    /**
     * Gets the value of the {@link #myCode} field.
     *
     * @return the value stored in the {@link #myCode} field.
     */
    @XmlAttribute(name = "code")
    public String getCode()
    {
        return myCode;
    }

    /**
     * Sets the value of the {@link #myCode} field.
     *
     * @param pCode the value to store in the {@link #myCode} field.
     */
    public void setCode(String pCode)
    {
        this.myCode = pCode;
    }

    /**
     * Gets the value of the {@link #myValue} field.
     *
     * @return the value stored in the {@link #myValue} field.
     */
    @XmlValue
    public String getValue()
    {
        return myValue;
    }

    /**
     * Sets the value of the {@link #myValue} field.
     *
     * @param pValue the value to store in the {@link #myValue} field.
     */
    public void setValue(String pValue)
    {
        this.myValue = pValue;
    }
}
