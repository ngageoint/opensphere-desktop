package com.bitsys.common.http.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.bitsys.common.http.util.UrlUtils;

/**
 * <code>KeyStoreUtils</code> provides utilities for loading Java key stores.
 */
public final class KeyStoreUtils
{
    /** The Java system property name for the key store path. */
    public static final String KEY_STORE = "javax.net.ssl.keyStore";

    /** The Java system property name for the key store password. */
    public static final String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";

    /** The Java system property name for the key store type. */
    public static final String KEY_STORE_TYPE = "javax.net.ssl.keyStoreType";

    /** The Java system property name for the trust store path. */
    public static final String TRUST_STORE = "javax.net.ssl.trustStore";

    /** The Java system property name for the trust store password. */
    public static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";

    /** The Java system property name for the trust store type. */
    public static final String TRUST_STORE_TYPE = "javax.net.ssl.trustStoreType";

    /** The Java KeyStore type. */
    public static final String JKS = "JKS";

    /** The PKCS#12 type. */
    public static final String PKCS12 = "PKCS12";

    /**
     * Prevents the constructions of a <code>KeyStoreUtils</code>.
     */
    private KeyStoreUtils()
    {
    }

    /**
     * Loads the system default key store. This method uses the values from
     * <code>javax.net.ssl.keyStore</code>,
     * <code>javax.net.ssl.keyStorePassword</code> and, optionally,
     * <code>javax.net.ssl.keyStoreType</code> to load the key store.
     *
     * @return the key store or <code>null</code> if
     *         <code>javax.net.ssl.keyStore</code> is not set.
     * @throws KeyStoreException if unable to create a key store using the value
     *             from <code>javax.net.ssl.keyStoreType</code>.
     * @throws NoSuchAlgorithmException if the algorithm used to check the
     *             integrity of the key store cannot be found.
     * @throws CertificateException if any of the certificates in the key store
     *             could not be loaded.
     * @throws IOException if any I/O error occurs while opening or reading the
     *             key store.
     */
    public static KeyStore getSystemKeyStore()
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
    {
        return loadKeyStore(getSystemKeyStorePath(), System.getProperty(KEY_STORE_PASSWORD, ""),
                System.getProperty(KEY_STORE_TYPE, KeyStore.getDefaultType()));
    }

    /**
     * Returns the system key store path.
     *
     * @return the system key store path or <code>null</code> if not set.
     */
    public static String getSystemKeyStorePath()
    {
        return System.getProperty(KEY_STORE);
    }

    /**
     * Returns the system key store password.
     *
     * @return the system key store password or <code>null</code> if not set.
     */
    public static char[] getSystemKeyStorePassword()
    {
        final String password = System.getProperty(KEY_STORE_PASSWORD);
        return password == null ? null : password.toCharArray();
    }

    /**
     * Loads the system default trust store. This method uses the values from
     * <code>javax.net.ssl.trustStore</code>,
     * <code>javax.net.ssl.trustStorePassword</code> and, optionally,
     * <code>javax.net.ssl.trustStoreType</code> to load the trust store.
     *
     * @return the trust store or <code>null</code> if
     *         <code>javax.net.ssl.trustStore</code> is not set.
     * @throws KeyStoreException if unable to create a key store using the value
     *             from <code>javax.net.ssl.trustStoreType</code>.
     * @throws NoSuchAlgorithmException if the algorithm used to check the
     *             integrity of the trust store cannot be found.
     * @throws CertificateException if any of the certificates in the trust
     *             store could not be loaded.
     * @throws IOException if any I/O error occurs while opening or reading the
     *             trust store.
     */
    public static KeyStore getSystemTrustStore()
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
    {
        return loadKeyStore(getSystemTrustStorePath(), System.getProperty(TRUST_STORE_PASSWORD, ""),
                System.getProperty(TRUST_STORE_TYPE, KeyStore.getDefaultType()));
    }

    /**
     * Returns the system key store path.
     *
     * @return the system key store path or <code>null</code> if not set.
     */
    public static String getSystemTrustStorePath()
    {
        return System.getProperty(TRUST_STORE);
    }

    /**
     * Returns the system trust store password.
     *
     * @return the system trust store password or <code>null</code> if it is not
     *         set.
     */
    public static char[] getSystemTrustStorePassword()
    {
        final String password = System.getProperty(TRUST_STORE_PASSWORD);
        return password == null ? null : password.toCharArray();
    }

    /**
     * Loads a Java KeyStore.
     *
     * @param keyStorePath the key store path.
     * @param password the key store password.
     * @param typeHint the hint for the key store type or <code>null</code>.
     * @return the key store.
     * @throws KeyStoreException if unable to create a key store using any type.
     * @throws NoSuchAlgorithmException if the algorithm used to check the
     *             integrity of the key store cannot be found.
     * @throws CertificateException if any of the certificates in the key store
     *             could not be loaded.
     * @throws IOException if any I/O error occurs while opening or reading the
     *             key store. If the password is incorrect, the cause will be an
     *             <code>UnrecoverableKeyException</code> or an
     *             <code>ArithmeticException</code>.
     */
    private static KeyStore loadKeyStore(final String keyStorePath, final String password, final String typeHint)
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
    {
        KeyStore keyStore = null;

        // If the trust store is defined.
        if (StringUtils.isNotBlank(keyStorePath))
        {
            keyStore = loadKeyStore(keyStorePath, password.toCharArray(), typeHint);
        }
        return keyStore;
    }

    /**
     * Loads a Java KeyStore.
     *
     * @param keyStorePath the key store path.
     * @param password the key store password.
     * @param typeHint the hint for the key store type or <code>null</code>.
     * @return the key store.
     * @throws KeyStoreException if unable to create a key store using any type.
     * @throws NoSuchAlgorithmException if the algorithm used to check the
     *             integrity of the key store cannot be found.
     * @throws CertificateException if any of the certificates in the key store
     *             could not be loaded.
     * @throws IOException if any I/O error occurs while opening or reading the
     *             key store. If the password is incorrect, the cause will be an
     *             <code>UnrecoverableKeyException</code> or an
     *             <code>ArithmeticException</code>.
     */
    public static KeyStore loadKeyStore(final String keyStorePath, final char[] password, final String typeHint)
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
    {
        return loadKeyStore(UrlUtils.toUrl(keyStorePath), password, typeHint);
    }

    /**
     * Loads the key store.
     *
     * @param keyStoreUrl the key store URL.
     * @param password the key store password.
     * @param typeHint the hint for the key store type or <code>null</code>.
     * @return the key store.
     * @throws KeyStoreException if unable to create a key store using any type.
     * @throws NoSuchAlgorithmException if the algorithm used to check the
     *             integrity of the key store cannot be found.
     * @throws CertificateException if any of the certificates in the key store
     *             could not be loaded.
     * @throws IOException if any I/O error occurs while opening or reading the
     *             key store. If the password is incorrect, the cause will be an
     *             <code>UnrecoverableKeyException</code> or an
     *             <code>ArithmeticException</code>.
     */
    public static KeyStore loadKeyStore(final URL keyStoreUrl, final char[] password, final String typeHint)
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
    {
        if (keyStoreUrl == null)
        {
            throw new IllegalArgumentException("The key store URL is null");
        }
        if (password == null)
        {
            throw new IllegalArgumentException("The key store password is null");
        }
        Exception firstException = null;
        KeyStore keyStore = null;

        // Attempt to process each of the supported key store types.
        for (final String type : getSupportedKeyStoreTypes(typeHint))
        {
            final InputStream inputStream = keyStoreUrl.openStream();
            try
            {
                final KeyStore tmpKeyStore = KeyStore.getInstance(type);
                tmpKeyStore.load(inputStream, password);
                keyStore = tmpKeyStore;
                break;
            }
            catch (final KeyStoreException e)
            {
                // The provider key store type was invalid. Try the next one.
                if (firstException == null)
                {
                    firstException = e;
                }
            }
            catch (final IOException e)
            {
                // If the IOException's cause is an UnrecoverableKeyException or
                // an
                // ArithmeticException, then the password is incorrect so just
                // throw
                // the exception.
                if (e.getCause() instanceof UnrecoverableKeyException || e.getCause() instanceof ArithmeticException)
                {
                    throw e;
                }
                if (firstException == null)
                {
                    firstException = e;
                }
            }
            finally
            {
                IOUtils.closeQuietly(inputStream);
            }
        }

        // If the key store was not loaded, throw the first inhibited
        // exception.
        if (keyStore == null)
        {
            if (firstException instanceof KeyStoreException)
            {
                throw (KeyStoreException)firstException;
            }
            else
            {
                throw (IOException)firstException;
            }
        }
        return keyStore;
    }

    /**
     * Returns the set of supported key store types using the given type hint.
     * If the hint is specified, it will be the first type in the set.
     *
     * @param typeHint an optional key store type.
     * @return the set of supported key store types.
     */
    public static Set<String> getSupportedKeyStoreTypes(final String typeHint)
    {
        final Set<String> types = new LinkedHashSet<>();
        if (typeHint != null)
        {
            types.add(typeHint.toUpperCase());
        }
        types.add(KeyStore.getDefaultType().toUpperCase());
        types.add(JKS);
        types.add(PKCS12);
        return types;
    }
}
