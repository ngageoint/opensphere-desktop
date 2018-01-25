package io.opensphere.wps.source;

/**
 * Interface to an object that can perform WPS requests on a server.
 */
@FunctionalInterface
public interface WPSRequestExecuter
{
    /**
     * Executes the given request.
     *
     * @param request The request to send to the server.
     * @return The response.
     */
    WPSResponse execute(WPSRequest request);
}
