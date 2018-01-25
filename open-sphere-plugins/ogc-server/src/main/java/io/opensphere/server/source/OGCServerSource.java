package io.opensphere.server.source;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.datasources.DataSourceChangeEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.AbstractDataSource;
import io.opensphere.server.customization.ServerCustomization;
import io.opensphere.server.util.ServerConstants;

/**
 * Stores the configuration for OGC Server Sources.
 */
@XmlRootElement(name = "OGCServerSource")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.GodClass")
public class OGCServerSource extends AbstractDataSource implements ServerSource
{
    /** String used to identify a WMS GetMap service. */
    public static final String WMS_GETMAP_SERVICE = "WMS GetMap";

    /** String used to identify a WPS service. */
    public static final String WPS_SERVICE = "WPS";

    /** String used to identify a WFS service. */
    public static final String WFS_SERVICE = "WFS";

    /** String used to identify a WMS service. */
    public static final String WMS_SERVICE = "WMS";

    /** The Constant ourSessionUniqueIDCounter. */
    private static final AtomicInteger ourSessionUniqueIDCounter = new AtomicInteger(1000);

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(OGCServerSource.class);

    /** The name given to servers until one can be assigned. */
    public static final String DEFAULT_SERVER_NAME = "------";

    /** The base URL used for WMS requests. */
    @XmlElement(name = "WMSServerURL")
    private String myWMSServerURL;

    /** The base URL used for WMS GetMap requests. */
    @XmlElement(name = "WMSGetMapServerUrlOverride")
    private String myWMSGetMapServerUrlOverride;

    /** The base URL used for WFS requests. */
    @XmlElement(name = "WFSServerURL")
    private String myWFSServerURL;

    /** The base URL used for WPS requests. */
    @XmlElement(name = "WPSServerURL")
    private String myWPSServerURL;

    /** String representation of which type of server this is. */
    @XmlElement(name = "ServerType")
    private String myServerType = ServerCustomization.DEFAULT_TYPE;

    /** The server name. */
    @XmlElement(name = "name")
    private String myName = DEFAULT_SERVER_NAME;

    /** The active flag. */
    @XmlElement(name = "active")
    private boolean myActive = true;

    /** The load error. */
    @XmlTransient
    private boolean myLoadError;

    /** The connect timeout in milliseconds. */
    @XmlAttribute(name = "connectTimeoutMillis", required = false)
    private int myConnectTimeoutMillis = -1;

    /** The read timeout in milliseconds. */
    @XmlAttribute(name = "readTimeoutMillis", required = false)
    private int myReadTimeoutMillis = -1;

    /** The activate timeout in milliseconds. */
    @XmlAttribute(name = "activateTimeoutMillis", required = false)
    private int myActivateTimeoutMillis = -1;

    /**
     * The url to the permalink file upload service.
     */
    @XmlElement(name = "permalinkUrl")
    private String myPermalinkUrl = ServerConstants.DEFAULT_PERMALINK_URL;

    /** The Session unique id. */
    @XmlTransient
    private final int mySessionUniqueId = ourSessionUniqueIDCounter.incrementAndGet();

    /**
     * Default constructor.
     */
    public OGCServerSource()
    {
        super();
    }

    @Override
    public OGCServerSource createExportDataSource()
    {
        OGCServerSource copySource = new OGCServerSource();
        copySource.myActive = false;
        copySource.myName = myName;
        copySource.myWMSServerURL = myWMSServerURL;
        copySource.myWMSGetMapServerUrlOverride = myWMSGetMapServerUrlOverride;
        copySource.myWFSServerURL = myWFSServerURL;
        copySource.myWPSServerURL = myWPSServerURL;
        copySource.myServerType = myServerType;
        return copySource;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        OGCServerSource other = (OGCServerSource)obj;
        //@formatter:off
        return Objects.equals(myWMSServerURL, other.myWMSServerURL)
                && Objects.equals(myWMSGetMapServerUrlOverride, other.myWMSGetMapServerUrlOverride)
                && Objects.equals(myWFSServerURL, other.myWFSServerURL)
                && Objects.equals(myWPSServerURL, other.myWPSServerURL)
                && Objects.equals(myServerType, other.myServerType)
                && Objects.equals(myName, other.myName)
                && myActive == other.myActive
                && myLoadError == other.myLoadError;
        //@formatter:on
    }

    @Override
    public void exportToFile(File selectedFile, Component parent, final ActionListener callback)
    {
        boolean success = true;

        // Create a copy of the data source suitable for export
        OGCServerSource copySource = createExportDataSource();

        // Write the data source config to the file system
        try
        {
            XMLUtilities.writeXMLObject(copySource, selectedFile);
        }
        catch (JAXBException e)
        {
            LOGGER.error(e.getMessage(), e);
            success = false;
        }

        // Notify the caller
        final String result = success ? EXPORT_SUCCESS : EXPORT_FAILED;
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                callback.actionPerformed(new ActionEvent(OGCServerSource.this, 0, result));
            }
        });
    }

    /**
     * Gets the activate timeout in milliseconds.
     *
     * @return the activate timeout in milliseconds
     */
    public int getActivateTimeoutMillis()
    {
        return myActivateTimeoutMillis <= 0 ? getReadTimeoutMillis() + getConnectTimeoutMillis() : myActivateTimeoutMillis;
    }

    /**
     * Get the connect timeout in milliseconds.
     *
     * @return The connect timeout in milliseconds.
     */
    public int getConnectTimeoutMillis()
    {
        return myConnectTimeoutMillis;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the permalink service url.
     *
     * @return The permalink url.
     */
    public String getPermalinkUrl()
    {
        return myPermalinkUrl;
    }

    /**
     * Get the read timeout in milliseconds.
     *
     * @return The read timeout in milliseconds.
     */
    public int getReadTimeoutMillis()
    {
        return myReadTimeoutMillis;
    }

    /**
     * Gets the server type.
     *
     * @return the server type
     */
    public String getServerType()
    {
        return myServerType;
    }

    /**
     * Gets the session unique id.
     *
     * @return the session unique id
     */
    public int getSessionUniqueId()
    {
        return mySessionUniqueId;
    }

    @Override
    public String getURL(String service)
    {
        String url;
        if (WMS_SERVICE.equals(service))
        {
            url = getWMSServerURL();
        }
        else if (WFS_SERVICE.equals(service))
        {
            url = getWFSServerURL();
        }
        else if (WPS_SERVICE.equals(service))
        {
            url = getWPSServerURL();
        }
        else if (WMS_GETMAP_SERVICE.equals(service))
        {
            url = getWMSGetMapServerUrlOverride();
        }
        else
        {
            url = null;
        }
        return url;
    }

    /**
     * Get the WFS URL.
     *
     * @return the WFS URL
     */
    public String getWFSServerURL()
    {
        return myWFSServerURL;
    }

    /**
     * Gets the URL to use for WMS GetMap requests if the user has overridden
     * the URL from the WMS GetCapabilities document.
     *
     * @return the WMS GetMap URL Override
     */
    public String getWMSGetMapServerUrlOverride()
    {
        return myWMSGetMapServerUrlOverride;
    }

    /**
     * Get the WMS URL.
     *
     * @return the WMS URL
     */
    public String getWMSServerURL()
    {
        return myWMSServerURL;
    }

    /**
     * Get the WPS URL.
     *
     * @return the WPS URL
     */
    public String getWPSServerURL()
    {
        return myWPSServerURL;
    }

    @Override
    @SuppressWarnings("PMD.OverrideMerelyCallsSuper")
    public int hashCode()
    {
        return super.hashCode();
    }

    /**
     * Check essential elements of this source for anything critical that has
     * been left blank. This will check if all of the Server URLs are empty or
     * if the server name is either null, empty, or defaulted.
     *
     * @return true, if source has valid content for all essential fields
     */
    public boolean hasValidContent()
    {
        return !StringUtils.isBlank(myName) && !DEFAULT_SERVER_NAME.equals(myName)
                && !(StringUtils.isBlank(myWMSServerURL) && StringUtils.isBlank(myWFSServerURL)
                        && StringUtils.isBlank(myWPSServerURL));
    }

    /**
     * Checks if is active.
     *
     * @return true, if is active
     */
    @Override
    public boolean isActive()
    {
        return myActive;
    }

    /**
     * Load error.
     *
     * @return true, if successful
     */
    @Override
    public boolean loadError()
    {
        return myLoadError;
    }

    /**
     * Sets the activate timeout milliseconds.
     *
     * @param activateTimeoutMillis the activate timeout milliseconds
     */
    public void setActivateTimeoutMillis(int activateTimeoutMillis)
    {
        myActivateTimeoutMillis = activateTimeoutMillis;
    }

    /**
     * Sets the active.
     *
     * @param active the new active
     */
    @Override
    public void setActive(boolean active)
    {
        myActive = active;
    }

    /**
     * Set the connect timeout in milliseconds.
     *
     * @param connectTimeoutMillis The connect timeout in milliseconds.
     */
    public void setConnectTimeoutMillis(int connectTimeoutMillis)
    {
        myConnectTimeoutMillis = connectTimeoutMillis;
    }

    /**
     * Sets the load error.
     *
     * @param error the error
     * @param source the source
     */
    @Override
    public void setLoadError(boolean error, Object source)
    {
        myLoadError = error;
        fireDataSourceChanged(new DataSourceChangeEvent(this, IDataSource.SOURCE_LOAD_ERROR_CHANGED, source));
    }

    @Override
    public void setName(String dataSetName)
    {
        myName = dataSetName;
    }

    /**
     * Sets the permalink service url.
     *
     * @param permalinkUrl The permalink url.
     */
    public void setPermalinkUrl(String permalinkUrl)
    {
        myPermalinkUrl = permalinkUrl;
    }

    /**
     * Set the read timeout in milliseconds.
     *
     * @param readTimeoutMillis The read timeout in milliseconds.
     */
    public void setReadTimeoutMillis(int readTimeoutMillis)
    {
        myReadTimeoutMillis = readTimeoutMillis;
    }

    /**
     * Sets the server type.
     *
     * @param serverType the new server type
     */
    public void setServerType(String serverType)
    {
        myServerType = serverType;
    }

    @Override
    public void setURL(String service, String url)
    {
        if (WMS_SERVICE.equals(service))
        {
            setWMSServerURL(url);
        }
        else if (WFS_SERVICE.equals(service))
        {
            setWFSServerURL(url);
        }
        else if (WPS_SERVICE.equals(service))
        {
            setWPSServerURL(url);
        }
        else if (WMS_GETMAP_SERVICE.equals(service))
        {
            setWMSGetMapServerUrlOverride(url);
        }
    }

    /**
     * Set the WFS URL.
     *
     * @param wfsServerUrl the WMS URL
     */
    public void setWFSServerURL(String wfsServerUrl)
    {
        myWFSServerURL = wfsServerUrl;
    }

    /**
     * Set the WMS GetMap Override URL.
     *
     * @param wmsGetMapServerUrlOverride the WMS GetMap Override URL
     */
    public void setWMSGetMapServerUrlOverride(String wmsGetMapServerUrlOverride)
    {
        myWMSGetMapServerUrlOverride = wmsGetMapServerUrlOverride;
    }

    /**
     * Set the WMS URL.
     *
     * @param wmsServerUrl the WMS URL
     */
    public void setWMSServerURL(String wmsServerUrl)
    {
        myWMSServerURL = wmsServerUrl;
    }

    /**
     * Set the WPS URL.
     *
     * @param wpsServerUrl the WPS URL
     */
    public void setWPSServerURL(String wpsServerUrl)
    {
        myWPSServerURL = wpsServerUrl;
    }

    @Override
    public boolean supportsFileExport()
    {
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append(": Name[").append(myName);
        sb.append("] WMS[").append(myWMSServerURL);
        sb.append("] WMSOverride[").append(myWMSGetMapServerUrlOverride);
        sb.append("] WFS[").append(myWFSServerURL);
        sb.append("] WPS[").append(myWPSServerURL);
        sb.append("] Type[").append(myServerType);
        sb.append("] Active[").append(myActive);
        sb.append("] LoadError[").append(myLoadError).append(']');
        return sb.toString();
    }
}
