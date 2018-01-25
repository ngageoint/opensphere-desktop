package io.opensphere.wps;

import io.opensphere.wps.source.WPSRequest;
import io.opensphere.wps.source.WPSRequestExecuter;
import io.opensphere.wps.source.WPSResponse;

/**
 * Executes a WPS request using the {@link LegacyWpsExecuteEnvoy}.
 */
public class WPSRequestExecuterImpl implements WPSRequestExecuter
{
    /**
     * The wps envoy.
     */
    private final LegacyWpsExecuteEnvoy myEnvoy;

    /**
     * Constructs a new request executer.
     *
     * @param envoy The envoy used to make the request.
     */
    public WPSRequestExecuterImpl(LegacyWpsExecuteEnvoy envoy)
    {
        myEnvoy = envoy;
    }

    @Override
    public WPSResponse execute(WPSRequest request)
    {
        WpsRequestor requestor = new WpsRequestor(request);

        return requestor.getResponse(myEnvoy);
    }
}
