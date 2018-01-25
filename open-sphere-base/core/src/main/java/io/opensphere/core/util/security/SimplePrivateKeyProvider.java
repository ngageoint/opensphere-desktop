package io.opensphere.core.util.security;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import io.opensphere.core.util.collections.New;

/**
 * A private key provider that simply stores the relevant information.
 */
public abstract class SimplePrivateKeyProvider extends AbstractPrivateKeyProvider
{
    /** The chain of certificates associated with the private key. */
    private List<? extends X509Certificate> myCertificateChain;

    /**
     * Constructor.
     *
     * @param alias The alias for the private key.
     * @param certificateChain The certificate chain associated with the private
     *            key.
     * @param source The source of the key.
     */
    public SimplePrivateKeyProvider(String alias, List<? extends X509Certificate> certificateChain, String source)
    {
        super(alias, source);
        myCertificateChain = New.unmodifiableList(certificateChain);
    }

    @Override
    public List<? extends X509Certificate> getCertificateChain()
    {
        return myCertificateChain;
    }

    /**
     * Set the certificate chain associated with the private key.
     *
     * @param chain The certificate chain.
     */
    public void setCertificateChain(Collection<? extends X509Certificate> chain)
    {
        myCertificateChain = New.unmodifiableList(chain);
    }
}
