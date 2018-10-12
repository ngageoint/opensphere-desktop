package com.bitsys.common.http.auth;

import java.util.Arrays;

import org.apache.http.auth.AuthScope;

/**
 * Provides utilities useful for credentials providers.
 */
public final class CredentialsProviderUtils
{
    /**
     * Hide the default constructor.
     */
    private CredentialsProviderUtils()
    {
    }

    /**
     * Converts the {@link AuthenticationScope} to an Apache {@link AuthScope}.
     *
     * @param scope the authentication scope.
     * @return the converted authentication scope or <code>null</code> if the
     *         given scope is <code>null</code>.
     */
    public static AuthScope toAuthScope(final AuthenticationScope scope)
    {
        return new AuthScope(scope.getHost(), scope.getPort(), scope.getRealm(), scope.getScheme());
    }

    /**
     * Converts the Apache {@link AuthScope} to an {@link AuthenticationScope}.
     *
     * @param authScope the Apache authentication scope.
     * @return the converted authentication scope or <code>null</code> if the
     *         given scope is <code>null</code>.
     */
    public static AuthenticationScope toAuthenticationScope(final AuthScope authScope)
    {
        return new AuthenticationScope(authScope);
    }

    /**
     * Converts the Apache {@link org.apache.http.auth.Credentials Credentials}
     * to a {@link Credentials}.
     *
     * @param credentials the credentials to convert.
     * @return the converted credentials or <code>null</code> if the given
     *         credentials are <code>null</code>.
     */
    public static Credentials toCredentials(final org.apache.http.auth.Credentials credentials)
    {
        Credentials creds = null;
        if (credentials instanceof org.apache.http.auth.UsernamePasswordCredentials)
        {
            final org.apache.http.auth.UsernamePasswordCredentials upCredentials = (org.apache.http.auth.UsernamePasswordCredentials)credentials;
            creds = new UsernamePasswordCredentials(upCredentials.getUserName(), upCredentials.getPassword().toCharArray());
        }
        else if (credentials != null)
        {
            throw new IllegalArgumentException("Unsupported Credentials " + credentials);
        }
        return creds;
    }

    /**
     * Converts the {@link Credentials} instance to the equivalent Apache
     * {@link org.apache.http.auth.Credentials Credentials}. If there is no
     * equivalent Apache class, the credentials are wrapped in a
     * {@link ApacheCredentialsProxy}.
     *
     * @param credentials the credentials.
     * @return the converted credentials or <code>null</code> if the given
     *         credentials are <code>null</code>.
     */
    public static org.apache.http.auth.Credentials toCredentials(final Credentials credentials)
    {
        org.apache.http.auth.Credentials creds = null;
        if (credentials instanceof UsernamePasswordCredentials)
        {
            final UsernamePasswordCredentials upCredentials = (UsernamePasswordCredentials)credentials;
            final char[] password = upCredentials.getPassword();
            creds = new org.apache.http.auth.UsernamePasswordCredentials(upCredentials.getUserName(), new String(password));
            Arrays.fill(password, '\0');
        }
        else if (credentials != null)
        {
            creds = new ApacheCredentialsProxy(credentials);
        }
        return creds;
    }
}
