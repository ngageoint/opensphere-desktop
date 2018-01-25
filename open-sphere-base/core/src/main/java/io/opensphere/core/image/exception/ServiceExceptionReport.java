package io.opensphere.core.image.exception;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A JAXB annotated POJO in which a service exception report is described.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "ServiceExceptionReport")
public class ServiceExceptionReport
{
    /**
     * The version of the ArcMap WMS Server that produced the error.
     */
    @XmlAttribute(name = "version")
    private String myVersion;

    /**
     * The service exception wrapped in the report.
     */
    @XmlElement(name = "ServiceException")
    private ServiceExceptionType myServiceException;

    /**
     * Sets the value of the {@link #myVersion} field.
     *
     * @param version the value to store in the {@link #myVersion} field.
     */
    public void setVersion(String version)
    {
        myVersion = version;
    }

    /**
     * Gets the value of the {@link #myVersion} field.
     *
     * @return the value stored in the {@link #myVersion} field.
     */
    public String getVersion()
    {
        return myVersion;
    }

    /**
     * Gets the value of the {@link #myServiceException} field.
     *
     * @return the value stored in the {@link #myServiceException} field.
     */
    public ServiceExceptionType getServiceException()
    {
        return myServiceException;
    }

    /**
     * Sets the value of the {@link #myServiceException} field.
     *
     * @param serviceException the value to store in the
     *            {@link #myServiceException} field.
     */
    public void setServiceException(ServiceExceptionType serviceException)
    {
        myServiceException = serviceException;
    }
}
