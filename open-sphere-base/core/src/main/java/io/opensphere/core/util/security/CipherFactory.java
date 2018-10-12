package io.opensphere.core.util.security;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import io.opensphere.core.util.Utilities;

/**
 * A factory for {@link Cipher}s that provides a mechanism for creating ciphers
 * while encapsulating the secret key.
 */
public class CipherFactory
{
    /** The default transformation for new ciphers. */
    private final String myDefaultTransformation;

    /** The provider for the key for the cipher. */
    private final SecretKeyProvider myKeyProvider;

    /** The optional algorithm provider. */
    private final Provider myProvider;

    /**
     * Constructor.
     *
     * @param keyProvider The provider for the key for the cipher.
     * @param defaultTransformation The default transformation for new ciphers.
     */
    public CipherFactory(SecretKeyProvider keyProvider, String defaultTransformation)
    {
        this(keyProvider, defaultTransformation, (Provider)null);
    }

    /**
     * Constructor.
     *
     * @param keyProvider The provider for the key for the cipher.
     * @param defaultTransformation The default transformation for new ciphers.
     * @param provider Optional algorithm provider.
     */
    public CipherFactory(SecretKeyProvider keyProvider, String defaultTransformation, Provider provider)
    {
        myKeyProvider = Utilities.checkNull(keyProvider, "keyProvider");
        myDefaultTransformation = Utilities.checkNull(defaultTransformation, "defaultTransformation");
        myProvider = provider;
    }

    /**
     * Create and initialize a new cipher object with the default transform and
     * no parameters. Note that for decryption, parameters are likely required,
     * so {@link #initCipher(int, String, AlgorithmParameters)} should be used
     * instead.
     *
     * @param mode The mode for the cipher ({@link Cipher#ENCRYPT_MODE},
     *            {@link Cipher#DECRYPT_MODE}, {@link Cipher#WRAP_MODE},
     *            {@link Cipher#UNWRAP_MODE}).
     * @return The initialized cipher.
     * @throws CipherException If the cipher cannot be initialized.
     */
    public Cipher initCipher(int mode) throws CipherException
    {
        Cipher cipher = getCipher();
        try
        {
            cipher.init(mode, myKeyProvider.getSecretKey());
        }
        catch (SecretKeyProviderException e)
        {
            throw new CipherException("Secret key could not be retrieved: " + e, e);
        }
        catch (InvalidKeyException e)
        {
            throw new CipherException("Secret key could not be created: " + e, e);
        }
        return cipher;
    }

    /**
     * Create and initialize a new cipher object.
     *
     * @param mode The mode for the cipher ({@link Cipher#ENCRYPT_MODE},
     *            {@link Cipher#DECRYPT_MODE}, {@link Cipher#WRAP_MODE},
     *            {@link Cipher#UNWRAP_MODE}).
     * @param transformation The transformation for the cipher.
     * @param params The algorithm parameters, which may be {@code null}.
     * @return The initialized cipher.
     * @throws CipherException If the algorithm specified in the transformation
     *             is not supported or could not be initialized.
     */
    public Cipher initCipher(int mode, String transformation, AlgorithmParameters params) throws CipherException
    {
        Cipher cipher = getCipher();
        try
        {
            cipher.init(mode, myKeyProvider.getSecretKey(), params);
        }
        catch (SecretKeyProviderException e)
        {
            throw new CipherException("Could not initialize cipher because secret key could not be retrieved: " + e, e);
        }
        catch (InvalidKeyException e)
        {
            throw new CipherException("Could not initialize cipher because the secret key is not valid for the algorithm: " + e,
                    e);
        }
        catch (InvalidAlgorithmParameterException e)
        {
            throw new CipherException("Could not initialize cipher because algorithm parameters are invalid: " + e, e);
        }
        return cipher;
    }

    /**
     * Return a new cipher factory that has the secret key retrieved.
     *
     * @return The cipher factory.
     *
     * @throws SecretKeyProviderException If the secret key cannot be retrieved.
     */
    public CipherFactory secureSecretKey() throws SecretKeyProviderException
    {
        if (isSecretKeySecure())
        {
            return this;
        }
        SecretKey secretKey = myKeyProvider.getSecretKey();
        return new CipherFactory(new DefaultSecretKeyProvider(secretKey), myDefaultTransformation);
    }

    /**
     * Create a cipher.
     *
     * @return The cipher.
     * @throws CipherException If the default transform is not available.
     */
    private Cipher getCipher() throws CipherException
    {
        Cipher cipher;
        try
        {
            if (myProvider == null)
            {
                cipher = Cipher.getInstance(myDefaultTransformation);
            }
            else
            {
                cipher = Cipher.getInstance(myDefaultTransformation, myProvider);
            }
        }
        catch (NoSuchPaddingException e)
        {
            throw new CipherException("Padding for algorithm is not supported: " + e, e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new CipherException("Cipher algorithm could not be loaded: " + e, e);
        }
        return cipher;
    }

    /**
     * Determine if the secret key is readily available.
     *
     * @return {@code true} if the key is ready.
     */
    private boolean isSecretKeySecure()
    {
        return myKeyProvider instanceof DefaultSecretKeyProvider;
    }
}
