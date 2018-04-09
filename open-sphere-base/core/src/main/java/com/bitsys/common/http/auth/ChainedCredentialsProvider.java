package com.bitsys.common.http.auth;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

/**
 * This class is a credentials provider where the credentials are first looked
 * up in the first credentials provider. If they are found, they are returned.
 * Otherwise, an attempt is made to find the credentials in the next provider. A
 * series of these providers can be linked together to satisfy different
 * credential provider needs.
 */
public class ChainedCredentialsProvider implements CredentialsProvider
{
    /**
     * The first credentials provider.
     */
    private final CredentialsProvider provider;

    /**
     * The next credentials provider if the {@link #provider first} credentials
     * provider cannot satisfy a request.
     */
    private final CredentialsProvider nextProvider;

    /**
     * Constructs a {@linkplain ChainedCredentialsProvider}.
     *
     * @param provider the first provider.
     * @param nextProvider the next provider.
     */
    public ChainedCredentialsProvider(final CredentialsProvider provider, final CredentialsProvider nextProvider)
    {
        this.provider = provider;
        this.nextProvider = nextProvider;
    }

    @Override
    public void setCredentials(final AuthScope authScope, final Credentials credentials)
    {
        provider.setCredentials(authScope, credentials);
    }

    /**
     * Attempts to satify the the request for credentials using the first
     * provider. If the first provider cannot provide them, then the next
     * provider is consulted. If neither provider has credentials, this method
     * returns <code>null</code>.
     *
     * @param authScope the authentication scope.
     */
    @Override
    public Credentials getCredentials(final AuthScope authScope)
    {
        Credentials credentials = provider.getCredentials(authScope);
        if (credentials == null)
        {
            credentials = nextProvider.getCredentials(authScope);
        }
        return credentials;
    }

    /**
     * Clears the first credentials provider while the next one is left alone.
     */
    @Override
    public void clear()
    {
        provider.clear();
    }

    /**
     * Returns the first credentials provider.
     *
     * @return the first credentials provider.
     */
    public CredentialsProvider getProvider()
    {
        return provider;
    }

    /**
     * Returns the next credentials provider.
     *
     * @return the next credentials provider.
     */
    public CredentialsProvider getNextProvider()
    {
        return nextProvider;
    }
}
