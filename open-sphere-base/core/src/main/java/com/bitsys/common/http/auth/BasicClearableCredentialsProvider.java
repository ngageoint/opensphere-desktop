package com.bitsys.common.http.auth;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.client.BasicCredentialsProvider;

/**
 * This class is an enhanced version of Apache's
 * {@link BasicCredentialsProvider}. It provides a
 * {@link #clearCredentials(AuthScope) method} to explicitly remove credentials
 * for a specific authentication scope. Also, it clearly defines the behavior
 * for setting a <code>null</code> credentials.
 */
public class BasicClearableCredentialsProvider implements ClearableCredentialsProvider
{
    /**
     * The mapping of authentication scope to credentials.
     */
    private final Map<AuthScope, Credentials> credentialsMap = new ConcurrentHashMap<>();

    /**
     * @see org.apache.http.client.CredentialsProvider#clear()
     */
    @Override
    public void clear()
    {
        credentialsMap.clear();
    }

    /**
     * @see com.bitsys.common.http.auth.ClearableCredentialsProvider#clearCredentials(org.apache.http.auth.AuthScope)
     */
    @Override
    public void clearCredentials(final AuthScope authScope)
    {
        credentialsMap.remove(authScope);
    }

    /**
     * @see com.bitsys.common.http.auth.ClearableCredentialsProvider#setCredentials(org.apache.http.auth.AuthScope,
     *      org.apache.http.auth.Credentials)
     */
    @Override
    public void setCredentials(final AuthScope authScope, final Credentials credentials)
    {
        if (authScope == null)
        {
            throw new IllegalArgumentException("The authentication scope is null");
        }

        if (credentials == null)
        {
            clearCredentials(authScope);
        }
        else
        {
            credentialsMap.put(authScope, credentials);
        }
    }

    /**
     * @see org.apache.http.client.CredentialsProvider#getCredentials(org.apache.http.auth.AuthScope)
     */
    @Override
    public Credentials getCredentials(final AuthScope authScope)
    {
        Credentials credentials = credentialsMap.get(authScope);
        if (credentials == null)
        {
            int bestMatch = -1;
            for (final Entry<AuthScope, Credentials> entry : credentialsMap.entrySet())
            {
                final int value = entry.getKey().match(authScope);
                if (value > bestMatch)
                {
                    credentials = entry.getValue();
                    bestMatch = value;
                }
            }
        }
        return credentials;
    }
}
