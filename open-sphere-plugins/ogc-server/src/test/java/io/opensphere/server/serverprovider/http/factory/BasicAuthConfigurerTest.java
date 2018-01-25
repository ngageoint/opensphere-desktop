package io.opensphere.server.serverprovider.http.factory;

import static org.junit.Assert.assertEquals;

import java.awt.Component;
import java.util.function.Supplier;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Test;

import com.bitsys.common.http.auth.AuthenticationScope;
import com.bitsys.common.http.auth.Credentials;
import com.bitsys.common.http.auth.CredentialsProvider;
import com.bitsys.common.http.client.HttpClient;
import com.bitsys.common.http.client.HttpClientOptions;

import io.opensphere.core.SecurityManager;
import io.opensphere.server.serverprovider.SecurityComponentsProvider;

/**
 * Tests the BasicAuthConfigurer class.
 *
 */
public class BasicAuthConfigurerTest
{
    /**
     * The test server name.
     */
    private static final String ourServerName = "serverName";

    /**
     * The test server key.
     */
    private static final String ourServerKey = "serverKey";

    /**
     * The test port.
     */
    private static final int ourPort = 10;

    /**
     * Tests the configure.
     */
    @Test
    public void testConfigure()
    {
        EasyMockSupport support = new EasyMockSupport();

        @SuppressWarnings("unchecked")
        Supplier<? extends Component> parentProvider = support.createMock(Supplier.class);
        SecurityManager securityManager = support.createMock(SecurityManager.class);

        Credentials creds = support.createMock(Credentials.class);
        SecurityComponentsProvider provider = createProvider(support, parentProvider, securityManager, creds);

        HttpClientOptions options = new HttpClientOptions();
        HttpClient client = createClient(support, options);

        support.replayAll();

        BasicAuthConfigurer configurer = new BasicAuthConfigurer();

        ConfigurerParameters parameters = new ConfigurerParameters();
        parameters.setProvider(provider);
        parameters.setClient(client);
        parameters.setHost(ourServerName);
        parameters.setPort(ourPort);
        parameters.setServerKey(ourServerKey);
        parameters.setParentComponent(parentProvider);
        parameters.setSecurityManager(securityManager);

        configurer.configure(parameters);

        CredentialsProvider credsProvider = options.getCredentialsProvider();
        Credentials credentials = credsProvider.getCredentials(new AuthenticationScope(ourServerName, ourPort));

        assertEquals(creds, credentials);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked HttpClient.
     *
     * @param support Used to create the mock.
     * @param options The options to return.
     * @return The http client.
     */
    private HttpClient createClient(EasyMockSupport support, HttpClientOptions options)
    {
        HttpClient client = support.createMock(HttpClient.class);
        client.getOptions();
        EasyMock.expectLastCall().andReturn(options);

        return client;
    }

    /**
     * Creates an easy mocked SecurityComponentsProvider.
     *
     * @param support The support used to create the mock.
     * @param parentProvider The easy mocked parent provider.
     * @param securityManager The security manager.
     * @param creds The credentials.
     * @return The SecurityComponentsProvider.
     */
    private SecurityComponentsProvider createProvider(EasyMockSupport support, Supplier<? extends Component> parentProvider,
            SecurityManager securityManager, Credentials creds)
    {
        SecurityComponentsProvider provider = support.createMock(SecurityComponentsProvider.class);
        provider.getUserCredentials(EasyMock.cmpEq(ourServerName), EasyMock.cmpEq(ourServerKey), EasyMock.eq(parentProvider),
                EasyMock.eq(securityManager));
        EasyMock.expectLastCall().andReturn(creds);

        return provider;
    }
}
