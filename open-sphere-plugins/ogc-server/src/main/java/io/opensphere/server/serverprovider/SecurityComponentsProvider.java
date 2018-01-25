package io.opensphere.server.serverprovider;

import java.awt.Component;
import java.util.function.Supplier;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import com.bitsys.common.http.auth.Credentials;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * Gets different security components necessary in building a valid
 * client/server connection.
 */
public interface SecurityComponentsProvider
{
    /**
     * Gets the key manager.
     *
     * @param serverName The server to connect to.
     * @param serverKey The server key.
     * @param parentProvider The parent provider.
     * @param preferencesRegistry The preferences registry.
     * @param securityManager The security manager.
     * @return The key manager.
     */
    KeyManager getKeyManager(String serverName, String serverKey, Supplier<? extends Component> parentProvider,
            PreferencesRegistry preferencesRegistry, SecurityManager securityManager);

    /**
     * Gets the trust manager.
     *
     * @param serverName The server to connect to.
     * @param serverKey The server key.
     * @param parentProvider The parent provider.
     * @param securityManager The security manager.
     * @return The trust manager.
     */
    TrustManager getTrustManager(String serverName, String serverKey, Supplier<? extends Component> parentProvider,
            SecurityManager securityManager);

    /**
     * Constructor.
     *
     * @param serverName The server name to present to the user in dialog text.
     * @param serverKey The unique key to use for the server in the preferences.
     * @param parentProvider The parent component provider.
     * @param securityManager The system security manager.
     *
     * @return The user's name and password.
     */
    Credentials getUserCredentials(String serverName, String serverKey, Supplier<? extends Component> parentProvider,
            SecurityManager securityManager);
}
