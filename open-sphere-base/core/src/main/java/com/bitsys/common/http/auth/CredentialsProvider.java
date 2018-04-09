package com.bitsys.common.http.auth;

/**
 * This interface defines the contract for a class that provides credentials.
 * <p>
 * Implementations of this class must be thread-safe.
 */
public interface CredentialsProvider
{
    /**
     * Clears all credentials.
     */
    void clear();

    /**
     * Sets the credentials for the given authentication scope.
     *
     * @param scope the authentication scope.
     * @param credentials the credentials.
     */
    void setCredentials(AuthenticationScope scope, Credentials credentials);

    /**
     * Returns the credentials for the given authentication scope.
     *
     * @param scope the authentication scope.
     * @return the credentials.
     */
    Credentials getCredentials(AuthenticationScope scope);
}
