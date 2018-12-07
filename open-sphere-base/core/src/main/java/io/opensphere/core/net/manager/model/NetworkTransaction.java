/**
 * 
 */
package io.opensphere.core.net.manager.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpResponse;
import com.google.common.collect.ListMultimap;

import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.lang.StringUtilities;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * @author Robert
 *
 */
public class NetworkTransaction
{
    private final ObjectProperty<HttpRequest> myRequestProperty = new ConcurrentObjectProperty<>();

    private final ObjectProperty<HttpResponse> myResponseProperty = new ConcurrentObjectProperty<>();

    private Date myTransactionStart;

    private Date myTransactionEnd;

    private String myUrl;

    private String myProtocol;

    private String myDomain;

    private String myFile;

    private Long myBytesSent;

    private Long myBytesReceived;

    /**
     * The HTTP method of request submitted (e.g.: HEAD, GET, POST, PUT, DELETE,
     * etc.).
     */
    private String myRequestMethod;

    private Integer myStatus;

    /** The content type of the response. */
    private String myContentType;

    private String myRequestBody;

    private final ObservableList<HttpKeyValuePair> myRequestHeaders = FXCollections.observableArrayList();

    private final ObservableList<HttpKeyValuePair> myResponseHeaders = FXCollections.observableArrayList();

    private final ObservableList<HttpKeyValuePair> myRequestParameters = FXCollections.observableArrayList();

    /**
     * 
     */
    public NetworkTransaction()
    {
        myRequestProperty.addListener((obs, ov, nv) -> updateValues(nv));
        myResponseProperty.addListener((obs, ov, nv) -> updateValues(nv));
    }

    protected void updateValues(HttpRequest request)
    {
        if (request != null)
        {
            myRequestMethod = request.getMethod();
            myBytesSent = request.getEntity().getContentLength();
            URI uri = request.getURI();
            myUrl = uri.toASCIIString();
            myDomain = uri.getHost();
            try
            {
                URL url = uri.toURL();
                myProtocol = url.getProtocol();
                myFile = myUrl.replaceAll(myProtocol + "://" + myDomain, "");
            }
            catch (MalformedURLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            ListMultimap<String, String> headers = request.getHeaders();
            Set<String> keys = headers.keySet();
            for (String key : keys)
            {
                headers.get(key).stream().forEach(v -> myRequestHeaders.add(new HttpKeyValuePair(key, v)));
            }

            // we can only read the body if the entity is marked as repeatable.
            if (request.getEntity().getContentLength() > 0 && request.getEntity().isRepeatable())
            {
                try (InputStream bodyStream = request.getEntity().getContent())
                {
                    myRequestBody = IOUtils.toString(bodyStream, StringUtilities.DEFAULT_CHARSET);
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        else
        {

        }
    }

    protected void updateValues(HttpResponse response)
    {
        if (response != null)
        {

        }
        else
        {

        }
    }

    /**
     * Gets the value of the {@link #myRequestProperty} field.
     *
     * @return the value of the myRequestProperty field.
     */
    public ObjectProperty<HttpRequest> getRequestProperty()
    {
        return myRequestProperty;
    }

    /**
     * Gets the value of the {@link #myResponseProperty} field.
     *
     * @return the value of the myResponseProperty field.
     */
    public ObjectProperty<HttpResponse> getResponseProperty()
    {
        return myResponseProperty;
    }

    /**
     * Gets the value of the {@link #myTransactionStart} field.
     *
     * @return the value of the myTransactionStart field.
     */
    public Date getTransactionStart()
    {
        return myTransactionStart;
    }

    /**
     * Gets the value of the {@link #myTransactionEnd} field.
     *
     * @return the value of the myTransactionEnd field.
     */
    public Date getTransactionEnd()
    {
        return myTransactionEnd;
    }

    /**
     * Gets the value of the {@link #myUrl} field.
     *
     * @return the value of the myUrl field.
     */
    public String getUrl()
    {
        return myUrl;
    }

    /**
     * Gets the value of the {@link #myProtocol} field.
     *
     * @return the value of the myProtocol field.
     */
    public String getProtocol()
    {
        return myProtocol;
    }

    /**
     * Gets the value of the {@link #myDomain} field.
     *
     * @return the value of the myDomain field.
     */
    public String getDomain()
    {
        return myDomain;
    }

    /**
     * Gets the value of the {@link #myFile} field.
     *
     * @return the value of the myFile field.
     */
    public String getFile()
    {
        return myFile;
    }

    /**
     * Gets the value of the {@link #myBytesSent} field.
     *
     * @return the value of the myBytesSent field.
     */
    public Long getBytesSent()
    {
        return myBytesSent;
    }

    /**
     * Gets the value of the {@link #myBytesReceived} field.
     *
     * @return the value of the myBytesReceived field.
     */
    public Long getBytesReceived()
    {
        return myBytesReceived;
    }

    /**
     * Gets the value of the {@link #myRequestMethod} field.
     *
     * @return the value of the myRequestMethod field.
     */
    public String getRequestMethod()
    {
        return myRequestMethod;
    }

    /**
     * Gets the value of the {@link #myStatus} field.
     *
     * @return the value of the myStatus field.
     */
    public Integer getStatus()
    {
        return myStatus;
    }

    /**
     * Gets the value of the {@link #myContentType} field.
     *
     * @return the value of the myRequestType field.
     */
    public String getRequestType()
    {
        return myContentType;
    }

    /**
     * Gets the value of the {@link #myRequestBody} field.
     *
     * @return the value of the myRequestBody field.
     */
    public String getRequestBody()
    {
        return myRequestBody;
    }

    /**
     * Gets the value of the {@link #myRequestHeaders} field.
     *
     * @return the value of the myRequestHeaders field.
     */
    public ObservableList<HttpKeyValuePair> getRequestHeaders()
    {
        return myRequestHeaders;
    }

    /**
     * Gets the value of the {@link #myResponseHeaders} field.
     *
     * @return the value of the myResponseHeaders field.
     */
    public ObservableList<HttpKeyValuePair> getResponseHeaders()
    {
        return myResponseHeaders;
    }

    /**
     * Gets the value of the {@link #myRequestParameters} field.
     *
     * @return the value of the myRequestParameters field.
     */
    public ObservableList<HttpKeyValuePair> getRequestParameters()
    {
        return myRequestParameters;
    }
}
