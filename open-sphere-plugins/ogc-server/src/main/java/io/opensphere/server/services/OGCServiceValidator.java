package io.opensphere.server.services;

/**
 * Interface that OGC service plugins use to register for validation.
 */
public interface OGCServiceValidator
{
    /**
     * Gets the service being validated.
     *
     * @return the service
     */
    String getService();

    /**
     * Validate the specific service that this validator is tied to.
     *
     * @param configParams the configuration parameters needed to connect to the
     *            server.
     * @return ValidationResponse with some pertinent information from the
     *         server
     */
    OGCServiceValidationResponse validate(ServerConnectionParams configParams);
}
