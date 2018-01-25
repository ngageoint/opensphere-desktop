/**
 *
 */
package io.opensphere.core.common.connection;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * This class encapsulates certificate configuration for SSL connections.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificateConfiguration implements Cloneable
{
    /**
     * The type of certificate.
     */
    public enum CertificateType
    {
        /** PKCS12. */
        PKCS12("PKCS12"),

        /** JKS. */
        JKS("JKS");

        /**
         * The Java KeyStore type.
         */
        private String keystoreType;

        /**
         * Constructor.
         *
         * @param keystoreType the Java KeyStore type.
         */
        private CertificateType(String keystoreType)
        {
            this.keystoreType = keystoreType;
        }

        /**
         * Returns the Java KeyStore type.
         *
         * @return the Java KeyStore type.
         */
        public String getKeyStoreType()
        {
            return keystoreType;
        }
    }

    /**
     * Indicates if the certificate configuration should be used.
     */
    @XmlAttribute(name = "useCertificate", required = true)
    private boolean useCertificate;

    /**
     * The certificate filename.
     */
    @XmlElement(name = "File")
    private String filename;

    /**
     * The certificate password.
     */
    @XmlElement(name = "Password")
    private ProtectedPassword password;

    /**
     * The trust store. If null the object will be used.
     */
    @XmlTransient
    private URL trustStore = null;

    /**
     * The trust store password. If null the system one will be used.
     */
    @XmlTransient
    private String trustStorePassword = null;

    /**
     * The Distinguished name attained from the certificate once it's loaded.
     */
    @XmlTransient
    private String distinguishedName = null;

    /**
     * The key managers determine which private key to present for client
     * authentication. May be {@code null} if no client authentication is
     * necessary.
     */
    @XmlTransient
    private List<? extends KeyManager> keyManagers = null;

    /**
     * The trust managers decide if a server is trusted.
     */
    @XmlTransient
    private List<? extends TrustManager> trustManagers = null;

    /**
     * Default constructor
     */
    public CertificateConfiguration()
    {
    }

    /**
     * Constructor
     *
     * @param UseCertificate
     * @param Filename
     * @param Password
     * @param TrustStore
     * @param TrustStorePassword
     */
    public CertificateConfiguration(boolean UseCertificate, String Filename, ProtectedPassword Password, URL TrustStore,
            String TrustStorePassword)
    {
        useCertificate = UseCertificate;
        filename = Filename;
        password = Password;
        trustStore = TrustStore;
        trustStorePassword = TrustStorePassword;
    }

    /**
     * Sets this {@link CertificateConfiguration} equal to another
     *
     * @param other
     */
    public void setEqualTo(CertificateConfiguration other)
    {
        useCertificate = other.useCertificate;
        filename = other.filename;
        if (other.password != null)
        {
            password = new ProtectedPassword();
            password.setPassword(other.getPassword().getPassword());
            password.setEncryptedPassword(other.getPassword().getEncryptedPassword());
        }
        if (other.trustStore != null)
        {
            try
            {
                trustStore = new URL(other.trustStore.toExternalForm());
            }
            catch (MalformedURLException e)
            {
                // Eat it
            }
        }
        trustStorePassword = other.trustStorePassword;
    }

    /**
     * Indicates if the certificate configuration should be used.
     *
     * @return indicates if the certificate configuration should be used.
     */
    public boolean isUseCertificate()
    {
        return useCertificate;
    }

    /**
     * Sets the use certificate flag indicating if the certificate configuration
     * should be used.
     *
     * @param useCertificate indicates if the certificate configuration should
     *            be used.
     */
    public void setUseCertificate(boolean useCertificate)
    {
        this.useCertificate = useCertificate;
    }

    /**
     * Returns the filename.
     *
     * @return the filename.
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Sets the filename.
     *
     * @param filename the filename to set.
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    /**
     * Returns the password.
     *
     * @return the password.
     */
    public ProtectedPassword getPassword()
    {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the password to set.
     */
    public void setPassword(ProtectedPassword password)
    {
        this.password = password;
    }

    /**
     * Gets the trust store.
     *
     * @return the trust store.
     */
    public URL getTrustStore()
    {
        return trustStore;
    }

    /**
     * Sets the trust store.
     *
     * @param trustStore the trust store to set.
     */
    public void setTrustStore(URL trustStore)
    {
        this.trustStore = trustStore;
    }

    /**
     * Gets the distinguished name
     *
     * @return the distinguished name.
     */
    public String getDistinguishedName()
    {
        return distinguishedName;
    }

    /**
     * Sets the distinguished name
     *
     * @param distinguishedName the distinguished name to set.
     */
    public void setDistinguishedName(String distinguishedName)
    {
        this.distinguishedName = distinguishedName;
    }

    /**
     * Gets the trust store password.
     *
     * @return the trust store password.
     */
    public String getTrustStorePassword()
    {
        return trustStorePassword;
    }

    /**
     * Sets the trust store password.
     *
     * @param trustStorePassword the trust store password to set.
     */
    public void setTrustStorePassword(String trustStorePassword)
    {
        this.trustStorePassword = trustStorePassword;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Certificate Configuration:\n");
        sb.append("Use certificate?: " + useCertificate + "\n");
        sb.append("File: " + filename + "\n");
        if (password != null)
        {
            sb.append("Encrypted password: " + password.getEncryptedPassword() + "\n");
            sb.append("Unencrypted password: " + new String(password.getPassword()) + "\n");
        }
        return sb.toString();
    }

    @Override
    public CertificateConfiguration clone() throws CloneNotSupportedException
    {
        CertificateConfiguration clone = (CertificateConfiguration)super.clone();
        if (password != null)
        {
            clone.password = password.clone();
        }
        return clone;
    }

    public List<? extends KeyManager> getKeyManagers()
    {
        return keyManagers;
    }

    public void setKeyManagers(List<? extends KeyManager> keyManagers)
    {
        this.keyManagers = keyManagers;
    }

    public List<? extends TrustManager> getTrustManagers()
    {
        return trustManagers;
    }

    public void setTrustManagers(List<? extends TrustManager> trustManagers)
    {
        this.trustManagers = trustManagers;
    }

}
