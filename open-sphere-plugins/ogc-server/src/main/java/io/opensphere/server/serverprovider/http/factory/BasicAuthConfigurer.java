package io.opensphere.server.serverprovider.http.factory;

import java.awt.Component;
import java.util.function.Supplier;

import com.bitsys.common.http.auth.AuthenticationScope;
import com.bitsys.common.http.auth.CredentialsProvider;
import com.bitsys.common.http.client.HttpClient;

import io.opensphere.core.SecurityManager;
import io.opensphere.server.serverprovider.SecurityComponentsProvider;

/**
 * Configures an HttpClient object with basic authentication information.
 *
 */
public class BasicAuthConfigurer
{
    /**
     * Configures the specified HttpClient object with basic authentication
     * information.
     *
     * @param parameters The parameters needed to configure an HttpClient.
     */
    public void configure(ConfigurerParameters parameters)
    {
        SecurityComponentsProvider securityProvider = parameters.getProvider();
        String host = parameters.getHost();
        String serverKey = parameters.getServerKey();
        Supplier<? extends Component> parentComponent = parameters.getParentComponent();
        SecurityManager securityManager = parameters.getSecurityManager();

        HttpClient client = parameters.getClient();
        int port = parameters.getPort();

        CredentialsProvider credentialsProvider = client.getOptions().getCredentialsProvider();
        credentialsProvider.setCredentials(new AuthenticationScope(host, port),
                securityProvider.getUserCredentials(host, serverKey, parentComponent, securityManager));
    }
}
