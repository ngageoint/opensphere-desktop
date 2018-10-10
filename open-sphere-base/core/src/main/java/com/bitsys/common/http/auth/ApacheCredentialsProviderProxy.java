package com.bitsys.common.http.auth;

import static com.bitsys.common.http.auth.CredentialsProviderUtils.toAuthenticationScope;
import static com.bitsys.common.http.auth.CredentialsProviderUtils.toCredentials;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

/**
 * This class is an Apache {@link CredentialsProvider} that delegates calls to a
 * {@link CredentialsProvider}.
 */
public class ApacheCredentialsProviderProxy implements CredentialsProvider
{
    /** This library's credentials provider. */
    private final com.bitsys.common.http.auth.CredentialsProvider credentialsProvider;

    /**
     * Creates a new instance using the given
     * {@link com.bitsys.common.http.auth.CredentialsProvider
     * CredentialsProvider}.
     *
     * @param credentialsProvider the delegated credentials provider.
     */
    public ApacheCredentialsProviderProxy(final com.bitsys.common.http.auth.CredentialsProvider credentialsProvider)
    {
        if (credentialsProvider == null)
        {
            throw new IllegalArgumentException("The Credentials Provider is null");
        }
        this.credentialsProvider = credentialsProvider;
    }

    @Override
    public void setCredentials(final AuthScope authScope, final Credentials credentials)
    {
        credentialsProvider.setCredentials(toAuthenticationScope(authScope), toCredentials(credentials));
    }

    @Override
    public Credentials getCredentials(final AuthScope authScope)
    {
        return toCredentials(credentialsProvider.getCredentials(toAuthenticationScope(authScope)));
    }

    @Override
    public void clear()
    {
        credentialsProvider.clear();
    }
}
