package io.opensphere.core.util.security;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

import io.opensphere.core.util.Utilities;

/**
 * A secret key provider that extracts a secret key from a
 * {@link WrappedSecretKey}.
 */
public class WrappedSecretKeyProvider implements SecretKeyProvider
{
    /** The wrapped secret key. */
    private final WrappedSecretKey myWrappedSecretKey;

    /** The provider for the private key used to wrap the secret key. */
    private final PrivateKeyProvider myPrivateKeyProvider;

    /**
     * Constructor.
     *
     * @param wrappedSecretKey The wrapped secret key.
     * @param privateKeyProvider The provider for the private key used to wrap
     *            the secret key.
     */
    public WrappedSecretKeyProvider(WrappedSecretKey wrappedSecretKey, PrivateKeyProvider privateKeyProvider)
    {
        myWrappedSecretKey = Utilities.checkNull(wrappedSecretKey, "wrappedSecretKey");
        myPrivateKeyProvider = Utilities.checkNull(privateKeyProvider, "privateKeyProvider");
    }

    @Override
    public SecretKey getSecretKey() throws SecretKeyProviderException
    {
        try
        {
            return myWrappedSecretKey.getSecretKey(myPrivateKeyProvider);
        }
        catch (NoSuchAlgorithmException | IllegalArgumentException | CipherException | PrivateKeyProviderException e)
        {
            throw new SecretKeyProviderException("The secret key could not be retrieved: " + e, e);
        }
    }
}
