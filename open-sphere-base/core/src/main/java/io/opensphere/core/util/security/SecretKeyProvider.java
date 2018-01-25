package io.opensphere.core.util.security;

import javax.crypto.SecretKey;

/**
 * A provider for a secret key.
 */
@FunctionalInterface
public interface SecretKeyProvider
{
    /**
     * Get the secret key from the provider.
     *
     * @return The secret key.
     * @throws SecretKeyProviderException If the secret key cannot be retrieved.
     */
    SecretKey getSecretKey() throws SecretKeyProviderException;
}
