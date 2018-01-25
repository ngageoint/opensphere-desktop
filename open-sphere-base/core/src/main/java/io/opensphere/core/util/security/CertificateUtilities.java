package io.opensphere.core.util.security;

import static io.opensphere.core.util.lang.StringUtilities.LINE_SEP;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.StringUtilities;

/**
 * Certificate utilities.
 */
public final class CertificateUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CertificateUtilities.class);

    /**
     * Query the runtime environment for the available keystore algorithms.
     *
     * @return The algorithm names.
     */
    public static Collection<? extends String> getAvailableKeyStoreAlgorithms()
    {
        Collection<String> keyStoreAlgorithms = New.set();
        for (Provider provider : Security.getProviders())
        {
            for (Service service : provider.getServices())
            {
                // Skip Windows key stores because they will load regardless of
                // the file stream.
                if (service.getType().equals("KeyStore") && !service.getAlgorithm().contains("Windows"))
                {
                    keyStoreAlgorithms.add(service.getAlgorithm());
                }
            }
        }
        return keyStoreAlgorithms;
    }

    /**
     * Get a detail string for a certificate chain.
     *
     * @param indent The indent for each line in the string.
     * @param certChain The certificate chain.
     * @return The string.
     */
    public static String getDetailString(String indent, List<? extends X509Certificate> certChain)
    {
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < certChain.size(); ++index)
        {
            X509Certificate cert = certChain.get(index);
            sb.append("Certificate[").append(index).append("] ");
            if (index == 0)
            {
                sb.append("Issued to:").append(LINE_SEP);
            }
            else
            {
                sb.append("Issued by:").append(LINE_SEP);
            }
            sb.append(CertificateUtilities.getDetailString(indent, cert)).append(LINE_SEP);
        }
        return sb.toString();
    }

    /**
     * Get a detail string for a certificate.
     *
     * @param indent The indent for each line in the string.
     * @param cert The certificate.
     * @return The string.
     */
    public static String getDetailString(String indent, X509Certificate cert)
    {
        StringBuilder sb = new StringBuilder(256);
        String dn = cert.getSubjectDN().getName();
        sb.append(indent).append("Serial number:            \t").append(getSerialNumberString(cert)).append(LINE_SEP);
        sb.append(indent).append("Common Name (CN):         \t").append(CertificateUtilities.getDistinguishedNamePart(dn, "CN="))
                .append(LINE_SEP);
        sb.append(indent).append("Organization (O):         \t").append(CertificateUtilities.getDistinguishedNamePart(dn, "O="))
                .append(LINE_SEP);
        sb.append(indent).append("Organizational Unit (OU): \t").append(CertificateUtilities.getDistinguishedNamePart(dn, "OU="))
                .append(LINE_SEP);
        sb.append(LINE_SEP);

        for (String alg : new String[] { "MD5", "SHA1" })
        {
            try
            {
                sb.append(indent).append(String.format("%-26s", alg + " Fingerprint:")).append('\t')
                        .append(CertificateUtilities.getFingerprint(cert, alg)).append(LINE_SEP);
            }
            catch (CertificateEncodingException e)
            {
                LOGGER.error("Failed to encode certificate: " + e, e);
            }
            catch (NoSuchAlgorithmException e)
            {
                LOGGER.error("Hash algorithm could not be found: " + e, e);
            }
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        sb.append(indent).append("Valid start:              \t").append(dateFormat.format(cert.getNotBefore())).append(LINE_SEP);
        sb.append(indent).append("Valid end:                \t").append(dateFormat.format(cert.getNotAfter())).append(LINE_SEP);
        sb.append(indent).append("Key Usage:                \t").append(new KeyUsage(cert.getKeyUsage())).append(LINE_SEP);

        return sb.toString();
    }

    /**
     * Get a part of a distinguished name.
     *
     * @param dn The distinguished name.
     * @param tag The tag to find.
     * @return The value for the tag, or an empty string if the tag is not
     *         found.
     */
    public static String getDistinguishedNamePart(String dn, String tag)
    {
        StringBuilder sb = new StringBuilder(32);
        for (int fromIndex = 0; fromIndex < dn.length();)
        {
            int beginIndex = dn.indexOf(tag, fromIndex) + tag.length();
            if (beginIndex == tag.length() - 1)
            {
                break;
            }
            int endIndex = beginIndex;
            do
            {
                endIndex = dn.indexOf(',', endIndex + 1);
            }
            while (endIndex != -1 && dn.charAt(endIndex - 1) == '\\');

            sb.append(dn.substring(beginIndex, endIndex == -1 ? dn.length() : endIndex));
            sb.append(", ");

            fromIndex = endIndex == -1 ? dn.length() : endIndex;
        }
        if (sb.length() >= 2)
        {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    /**
     * Get the fingerprint of a certificate.
     *
     * @param certificate The certificate.
     * @param algorithm The digest algorithm to use, for
     *            {@link MessageDigest#getInstance(String)}.
     * @return The fingerprint.
     * @throws CertificateEncodingException If the certificate cannot be
     *             encoded.
     * @throws NoSuchAlgorithmException If the digest algorithm is not
     *             supported.
     * @see MessageDigest#getInstance(String)
     */
    public static String getFingerprint(Certificate certificate, String algorithm)
        throws CertificateEncodingException, NoSuchAlgorithmException
    {
        return StringUtilities.toHexString(new Digest(algorithm, certificate).getMessageDigest(), ":");
    }

    /**
     * Get the last name (CN, OU, or O) from a distinguished name.
     *
     * @param dn The distinguished name.
     * @return The last name.
     */
    public static String getLastDistinguishedNamePart(String dn)
    {
        String name = getDistinguishedNamePart(dn, "CN=");
        if (StringUtils.isBlank(name))
        {
            name = getDistinguishedNamePart(dn, "OU=");
        }
        if (StringUtils.isBlank(name))
        {
            name = getDistinguishedNamePart(dn, "O=");
        }
        return name;
    }

    /**
     * Get the serial number of a certificate as a formatted string.
     *
     * @param cert The certificate.
     * @return The string.
     */
    public static String getSerialNumberString(X509Certificate cert)
    {
        String str = cert.getSerialNumber().toString(16);
        StringBuilder sb = new StringBuilder((str.length() + 1) / 2 * 3);
        int index = 0;
        if (str.length() % 2 != 0)
        {
            sb.append('0');
            sb.append(Character.toUpperCase(str.charAt(index++)));
            sb.append(':');
        }
        while (index < str.length())
        {
            sb.append(Character.toUpperCase(str.charAt(index++)));
            sb.append(Character.toUpperCase(str.charAt(index++)));
            sb.append(':');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Private constructor.
     */
    private CertificateUtilities()
    {
    }
}
