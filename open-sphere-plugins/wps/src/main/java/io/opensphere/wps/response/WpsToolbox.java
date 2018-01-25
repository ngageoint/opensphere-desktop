package io.opensphere.wps.response;

import java.util.function.Function;

import io.opensphere.core.PluginToolbox;
import io.opensphere.wps.source.WPSResponse;

/** The WPS toolbox. */
public class WpsToolbox implements PluginToolbox
{
    /** The GML response handler creator. */
    private volatile Function<WPSResponse, WPSGmlResponseHandler> myGmlResponseHandlerCreator;

    /**
     * Creates a GML response handler.
     *
     * @param response the response to handle
     * @return the handler
     */
    public WPSGmlResponseHandler createGmlResponseHandler(WPSResponse response)
    {
        return myGmlResponseHandlerCreator.apply(response);
    }

    /**
     * Sets the GML response handler creator.
     *
     * @param gmlResponseHandlerCreator the GML response handler creator
     */
    public void setGmlResponseHandlerCreator(Function<WPSResponse, WPSGmlResponseHandler> gmlResponseHandlerCreator)
    {
        myGmlResponseHandlerCreator = gmlResponseHandlerCreator;
    }

    @Override
    public String getDescription()
    {
        return "WPS Toolbox";
    }
}
