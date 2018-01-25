package io.opensphere.core.authentication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreeTuple;
import io.opensphere.core.util.net.UrlUtilities;

/**
 * This is an SSLSocketFactory that contains a collection of
 * {@link SSLSocketFactory} that use the {@link UserInteractionX509KeyManager}
 * and {@link UserInteractionX509TrustManager}.
 */
public class UserInteractionSSLSocketFactory extends SSLSocketFactory
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(UserInteractionSSLSocketFactory.class);

    /**
     * The factory to use when the server has not been added.
     */
    private final SSLSocketFactory myDefaultFactory;

    /**
     * Map of host name to {@link SSLSocketFactory}. This gets populated when
     * addServer is called.
     */
    private final Map<String, SSLSocketFactory> myFactories = Collections.synchronizedMap(New.map());

    /**
     * Used by the key and trust managers.
     */
    private final Supplier<? extends JFrame> myMainFrameProvider;

    /**
     * Used by the key and trust managers.
     */
    private final PreferencesRegistry myPreferencesRegistry;

    /**
     * Used by the key and trust managers.
     */
    private final SecurityManager mySecurityManager;

    /**
     * Constructs a new {@link UserInteractionSSLSocketFactory}.
     *
     * @param defaultFactory The {@link SSLSocketFactory} to use when the server
     *            has not been added yet.
     * @param preferencesRegistry Used by the key and trust managers.
     * @param securityManager Used by the key and trust managers.
     * @param mainFrameProvider Used by the key and trust managers.
     */
    public UserInteractionSSLSocketFactory(SSLSocketFactory defaultFactory, PreferencesRegistry preferencesRegistry,
            SecurityManager securityManager, Supplier<? extends JFrame> mainFrameProvider)
    {
        myDefaultFactory = defaultFactory;
        myPreferencesRegistry = preferencesRegistry;
        mySecurityManager = securityManager;
        myMainFrameProvider = mainFrameProvider;
    }

    /**
     * Adds the {@link UserInteractionX509KeyManager} and
     * {@link UserInteractionX509TrustManager} to this socket factory for the
     * given server.
     *
     * @param serverName The user friendly server name.
     * @param serverKey The unique id for the server.
     * @param url The url to the server.
     * @throws NoSuchAlgorithmException Thrown if SSL is not available.
     * @throws KeyManagementException If we failed to install the key and/or
     *             truststore manager.
     */
    public void addServer(String serverName, String serverKey, String url) throws NoSuchAlgorithmException, KeyManagementException
    {
        KeyManager keyManager = new UserInteractionX509KeyManager(serverName, serverKey, myMainFrameProvider,
                myPreferencesRegistry, mySecurityManager, null);
        TrustManager trustManager = new UserInteractionX509TrustManager(serverName, serverKey, myMainFrameProvider,
                mySecurityManager, null);
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(new KeyManager[] { keyManager }, new TrustManager[] { trustManager }, new SecureRandom());

        ThreeTuple<String, String, Integer> protoHostPort = UrlUtilities.getProtocolHostPort(url, 80);

        myFactories.put(protoHostPort.getSecondObject(), sc.getSocketFactory());
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException
    {
        SSLSocketFactory factory = getFactory(arg0.getHostName());

        return factory.createSocket(arg0, arg1);
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException
    {
        SSLSocketFactory factory = getFactory(arg0.getHostName());

        return factory.createSocket(arg0, arg1, arg2, arg3);
    }

    @Override
    public Socket createSocket(Socket arg0, String hostName, int arg2, boolean arg3) throws IOException
    {
        SSLSocketFactory factory = getFactory(hostName);

        return factory.createSocket(arg0, hostName, arg2, arg3);
    }

    @Override
    public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException
    {
        SSLSocketFactory factory = getFactory(arg0);

        return factory.createSocket(arg0, arg1);
    }

    @Override
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException
    {
        SSLSocketFactory factory = getFactory(arg0);

        return factory.createSocket(arg0, arg1, arg2, arg3);
    }

    @Override
    public String[] getDefaultCipherSuites()
    {
        List<SSLSocketFactory> factories = getFactories();

        Set<String> ciphers = New.set();

        for (SSLSocketFactory factory : factories)
        {
            for (String cipher : factory.getDefaultCipherSuites())
            {
                ciphers.add(cipher);
            }
        }

        String[] cipherArray = new String[ciphers.size()];
        return ciphers.toArray(cipherArray);
    }

    /**
     * Gets the {@link SSLSocketFactory} to use for the specified server.
     *
     * @param hostName The server host name.
     * @return The factory to use for the server.
     */
    public SSLSocketFactory getFactory(String hostName)
    {
        SSLSocketFactory factory = myFactories.get(hostName);

        if (factory == null)
        {
            try
            {
                addServer(hostName, hostName, "https://" + hostName);
                factory = myFactories.get(hostName);
            }
            catch (KeyManagementException | NoSuchAlgorithmException e)
            {
                LOGGER.error("Unable to create ssl factory for " + hostName + " using default instead.", e);
                factory = myDefaultFactory;
            }
        }

        return factory;
    }

    @Override
    public String[] getSupportedCipherSuites()
    {
        List<SSLSocketFactory> factories = getFactories();

        Set<String> ciphers = New.set();

        for (SSLSocketFactory factory : factories)
        {
            for (String cipher : factory.getSupportedCipherSuites())
            {
                ciphers.add(cipher);
            }
        }

        String[] cipherArray = new String[ciphers.size()];
        return ciphers.toArray(cipherArray);
    }

    /**
     * Removes the {@link UserInteractionX509KeyManager} and
     * {@link UserInteractionX509TrustManager} from this factory for the given
     * server.
     *
     * @param url The url to the server.
     */
    public void removeServer(String url)
    {
        ThreeTuple<String, String, Integer> protoHostPort = UrlUtilities.getProtocolHostPort(url, 80);

        myFactories.remove(protoHostPort.getSecondObject());
    }

    /**
     * Gets a list of all {@link SSLSocketFactory} this class currently has.
     *
     * @return The list of all {@link SSLSocketFactory}.
     */
    private List<SSLSocketFactory> getFactories()
    {
        List<SSLSocketFactory> factories = New.list(myFactories.values());
        factories.add(myDefaultFactory);

        return factories;
    }
}
