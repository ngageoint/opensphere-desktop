package io.opensphere.mantle.controller;

import io.opensphere.core.util.Service;

/**
 * Manager for bulk data group activations.
 */
public interface DataGroupActivationManager
{
    /**
     * Get a service, that when opened, will add the given listener, and when
     * closed, will remove the given listener.
     *
     * @param listener The listener.
     * @return The service.
     */
    Service getActivationListenerService(Runnable listener);

    /**
     * Removes the activation listener.
     *
     * @param activationListener The activation listener.
     */
    void removeActivationListener(Runnable activationListener);

    /**
     * Adds the activation listener.
     *
     * @param activationListener The activation listener.
     */
    void addActivationListener(Runnable activationListener);
}
