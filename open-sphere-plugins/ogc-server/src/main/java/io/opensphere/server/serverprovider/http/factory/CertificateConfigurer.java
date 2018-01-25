package io.opensphere.server.serverprovider.http.factory;

import java.awt.Component;
import java.util.Collections;
import java.util.function.Supplier;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.client.SslConfig;
import com.bitsys.common.http.ssl.LenientHostNameVerifier;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.common.connection.CertificateConfiguration;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.server.serverprovider.SecurityComponentsProvider;

/**
 * Configures the HttpClient with the necessary user's certificate information.
 *
 */
public class CertificateConfigurer
{
    /**
     * Configures the HttpClient with the necessary user's certificate
     * information.
     *
     * @param parameters The parameters needed to configure an HttpClient.
     */
    public void configure(ConfigurerParameters parameters)
    {
        CertificateConfiguration certConfig = getCertConfig(parameters);

        if (certConfig != null && certConfig.isUseCertificate())
        {
            HttpClient client = parameters.getClient();

            SslConfig sslConfig = client.getOptions().getSslConfig();

            sslConfig.getCustomKeyManagers().addAll(certConfig.getKeyManagers());
            sslConfig.getCustomTrustManagers().addAll(certConfig.getTrustManagers());
            sslConfig.setHostNameVerifier(new LenientHostNameVerifier());
        }
    }

    /**
     * Get the certificate configuration using interactive components.
     *
     * @param parameters The parameters needed to configure an HttpClient.
     * @return The certification configuration.
     */
    private CertificateConfiguration getCertConfig(ConfigurerParameters parameters)
    {
        CertificateConfiguration certConfig = new CertificateConfiguration();

        certConfig.setUseCertificate(true);

        SecurityComponentsProvider provider = parameters.getProvider();
        String host = parameters.getHost();
        String serverKey = parameters.getServerKey();
        Supplier<? extends Component> parentComponent = parameters.getParentComponent();
        PreferencesRegistry prefsRegistry = parameters.getPrefsRegistry();
        SecurityManager securityManager = parameters.getSecurityManager();

        certConfig.setKeyManagers(Collections
                .singletonList(provider.getKeyManager(host, serverKey, parentComponent, prefsRegistry, securityManager)));
        certConfig.setTrustManagers(
                Collections.singletonList(provider.getTrustManager(host, serverKey, parentComponent, securityManager)));
        return certConfig;
    }
}
