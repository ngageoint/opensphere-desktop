package io.opensphere.arcgis.config.v1;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.datasources.DataSourceChangeEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.AbstractDataSource;
import io.opensphere.server.source.ServerSource;

/**
 * Stores the configuration for ArcGIS Server Sources.
 */
@XmlRootElement(name = "ArcGISServerSource")
@XmlAccessorType(XmlAccessType.FIELD)
public class ArcGISServerSource extends AbstractDataSource implements ServerSource
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ArcGISServerSource.class);

    /** The active flag. */
    @XmlElement(name = "active")
    private boolean myActive = true;

    /** The base URL for the server. */
    @XmlElement(name = "BaseServerURL")
    private String myBaseServerURL;

    /** The connect timeout in milliseconds. */
    @XmlAttribute(name = "connectTimeoutMillis", required = false)
    private int myConnectTimeoutMillis = 20000;

    /** The load error. */
    @XmlAttribute(name = "loadError", required = false)
    private boolean myLoadError;

    /** The server name. */
    @XmlAttribute(name = "name", required = true)
    private String myName;

    /** The read timeout in milliseconds. */
    @XmlAttribute(name = "readTimeoutMillis", required = false)
    private int myReadTimeoutMillis = 120000;

    /**
     * Default constructor.
     */
    public ArcGISServerSource()
    {
        super();
    }

    @Override
    public ServerSource createExportDataSource()
    {
        ArcGISServerSource copySource = new ArcGISServerSource();
        copySource.myActive = false;
        copySource.myName = myName;
        copySource.myBaseServerURL = myBaseServerURL;
        return copySource;
    }

    @Override
    public void exportToFile(File selectedFile, Component parent, final ActionListener callback)
    {
        boolean success = true;

        // Write the data source config to the file system
        try
        {
            XMLUtilities.writeXMLObject(this, selectedFile);
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
                callback.actionPerformed(new ActionEvent(ArcGISServerSource.this, 0, result));
            }
        });
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

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName()
    {
        return myName;
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

    @Override
    public String getURL(String service)
    {
        return myBaseServerURL;
    }

    @Override
    public String getURLString()
    {
        return myBaseServerURL;
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
     * Accessor for the loadError.
     *
     * @return The loadError.
     */
    public boolean isLoadError()
    {
        return myLoadError;
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
     * Mutator for the loadError.
     *
     * @param loadError The loadError to set.
     */
    public void setLoadError(boolean loadError)
    {
        myLoadError = loadError;
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

    /**
     * Sets the name.
     *
     * @param dataSetName the new name
     */
    @Override
    public void setName(String dataSetName)
    {
        myName = dataSetName;
    }

    @Override
    public void setURL(String service, String url)
    {
        myBaseServerURL = url;
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
        sb.append("] Base URL[").append(myBaseServerURL);
        sb.append("] Active[").append(myActive);
        sb.append("] LoadError[").append(myLoadError).append(']');
        return sb.toString();
    }

    /**
     * Set the connect timeout in milliseconds.
     *
     * @param connectTimeoutMillis The connect timeout in milliseconds.
     */
    protected void setConnectTimeoutMillis(int connectTimeoutMillis)
    {
        myConnectTimeoutMillis = connectTimeoutMillis;
    }

    /**
     * Set the read timeout in milliseconds.
     *
     * @param readTimeoutMillis The read timeout in milliseconds.
     */
    protected void setReadTimeoutMillis(int readTimeoutMillis)
    {
        myReadTimeoutMillis = readTimeoutMillis;
    }
}
