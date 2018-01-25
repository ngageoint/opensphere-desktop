package io.opensphere.server.serverprovider.http.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.function.Supplier;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.client.HttpClientOptions;
import com.bitsys.common.http.ssl.LenientHostNameVerifier;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.server.serverprovider.SecurityComponentsProvider;

/**
 * Tests the CertificateConfigurer class.
 *
 */
public class CertificateConfigurerTest
{
    /**
     * The expected host.
     */
    private static final String ourHost = "host";

    /**
     * The expected server key.
     */
    private static final String ourServerKey = "serverKey";

    /**
     * Tests configuring the certs.
     *
     * @throws IOException Bad IO.
     * @throws GeneralSecurityException Bad security.
     */
    @Test
    public void testConfigure() throws GeneralSecurityException, IOException
    {
        EasyMockSupport support = new EasyMockSupport();

        HttpClientOptions options = new HttpClientOptions();
        HttpClient client = createClient(support, options);

        KeyManager keyManager = support.createMock(KeyManager.class);
        TrustManager trustManager = support.createMock(TrustManager.class);
        @SuppressWarnings("unchecked")
        Supplier<? extends Component> parentProvider = support.createMock(Supplier.class);
        PreferencesRegistry preferencesRegistry = support.createMock(PreferencesRegistry.class);
        SecurityManager securityManager = support.createMock(SecurityManager.class);

        SecurityComponentsProvider provider = createProvider(support, parentProvider, preferencesRegistry, securityManager,
                keyManager, trustManager);

        support.replayAll();

        ConfigurerParameters parameters = new ConfigurerParameters();
        parameters.setClient(client);
        parameters.setProvider(provider);
        parameters.setHost(ourHost);
        parameters.setServerKey(ourServerKey);
        parameters.setParentComponent(parentProvider);
        parameters.setPrefsRegistry(preferencesRegistry);
        parameters.setSecurityManager(securityManager);

        CertificateConfigurer configurer = new CertificateConfigurer();
        configurer.configure(parameters);

        assertEquals(1, options.getSslConfig().getCustomKeyManagers().size());
        assertEquals(keyManager, options.getSslConfig().getCustomKeyManagers().get(0));

        assertEquals(1, options.getSslConfig().getCustomTrustManagers().size());
        assertEquals(trustManager, options.getSslConfig().getCustomTrustManagers().get(0));
        assertTrue(options.getSslConfig().getHostNameVerifier() instanceof LenientHostNameVerifier);

        support.verifyAll();
    }

    /**
     * Creates the easy mocked HttpClient.
     *
     * @param support Used to create mock.
     * @param options The options to return.
     * @return The HttpClient.
     */
    private HttpClient createClient(EasyMockSupport support, HttpClientOptions options)
    {
        HttpClient httpClient = support.createMock(HttpClient.class);

        httpClient.getOptions();
        EasyMock.expectLastCall().andReturn(options);

        return httpClient;
    }

    /**
     * Creates the easy mocked SecurityComponentsProvider.
     *
     * @param support Used to create the mock.
     * @param parentProvider The expected parent provider.
     * @param preferencesRegistry The expected preferencesRegistry.
     * @param securityManager The expected security manager.
     * @param keyManagers The key manager to return.
     * @param trustManagers The trust manager to return.
     * @return The SecurityComponentsProvider.
     */
    private SecurityComponentsProvider createProvider(EasyMockSupport support, Supplier<? extends Component> parentProvider,
            PreferencesRegistry preferencesRegistry, SecurityManager securityManager, KeyManager keyManagers,
            TrustManager trustManagers)
    {
        SecurityComponentsProvider provider = support.createMock(SecurityComponentsProvider.class);

        provider.getKeyManager(EasyMock.cmpEq(ourHost), EasyMock.cmpEq(ourServerKey), EasyMock.eq(parentProvider),
                EasyMock.eq(preferencesRegistry), EasyMock.eq(securityManager));
        EasyMock.expectLastCall().andReturn(keyManagers);

        provider.getTrustManager(EasyMock.cmpEq(ourHost), EasyMock.cmpEq(ourServerKey), EasyMock.eq(parentProvider),
                EasyMock.eq(securityManager));
        EasyMock.expectLastCall().andReturn(trustManagers);

        return provider;
    }
}
