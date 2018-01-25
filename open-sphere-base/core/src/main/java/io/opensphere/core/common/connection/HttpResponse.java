package io.opensphere.core.common.connection;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class contains values available in the HTTP response header.
 */
public class HttpResponse
{
    /**
     * The HTTP response code.
     */
    private int responseCode;

    /**
     * The textual response message.
     */
    private String responseMessage;

    /**
     * The HTTP response headers.
     */
    private Map<String, String> responseHeaders;

    /**
     * The character set encoding.
     */
    private String charSet;

    /**
     * The content length in bytes.
     */
    private long contentLength;

    /**
     * Sets the response code.
     *
     * @param responseCode the HTTP response code.
     */
    protected void setResponseCode(int responseCode)
    {
        this.responseCode = responseCode;
    }

    /**
     * Returns the response code.
     *
     * @return the HTTP response code.
     */
    public int getResponseCode()
    {
        return responseCode;
    }

    /**
     * Sets the textual response message.
     *
     * @param reasonPhrase the textual response message.
     */
    public void setResponseMessage(String responseMessage)
    {
        this.responseMessage = responseMessage;
    }

    /**
     * Returns the textual response message.
     *
     * @return the textual response message.
     */
    public String getResponseMessage()
    {
        return responseMessage;
    }

    /**
     * Sets the HTTP response headers.
     *
     * @param responseHeaders the HTTP response headers.
     */
    protected void setResponseHeaders(Map<String, String> responseHeaders)
    {
        SortedMap<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        map.putAll(responseHeaders);
        this.responseHeaders = Collections.unmodifiableSortedMap(map);
    }

    /**
     * Returns the HTTP response headers. This map is unmodifiable and the keys
     * are case-insensitive.
     *
     * @return the HTTP response headers.
     */
    public Map<String, String> getResponseHeaders()
    {
        return responseHeaders;
    }

    /**
     * Sets the character encoding of the response from the
     * <code>Content-Type</code> header.
     *
     * @param charSet the character set encoding.
     */
    public void setCharSet(String charSet)
    {
        this.charSet = charSet;
    }

    /**
     * Returns the character encoding of the response from the
     * <code>Content-Type</code> header.
     *
     * @return the character set encoding.
     */
    public String getCharSet()
    {
        return charSet;
    }

    /**
     * Sets the length (in bytes) of the response body, as specified in a
     * <code>Content-Length</code> header.
     *
     * @param contentLength the content length in bytes.
     */
    public void setContentLength(long contentLength)
    {
        this.contentLength = contentLength;
    }

    /**
     * Return the length (in bytes) of the response body, as specified in a
     * <code>Content-Length</code> header.
     *
     * @return content length, if <code>Content-Length</code> header is
     *         available. <code>0</code> indicates that the request has no body.
     *         If the <code>Content-Length</code> header is not present, returns
     *         <code>-1</code>.
     */
    public long getContentLength()
    {
        return contentLength;
    }
}
