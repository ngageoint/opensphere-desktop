package io.opensphere.wps.response;

import io.opensphere.core.Toolbox;
import io.opensphere.wps.source.WPSResponse;

/** The wps image response handler class. */
public class WPSImageResponseHandler extends WPSResponseHandler
{
    /**
     * Constructor.
     *
     * @param response The wps response.
     */
    public WPSImageResponseHandler(WPSResponse response)
    {
        super(response);
    }

    @Override
    public Object handleResponse(Toolbox toolbox, String name)
    {
        return null;
    }
}
