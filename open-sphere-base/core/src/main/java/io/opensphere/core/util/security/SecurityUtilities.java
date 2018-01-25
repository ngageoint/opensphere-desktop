package io.opensphere.core.util.security;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;

/**
 * Security utilities.
 */
public final class SecurityUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SecurityUtilities.class);

    /** Number of iterations for PBE hashes used for password validation. */
    private static final int PBE_HASH_ITERATIONS = Utilities.parseSystemProperty("opensphere.security.pbe.hashIterations", 60000);

    /** Length for PBE hash. */
    private static final int PBE_HASH_LENGTH = Utilities.parseSystemProperty("opensphere.security.pbe.hashlength", 64);

    /** Number of iterations for PBE secret keys. */
    private static final int PBE_KEY_ITERATIONS = Utilities.parseSystemProperty("opensphere.security.pbe.keyIterations", 50000);

    /** Length for PBE secret keys. */
    private static final int PBE_KEY_LENGTH = Utilities.parseSystemProperty("opensphere.security.pbe.keylength", 128);

    /**
     * Get a private key provider that contains the private key identified by
     * the given alias from the given keystore.
     *
     * @param keystore The keystore containing the private key.
     * @param alias The alias for the private key.
     * @param source The source of the private key, to be displayed to the user.
     * @param password The password used to extract the private key from the
     *            keystore.
     * @param cipherFactory The source of the cipher used to encrypt the key.
     * @param persistable If the private key is persistable.
     * @return The private key provider.
     * @throws PrivateKeyProviderException If the private key provider cannot be
     *             created.
     * @throws NoSuchAlgorithmException If the algorithm for decoding the key
     *             cannot be found.
     * @throws KeyStoreException If the keystore has not been initialized.
     */
    public static CipherEncryptedPrivateKeyProvider createCipherEncryptedPrivateKeyProvider(KeyStore keystore, String alias,
            String source, char[] password, CipherFactory cipherFactory, boolean persistable)
                throws PrivateKeyProviderException, KeyStoreException, NoSuchAlgorithmException
    {
        try
        {
            final PrivateKey key = (PrivateKey)keystore.getKey(alias, password);
            final List<? extends X509Certificate> chain = getCertificateChain(keystore, alias);
            return new CipherEncryptedPrivateKeyProvider(alias, source, key, chain, cipherFactory, persistable);
        }
        catch (final PrivateKeyProviderException e)
        {
            if (e.getCause() instanceof UnrecoverableKeyException)
            {
                throw new PrivateKeyProviderException(
                        "The key for alias " + alias + " could not be loaded because the password is incorrect.", e);
            }
            else if (e.getCause() instanceof NoSuchAlgorithmException)
            {
                throw new PrivateKeyProviderException(
                        "The key for alias " + alias + " could not be loaded because it has an unsupported algorithm.", e);
            }
            else
            {
                throw new PrivateKeyProviderException(
                        "The key for alias " + alias + " could not be loaded due to an unforeseen error: " + e.getCause(), e);
            }
        }
        catch (final CertificateEncodingException e)
        {
            throw new PrivateKeyProviderException("The certificate chain for alias " + alias + " could not be encoded.", e);
        }
        catch (final UnrecoverableKeyException e)
        {
            throw new PrivateKeyProviderException(
                    "The key for alias " + alias + " could not be loaded because the password is incorrect.", e);
        }
        catch (final CipherException e)
        {
            throw new PrivateKeyProviderException("The private key for alias " + alias + " could not be encrypted.", e);
        }
    }

    /**
     * Generate a password salt and put it in the given array.
     *
     * @param salt The array to contain the salt.
     * @throws NoSuchAlgorithmException If a secure random algorithm is not
     *             available.
     */
    public static void generateSalt(byte[] salt) throws NoSuchAlgorithmException
    {
        String algorithm = getPreferredServiceAlgorithm("SecureRandom");
        if (algorithm == null)
        {
            algorithm = "SHA1PRNG";
        }
        SecureRandom.getInstance(algorithm).nextBytes(salt);
    }

    /**
     * Generate a secret key using the given password with password based
     * encryption (PBE). If the password digest is given, extract the salt from
     * the digest. Otherwise generate a new salt.
     *
     * @param password The password.
     * @param salt Array to be populated with the salt, either from the digest
     *            or newly generated.
     * @return The secret key, or {@code null}.
     */
    public static SecretKey getAESSecretKey(char[] password, byte[] salt)
    {
        final SecretKey secretKey = getPBESecretKey(Utilities.checkNull(password, "password"), salt, PBE_KEY_ITERATIONS,
                PBE_KEY_LENGTH);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    /**
     * Get the available service provider algorithms of a given type.
     *
     * @param serviceType The service type.
     * @return The list of algorithms.
     */
    public static List<String> getAvailableServiceAlgorithms(String serviceType)
    {
        final List<String> algorithms = New.list();
        for (final Provider provider : Security.getProviders())
        {
            final Set<Service> services = provider.getServices();
            for (final Service service : services)
            {
                if (service.getType().equals(serviceType))
                {
                    algorithms.add(service.getAlgorithm());
                }
            }
        }
        return algorithms;
    }

    /**
     * Get a certificate chain from a keystore.
     *
     * @param keystore The keystore.
     * @param alias The alias for the key associated with the chain.
     * @return The certificate chain.
     * @throws PrivateKeyProviderException If the certificate chain cannot be
     *             retrieved.
     */
    public static List<? extends X509Certificate> getCertificateChain(KeyStore keystore, String alias)
        throws PrivateKeyProviderException
    {
        return new KeyStorePrivateKeyProvider(alias, keystore, (char[])null, "Key Store").getCertificateChain();
    }

    /**
     * Get a password hash using PBE.
     *
     * @param password The password.
     * @param salt The salt.
     * @return The hash.
     */
    public static byte[] getPBEHash(char[] password, byte[] salt)
    {
        return getPBESecretKey(password, salt, PBE_HASH_ITERATIONS, PBE_HASH_LENGTH).getEncoded();
    }

    /**
     * Generate a secret key from a password.
     *
     * @param password The password.
     * @param salt The salt for the password.
     * @param iterations The number of digest iterations.
     * @param keyLength The desired key length.
     * @return The secret key.
     */
    public static SecretKey getPBESecretKey(char[] password, byte[] salt, int iterations, int keyLength)
    {
        SecretKey secretKey;
        try
        {
            final PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, iterations, keyLength);
            secretKey = getPBESecretKeyFactory().generateSecret(pbeKeySpec);
        }
        catch (final InvalidKeySpecException e)
        {
            LOGGER.error("Could not create PBE secret key: " + e, e);
            return null;
        }
        return secretKey;
    }

    /**
     * Get a secret key factory for password-based encryption.
     *
     * @return The secret key factory.
     */
    public static SecretKeyFactory getPBESecretKeyFactory()
    {
        try
        {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        }
        catch (final NoSuchAlgorithmException e)
        {
            LOGGER.error("Failed to load secret key factory: " + e, e);
        }
        for (final String alg : getAvailableServiceAlgorithms("SecretKeyFactory"))
        {
            if (alg.startsWith("PBE"))
            {
                try
                {
                    return SecretKeyFactory.getInstance(alg);
                }
                catch (final NoSuchAlgorithmException e)
                {
                    LOGGER.error("Failed to load secret key factory: " + e, e);
                }
            }
        }
        return null;
    }

    /**
     * Get the algorithm of the preferred service provider of a given type.
     *
     * @param serviceType The service type.
     * @return The algorithm, or {@code null} if no service was found.
     */
    public static String getPreferredServiceAlgorithm(String serviceType)
    {
        final List<String> algorithms = getAvailableServiceAlgorithms(serviceType);
        return algorithms.isEmpty() ? null : algorithms.get(0);
    }

    /** Disallow instantiation. */
    private SecurityUtilities()
    {
    }
}
