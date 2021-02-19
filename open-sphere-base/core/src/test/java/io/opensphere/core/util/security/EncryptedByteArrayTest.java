package io.opensphere.core.util.security;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/** Test for {@link EncryptedByteArray}. */
public class EncryptedByteArrayTest
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(EncryptedByteArrayTest.class);

    /**
     * Test creating and decrypting a {@link EncryptedByteArray}.
     *
     * @throws GeneralSecurityException If the test fails.
     * @throws IOException If the test fails.
     */
    @Test
    public void test() throws GeneralSecurityException, IOException
    {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        int successCount = 0;

        Provider[] providers = Security.getProviders();
        for (Provider keyGeneratorProvider : providers)
        {
            for (Provider.Service keyGeneratorService : keyGeneratorProvider.getServices())
            {
                if ("KeyGenerator".equals(keyGeneratorService.getType()))
                {
                    KeyGenerator keyGenerator = KeyGenerator.getInstance(keyGeneratorService.getAlgorithm());
                    SecretKey key;
                    try
                    {
                        key = keyGenerator.generateKey();
                    }
                    catch (IllegalStateException e)
                    {
                        // Skip key generators that require parameters.
                        if (LOGGER.isDebugEnabled())
                        {
                            LOGGER.debug(e, e);
                        }
                        continue;
                    }

                    for (Provider algProvider : providers)
                    {
                        for (Provider.Service algService : algProvider.getServices())
                        {
                            if ("Cipher".equals(algService.getType())
                                    && algService.getAlgorithm().equals(keyGeneratorService.getAlgorithm()))
                            {
                                String[] modes = algService.getAttribute("SupportedModes") == null ?
                                        new String[0] : algService.getAttribute("SupportedModes").split("\\|");

                                String[] paddings = algService.getAttribute("SupportedPaddings") == null ?
                                        new String[0] : algService.getAttribute("SupportedPaddings").split("\\|");

                                for (String mode : modes)
                                {
                                    for (String padding : paddings)
                                    {
                                        String transform = algService.getAlgorithm() + "/" + mode + "/" + padding;

                                        final Cipher encryptCipher;
                                        try
                                        {
                                            encryptCipher = Cipher.getInstance(transform, algProvider);
                                        }
                                        catch (NoSuchPaddingException e)
                                        {
                                            // Some modes are not compatible
                                            // with some paddings.
                                            continue;
                                        }

                                        CipherFactory cipherFactory = new CipherFactory(new DefaultSecretKeyProvider(key),
                                                transform, algProvider);
                                        encryptCipher.init(Cipher.ENCRYPT_MODE, key);

                                        byte[] testBytes = new byte[1133];
                                        random.nextBytes(testBytes);

                                        EncryptedByteArray encryptedByteArray;
                                        try
                                        {
                                            encryptedByteArray = new EncryptedByteArray(testBytes, cipherFactory);
                                        }
                                        catch (CipherException e)
                                        {
                                            // Try again using the cipher block
                                            // size.
                                            if ("NOPADDING".equals(padding))
                                            {
                                                testBytes = new byte[encryptCipher.getBlockSize() * 7];
                                                encryptedByteArray = new EncryptedByteArray(testBytes, cipherFactory);
                                            }
                                            else
                                            {
                                                // Shouldn't have to use the
                                                // cipher block size if padding
                                                // is enabled.
                                                throw e;
                                            }
                                        }

                                        byte[] decryptedData = encryptedByteArray.getDecryptedData(cipherFactory);

                                        Assert.assertTrue(Arrays.equals(testBytes, decryptedData));

                                        if (LOGGER.isDebugEnabled())
                                        {
                                            LOGGER.debug("Test succeeded for " + encryptCipher.getAlgorithm());
                                        }
                                        successCount++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Assert.assertTrue(successCount > 0);
    }
}
