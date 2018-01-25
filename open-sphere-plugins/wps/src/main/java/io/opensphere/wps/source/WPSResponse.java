package io.opensphere.wps.source;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/** WPS response class. */
public class WPSResponse
{
    /** The class logger. */
    private static final Logger LOGGER = Logger.getLogger(WPSResponse.class);

    /** The response stream. */
    private InputStream myResponse;

    /** The request. */
    private WPSRequest myRequest;

    /** The response type. */
    private String myResponseType;

    /**
     * Constructor.
     *
     * @param request The wps request.
     * @param response The response.
     */
    public WPSResponse(WPSRequest request, InputStream response)
    {
        myResponse = response;
        myRequest = request;
    }

    /**
     * Close the input stream.
     */
    public void closeInputStream()
    {
        try
        {
            // Ensure that the input stream is closed.
            if (myResponse != null)
            {
                myResponse.close();
            }
        }
        catch (IOException e)
        {
            LOGGER.info("IOException: " + e.getMessage());
        }
    }

    /**
     * Standard getter.
     *
     * @return The request.
     */
    public WPSRequest getRequest()
    {
        return myRequest;
    }

    /**
     * Standard getter.
     *
     * @return The response.
     */
    public InputStream getResponseStream()
    {
        return myResponse;
    }

    /**
     * Standard getter.
     *
     * @return The response type.
     */
    public String getResponseType()
    {
        return myResponseType;
    }

    /**
     * Standard setter.
     *
     * @param request The request.
     */
    public void setRequest(WPSRequest request)
    {
        myRequest = request;
    }

    /**
     * Standard setter.
     *
     * @param response The response.
     */
    public void setResponseStream(InputStream response)
    {
        myResponse = response;
    }

    /**
     * Standard setter.
     *
     * @param type The response type.
     */
    public void setResponseType(String type)
    {
        myResponseType = type;
    }
}
