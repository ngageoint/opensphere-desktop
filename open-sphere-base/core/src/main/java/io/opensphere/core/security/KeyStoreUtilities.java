package io.opensphere.core.security;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ReflectionUtilities;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.security.CertificateUtilities;
import io.opensphere.core.util.security.KeyStorePrivateKeyProvider;
import io.opensphere.core.util.security.PrivateKeyProvider;

/**
 * Utilities for key stores.
 */
public final class KeyStoreUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(KeyStoreUtilities.class);

    /**
     * Get the certs from the trust stores on the classpath.
     *
     * @return The certs.
     */
    public static Collection<X509Certificate> getPackagedTrustStoreCerts()
    {
        Collection<X509Certificate> certs = New.collection();

        String trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");
        if (trustStoreLocation != null)
        {
            String pw = System.getProperty("javax.net.ssl.trustStorePassword");
            if (pw != null)
            {
                boolean found = false;
                try
                {
                    Enumeration<URL> urls = KeyStoreUtilities.class.getClassLoader().getResources(trustStoreLocation);
                    while (urls.hasMoreElements())
                    {
                        found |= loadTrustStore(urls.nextElement(), pw.toCharArray(), certs);
                    }
                }
                catch (IOException e)
                {
                    LOGGER.warn(
                            "Failed to load trust store specified in javax.net.ssl.trustStore [" + trustStoreLocation + "]: " + e,
                            e);
                }

                try
                {
                    File file = new File(trustStoreLocation);
                    if (file.exists())
                    {
                        if (file.canRead())
                        {
                            found |= loadTrustStore(file.toURI().toURL(), pw.toCharArray(), certs);
                        }
                        else
                        {
                            LOGGER.warn("Failed to load trust store specified in javax.net.ssl.trustStore [" + trustStoreLocation
                                    + "]: file cannot be read.");
                        }
                    }
                }
                catch (MalformedURLException e)
                {
                    LOGGER.warn(
                            "Failed to load trust store specified in javax.net.ssl.trustStore [" + trustStoreLocation + "]: " + e,
                            e);
                }

                if (!found)
                {
                    LOGGER.warn("No trust store found for location: " + trustStoreLocation);
                }
            }
        }
        return certs;
    }

    /**
     * Get providers for the available keys in the Windows-MY keystore.
     *
     * @return The providers.
     */
    public static Collection<? extends PrivateKeyProvider> getWindowsKeyProviders()
    {
        Set<PrivateKeyProvider> providers = New.set();
        try
        {
            final KeyStore keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null, null);

            for (String alias : fixDuplicateAliases(keyStore))
            {
                if (keyStore.isKeyEntry(alias)
                        && !providers.add(new KeyStorePrivateKeyProvider(alias, keyStore, (char[])null, "Windows")))
                {
                    LOGGER.warn("Duplicate alias found in Windows-MY keystore: " + alias);
                }
            }
        }
        catch (IOException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to load Windows-MY keystore: " + e, e);
            }
        }
        catch (GeneralSecurityException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to load Windows-MY keystore: " + e, e);
            }
        }

        return providers;
    }

    /**
     * Load the certs from the Windows-ROOT trust store if possible.
     *
     * @return The certs.
     */
    public static Collection<? extends X509Certificate> getWindowsTrustStoreCerts()
    {
        Collection<X509Certificate> certs = New.collection();
        try
        {
            KeyStore trustStore = KeyStore.getInstance("Windows-ROOT");
            trustStore.load(null, null);

            addCertsFromTrustStore(trustStore, certs);
        }
        catch (KeyStoreException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to load Windows-ROOT trust store: " + e, e);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to load Windows-ROOT trust store: " + e, e);
            }
        }
        catch (CertificateException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to load Windows-ROOT trust store: " + e, e);
            }
        }
        catch (IOException e)
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Failed to load Windows-ROOT trust store: " + e, e);
            }
        }
        return certs;
    }

    /**
     * Add certificates from a trust store to a collection.
     *
     * @param trustStore The trust store.
     * @param certs The output collection.
     * @throws KeyStoreException If the trust store is not initialized.
     */
    private static void addCertsFromTrustStore(KeyStore trustStore, Collection<? super X509Certificate> certs)
            throws KeyStoreException
    {
        for (String alias : fixDuplicateAliases(trustStore))
        {
            Certificate cert = trustStore.getCertificate(alias);
            if (cert instanceof X509Certificate)
            {
                certs.add((X509Certificate)cert);
            }
        }
    }

    /**
     * Check the given key store for duplicate aliases; rename the duplicates if
     * necessary.
     *
     * @param store The key store.
     * @return The aliases.
     * @throws KeyStoreException If there is an error accessing the key store.
     */
    @SuppressWarnings("unchecked")
    private static Collection<String> fixDuplicateAliases(KeyStore store) throws KeyStoreException
    {
        Collection<String> aliases = Collections.list(store.aliases());
        Set<String> set = New.set(aliases);
        if (aliases.size() != set.size())
        {
            try
            {
                KeyStoreSpi spi = ReflectionUtilities.getFieldValue(store, "keyStoreSpi", KeyStoreSpi.class);
                if (spi != null && "sun.security.mscapi.KeyStore$MY".equals(spi.getClass().getName()))
                {
                    Map<Object, Integer> aliasCountMap = New.map();

                    Iterable<Object> entries = ReflectionUtilities.getFieldValue(spi, "entries", Iterable.class);
                    for (Object keyEntry : entries)
                    {
                        String alias = ReflectionUtilities.getFieldValue(keyEntry, "alias", String.class);
                        Integer count = aliasCountMap.get(alias);
                        if (count == null)
                        {
                            aliasCountMap.put(alias, Integer.valueOf(1));
                        }
                        else
                        {
                            count = Integer.valueOf(count.intValue() + 1);
                            aliasCountMap.put(alias, count);
                            ReflectionUtilities.setFieldValue(keyEntry, "alias", StringUtilities.concat(alias, " (", count, ")"));
                        }
                    }
                }
            }
            catch (IllegalArgumentException | SecurityException | NoSuchFieldException e)
            {
                LOGGER.error("Failed to rename duplicate aliases: " + e, e);
            }
        }

        return Collections.list(store.aliases());
    }

    /**
     * Load a trust store and put the certificates in the given collection.
     *
     * @param url The URL for the trust store.
     * @param pw The password for the trust store.
     * @param certs The return collection.
     * @return {@code true} if the trust store was loaded successfully.
     */
    private static boolean loadTrustStore(URL url, char[] pw, Collection<? super X509Certificate> certs)
    {
        boolean found = false;
        for (String keyStoreType : CertificateUtilities.getAvailableKeyStoreAlgorithms())
        {
            try
            {
                KeyStore trustStore = KeyStore.getInstance(keyStoreType);
                InputStream is = url.openStream();
                try
                {
                    trustStore.load(is, pw);
                    addCertsFromTrustStore(trustStore, certs);
                    found = true;
                    break;
                }
                finally
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                        LOGGER.warn("Failed to close input stream: " + e, e);
                    }
                }
            }
            catch (IOException | GeneralSecurityException e)
            {
                LOGGER.warn("Failed to load trust store at URL [" + url + "]: " + e, e);
            }
        }
        return found;
    }

    /** Disallow instantiation. */
    private KeyStoreUtilities()
    {
    }
}
