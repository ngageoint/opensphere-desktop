package io.opensphere.server.serverprovider.http.factory;

import java.awt.Component;
import java.util.function.Supplier;

import com.bitsys.common.http.client.ProxyConfig;

import io.opensphere.core.NetworkConfigurationManager;
import io.opensphere.core.SecurityManager;
import io.opensphere.server.serverprovider.SecurityComponentsProvider;

/**
 * Configures the proxy settings for an HttpClient object.
 *
 */
public class ProxyConfigurer
{
    /**
     * Configures the proxy settings for the specified HttpClient object.
     *
     * @param parameters The parameters to use to configure the proxy for the
     *            client.
     */
    public void configure(ConfigurerParameters parameters)
    {
        Supplier<? extends Component> parentComponent = parameters.getParentComponent();
        SecurityManager securityManager = parameters.getSecurityManager();
        SecurityComponentsProvider provider = parameters.getProvider();

        ProxyConfig httpProxyConfig = parameters.getClient().getOptions().getProxyConfig();
        httpProxyConfig.setProxyResolver(new SystemProxyResolver());
        httpProxyConfig.setCredentials(provider.getUserCredentials("proxy", NetworkConfigurationManager.PROXY_PURPOSE,
                parentComponent, securityManager));
    }
}
