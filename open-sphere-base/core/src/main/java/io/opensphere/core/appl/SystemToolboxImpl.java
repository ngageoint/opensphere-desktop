package io.opensphere.core.appl;

import io.opensphere.core.MemoryManager;
import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.SplashScreenManager;
import io.opensphere.core.SystemToolbox;
import io.opensphere.core.net.NetworkConfigurationManagerImpl;
import io.opensphere.core.preferences.PreferencesRegistry;

/** Implementation of {@link SystemToolbox}. */
public abstract class SystemToolboxImpl implements SystemToolbox
{
    /** The memory manager. */
    private final MemoryManager myMemoryManager;

    /** The network configuration manager. */
    private final NetworkConfigurationManager myNetworkConfigurationManager;

    /** The splash screen manager. */
    private final SplashScreenManager mySplashScreenManager;

    /**
     * Constructor.
     *
     * @param prefsRegistry The system preferences registry.
     */
    public SystemToolboxImpl(PreferencesRegistry prefsRegistry)
    {
        mySplashScreenManager = new SplashScreenManagerImpl();
        myMemoryManager = new MemoryManagerImpl();
        myNetworkConfigurationManager = new NetworkConfigurationManagerImpl(prefsRegistry);
    }

    @Override
    public MemoryManager getMemoryManager()
    {
        return myMemoryManager;
    }

    @Override
    public NetworkConfigurationManager getNetworkConfigurationManager()
    {
        return myNetworkConfigurationManager;
    }

    @Override
    public SplashScreenManager getSplashScreenManager()
    {
        return mySplashScreenManager;
    }
}
