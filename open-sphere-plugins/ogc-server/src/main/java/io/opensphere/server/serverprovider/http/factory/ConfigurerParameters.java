package io.opensphere.server.serverprovider.http.factory;

import java.awt.Component;
import java.util.function.Supplier;

import com.bitsys.common.http.client.HttpClient;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.server.serverprovider.SecurityComponentsProvider;

/**
 * Used to pass parameters to the configurers.
 *
 */
public class ConfigurerParameters
{
    /**
     * Retrieves different security configurations from the system and user,
     * used to connect to a server.
     */
    private SecurityComponentsProvider myProvider;

    /**
     * The client to configure.
     */
    private HttpClient myClient;

    /**
     * The server to connect to.
     */
    private String myHost;

    /**
     * The port to connect to.
     */
    private int myPort;

    /**
     * The server key.
     */
    private String myServerKey;

    /**
     * The main UI to use as a parent if a UI needs to be displayed for user
     * input.
     */
    private Supplier<? extends Component> myParentComponent;

    /**
     * The security manager.
     */
    private SecurityManager mySecurityManager;

    /**
     * The preferences registry.
     */
    private PreferencesRegistry myPrefsRegistry;

    /**
     * The network configuraiton manager.
     */
    private NetworkConfigurationManager myNetworkConfigurationManager;

    /**
     * Gets the provider that retrieves different security configurations from
     * the system and user, used to connect to a server.
     *
     * @return The security components provider.
     */
    public SecurityComponentsProvider getProvider()
    {
        return myProvider;
    }

    /**
     * Sets the provider that retrieves different security configurations from
     * the system and user, used to connect to a server.
     *
     * @param provider The new security components provider.
     */
    public void setProvider(SecurityComponentsProvider provider)
    {
        myProvider = provider;
    }

    /**
     * Gets the client to configure.
     *
     * @return The client to configure.
     */
    public HttpClient getClient()
    {
        return myClient;
    }

    /**
     * Sets the client to configure.
     *
     * @param client The client to configure.
     */
    public void setClient(HttpClient client)
    {
        myClient = client;
    }

    /**
     * Gets the server to connect to.
     *
     * @return The server to connect to.
     */
    public String getHost()
    {
        return myHost;
    }

    /**
     * Sets the server to connect to.
     *
     * @param host The server to connect to.
     */
    public void setHost(String host)
    {
        myHost = host;
    }

    /**
     * Gets the port to connect to.
     *
     * @return The port to connect to.
     */
    public int getPort()
    {
        return myPort;
    }

    /**
     * Sets the port to connect to.
     *
     * @param port The port to connect to.
     */
    public void setPort(int port)
    {
        myPort = port;
    }

    /**
     * Gets the server key.
     *
     * @return The server key.
     */
    public String getServerKey()
    {
        return myServerKey;
    }

    /**
     * Sets the server key.
     *
     * @param serverKey The server key.
     */
    public void setServerKey(String serverKey)
    {
        myServerKey = serverKey;
    }

    /**
     * Gets the main UI provider to use as a parent if a UI needs to be
     * displayed for user input.
     *
     * @return The main UI provider to use as a parent if a UI needs to be
     *         displayed for user input.
     */
    public Supplier<? extends Component> getParentComponent()
    {
        return myParentComponent;
    }

    /**
     * Sets the main UI provider to use as a parent if a UI needs to be
     * displayed for user input.
     *
     * @param parentComponent The main UI provider to use as a parent if a UI
     *            needs to be displayed for user input.
     */
    public void setParentComponent(Supplier<? extends Component> parentComponent)
    {
        myParentComponent = parentComponent;
    }

    /**
     * Gets the security manager.
     *
     * @return The security manager.
     */
    public SecurityManager getSecurityManager()
    {
        return mySecurityManager;
    }

    /**
     * Sets the security manager.
     *
     * @param securityManager The security manager.
     */
    public void setSecurityManager(SecurityManager securityManager)
    {
        mySecurityManager = securityManager;
    }

    /**
     * Gets the preferences registry.
     *
     * @return The preferences registry.
     */
    public PreferencesRegistry getPrefsRegistry()
    {
        return myPrefsRegistry;
    }

    /**
     * Sets the preferences registry.
     *
     * @param prefsRegistry the preferences registry.
     */
    public void setPrefsRegistry(PreferencesRegistry prefsRegistry)
    {
        myPrefsRegistry = prefsRegistry;
    }

    /**
     * Gets the network configuration manager.
     *
     * @return The network configuration manager.
     */
    public NetworkConfigurationManager getNetworkConfigurationManager()
    {
        return myNetworkConfigurationManager;
    }

    /**
     * Sets the network configuration manager.
     *
     * @param networkConfigurationManager The network configuration manager.
     */
    public void setNetworkConfigurationManager(NetworkConfigurationManager networkConfigurationManager)
    {
        myNetworkConfigurationManager = networkConfigurationManager;
    }
}
