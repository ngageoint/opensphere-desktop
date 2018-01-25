package io.opensphere.server.serverprovider;

import java.awt.Component;
import java.util.function.Supplier;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import com.bitsys.common.http.auth.Credentials;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.authentication.UserInteractionX509KeyManager;
import io.opensphere.core.authentication.UserInteractionX509TrustManager;
import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * Gets different security components necessary in building a valid
 * client/server connection.
 */
public class SecurityComponentsProviderImpl implements SecurityComponentsProvider
{
    @Override
    public KeyManager getKeyManager(String serverName, String serverKey, Supplier<? extends Component> parentProvider,
            PreferencesRegistry preferencesRegistry, SecurityManager securityManager)
    {
        return new UserInteractionX509KeyManager(serverName, serverKey, parentProvider, preferencesRegistry, securityManager,
                null);
    }

    @Override
    public TrustManager getTrustManager(String serverName, String serverKey, Supplier<? extends Component> parentProvider,
            SecurityManager securityManager)
    {
        return new UserInteractionX509TrustManager(serverName, serverKey, parentProvider, securityManager, null);
    }

    @Override
    public Credentials getUserCredentials(String serverName, String serverKey, Supplier<? extends Component> parentProvider,
            SecurityManager securityManager)
    {
        return new UserInteractionUsernamePasswordCredentials(serverName, serverKey, parentProvider, securityManager, null);
    }
}
