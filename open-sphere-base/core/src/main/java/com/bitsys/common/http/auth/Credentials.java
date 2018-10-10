package com.bitsys.common.http.auth;

import java.security.Principal;

/**
 * This interface represents a set of credentials consisting of a security
 * principal and a password that are used to establish a user's identity.
 */
public interface Credentials
{
    /**
     * Returns the user's principal.
     *
     * @return the user's principal.
     */
    Principal getUserPrincipal();

    /**
     * Returns the user's password. The caller takes ownership of the returned
     * array and clears it.
     *
     * @return the user's password.
     */
    char[] getPassword();
}
