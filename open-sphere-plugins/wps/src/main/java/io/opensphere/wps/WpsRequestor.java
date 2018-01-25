package io.opensphere.wps;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.server.ResponseValues;
import io.opensphere.wps.source.WPSRequest;
import io.opensphere.wps.source.WPSResponse;

/** WPS requestor class. */
public class WpsRequestor
{
    /** The class logger. */
    private static final Logger LOGGER = Logger.getLogger(WpsRequestor.class);

    /** The wps request. */
    private final WPSRequest myRequest;

    /**
     * Constructor.
     *
     * @param request The wps request.
     */
    public WpsRequestor(WPSRequest request)
    {
        myRequest = request;
    }

    /**
     * Find the wps response.
     *
     * @param pEnvoy The envoy with which the request will be executed.
     * @return The wps response.
     */
    public WPSResponse getResponse(LegacyWpsExecuteEnvoy pEnvoy)
    {
        InputStream inputStream = null;
        WPSResponse response = null;
        if (StringUtils.isEmpty(myRequest.getBaseWpsUrl()))
        {
            String serverURL = pEnvoy.getServerConfig().getWpsUrl();
            if (StringUtils.isNotEmpty(serverURL))
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Base URL not set, using the appropriate from the envoy: " + serverURL);
                }
                myRequest.setBaseWpsUrl(serverURL);
            }
        }
        String contentType = "unknown";
        try
        {
            URL url = new URL(myRequest.createURLString());
            LOGGER.info("Performing request: " + myRequest.getIdentifier() + " - " + url.toString());
            ResponseValues httpResponse = new ResponseValues();

            if (pEnvoy != null)
            {
                inputStream = pEnvoy.requestExecuteFromServer(url, httpResponse);
            }
            if (httpResponse.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                LOGGER.error("Server execute request failed: " + httpResponse.getResponseCode() + " "
                        + httpResponse.getResponseMessage());
                return response;
            }

            contentType = httpResponse.getHeaderValue("Content-Type");
            LOGGER.info("Response is of type: " + contentType);
        }
        catch (MalformedURLException e)
        {
            LOGGER.error("Error while executing WPS request", e);
        }
        response = new WPSResponse(myRequest, inputStream);
        response.setResponseType(contentType);
        return response;
    }
}
