package io.opensphere.server.toolbox;

import io.opensphere.server.services.OGCServiceValidator;

/**
 * The ServerValidatorRegistry Interface.
 */
public interface ServerValidatorRegistry
{
    /**
     * Register a validator for a given service.
     *
     * @param validator the validator associated with the service
     */
    void register(OGCServiceValidator validator);

    /**
     * Retrieve the validator for the specified service.
     *
     * @param service the service to be validated
     * @return the validator associated with the specified service
     */
    OGCServiceValidator retrieve(String service);
}
