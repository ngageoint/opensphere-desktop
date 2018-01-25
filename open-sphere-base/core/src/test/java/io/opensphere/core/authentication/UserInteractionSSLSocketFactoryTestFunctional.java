package io.opensphere.core.authentication;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.swing.JFrame;

import org.easymock.EasyMockSupport;
import org.junit.Test;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferencesRegistry;

/**
 * Tests the {@link UserInteractionSSLSocketFactory}.
 */
public class UserInteractionSSLSocketFactoryTestFunctional
{
    /**
     * The test server.
     */
    private static final String ourServer = "gfb-int-cert1";

    /**
     * Tests creating a socket for default factory, then for an added server,
     * and then for a removed server.
     *
     * @throws IOException Bad IO.
     * @throws UnknownHostException Bad host.
     * @throws NoSuchAlgorithmException Bad algorithm.
     * @throws KeyManagementException Bad keys.
     * @throws SecurityException Bad security.
     * @throws NoSuchFieldException No field.
     * @throws IllegalAccessException illegal.
     * @throws IllegalArgumentException illegal.
     */
    @Test
    public void testAddServer() throws UnknownHostException, IOException, KeyManagementException, NoSuchAlgorithmException,
        NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = createRegistry(support);
        SecurityManager manager = createManager(support);
        Supplier<JFrame> supplier = createSupplier(support);

        support.replayAll();

        UserInteractionSSLSocketFactory factory = new UserInteractionSSLSocketFactory(
                HttpsURLConnection.getDefaultSSLSocketFactory(), registry, manager, supplier);

        factory.addServer("testServer", "testServer", "https://" + ourServer);

        Socket socketAfter = factory.createSocket(ourServer, 80);
        Field sslContextField = socketAfter.getClass().getDeclaredField("sslContext");
        sslContextField.setAccessible(true);
        Object afterContext = sslContextField.get(socketAfter);
        Field keyField = afterContext.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("keyManager");
        keyField.setAccessible(true);
        X509ExtendedKeyManager keyWrapper = (X509ExtendedKeyManager)keyField.get(afterContext);
        Field kmField = keyWrapper.getClass().getDeclaredField("km");
        kmField.setAccessible(true);
        assertTrue(kmField.get(keyWrapper) instanceof UserInteractionX509KeyManager);

        Field trustField = afterContext.getClass().getSuperclass().getSuperclass().getSuperclass()
                .getDeclaredField("trustManager");
        trustField.setAccessible(true);
        Object trustWrapper = trustField.get(afterContext);
        Field tmField = trustWrapper.getClass().getDeclaredField("tm");
        tmField.setAccessible(true);
        assertTrue(tmField.get(trustWrapper) instanceof UserInteractionX509TrustManager);

        support.verifyAll();
    }

    /**
     * Tests creating a socket for default factory, then for an added server,
     * and then for a removed server.
     *
     * @throws IOException Bad IO.
     * @throws UnknownHostException Bad host.
     * @throws NoSuchAlgorithmException Bad algorithm.
     * @throws KeyManagementException Bad keys.
     * @throws SecurityException Bad security.
     * @throws NoSuchFieldException No field.
     * @throws IllegalAccessException illegal.
     * @throws IllegalArgumentException illegal.
     */
    @Test
    public void testCreateSocket() throws UnknownHostException, IOException, KeyManagementException, NoSuchAlgorithmException,
        NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
    {
        EasyMockSupport support = new EasyMockSupport();

        PreferencesRegistry registry = createRegistry(support);
        SecurityManager manager = createManager(support);
        Supplier<JFrame> supplier = createSupplier(support);

        support.replayAll();

        UserInteractionSSLSocketFactory factory = new UserInteractionSSLSocketFactory(
                HttpsURLConnection.getDefaultSSLSocketFactory(), registry, manager, supplier);

        Socket socket = factory.createSocket(ourServer, 80);
        Field sslContextField = socket.getClass().getDeclaredField("sslContext");
        sslContextField.setAccessible(true);

        Socket clientSocket = new Socket(ourServer, 443);
        Socket socketAfter = factory.createSocket(clientSocket, ourServer, 443, true);
        Object afterContext = sslContextField.get(socketAfter);
        Field keyField = afterContext.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("keyManager");
        keyField.setAccessible(true);
        X509ExtendedKeyManager keyWrapper = (X509ExtendedKeyManager)keyField.get(afterContext);
        Field kmField = keyWrapper.getClass().getDeclaredField("km");
        kmField.setAccessible(true);
        assertTrue(kmField.get(keyWrapper) instanceof UserInteractionX509KeyManager);

        Field trustField = afterContext.getClass().getSuperclass().getSuperclass().getSuperclass()
                .getDeclaredField("trustManager");
        trustField.setAccessible(true);
        Object trustWrapper = trustField.get(afterContext);
        Field tmField = trustWrapper.getClass().getDeclaredField("tm");
        tmField.setAccessible(true);
        assertTrue(tmField.get(trustWrapper) instanceof UserInteractionX509TrustManager);

        support.verifyAll();
    }

    /**
     * Creates an easy mocked {@link SecurityManager}.
     *
     * @param support Used to create the mock.
     * @return The security manager.
     */
    private SecurityManager createManager(EasyMockSupport support)
    {
        SecurityManager manager = support.createNiceMock(SecurityManager.class);

        return manager;
    }

    /**
     * Creates the easy mocked registry.
     *
     * @param support Used to create the mock.
     * @return The {@link PreferencesRegistry}.
     */
    private PreferencesRegistry createRegistry(EasyMockSupport support)
    {
        PreferencesRegistry registry = support.createNiceMock(PreferencesRegistry.class);

        return registry;
    }

    /**
     * Creates an easy mocked main frame provider.
     *
     * @param support Used to create the mock.
     * @return The main frame provider.
     */
    private Supplier<JFrame> createSupplier(EasyMockSupport support)
    {
        @SuppressWarnings("unchecked")
        Supplier<JFrame> supplier = support.createNiceMock(Supplier.class);

        return supplier;
    }
}
