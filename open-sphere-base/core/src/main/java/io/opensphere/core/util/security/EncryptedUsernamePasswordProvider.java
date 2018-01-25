package io.opensphere.core.util.security;

/**
 * A {@link UsernamePasswordProvider} that stores its password encrypted.
 */
public interface EncryptedUsernamePasswordProvider extends UsernamePasswordProvider
{
    /**
     * Get the encrypted username/password.
     *
     * @return The encrypted username/password.
     */
    EncryptedUsernamePassword getEncryptedUsernamePassword();

    /**
     * Get if the username/password from this provider may be persisted.
     *
     * @return {@code true} if the username/password may be persisted.
     */
    boolean isPersistable();
}
