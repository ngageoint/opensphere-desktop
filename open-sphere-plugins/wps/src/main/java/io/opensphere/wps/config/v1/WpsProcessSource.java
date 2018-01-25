package io.opensphere.wps.config.v1;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Objects;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.opensphere.core.util.ColorUtilities;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.HashCodeHelper;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.LoadsTo;
import io.opensphere.mantle.datasources.DataSourceChangeEvent;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.impl.AbstractDataSource;
import io.opensphere.wps.source.WPSRequest;

/**
 * Configuration of a single WPS source.
 */
@XmlRootElement(name = "WpsSource")
@XmlAccessorType(XmlAccessType.FIELD)
public class WpsProcessSource extends AbstractDataSource
{
    /** My process type. */
    @XmlElement(name = "processType", required = true)
    private String myProcessType = "";

    /** The server id. */
    @XmlElement(name = "serverId", required = true)
    private String myServerId = "";

    /** My server. */
    @XmlElement(name = "server", required = true)
    private String myServer = "";

    /** My name. */
    @XmlAttribute
    private String myName = "";

    /** My enabled flag. */
    @XmlAttribute
    private boolean myEnabled;

    /** My request. */
    @XmlElement(name = "request", required = true)
    private WPSRequest myRequest;

    /** My color. */
    @XmlElement(name = "color", required = false)
    private String myColor = "0-255-0-255";

    /** The last response received for this request. */
    @XmlTransient
    private Object myServerResponse;

    /** My override color. */
    @XmlElement(name = "overrideColor", required = false)
    private boolean myOverrideColor;

    /** My load error. */
    @XmlAttribute(name = "loadError", required = false)
    private boolean myLoadError;

    /** My loads to. */
    @XmlElement(name = "LoadsTo", required = false)
    private LoadsTo myLoadsTo = LoadsTo.BASE;

    /**
     * Get Color as RGB string.
     *
     * @param color the color
     * @return the RGB string representation
     */
    public static String colorToRGBString(Color color)
    {
        if (color == null)
        {
            return "0-0-0-0";
        }

        int red = color.getRed();
        int blue = color.getBlue();
        int green = color.getGreen();
        int alpha = color.getAlpha();

        String s = Integer.toString(red) + "-" + Integer.toString(green) + "-" + Integer.toString(blue) + "-"
                + Integer.toString(alpha);

        return s;
    }

    /**
     * Default constructor.
     */
    public WpsProcessSource()
    {
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
        WpsProcessSource other = (WpsProcessSource)obj;
        //@formatter:off
        return Objects.equals(myName, other.myName)
                && Objects.equals(myServerId, other.myServerId)
                && myEnabled == other.myEnabled
                && Objects.equals(myRequest, other.myRequest)
                && Objects.equals(myColor, other.myColor)
                && Objects.equals(myProcessType, other.myProcessType)
                && Objects.equals(myServer, other.myServer)
                && myLoadError == other.myLoadError
                && Objects.equals(myLoadsTo, other.myLoadsTo);
        //@formatter:on
    }

    @Override
    public void exportToFile(File selectedFile, Component parent, final ActionListener callback)
    {
        boolean success = false;
        try
        {
            XMLUtilities.writeXMLObject(this, selectedFile);
            success = true;
        }
        catch (JAXBException exc)
        {
            success = false;
        }

        final String result = success ? EXPORT_SUCCESS : EXPORT_FAILED;
        EventQueueUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                callback.actionPerformed(new ActionEvent(WpsProcessSource.this, 0, result));
            }
        });
    }

    /**
     * Gets the color.
     *
     * @return a Color object constructed from the text color string
     */
    public Color getColor()
    {
        return ColorUtilities.convertFromColorString(myColor);
    }

    /**
     * Gets the loads to.
     *
     * @return the loads to
     */
    public LoadsTo getLoadsTo()
    {
        return myLoadsTo;
    }

    @Override
    public String getName()
    {
        return myName;
    }

    /**
     * Gets the process type.
     *
     * @return the process type
     */
    public String getProcessType()
    {
        return myProcessType;
    }

    /**
     * Gets the request.
     *
     * @return the request
     */
    public WPSRequest getRequest()
    {
        return myRequest;
    }

    /**
     * Gets the server.
     *
     * @return the server
     */
    public String getServer()
    {
        return myServer;
    }

    /**
     * Gets the server id.
     *
     * @return the server id
     */
    public String getServerId()
    {
        return myServerId;
    }

    /**
     * Gets the server response.
     *
     * @return the server response
     */
    public Object getServerResponse()
    {
        return myServerResponse;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + HashCodeHelper.getHashCode(myName);
        result = prime * result + HashCodeHelper.getHashCode(myServerId);
        result = prime * result + HashCodeHelper.getHashCode(myEnabled);
        result = prime * result + HashCodeHelper.getHashCode(myRequest);
        result = prime * result + HashCodeHelper.getHashCode(myColor);
        result = prime * result + HashCodeHelper.getHashCode(myProcessType);
        result = prime * result + HashCodeHelper.getHashCode(myServer);
        result = prime * result + HashCodeHelper.getHashCode(myLoadError);
        result = prime * result + HashCodeHelper.getHashCode(myLoadsTo);
        return result;
    }

    @Override
    public boolean isActive()
    {
        return isEnabled();
    }

    /**
     * Checks if is enabled.
     *
     * @return true, if is enabled
     */
    public boolean isEnabled()
    {
        return myEnabled;
    }

    /**
     * Checks if color has been overridden.
     *
     * @return true, if is override color
     */
    public boolean isOverrideColor()
    {
        return myOverrideColor;
    }

    @Override
    public boolean loadError()
    {
        return myLoadError;
    }

    @Override
    public void setActive(boolean active)
    {
        setEnabled(active);
    }

    /**
     * Sets the color.
     *
     * @param color the new color from java.awt.Color
     */
    public void setColor(Color color)
    {
        myColor = colorToRGBString(color);
    }

    /**
     * Sets the color.
     *
     * @param color the new Color to set from String
     */
    public void setColor(String color)
    {
        myColor = color;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        myEnabled = enabled;
    }

    /**
     * Sets this {@link WpsProcessSource} equal to another WpsProcessSource.
     *
     * @param other the other shape file source
     * @throws IllegalArgumentException if other is null
     */
    public void setEqualTo(WpsProcessSource other)
    {
        if (other == null)
        {
            throw new IllegalArgumentException("Argument to setEqualTo method cannot be null.");
        }
        myName = other.myName;
        myServerId = other.myServerId;
        myEnabled = other.myEnabled;
        myLoadError = other.myLoadError;
        myLoadsTo = other.myLoadsTo;
        myRequest = other.myRequest;
        myColor = other.myColor;
        myProcessType = other.myProcessType;
        myServer = other.myServer;
        myOverrideColor = other.myOverrideColor;
    }

    @Override
    public void setLoadError(boolean error, Object source)
    {
        myLoadError = error;
        fireDataSourceChanged(new DataSourceChangeEvent(this, IDataSource.SOURCE_LOAD_ERROR_CHANGED, source));
    }

    /**
     * Sets the loads to.
     *
     * @param loadsTo the new loads to
     */
    public void setLoadsTo(LoadsTo loadsTo)
    {
        myLoadsTo = loadsTo;
    }

    @Override
    public void setName(String name)
    {
        myName = name;
    }

    /**
     * Sets whether the color has been overridden.
     *
     * @param overrideColor the new override color
     */
    public void setOverrideColor(boolean overrideColor)
    {
        myOverrideColor = overrideColor;
    }

    /**
     * Sets the process type.
     *
     * @param processType the new process type
     */
    public void setProcessType(String processType)
    {
        myProcessType = processType;
    }

    /**
     * Sets the request.
     *
     * @param request the new request
     */
    public void setRequest(WPSRequest request)
    {
        myRequest = request;
    }

    /**
     * Sets the server.
     *
     * @param server the new server
     */
    public void setServer(String server)
    {
        myServer = server;
    }

    /**
     * Sets the server id.
     *
     * @param server the new server id
     */
    public void setServerId(String server)
    {
        myServerId = server;
    }

    /**
     * Sets the server response.
     *
     * @param serverResponse the new server response
     */
    public void setServerResponse(Object serverResponse)
    {
        myServerResponse = serverResponse;
    }

    @Override
    public boolean supportsFileExport()
    {
        return true;
    }
}
