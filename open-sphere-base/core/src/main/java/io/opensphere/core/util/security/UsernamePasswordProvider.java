package io.opensphere.core.util.security;

/**
 * A provider for a username and password.
 */
public interface UsernamePasswordProvider
{
    /**
     * Get the password.
     *
     * @return The password.
     * @throws DecryptionException If the password cannot be decrypted.
     */
    char[] getPassword() throws DecryptionException;

    /**
     * Get the purpose for the username/password.
     *
     * @return The purpose.
     */
    String getPurpose();

    /**
     * Get the user name.
     *
     * @return The user name.
     */
    String getUsername();
}
