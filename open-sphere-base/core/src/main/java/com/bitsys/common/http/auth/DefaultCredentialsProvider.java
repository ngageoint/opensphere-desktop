package com.bitsys.common.http.auth;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides a default {@link CredentialsProvider} implementation.
 */
public class DefaultCredentialsProvider implements CredentialsProvider
{
    /**
     * The mapping of authentication scope to credentials.
     */
    private final Map<AuthenticationScope, Credentials> credentialsMap = new ConcurrentHashMap<>();

    @Override
    public void clear()
    {
        credentialsMap.clear();
    }

    @Override
    public void setCredentials(final AuthenticationScope scope, final Credentials credentials)
    {
        if (scope == null)
        {
            throw new IllegalArgumentException("The authentication scope is null");
        }
        if (credentials == null)
        {
            throw new IllegalArgumentException("The credentials are null");
        }
        credentialsMap.put(scope, credentials);
    }

    @Override
    public Credentials getCredentials(final AuthenticationScope scope)
    {
        Credentials credentials = credentialsMap.get(scope);
        if (credentials == null)
        {
            int bestMatch = -1;
            for (final Entry<AuthenticationScope, Credentials> entry : credentialsMap.entrySet())
            {
                final int value = entry.getKey().match(scope);
                if (value > bestMatch)
                {
                    credentials = entry.getValue();
                    bestMatch = value;
                }
            }
        }
        return credentials;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("DefaultCredentialsProvider [Credentials Map=");
        builder.append(credentialsMap);
        builder.append("]");
        return builder.toString();
    }
}
