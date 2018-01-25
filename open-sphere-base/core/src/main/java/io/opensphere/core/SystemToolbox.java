package io.opensphere.core;

/**
 * Toolbox for interacting with general system facilities.
 */
public interface SystemToolbox
{
    /**
     * Get the manager for the system memory.
     *
     * @return The memory manager.
     */
    MemoryManager getMemoryManager();

    /**
     * Get the network configuration manager.
     *
     * @return The network configuration manager.
     */
    NetworkConfigurationManager getNetworkConfigurationManager();

    /**
     * Get the manager for the splash screen. This allows the consumer to put
     * test messages on the splash image.
     *
     * @return the splash screen manager.
     */
    SplashScreenManager getSplashScreenManager();

    /**
     * Request that the application restart.
     */
    void requestRestart();
}
