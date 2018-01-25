package io.opensphere.wps.response;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.wps.source.WPSResponse;

/**
 * WPS response handler factory that determines what response handler to use for
 * the different types.
 */
public final class WPSResponseHandlerFactory
{
    /** Static logging reference. */
    private static final Logger LOGGER = Logger.getLogger(WPSResponseHandlerFactory.class);

    /** The instance of this class. */
    private static WPSResponseHandlerFactory ourInstance;

    /**
     * Get the instance of this class.
     *
     * @return The instance of this class.
     */
    public static synchronized WPSResponseHandlerFactory instance()
    {
        if (ourInstance == null)
        {
            ourInstance = new WPSResponseHandlerFactory();
        }
        return ourInstance;
    }

    /**
     * Default constructor.
     */
    private WPSResponseHandlerFactory()
    {
    }

    /**
     * Get the correct response handler for the given response.
     *
     * @param response The wps response.
     * @param toolbox The toolbox
     * @return The wps response handler.
     */
    public WPSResponseHandler getResponseHandler(WPSResponse response, Toolbox toolbox)
    {
        WPSResponseHandler ret = null;
        if (response.getResponseType().contains("vnd.ogc.se"))
        {
            ret = new WPSServiceErrorHandler(response);
        }
        else if (response.getResponseType().contains("image"))
        {
            ret = new WPSImageResponseHandler(response);
        }
        else if (response.getResponseType().contains("xml"))
        {
            WpsToolbox wpsToolbox = toolbox.getPluginToolboxRegistry().getPluginToolbox(WpsToolbox.class);
            ret = wpsToolbox.createGmlResponseHandler(response);
        }
        /* else if (response.getResponseType().contains("kml")) { // TODO: not
         * implemented yet } */
        else if (response.getResponseType().contains("zip") && response.getResponseType().contains("shape"))
        {
            ret = new WPSShapefileResponseHandler(response);
        }
        else
        {
            LOGGER.error("Could not find WPSResponseHandler for return type" + response.getResponseType());
            ret = new WPSTextResponseHandler(response);
        }
        return ret;
    }
}
