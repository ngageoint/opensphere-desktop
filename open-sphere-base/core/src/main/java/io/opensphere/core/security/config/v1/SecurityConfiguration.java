package io.opensphere.core.security.config.v1;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.EqualsHelper;
import io.opensphere.core.util.lang.ExpectedCloneableException;
import io.opensphere.core.util.security.EncryptedPrivateKeyAndCertChain;
import io.opensphere.core.util.security.EncryptedUsernamePassword;
import io.opensphere.core.util.xml.JAXBableStringMap;

/**
 * User security configuration. This comprises the user's configuration for
 * encrypting local data, their stored certificates and username/passwords, and
 * their trusted servers.
 */
@XmlRootElement(name = "SecurityConfiguration")
@XmlAccessorType(XmlAccessType.NONE)
public class SecurityConfiguration implements Cloneable
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SecurityConfiguration.class);

    /** Configuration for local cryptography. */
    @XmlElement(name = "CryptoConfig")
    private CryptoConfig myCryptoConfig;

    /** The imported private keys. */
    @XmlElement(name = "PrivateKey")
    private Collection<EncryptedPrivateKeyAndCertChain> myEncryptedPrivateKeys = New.collection();

    /** The user-entered username/password pairs. */
    @XmlElement(name = "UsernamePassword")
    private Collection<EncryptedUsernamePassword> myEncryptedUsernamePasswords = New.collection();

    /** Map of purposes to preferred certificate fingerprints. */
    @XmlJavaTypeAdapter(JAXBableStringMap.JAXBableStringMapAdapter.class)
    @XmlElement(name = "PreferredCertificates")
    private JAXBableStringMap myPreferredCertificates = new JAXBableStringMap(New.<String, String>map());

    /** The trusted server certificates. */
    @XmlElement(name = "TrustedCertificate")
    private Collection<byte[]> myUserTrustedCertificates = New.collection();

    /** The trusted servers. */
    @XmlElement(name = "TrustedServer")
    private Collection<String> myUserTrustedServers = New.set();

    /**
     * Default constructor.
     */
    public SecurityConfiguration()
    {
    }

    /**
     * Add an encrypted private key and its certificate chain to the
     * configuration.
     *
     * @param key The private key and its certificate chain.
     */
    public void addEncryptedPrivateKeyAndCertChain(EncryptedPrivateKeyAndCertChain key)
    {
        if (!myEncryptedPrivateKeys.contains(key))
        {
            myEncryptedPrivateKeys.add(key);
        }
    }

    /**
     * Add a trusted server to the configuration.
     *
     * @param server The trusted server.
     */
    public void addTrustedServer(String server)
    {
        myUserTrustedServers.add(server);
    }

    /**
     * Add a trusted server certificate to the configuration.
     *
     * @param encoded The encoded certificate.
     */
    public void addTrustedServerCert(byte[] encoded)
    {
        for (byte[] cert : myUserTrustedCertificates)
        {
            if (Arrays.equals(cert, encoded))
            {
                return;
            }
        }
        myUserTrustedCertificates.add(encoded);
    }

    /**
     * Add username/password pair to the configuration.
     *
     * @param encryptedUsernamePassword The username/password.
     */
    public void addUsernamePassword(EncryptedUsernamePassword encryptedUsernamePassword)
    {
        if (!myEncryptedUsernamePasswords.contains(encryptedUsernamePassword))
        {
            myEncryptedUsernamePasswords.add(encryptedUsernamePassword.clone());
        }
    }

    /**
     * Clear the encrypted data in this configuration.
     */
    public void clearEncryptedData()
    {
        myEncryptedPrivateKeys.clear();
        myEncryptedUsernamePasswords.clear();
    }

    /**
     * Clear the preferred certificate fingerprints.
     */
    public void clearPreferredCertificateFingerprints()
    {
        myPreferredCertificates.clear();
    }

    @Override
    public SecurityConfiguration clone()
    {
        SecurityConfiguration clone;
        try
        {
            clone = (SecurityConfiguration)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new ExpectedCloneableException(e);
        }

        clone.myPreferredCertificates = new JAXBableStringMap(New.map(myPreferredCertificates));
        clone.myUserTrustedCertificates = New.set(myUserTrustedCertificates);
        clone.myUserTrustedServers = New.set(myUserTrustedServers);
        clone.myEncryptedPrivateKeys = New.collection(myEncryptedPrivateKeys.size());
        for (EncryptedPrivateKeyAndCertChain obj : myEncryptedPrivateKeys)
        {
            clone.myEncryptedPrivateKeys.add(obj.clone());
        }
        clone.myEncryptedUsernamePasswords = New.collection(myEncryptedUsernamePasswords.size());
        for (EncryptedUsernamePassword obj : myEncryptedUsernamePasswords)
        {
            clone.myEncryptedUsernamePasswords.add(obj.clone());
        }

        return clone;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        SecurityConfiguration other = (SecurityConfiguration)obj;
        // @formatter:off
        return EqualsHelper.equals(myCryptoConfig,               other.myCryptoConfig,
                myEncryptedPrivateKeys,       other.myEncryptedPrivateKeys,
                myEncryptedUsernamePasswords, other.myEncryptedUsernamePasswords,
                myPreferredCertificates,      other.myPreferredCertificates,
                myUserTrustedCertificates,    other.myUserTrustedCertificates,
                myUserTrustedServers,         other.myUserTrustedServers);
        // @formatter:on
    }

    /**
     * Get the configuration for local cryptography.
     *
     * @return The crypto config.
     */
    public CryptoConfig getCryptoConfig()
    {
        return myCryptoConfig;
    }

    /**
     * Get my encrypted private keys with their certificate chains.
     *
     * @return The private keys and certificate chains.
     */
    public Collection<? extends EncryptedPrivateKeyAndCertChain> getEncryptedPrivateKeyAndCertChains()
    {
        validate();
        return myEncryptedPrivateKeys;
    }

    /**
     * Get the preferred certificate fingerprint for a purpose, if it has been
     * set.
     *
     * @param purpose An arbitrary string used to allow for different aliases to
     *            be preferred for different uses.
     * @return The preferred certificate fingerprint or {@code null}.
     */
    public String getPreferredCertificateFingerprint(String purpose)
    {
        return myPreferredCertificates.get(purpose);
    }

    /**
     * Get my username/password pairs.
     *
     * @return The username/password pairs.
     */
    public Collection<? extends EncryptedUsernamePassword> getUsernamePasswords()
    {
        return myEncryptedUsernamePasswords;
    }

    /**
     * Get the user trusted server certificates.
     *
     * @return The encoded certificates.
     */
    public Collection<byte[]> getUserTrustedCerts()
    {
        return myUserTrustedCertificates;
    }

    /**
     * Get the user trusted servers.
     *
     * @return The trusted servers.
     */
    public Collection<String> getUserTrustedServers()
    {
        return myUserTrustedServers;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (myEncryptedPrivateKeys == null ? 0 : myEncryptedPrivateKeys.hashCode());
        result = prime * result + (myEncryptedUsernamePasswords == null ? 0 : myEncryptedUsernamePasswords.hashCode());
        result = prime * result + (myPreferredCertificates == null ? 0 : myPreferredCertificates.hashCode());
        result = prime * result + (myUserTrustedCertificates == null ? 0 : myUserTrustedCertificates.hashCode());
        result = prime * result + (myUserTrustedServers == null ? 0 : myUserTrustedServers.hashCode());
        result = prime * result + (myCryptoConfig == null ? 0 : myCryptoConfig.hashCode());
        return result;
    }

    /**
     * Remove a username/password pair from the configuration.
     *
     * @param purpose The purpose.
     * @return {@code true} if the username/password was removed.
     */
    public boolean removeUsernamePassword(String purpose)
    {
        for (Iterator<EncryptedUsernamePassword> iter = myEncryptedUsernamePasswords.iterator(); iter.hasNext();)
        {
            EncryptedUsernamePassword encryptedUsernamePassword = iter.next();
            if (encryptedUsernamePassword.getPurpose().equals(purpose))
            {
                iter.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Set the cryptography configuration.
     *
     * @param cryptoConfig The crypto config.
     */
    public void setCryptoConfig(CryptoConfig cryptoConfig)
    {
        myCryptoConfig = cryptoConfig;
    }

    /**
     * Set the preferred certificate fingerprint for a purpose.
     *
     * @param purpose An arbitrary string used to allow for different aliases to
     *            be preferred for different uses.
     * @param fingerprint The fingerprint of the preferred certificate.
     */
    public void setPreferredCertificateFingerprint(String purpose, String fingerprint)
    {
        Utilities.checkNull(purpose, "purpose");
        if (fingerprint == null)
        {
            myPreferredCertificates.remove(purpose);
        }
        else
        {
            myPreferredCertificates.put(purpose, fingerprint);
        }
    }

    /**
     * Check the validity of this configuration.
     */
    public void validate()
    {
        for (Iterator<EncryptedPrivateKeyAndCertChain> iter = myEncryptedPrivateKeys.iterator(); iter.hasNext();)
        {
            EncryptedPrivateKeyAndCertChain key = iter.next();
            if (!key.validate())
            {
                LOGGER.warn("Discarding invalid private key configuration: " + key);
                iter.remove();
            }
        }
    }
}
