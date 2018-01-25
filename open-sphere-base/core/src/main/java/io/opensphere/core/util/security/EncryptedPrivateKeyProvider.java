package io.opensphere.core.util.security;

/**
 * A {@link PrivateKeyProvider} that stores its key encrypted.
 */
public interface EncryptedPrivateKeyProvider extends PrivateKeyProvider
{
    /**
     * Get the encrypted private key and certificate chain.
     *
     * @return The encrypted private key and certificate chain.
     */
    EncryptedPrivateKeyAndCertChain getEncryptedPrivateKey();

    /**
     * Get if this private key may be persisted.
     *
     * @return If the private key may be persisted.
     */
    boolean isPeristable();
}
