package io.opensphere.mantle.datasources.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Stores the configuration for a single URL data source.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement()
public class UrlDataSource extends AbstractDataSource
{
    /** The layer name. */
    @XmlElement(name = "name", required = true)
    private volatile String myName;

    /** Whether the layer is active. */
    @XmlElement(name = "active", required = false)
    private volatile boolean myIsActive = true;

    /** Whether there was a load error. */
    @XmlTransient
    private volatile boolean myLoadError;

    /** The base URL string. */
    @XmlElement(name = "baseUrl", required = true)
    private volatile String myBaseUrl;

    /**
     * Constructor.
     */
    public UrlDataSource()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param name the name
     * @param baseUrl the base URL
     */
    public UrlDataSource(String name, String baseUrl)
    {
        super();
        myName = name;
        myBaseUrl = baseUrl;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    @Override
    public boolean isActive()
    {
        return myIsActive;
    }

    @Override
    public boolean loadError()
    {
        return myLoadError;
    }

    @Override
    public void setActive(boolean isActive)
    {
        myIsActive = isActive;
    }

    @Override
    public void setLoadError(boolean error, Object source)
    {
        myLoadError = error;
    }

    @Override
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Gets the base URL string.
     *
     * @return the base URL string
     */
    public String getBaseUrl()
    {
        return myBaseUrl;
    }

    /**
     * Sets the base URL.
     *
     * @param baseUrl the base URL
     */
    public void setBaseUrl(String baseUrl)
    {
        myBaseUrl = baseUrl;
    }

    /**
     * Creates a near copy of this data source suitable for export.
     *
     * @return An exportable copy of this data source
     */
    public UrlDataSource createExportDataSource()
    {
        UrlDataSource copySource = new UrlDataSource();
        copySource.myName = myName;
        copySource.myBaseUrl = myBaseUrl;
        copySource.myIsActive = false;
        copySource.myLoadError = false;
        return copySource;
    }
}
