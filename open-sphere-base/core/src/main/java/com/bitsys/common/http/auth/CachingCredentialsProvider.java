package com.bitsys.common.http.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

/**
 * This class is a credentials provider that caches credentials from another
 * provider. This class also provides more fine-grained control of cache
 * clearing.
 */
public class CachingCredentialsProvider extends ChainedCredentialsProvider implements ClearableCredentialsProvider
{
    /** The mapping of cached time to authentication scope. */
    private final Map<AuthScope, Long> cachedTimeMap;

    /**
     * Constructs a new {@linkplain CachingCredentialsProvider} the uses the
     * given provider to provide credentials not already in the cache.
     *
     * @param provider the other credentials provider.
     */
    public CachingCredentialsProvider(final CredentialsProvider provider)
    {
        super(new BasicClearableCredentialsProvider(), provider);
        cachedTimeMap = Collections.synchronizedMap(new HashMap<AuthScope, Long>());
    }

    @Override
    public void clear()
    {
        synchronized (cachedTimeMap)
        {
            cachedTimeMap.clear();
            super.clear();
        }
    }

    @Override
    public void clearCredentials(final AuthScope authScope)
    {
        if (authScope == null)
        {
            throw new IllegalArgumentException("The authentication scope is null");
        }
        if (cachedTimeMap.containsKey(authScope))
        {
            final ClearableCredentialsProvider provider = (ClearableCredentialsProvider)getProvider();
            synchronized (cachedTimeMap)
            {
                provider.clearCredentials(authScope);
                cachedTimeMap.remove(authScope);
            }
        }
    }

    // TODO: Add more clear methods for clearing based on time and
    // authentication
    // scope.

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
            synchronized (cachedTimeMap)
            {
                super.setCredentials(authScope, credentials);
                cachedTimeMap.put(authScope, Long.valueOf(System.currentTimeMillis()));
            }
        }
    }

    /**
     * Returns the credentials for the given authentication scope. This method
     * will first look internally to satisfy the request. If no credentials are
     * found, it asks the provider given in the constructor for credentials. If
     * it returns credentials, they are stored within this provider.
     */
    @Override
    public Credentials getCredentials(final AuthScope authScope)
    {
        final Credentials credentials = super.getCredentials(authScope);

        if (credentials != null && !cachedTimeMap.containsKey(authScope))
        {
            setCredentials(authScope, credentials);
        }
        return credentials;
    }
}
