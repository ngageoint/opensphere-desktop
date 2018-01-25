package io.opensphere.wps.envoy;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.client.utils.URIBuilder;
import org.apache.log4j.Logger;

import io.opensphere.server.source.OGCServerSource;

/**
 * A generator in which URLs are built for interacting with remote WPS servers.
 */
public final class WpsUrlHelper
{
    /**
     * The <code>Log</code> instance used for logging.
     */
    private static final Logger LOG = Logger.getLogger(WpsUrlHelper.class);

    /**
     * Default constructor, private to prevent instantiation.
     */
    private WpsUrlHelper()
    {
        throw new UnsupportedOperationException("Instantiation of utility classes is not permitted.");
    }

    /**
     * Builds the URL to get WPS capabilities from the remote system.
     *
     * @param pRootUrl The root URL of the remote OGC WPS server.
     * @return the URL with which to get WPS capabilities from the remote system.
     */
    public static URL buildGetCapabilitiesUrl(String pRootUrl)
    {
        URL url = null;
        try
        {
            URIBuilder urlBuilder = new URIBuilder(pRootUrl);
            urlBuilder.addParameter(WpsParameter.SERVICE.getVariableName(), OGCServerSource.WPS_SERVICE);
            urlBuilder.addParameter(WpsParameter.REQUEST.getVariableName(), WpsRequestType.GET_CAPABLITIES.getValue());
            urlBuilder.addParameter(WpsParameter.VERSION.getVariableName(), "1.0.0");
            url = urlBuilder.build().toURL();
        }
        catch (MalformedURLException | URISyntaxException e)
        {
            LOG.error("Could not build URL to retrieve Capabilities doc.", e);
        }

        return url;
    }

    /**
     * Builds the URL to describe a single WPS process in the remote system.
     *
     * @param pRootUrl The root URL of the remote OGC WPS server.
     * @param pProcessId the identifier of the process to describe.
     * @return the URL with which to describe a single WPS process in the remote system.
     */
    public static URL buildDescribeProcessUrl(String pRootUrl, String pProcessId)
    {
        URL url = null;
        try
        {
            URIBuilder urlBuilder = new URIBuilder(pRootUrl);
            urlBuilder.addParameter(WpsParameter.SERVICE.getVariableName(), OGCServerSource.WPS_SERVICE);
            urlBuilder.addParameter(WpsParameter.REQUEST.getVariableName(), WpsRequestType.DESCRIBE_PROCESS_TYPE.getValue());
            urlBuilder.addParameter(WpsParameter.VERSION.getVariableName(), "1.1.0");
            urlBuilder.addParameter(WpsParameter.PROCESS_ID.getVariableName(), pProcessId);
            url = urlBuilder.build().toURL();
        }
        catch (MalformedURLException | URISyntaxException e)
        {
            LOG.error("Could not build URL to retrieve Capabilities doc.", e);
        }

        return url;
    }

    /**
     * Builds the URL to describe a single WPS process in the remote system.
     *
     * @param pRootUrl The root URL of the remote OGC WPS server.
     * @param pProcessIdentifier the identifier of the process to invoke.
     * @return the URL with which to describe a single WPS process in the remote system.
     */
    public static URL buildExecuteProcessUrl(String pRootUrl, String pProcessIdentifier)
    {
        URL url = null;
        try
        {
            URIBuilder urlBuilder = new URIBuilder(pRootUrl);
            urlBuilder.addParameter(WpsParameter.SERVICE.getVariableName(), OGCServerSource.WPS_SERVICE);
            urlBuilder.addParameter(WpsParameter.REQUEST.getVariableName(), WpsRequestType.EXECUTE.getValue());
            urlBuilder.addParameter(WpsParameter.VERSION.getVariableName(), "1.0.0");
            urlBuilder.addParameter(WpsParameter.PROCESS_ID.getVariableName(), pProcessIdentifier);
            url = urlBuilder.build().toURL();
        }
        catch (MalformedURLException | URISyntaxException e)
        {
            LOG.error("Could not build URL to execute process.", e);
        }

        return url;
    }
}
