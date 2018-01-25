package com.bitsys.common.http.client;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.Builder;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.PKIXBuilderParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.commons.lang3.ArrayUtils;

import com.bitsys.common.http.ssl.CertificateVerifier;
import com.bitsys.common.http.ssl.ClientCertificateSelector;
import com.bitsys.common.http.ssl.DefaultClientCertificateSelector;
import com.bitsys.common.http.ssl.HostNameVerifier;
import com.bitsys.common.http.ssl.KeyStoreUtils;
import com.bitsys.common.http.ssl.StrictCertificateVerifier;
import com.bitsys.common.http.ssl.StrictHostNameVerifier;
import com.bitsys.common.http.util.UrlUtils;

/**
 * This class defines SSL configuration options.
 */
public class SslConfig
{
   /** The default SSL Context. */
   private static final SSLContext SSL_CONTEXT;

   /** The array of enabled cipher suites. */
   private String[] enabledCipherSuites;

   /** The array of enabled protocols. */
   private String[] enabledProtocols;

   /** The host name verifier instance. */
   private HostNameVerifier hostNameVerifier;

   /** The PKIX builder parameters for verifying server certificates. */
   private PKIXBuilderParameters pkixBuilderParameters;

   /** The certificate verifier instance. */
   private CertificateVerifier certificateVerifier;

   /** The list of client certificates. */
   private final List<Builder> clientCertificates = new ArrayList<>();

   /** The client certificate selector. */
   private ClientCertificateSelector certificateSelector;

   /** The list of custom key managers. */
   private final List<KeyManager> customKeyManagers = new ArrayList<>();

   /** The list of custom trust managers. */
   private final List<TrustManager> customTrustManagers = new ArrayList<>();

   static
   {
      SSLContext sslContext;
      try
      {
         sslContext = SSLContext.getDefault();
      }
      catch (final NoSuchAlgorithmException e)
      {
         sslContext = null;
      }
      SSL_CONTEXT = sslContext;
   }

   /**
    * Returns the names of the cipher suites that could be enabled for use on an
    * SSL connection.
    *
    * @return the array of supported cipher suites.
    */
   public static String[] getSupportedCipherSuites()
   {
      return SSL_CONTEXT.getDefaultSSLParameters().getCipherSuites();
   }

   /**
    * Returns the names of the protocols that could be enabled for use on an SSL
    * connection.
    *
    * @return the array of supported protocols.
    */
   public static String[] getSupportedProtocols()
   {
      return SSL_CONTEXT.getDefaultSSLParameters().getProtocols();
   }

   /**
    * Sets the cipher suites to enable for SSL connections.
    * <p>
    * Each cipher suite in the <code>suites</code> parameter must be listed by
    * {@link #getSupportedCipherSuites()}. The suites should be in order of
    * preference.
    *
    * @param suites
    *           the names of all of the cipher suites to enable or
    *           <code>null</code> to use the {@link #getSupportedCipherSuites()
    *           supported} set.
    */
   public void setEnabledCipherSuites(final String[] suites)
   {
      enabledCipherSuites = ArrayUtils.clone(suites);
   }

   /**
    * Returns the names of the cipher suites that can be used on SSL
    * connections.
    *
    * @return the array of enabled cipher suites.
    */
   public String[] getEnabledCipherSuites()
   {
      if (enabledCipherSuites == null)
      {
         enabledCipherSuites = getSupportedCipherSuites();
      }
      return enabledCipherSuites;
   }

   /**
    * Sets the cipher suites to enable for SSL connections.
    * <p>
    * Each protocol in the <code>protocols</code> parameter must be listed by
    * {@link #getSupportedProtocols()}.
    *
    * @param protocols
    *           the names of all of the protocols to enable or <code>null</code>
    *           to use the {@link #getSupportedProtocols() supported} set.
    */
   public void setEnabledProtocols(final String[] protocols)
   {
      enabledProtocols = ArrayUtils.clone(protocols);
   }

   /**
    * Returns the names of the protocols that can be used on SSL connections.
    *
    * @return the array of enabled protocols.
    */
   public String[] getEnabledProtocols()
   {
      if (enabledProtocols == null)
      {
         enabledProtocols = getSupportedProtocols();
      }
      return enabledProtocols;
   }

   /**
    * Returns the host name verifier.
    *
    * @return the host name verifier.
    */
   public HostNameVerifier getHostNameVerifier()
   {
      if (hostNameVerifier == null)
      {
         hostNameVerifier = new StrictHostNameVerifier();
      }
      return hostNameVerifier;
   }

   /**
    * Sets the host name verifier. Defaults to a strict verifier if not set or
    * the argument is <code>null</code>.
    *
    * @param hostNameVerifier
    *           the host name verifier.
    */
   public void setHostNameVerifier(final HostNameVerifier hostNameVerifier)
   {
      this.hostNameVerifier = hostNameVerifier;
   }

   /**
    * Loads the Java system default trust store (i.e. defined by
    * <code>javax.net.ssl.trustStore</code>).
    *
    * @throws IOException
    *            if any I/O error occurs while opening or reading the trust
    *            store.
    * @throws GeneralSecurityException
    *            if any other trust store error occurs.
    */
   public void setSystemTrustStore() throws IOException, GeneralSecurityException
   {
      final KeyStore trustStore = KeyStoreUtils.getSystemTrustStore();
      if (trustStore != null)
      {
         setTrustStore(trustStore);
      }
   }

   /**
    * Sets the trust store.
    *
    * @param trustStorePath
    *           the trust store path.
    * @param password
    *           the password to access the trust store.
    * @throws IOException
    *            if any I/O error occurs while opening or reading the trust
    *            store. If the password is incorrect, the cause will be an
    *            <code>UnrecoverableKeyException</code> or an
    *            <code>ArithmeticException</code>.
    * @throws GeneralSecurityException
    *            if any other trust store error occurs.
    */
   public void setTrustStore(final String trustStorePath, final char[] password)
      throws IOException, GeneralSecurityException
   {
      setTrustStore(UrlUtils.toUrl(trustStorePath), password);
   }

   /**
    * Sets the trust store.
    *
    * @param trustStoreUrl
    *           the URL to the key store.
    * @param password
    *           the password to access the trust store.
    * @throws IOException
    *            if any I/O error occurs while opening or reading the trust
    *            store. If the password is incorrect, the cause will be an
    *            <code>UnrecoverableKeyException</code> or an
    *            <code>ArithmeticException</code>.
    * @throws GeneralSecurityException
    *            if any other trust store error occurs.
    */
   public void setTrustStore(final URL trustStoreUrl, final char[] password)
      throws IOException, GeneralSecurityException
   {
      setTrustStore(KeyStoreUtils.loadKeyStore(trustStoreUrl, password, null));
   }

   /**
    * Sets the trust store to use for connections.
    *
    * @param trustStore
    *           the trust store or <code>null</code> to not use a trust store.
    * @throws GeneralSecurityException
    *            if the trust store is empty or does not contain at least one
    *            trusted entry.
    */
   public void setTrustStore(final KeyStore trustStore)
      throws GeneralSecurityException
   {
      PKIXBuilderParameters parameters = null;
      if (trustStore != null)
      {
         parameters = new PKIXBuilderParameters(trustStore, null);
         parameters.setRevocationEnabled(false);
      }
      setPkixBuilderParameters(parameters);
   }

   /**
    * Sets the {@link PKIXBuilderParameters} used for verifying server
    * certificates.
    *
    * @param pkixBuilderParameters
    *           the {@link PKIXBuilderParameters}.
    */
   public void setPkixBuilderParameters(final PKIXBuilderParameters pkixBuilderParameters)
   {
      this.pkixBuilderParameters = pkixBuilderParameters;
   }

   /**
    * Returns the {@link PKIXBuilderParameters} used for verifying server
    * certificates.
    *
    * @return the {@link PKIXBuilderParameters}.
    */
   public PKIXBuilderParameters getPkixBuilderParameters()
   {
      return pkixBuilderParameters;
   }

   /**
    * Returns the certificate verifier.
    *
    * @return the certificate verifier.
    */
   public CertificateVerifier getCertificateVerifier()
   {
      if (certificateVerifier == null)
      {
         certificateVerifier = new StrictCertificateVerifier();
      }
      return certificateVerifier;
   }

   /**
    * Sets the certificate verifier. Defaults to a strict verifier if not set or
    * the argument is <code>null</code>.
    *
    * @param certificateVerifier
    *           the certificate verifier.
    */
   public void setCertificateVerifier(final CertificateVerifier certificateVerifier)
   {
      this.certificateVerifier = certificateVerifier;
   }

   /**
    * Adds the Java system default key store (i.e. defined by
    * <code>javax.net.ssl.keyStore</code>).
    *
    * @throws IOException
    *            if any I/O error occurs while opening or reading the key store.
    * @throws GeneralSecurityException
    *            if any other key store error occurs.
    */
   public void addSystemKeyStore() throws IOException, GeneralSecurityException
   {
      final KeyStore keyStore = KeyStoreUtils.getSystemKeyStore();
      if (keyStore != null)
      {
         addClientCertificate(keyStore,
                              new PasswordProtection(KeyStoreUtils
                                 .getSystemKeyStorePassword()));
      }
   }

   /**
    * Adds a new client certificate.
    *
    * @param keyStorePath
    *           the key store path.
    * @param password
    *           the password to access the key store and private key. The
    *           password will be cloned prior to returning.
    * @throws KeyStoreException
    *            if unable to create a key store using any type.
    * @throws NoSuchAlgorithmException
    *            if the algorithm used to check the integrity of the key store
    *            cannot be found.
    * @throws CertificateException
    *            if any of the certificates in the key store could not be
    *            loaded.
    * @throws IOException
    *            if any I/O error occurs while opening or reading the key store.
    *            If the password is incorrect, the cause will be an
    *            <code>UnrecoverableKeyException</code> or an
    *            <code>ArithmeticException</code>.
    */
   public void addClientCertificate(final String keyStorePath, final char[] password)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
      IOException
   {
      addClientCertificate(UrlUtils.toUrl(keyStorePath), password);
   }

   /**
    * Adds a new client certificate.
    *
    * @param keyStoreUrl
    *           the URL to the key store.
    * @param password
    *           the password to access the key store and private key. The
    *           password will be cloned prior to returning.
    * @throws KeyStoreException
    *            if unable to create a key store using any type.
    * @throws NoSuchAlgorithmException
    *            if the algorithm used to check the integrity of the key store
    *            cannot be found.
    * @throws CertificateException
    *            if any of the certificates in the key store could not be
    *            loaded.
    * @throws IOException
    *            if any I/O error occurs while opening or reading the key store.
    *            If the password is incorrect, the cause will be an
    *            <code>UnrecoverableKeyException</code> or an
    *            <code>ArithmeticException</code>.
    */
   public void addClientCertificate(final URL keyStoreUrl, final char[] password)
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
      IOException
   {
      addClientCertificate(KeyStoreUtils.loadKeyStore(keyStoreUrl, password, null),
                           new PasswordProtection(password));
   }

   /**
    * Adds a new client certificate.
    *
    * @param keyStore
    *           the initialized key store containing the client certificate.
    * @param protectionParameter
    *           the key store protection parameter that provides the password to
    *           the certificate's private key.
    */
   public void addClientCertificate(final KeyStore keyStore,
                                    final ProtectionParameter protectionParameter)
   {
      getClientCertificates()
         .add(Builder.newInstance(keyStore, protectionParameter));
   }

   /**
    * Returns the list of client certificates.
    *
    * @return the list of client certificates.
    */
   public List<KeyStore.Builder> getClientCertificates()
   {
      return clientCertificates;
   }

   /**
    * Gets the list of custom KeyManagers.
    *
    * @return The list of custom KeyManagers.
    */
   public List<KeyManager> getCustomKeyManagers()
   {
       return customKeyManagers;
   }

   /**
    * Gets the list of custom TrustManagers.
    *
    * @return The list of custom KeyManagers.
    */
   public List<TrustManager> getCustomTrustManagers()
   {
       return customTrustManagers;
   }

   /**
    * Sets the client certificate selector.
    *
    * @param certificateSelector
    *           the client certificate selector.
    */
   public void setCertificateSelector(final ClientCertificateSelector certificateSelector)
   {
      this.certificateSelector = certificateSelector;
   }

   /**
    * Returns the client certificate selector.
    *
    * @return the client certificate selector.
    */
   public ClientCertificateSelector getCertificateSelector()
   {
      if (certificateSelector == null)
      {
         certificateSelector = new DefaultClientCertificateSelector();
      }
      return certificateSelector;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("SslConfig [Enabled Cipher Suites=");
      builder.append(Arrays.toString(getEnabledCipherSuites()));
      builder.append(", Enabled Protocols=");
      builder.append(Arrays.toString(getEnabledProtocols()));
      builder.append(", Host Name Verifier=");
      builder.append(getHostNameVerifier());
      builder.append(", Certificate Verifier=");
      builder.append(getCertificateVerifier());
      builder.append(", Certificate Selector=");
      builder.append(getCertificateSelector());
      builder.append("]");
      return builder.toString();
   }
}
