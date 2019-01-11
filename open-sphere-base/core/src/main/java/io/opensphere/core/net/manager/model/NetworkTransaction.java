package io.opensphere.core.net.manager.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.log4j.Logger;

import com.bitsys.common.http.entity.HttpEntity;
import com.bitsys.common.http.message.HttpRequest;
import com.bitsys.common.http.message.HttpResponse;

import io.opensphere.core.net.NetworkReceiveEvent;
import io.opensphere.core.net.NetworkTransmitEvent;
import io.opensphere.core.util.javafx.ConcurrentObjectProperty;
import io.opensphere.core.util.javafx.ConcurrentStringProperty;
import io.opensphere.core.util.lang.StringUtilities;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A model of a network transaction, in which all fields describing the
 * transaction are encapsulated and observable.
 */
public class NetworkTransaction
{
    /** The logger used to capture output from instances of this class. */
    private static final Logger LOG = Logger.getLogger(NetworkTransaction.class);

    /** The optional transaction ID used to identify the transaction. */
    private final String myTransactionId;

    /** The property in which the send event is maintained. */
    private final ObjectProperty<NetworkTransmitEvent> mySendEventProperty = new ConcurrentObjectProperty<>();

    /** The property in which the receive event is maintained. */
    private final ObjectProperty<NetworkReceiveEvent> myReceiveEventProperty = new ConcurrentObjectProperty<>();

    /** The date / time at which the send event was initiated. */
    private Date myTransactionStart;

    /**
     * The date / time at which the send event concluded, or if a response was
     * received, the last byte of the response was received.
     */
    private Date myTransactionEnd;

    /** The property in which the string form of the URL is stored. */
    private final StringProperty myUrl = new ConcurrentStringProperty();

    /** The protocol used in the transaction. */
    private String myProtocol;

    /** The domain called during the transaction. */
    private String myDomain;

    /**
     * The file portion of the URL called during the transaction (technically,
     * this is actually, the 'path', 'query' and 'fragment').
     */
    private String myFile;

    /** The amount of data sent during the transaction. */
    private Long myBytesSent;

    /** The amount of data received during the transaction. */
    private Long myBytesReceived;

    /**
     * The HTTP method of request submitted (e.g.: HEAD, GET, POST, PUT, DELETE,
     * etc.).
     */
    private String myRequestMethod;

    /** The status of the transaction. */
    private Integer myStatus;

    /** The content type of the response. */
    private String myContentType;

    /** The body of the request, if sent. */
    private String myRequestBody;

    /** The body of the response, if sent. */
    private InputStream myResponseBody;

    /** The cookies referenced during the transaction. */
    private final ObservableList<HttpKeyValuePair> myRequestCookies = FXCollections.observableArrayList();

    /** The headers sent during the transaction. */
    private final ObservableList<HttpKeyValuePair> myRequestHeaders = FXCollections.observableArrayList();

    /** The headers received during the transaction. */
    private final ObservableList<HttpKeyValuePair> myResponseHeaders = FXCollections.observableArrayList();

    /** The parameters sent during the transaction. */
    private final ObservableList<HttpKeyValuePair> myRequestParameters = FXCollections.observableArrayList();

    /**
     * Creates a new transaction using the supplied transaction ID. The ID is
     * used to link a request and a response into the the same transaction, as
     * they may not occur synchronously.
     *
     * @param transactionId the ID of the transaction.
     */
    public NetworkTransaction(final String transactionId)
    {
        myTransactionId = transactionId;
        mySendEventProperty.addListener((obs, ov, nv) -> updateValues(nv));
        myReceiveEventProperty.addListener((obs, ov, nv) -> updateValues(nv));
    }

    /**
     * Gets the value of the {@link #myTransactionId} field.
     *
     * @return the value stored in the {@link #myTransactionId} field.
     */
    public String getTransactionId()
    {
        return myTransactionId;
    }

    /**
     * Updates the transaction with the send event's information.
     *
     * @param event the event to process.
     */
    private void updateValues(final NetworkTransmitEvent event)
    {
        final HttpRequest request = event.getRequest();
        myTransactionStart = event.getEventTime();

        final CookieStore cookieStore = event.getCookieStore();
        if (cookieStore != null)
        {
            cookieStore.getCookies().stream().map(c -> new HttpKeyValuePair(c.getName(), c.getValue()))
                    .forEach(myRequestCookies::add);
        }

        if (request != null)
        {
            myRequestMethod = request.getMethod();

            final URI uri = request.getURI();
            myUrl.set(uri.toASCIIString());
            myDomain = uri.getHost();
            try
            {
                final URL url = uri.toURL();
                myProtocol = url.getProtocol();
                myFile = myUrl.get().replaceAll(myProtocol + "://" + myDomain, "");

                final String queryString = url.getQuery();
                if (StringUtils.isNotBlank(queryString))
                {
                    final String[] tokens = queryString.split("&");
                    for (final String parameterPair : tokens)
                    {
                        if (parameterPair.contains("="))
                        {
                            final String[] parameterTokens = parameterPair.split("=");
                            String value = "";
                            if (parameterTokens.length > 1)
                            {
                                value = parameterTokens[1];
                            }
                            myRequestParameters.add(new HttpKeyValuePair(parameterTokens[0], value));
                        }
                    }
                }
            }
            catch (final MalformedURLException e)
            {
                LOG.debug("Malformed URL.", e);
            }

            request.getHeaders().forEach((k, v) -> myRequestHeaders.add(new HttpKeyValuePair(k, v)));

            if (request.getEntity() != null)
            {
                myBytesSent = request.getEntity().getContentLength();
                // we can only read the body if the entity is marked as
                // repeatable.
                if (request.getEntity().getContentLength() > 0 && request.getEntity().isRepeatable())
                {
                    try (InputStream bodyStream = request.getEntity().getContent())
                    {
                        myRequestBody = IOUtils.toString(bodyStream, StringUtilities.DEFAULT_CHARSET);
                    }
                    catch (final IOException e)
                    {
                        LOG.debug("Malformed URL.", e);
                    }
                }
            }
        }
    }

    /**
     * Updates the transaction with the receive event's information.
     *
     * @param event the event to process.
     */
    private void updateValues(final NetworkReceiveEvent event)
    {
        final HttpResponse response = event.getResponse();
        myTransactionEnd = event.getEventTime();
        if (response != null)
        {
            myStatus = response.getStatusCode();
            myResponseBody = event.getContent();

            response.getHeaders().forEach((k, v) -> myResponseHeaders.add(new HttpKeyValuePair(k, v)));

            if (response.getEntity() != null)
            {
                final HttpEntity entity = response.getEntity();
                myBytesReceived = entity.getContentLength();
                if (entity.getContentType() != null)
                {
                    myContentType = entity.getContentType().getMimeType();
                }
                else
                {
                    myContentType = "UNKNOWN";
                }
            }
        }
    }

    /**
     * Gets the value of the {@link #mySendEventProperty} field.
     *
     * @return the value of the mySendEventProperty field.
     */
    public ObjectProperty<NetworkTransmitEvent> sendEventProperty()
    {
        return mySendEventProperty;
    }

    /**
     * Gets the value of the {@link #myReceiveEventProperty} field.
     *
     * @return the value of the myReceiveEventProperty field.
     */
    public ObjectProperty<NetworkReceiveEvent> receiveEventProperty()
    {
        return myReceiveEventProperty;
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
        return myUrl.get();
    }

    /**
     * Gets the value of the {@link #myUrl} field.
     *
     * @return the value of the myUrl field.
     */
    public StringProperty urlProperty()
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
     * @return the value stored in the {@link #myContentType} field.
     */
    public String getContentType()
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

    /**
     * Gets the value of the {@link #myRequestCookies} field.
     *
     * @return the value stored in the {@link #myRequestCookies} field.
     */
    public ObservableList<HttpKeyValuePair> getRequestCookies()
    {
        return myRequestCookies;
    }

    /**
     * Gets the value of the {@link #myResponseBody} field.
     *
     * @return the value stored in the {@link #myResponseBody} field.
     */
    public InputStream getResponseBody()
    {
        return myResponseBody;
    }
}
