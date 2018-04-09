package com.bitsys.common.http.ssl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * This class is an Apache {@link SSLSocketFactory} that provides more
 * configuration options beyond the default implementation. Specifically, this
 * class provides the ability to customize the generated {@link SSLSocket}
 * instances.
 */
public class EnhancedSSLSocketFactory extends SSLSocketFactory
{
    /**
     * Instances of this class have the ability to customize an
     * {@link SSLSocket} before it is used. Typical customizations include
     * setting the protocols (e.g. SSLv3 or TLSv1) and modifying the cipher
     * suites.
     */
    public interface SSLSocketCustomizer
    {
        /**
         * Customizes the {@link SSLSocket} prior to its use.
         *
         * @param socket the SSL socket to customize.
         * @throws IOException if an  error occurs while customizing the socket.
         */
        void customize(SSLSocket socket) throws IOException;
    }

    private final SSLSocketCustomizer customizer;

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given trust
     * store and socket customizer.
     *
     * @param truststore the trust store.
     * @param customizer the SSL socket customizer.
     * @throws NoSuchAlgorithmException if unable to process the trust store.
     * @throws KeyManagementException if unable to process the trust store.
     * @throws KeyStoreException if unable to process the trust store.
     * @throws UnrecoverableKeyException if unable to process the trust store.
     */
    public EnhancedSSLSocketFactory(final KeyStore truststore, final SSLSocketCustomizer customizer)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        super(truststore);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given trust
     * strategy and socket customizer.
     *
     * @param trustStrategy the trust strategy.
     * @param customizer the SSL socket customizer.
     * @throws NoSuchAlgorithmException if unable to process the trust store.
     * @throws KeyManagementException if unable to process the trust store.
     * @throws KeyStoreException if unable to process the trust store.
     * @throws UnrecoverableKeyException if unable to process the trust store.
     */
    public EnhancedSSLSocketFactory(final TrustStrategy trustStrategy, final SSLSocketCustomizer customizer)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        super(trustStrategy);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given SSL
     * context and socket customizer.
     *
     * @param sslContext
     * @param customizer the SSL socket customizer.
     */
    public EnhancedSSLSocketFactory(final SSLContext sslContext, final SSLSocketCustomizer customizer)
    {
        super(sslContext);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given key
     * store, password and socket customizer.
     *
     * @param keystore the key store.
     * @param keystorePassword the key store password.
     * @param customizer the SSL socket customizer.
     * @throws NoSuchAlgorithmException if unable to process the key store.
     * @throws KeyManagementException if unable to process the key store.
     * @throws KeyStoreException if unable to process the key store.
     * @throws UnrecoverableKeyException if unable to process the key store.
     */
    public EnhancedSSLSocketFactory(final KeyStore keystore, final String keystorePassword, final SSLSocketCustomizer customizer)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        super(keystore, keystorePassword);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given trust
     * strategy, hostname verifier and socket customizer.
     *
     * @param trustStrategy the trust strategy.
     * @param hostnameVerifier the hostname verifier.
     * @param customizer the SSL socket customizer.
     * @throws NoSuchAlgorithmException if unable to process the trust store.
     * @throws KeyManagementException if unable to process the trust store.
     * @throws KeyStoreException if unable to process the trust store.
     * @throws UnrecoverableKeyException if unable to process the trust store.
     */
    public EnhancedSSLSocketFactory(final TrustStrategy trustStrategy, final X509HostnameVerifier hostnameVerifier,
            final SSLSocketCustomizer customizer)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        super(trustStrategy, hostnameVerifier);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given SSL
     * context, hostname verifier and socket customizer.
     *
     * @param sslContext the SSL context.
     * @param hostnameVerifier the hostname verifier.
     * @param customizer the SSL socket customizer.
     */
    public EnhancedSSLSocketFactory(final SSLContext sslContext, final X509HostnameVerifier hostnameVerifier,
            final SSLSocketCustomizer customizer)
    {
        super(sslContext, hostnameVerifier);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given Java
     * socket factory, hostname verifier and socket customizer.
     *
     * @param socketfactory the Java socket factory.
     * @param hostnameVerifier the hostname verifier.
     * @param customizer the SSL socket customizer.
     */
    public EnhancedSSLSocketFactory(final javax.net.ssl.SSLSocketFactory socketfactory,
            final X509HostnameVerifier hostnameVerifier, final SSLSocketCustomizer customizer)
    {
        super(socketfactory, hostnameVerifier);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given key
     * store, password, trust store and socket customizer.
     *
     * @param keystore the key store.
     * @param keystorePassword the key store password.
     * @param truststore the trust store.
     * @param customizer the SSL socket customizer.
     * @throws NoSuchAlgorithmException if unable to process the key or trust
     *             store.
     * @throws KeyManagementException if unable to process the key or trust
     *             store.
     * @throws KeyStoreException if unable to process the key or trust store.
     * @throws UnrecoverableKeyException if unable to process the key or trust
     *             store.
     */
    public EnhancedSSLSocketFactory(final KeyStore keystore, final String keystorePassword, final KeyStore truststore,
            final SSLSocketCustomizer customizer)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        super(keystore, keystorePassword, truststore);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given
     * parameters.
     *
     * @param algorithm the SSL/TLS algorithm.
     * @param keystore the key store.
     * @param keystorePassword the key store password.
     * @param truststore the trust store.
     * @param random the random number generator.
     * @param hostnameVerifier the hostname verifier.
     * @param customizer the SSL socket customizer.
     * @throws NoSuchAlgorithmException if unable to process the trust store.
     * @throws KeyManagementException if unable to process the trust store.
     * @throws KeyStoreException if unable to process the trust store.
     * @throws UnrecoverableKeyException if unable to process the trust store.
     */
    public EnhancedSSLSocketFactory(final String algorithm, final KeyStore keystore, final String keystorePassword,
            final KeyStore truststore, final SecureRandom random, final X509HostnameVerifier hostnameVerifier,
            final SSLSocketCustomizer customizer)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        super(algorithm, keystore, keystorePassword, truststore, random, hostnameVerifier);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * Creates a new {@linkplain EnhancedSSLSocketFactory} using the given
     * parameters.
     *
     * @param algorithm the SSL/TLS algorithm.
     * @param keystore the key store.
     * @param keystorePassword the key store password.
     * @param truststore the trust store.
     * @param random the random number generator.
     * @param trustStrategy the trust strategy.
     * @param hostnameVerifier the hostname verifier.
     * @param customizer the SSL socket customizer.
     * @throws NoSuchAlgorithmException if unable to process the trust store.
     * @throws KeyManagementException if unable to process the trust store.
     * @throws KeyStoreException if unable to process the trust store.
     * @throws UnrecoverableKeyException if unable to process the trust store.
     */
    public EnhancedSSLSocketFactory(final String algorithm, final KeyStore keystore, final String keystorePassword,
            final KeyStore truststore, final SecureRandom random, final TrustStrategy trustStrategy,
            final X509HostnameVerifier hostnameVerifier, final SSLSocketCustomizer customizer)
        throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
        super(algorithm, keystore, keystorePassword, truststore, random, trustStrategy, hostnameVerifier);
        if (customizer == null)
        {
            throw new IllegalArgumentException("The SSL socket customizer is null");
        }
        this.customizer = customizer;
    }

    /**
     * @see org.apache.http.conn.ssl.SSLSocketFactory#prepareSocket(javax.net.ssl.SSLSocket)
     */
    @Override
    protected void prepareSocket(final SSLSocket socket) throws IOException
    {
        super.prepareSocket(socket);
        customizer.customize(socket);
    }
}
