package io.opensphere.core.util.security;

import javax.crypto.SecretKey;

/** Provider that just wraps a secret key. */
public final class DefaultSecretKeyProvider implements SecretKeyProvider
{
    /** The key. */
    private final SecretKey myKey;

    /**
     * Constructor.
     *
     * @param key The secret key.
     */
    public DefaultSecretKeyProvider(SecretKey key)
    {
        myKey = key;
    }

    @Override
    public SecretKey getSecretKey()
    {
        return myKey;
    }
}
