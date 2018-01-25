package io.opensphere.core.authentication;

/**
 * Interface to an object that provides a user's user name and password for
 * login purposes.
 */
public interface InteractiveUsernamePasswordProvider
{
    /**
     * Method called when authentication with the server fails.
     */
    void failedAuthentication();

    /**
     * Get the password, querying the user if necessary.
     *
     * @return The password.
     */
    char[] getPassword();

    /**
     * Get the username, querying the user if necessary.
     *
     * @return The username.
     */
    String getUsername();
}
