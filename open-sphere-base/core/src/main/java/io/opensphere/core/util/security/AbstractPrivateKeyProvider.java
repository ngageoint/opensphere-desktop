package io.opensphere.core.util.security;

import static io.opensphere.core.util.lang.StringUtilities.LINE_SEP;

import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;

import io.opensphere.core.util.collections.CollectionUtilities;

/**
 * Abstract class containing common functionality for private key providers.
 */
public abstract class AbstractPrivateKeyProvider implements PrivateKeyProvider
{
    /** The X.509 KeyUsage OID. */
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String KEY_USAGE_OID = "2.5.29.15";

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractPrivateKeyProvider.class);

    /** The alias. */
    private final String myAlias;

    /** The source of the key. */
    private final String mySource;

    /**
     * Constructor.
     *
     * @param alias The alias for the key.
     * @param source The source of the key.
     */
    public AbstractPrivateKeyProvider(String alias, String source)
    {
        myAlias = alias;
        mySource = source;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (!(obj instanceof PrivateKeyProvider))
        {
            return false;
        }

        try
        {
            return getCertificateChain().equals(((PrivateKeyProvider)obj).getCertificateChain());
        }
        catch (PrivateKeyProviderException e)
        {
            throw new PrivateKeyProviderRuntimeException(e);
        }
    }

    @Override
    public String getAlias()
    {
        return myAlias;
    }

    @Override
    public String getDetailString()
    {
        StringBuilder sb = new StringBuilder(128);
        String indent = "    ";
        try
        {
            sb.append("Source: ").append(getSource()).append(LINE_SEP).append(LINE_SEP);
            sb.append(CertificateUtilities.getDetailString(indent, getCertificateChain()));
        }
        catch (PrivateKeyProviderException e)
        {
            LOGGER.error("Certificate could not be loaded: " + e, e);
        }
        return sb.toString();
    }

    @Override
    public Digest getDigest(String algorithm)
        throws PrivateKeyProviderException, CertificateEncodingException, NoSuchAlgorithmException
    {
        return new Digest(algorithm, getFirstCertificate());
    }

    @Override
    public String getFingerprint(String algorithm)
        throws CertificateEncodingException, NoSuchAlgorithmException, PrivateKeyProviderException
    {
        Certificate cert = getFirstCertificate();
        return CertificateUtilities.getFingerprint(cert, algorithm);
    }

    @Override
    public String getSerialNumber() throws PrivateKeyProviderException
    {
        X509Certificate cert = getFirstCertificate();
        return CertificateUtilities.getSerialNumberString(cert);
    }

    @Override
    public String getSource()
    {
        return mySource;
    }

    @Override
    public boolean hasDigest(Digest digest)
    {
        try
        {
            return digest == null || digest.equals(getDigest(digest.getAlgorithm()));
        }
        catch (CertificateEncodingException e)
        {
            LOGGER.warn("Could not encode certificate: " + e, e);
        }
        catch (NoSuchAlgorithmException e)
        {
            LOGGER.warn("Provided digest algorithm was not found: " + e, e);
        }
        catch (PrivateKeyProviderException e)
        {
            LOGGER.warn("Could not compute digest: " + e, e);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        try
        {
            result = prime * result + getCertificateChain().hashCode();
        }
        catch (PrivateKeyProviderException e)
        {
            throw new PrivateKeyProviderRuntimeException(e);
        }
        return result;
    }

    @Override
    public boolean hasKeyType(Set<? extends String> keyTypes) throws PrivateKeyProviderException
    {
        if (keyTypes == null)
        {
            return true;
        }
        for (String keyType : keyTypes)
        {
            String keyAlgorithm;
            String sigKeyAlgorithm;
            int separatorIndex = keyType.indexOf('_');
            if (separatorIndex == -1)
            {
                keyAlgorithm = keyType;
                sigKeyAlgorithm = null;
            }
            else
            {
                keyAlgorithm = keyType.substring(0, separatorIndex);
                sigKeyAlgorithm = keyType.substring(separatorIndex + 1);
            }

            List<? extends X509Certificate> certificateChain = getCertificateChain();
            if (certificateChain.get(0).getPublicKey().getAlgorithm().equals(keyAlgorithm))
            {
                if (sigKeyAlgorithm == null)
                {
                    return true;
                }
                else if (certificateChain.size() > 1)
                {
                    if (sigKeyAlgorithm.equals(certificateChain.get(1).getPublicKey().getAlgorithm()))
                    {
                        return true;
                    }
                }
                else
                {
                    String s = certificateChain.get(0).getSigAlgName().toUpperCase(Locale.ENGLISH);
                    String s1 = new StringBuilder().append("WITH").append(sigKeyAlgorithm.toUpperCase(Locale.ENGLISH)).toString();
                    if (s.contains(s1))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasKeyUsage(KeyUsage keyUsage) throws PrivateKeyProviderException
    {
        if (keyUsage == null)
        {
            return true;
        }

        X509Certificate firstCertificate = getFirstCertificate();
        Set<String> criticalExtensionOIDs = firstCertificate.getCriticalExtensionOIDs();
        if (criticalExtensionOIDs == null || !criticalExtensionOIDs.contains(KEY_USAGE_OID))
        {
            return true;
        }

        boolean[] certKeyUsage = firstCertificate.getKeyUsage();
        if (certKeyUsage == null)
        {
            return true;
        }

        return keyUsage.anyAllowedBy(new KeyUsage(certKeyUsage));
    }

    @Override
    public boolean isAcceptable(Collection<? extends X500Principal> principals) throws PrivateKeyProviderException
    {
        if (!CollectionUtilities.hasContent(principals))
        {
            return true;
        }
        List<? extends X509Certificate> certificateChain = getCertificateChain();
        if (certificateChain != null)
        {
            for (X509Certificate cert : certificateChain)
            {
                if (principals.contains(cert.getIssuerX500Principal()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAcceptable(Set<? extends String> keyTypes, Collection<? extends X500Principal> principals)
        throws PrivateKeyProviderException
    {
        return isCurrent() && hasKeyType(keyTypes) && isAcceptable(principals);
    }

    @Override
    public boolean isCurrent() throws PrivateKeyProviderException
    {
        Date now = new Date();
        for (X509Certificate cert : getCertificateChain())
        {
            if (now.before(cert.getNotBefore()) || now.after(cert.getNotAfter()))
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString()
    {
        return getAlias();
    }

    /**
     * Helper to get the first certificate in the chain.
     *
     * @return The first certificate.
     * @throws PrivateKeyProviderException If the chain has no certificates.
     */
    private X509Certificate getFirstCertificate() throws PrivateKeyProviderException
    {
        List<? extends X509Certificate> certChain = getCertificateChain();
        if (!CollectionUtilities.hasContent(certChain))
        {
            throw new PrivateKeyProviderException("No certificate chain found.");
        }
        return certChain.get(0);
    }
}
