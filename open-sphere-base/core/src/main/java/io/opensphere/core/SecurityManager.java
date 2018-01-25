package io.opensphere.core;

import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.crypto.Cipher;

import io.opensphere.core.util.security.CipherFactory;
import io.opensphere.core.util.security.Digest;
import io.opensphere.core.util.security.PrivateKeyProvider;
import io.opensphere.core.util.security.UsernamePasswordProvider;

/**
 * Manager for user security activities, like authentication, server trust, and
 * crypto.
 */
public interface SecurityManager
{
    /**
     * A "purpose" is an arbitrary string used to remember a private key or
     * username/password provider. Associating a provider with this general
     * purpose indicates the provider should be used whenever possible, without
     * prompting the user again.
     */
    String GENERAL_AUTHENTICATION_PURPOSE = "GENERAL_AUTHENTICATION";

    /**
     * Add a listener for changes to the cipher provided by the cipher factory.
     * Only a weak reference is held to the listener.
     *
     * @param listener The listener.
     */
    void addCipherChangeListener(CipherChangeListener listener);

    /**
     * Add a private key provider.
     *
     * @param provider The provider.
     * @return <tt>true</tt> if this set did not already contain the specified
     *         element
     */
    boolean addPrivateKeyProvider(PrivateKeyProvider provider);

    /**
     * Add a trusted server.
     *
     * @param server The trusted server.
     */
    void addTrustedServer(String server);

    /**
     * Add trusted server certificates.
     *
     * @param certs The certificates.
     */
    void addTrustedServerCerts(Collection<? extends X509Certificate> certs);

    /**
     * Add a username/password provider.
     *
     * @param provider The provider.
     */
    void addUsernamePasswordProvider(UsernamePasswordProvider provider);

    /**
     * Clear all stored encrypted data, along with the secret used to encrypt
     * it.
     */
    void clearEncryptedData();

    /**
     * Clear the preferred and preselected private key providers for all
     * purposes.
     */
    void clearPreselectedAndPreferredPrivateKeyProviders();

    /**
     * Clear a username/password provider.
     *
     * @param purpose The purpose the provider is associated with.
     */
    void clearUsernamePasswordProvider(String purpose);

    /**
     * Get a {@link CipherFactory} that may be used to create {@link Cipher}s
     * for crypto operations. Creating a cipher using the factory may result in
     * the user being prompted.
     *
     * @return The factory.
     */
    CipherFactory getCipherFactory();

    /**
     * Get the preferred private key provider. This is the last private key
     * provider selected by the user for this purpose.
     *
     * @param purpose An arbitrary string used to distinguish between different
     *            preferred private keys.
     * @return The private key provider, or {@code null}.
     */
    PrivateKeyProvider getPreferredPrivateKeyProvider(String purpose);

    /**
     * Get the pre-selected private key provider for a given server key, if
     * there is one. This is the last private key provider selected by the user
     * for this purpose <b>during this session</b>.
     *
     * @param purpose An arbitrary string used to distinguish between different
     *            preselected private keys.
     * @return The private key provider, or {@code null} if one has not been
     *         selected.
     */
    PrivateKeyProvider getPreselectedPrivateKeyProvider(String purpose);

    /**
     * Get the available private key providers.
     *
     * @return The providers.
     */
    Collection<? extends PrivateKeyProvider> getPrivateKeyProviders();

    /**
     * Get the private key provider that has a certain digest.
     *
     * @param digest The digest of the private key provider.
     * @return The private key provider, or {@code null} if not matching
     *         provider exists.
     */
    PrivateKeyProvider getPrivateKeyProviderWithDigest(Digest digest);

    /**
     * Get a {@link CipherFactory} that may be used to create {@link Cipher}s
     * for crypto operations. The ciphers created by this factory will use a
     * session-only secret key that is not persisted. The user will not be
     * prompted.
     *
     * @return The factory.
     */
    CipherFactory getSessionOnlyCipherFactory();

    /**
     * Get the server certificates trusted by the user.
     *
     * @return The certificates.
     */
    Collection<X509Certificate> getTrustedServerCerts();

    /**
     * Get the servers trusted by the user.
     *
     * @return The servers.
     */
    Collection<? extends String> getTrustedServers();

    /**
     * Get a username/password provider.
     *
     * @param purpose The purpose.
     * @return The provider, or {@code null} if one hasn't been added for the
     *         given purpose.
     */
    UsernamePasswordProvider getUsernamePasswordProvider(String purpose);

    /**
     * Get the server certificates trusted by the user.
     *
     * @return The certificates.
     */
    Collection<? extends X509Certificate> getUserTrustedServerCerts();

    /**
     * Remove a listener for changes to the cipher provided by the cipher
     * factory.
     *
     * @param listener The listener.
     */
    void removeCipherChangeListener(CipherChangeListener listener);

    /**
     * Reset the current secret key. If the current secret key is available,
     * re-encrypt all data with the new secret key. If the current secret key is
     * not available, clear the encrypted data.
     */
    void resetSecretKey();

    /**
     * Set the pre-selected private key provider for a certain purpose.
     *
     * @param purpose An arbitrary string used to allow for different aliases to
     *            be preferred for different uses.
     * @param selectedProvider The selected provider (which may be {@code null}
     *            to indicate no selection).
     */
    void setPreselectedPrivateKeyProvider(String purpose, PrivateKeyProvider selectedProvider);
}
