package io.opensphere.core.server;

import java.util.Collection;
import java.util.Map;

import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ToStringHelper;

/**
 * Contains the response code and response message from the server.
 *
 */
public class ResponseValues
{
    /**
     * The returned length of the content.
     */
    private long myContentLength;

    /**
     * The header values of the response.
     */
    private Map<String, Collection<String>> myHeader;

    /**
     * The response code.
     */
    private int myResponseCode;

    /**
     * The response message.
     */
    private String myResponseMessage;

    /**
     * Gets the length of the returned content.
     *
     * @return The length of the returned content.
     */
    public long getContentLength()
    {
        return myContentLength;
    }

    /**
     * Gets the header values.
     *
     * @return header The header values.
     */
    public Map<String, Collection<String>> getHeader()
    {
        return myHeader;
    }

    /**
     * Gets the header value with the specified name.
     *
     * @param name The name of the header value to get.
     * @return The header value or null if there isn't one.
     */
    public String getHeaderValue(String name)
    {
        String value = null;

        if (myHeader != null)
        {
            String key = name != null ? name.toLowerCase() : null;
            Collection<String> values = myHeader.get(key);

            if (values != null)
            {
                value = StringUtilities.join(",", values);
            }
        }

        return value;
    }

    /**
     * Gets the response code.
     *
     * @return The response code.
     */
    public int getResponseCode()
    {
        return myResponseCode;
    }

    /**
     * Gets the response message.
     *
     * @return The response message.
     */
    public String getResponseMessage()
    {
        return myResponseMessage;
    }

    /**
     * Sets the length of the returned content.
     *
     * @param contentLength the length of the returned content.
     */
    public void setContentLength(long contentLength)
    {
        myContentLength = contentLength;
    }

    /**
     * Sets the header values.
     *
     * @param header The returned header values.
     */
    public void setHeader(Map<String, ? extends Collection<String>> header)
    {
        myHeader = New.map();
        for (Map.Entry<String, ? extends Collection<String>> entry : header.entrySet())
        {
            if (entry.getKey() != null)
            {
                myHeader.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }
    }

    /**
     * Sets the response code.
     *
     * @param responseCode The response code.
     */
    public void setResponseCode(int responseCode)
    {
        myResponseCode = responseCode;
    }

    /**
     * Sets the response message.
     *
     * @param responseMessage The response message.
     */
    public void setResponseMessage(String responseMessage)
    {
        myResponseMessage = responseMessage;
    }

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType()
    {
        String contentType = null;
        Collection<String> contentTypes = myHeader.get("content-type");
        if (CollectionUtilities.hasContent(contentTypes))
        {
            contentType = contentTypes.iterator().next();
        }
        return contentType;
    }

    @Override
    public String toString()
    {
        ToStringHelper helper = new ToStringHelper(this);
        helper.add("Response Code", myResponseCode);
        helper.add("Response Message", myResponseMessage);
        helper.add("Content Length", myContentLength);
        helper.add("Header", myHeader);
        return helper.toStringMultiLine();
    }
}
