package io.opensphere.mantle.util.importer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.opensphere.core.common.connection.HttpHeaders.HttpResponseHeader;
import io.opensphere.core.util.net.UrlUtilities;

/**
 * Configuration of a single KML file source.
 */
public class URLDataSource
{
    /** The actual path (including any query parameters). */
    private String myActualPath;

    /** The error message if this data source failed to load. */
    private String myErrorMessage;

    /** The reason this data source failed to load. */
    private FailureReason myFailureReason;

    /** Whether there was a load error. */
    private boolean myLoadError;

    /** The KML file absolute path. */
    private String myPath = "";

    /** The HTTP response headers. */
    private Map<String, String> myResponseHeaders;

    /** The type of data source (where to read the file). */
    private Type myType = Type.URL;

    /**
     * Instantiates a new uRL data source.
     *
     * @param url the url
     */
    public URLDataSource(String url)
    {
        myPath = url;
    }

    /**
     * Instantiates a new uRL data source.
     *
     * @param url the url
     */
    public URLDataSource(URL url)
    {
        myPath = url.toExternalForm();
        if (UrlUtilities.isFile(url))
        {
            myType = Type.FILE;
        }
    }

    /**
     * Getter for actualPath.
     *
     * @return the actualPath
     */
    public String getActualPath()
    {
        if (myActualPath == null)
        {
            myActualPath = getPath();
        }
        return myActualPath;
    }

    /**
     * Gets the content length.
     *
     * @return the content length
     */
    public int getContentLength()
    {
        int contentLength = 0;
        try
        {
            contentLength = Integer.parseInt(myResponseHeaders.get(HttpResponseHeader.CONTENT_LENGTH.getFieldName()));
        }
        catch (NumberFormatException ex)
        {
            contentLength = -1;
        }
        return contentLength;
    }

    /**
     * Getter for errorMessage.
     *
     * @return the errorMessage
     */
    public String getErrorMessage()
    {
        return myErrorMessage;
    }

    /**
     * Getter for failureReason.
     *
     * @return the failureReason
     */
    public FailureReason getFailureReason()
    {
        return myFailureReason;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath()
    {
        return myPath;
    }

    /**
     * Gets the response headers.
     *
     * @return the response headers
     */
    public Map<String, String> getResponseHeaders()
    {
        if (myResponseHeaders == null)
        {
            myResponseHeaders = new HashMap<>();
        }
        return myResponseHeaders;
    }

    /**
     * Getter for type.
     *
     * @return the type
     */
    public Type getType()
    {
        return myType;
    }

    /**
     * Load error.
     *
     * @return true, if successful
     */
    public boolean loadError()
    {
        return myLoadError;
    }

    /**
     * Setter for actualPath.
     *
     * @param actualPath the actualPath
     */
    public void setActualPath(String actualPath)
    {
        myActualPath = actualPath;
    }

    /**
     * Setter for errorMessage.
     *
     * @param errorMessage the errorMessage
     */
    public void setErrorMessage(String errorMessage)
    {
        myErrorMessage = errorMessage;
    }

    /**
     * Setter for failureReason.
     *
     * @param failureReason the failureReason
     */
    public void setFailureReason(FailureReason failureReason)
    {
        myFailureReason = failureReason;
    }

    /**
     * Sets the load error.
     *
     * @param error the error
     */
    public void setLoadError(boolean error)
    {
        myLoadError = error;
    }

    /**
     * Sets the path.
     *
     * @param path the new path
     */
    public void setPath(String path)
    {
        myPath = path;
    }

    /**
     * Setter for type.
     *
     * @param type the type
     */
    public void setType(Type type)
    {
        myType = type;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString()
    {
        return "URLDataSource [path=" + myPath + ", type=" + getType() + "]";
    }

    /** The type of data source (where to read the file). */
    public enum Type
    {
        /** File data source type. */
        FILE,

        /** URL data source type. */
        URL;
    }
}
