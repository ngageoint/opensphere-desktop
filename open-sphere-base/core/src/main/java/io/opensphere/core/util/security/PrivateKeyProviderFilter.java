package io.opensphere.core.util.security;

import java.util.Collection;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import io.opensphere.core.util.collections.New;

/**
 * A filter for private key providers.
 */
public class PrivateKeyProviderFilter
{
    /** The acceptable issuers. */
    private final Collection<? extends X500Principal> myAcceptableIssuers;

    /** Indicates if the provider must be current. */
    private final Boolean myCurrent;

    /** The digest for the private key certificate. */
    private final Digest myDigest;

    /** Indicates if the provider must be encrypted or not. */
    private final Boolean myEncrypted;

    /** The possible key types. */
    private final Set<? extends String> myKeyTypes;

    /** The required key usage. */
    private final KeyUsage myKeyUsage;

    /**
     * Construct the filter. Any parameters may be {@code null} to indicate no
     * filter.
     *
     * @param digest The certificate digest.
     * @param keyTypes The possible key types.
     * @param acceptableIssuers The acceptable issuers.
     * @param encrypted If the provider must be encrypted or not.
     * @param current If the provider must be current or not.
     * @param keyUsage The required key usage array..
     */
    public PrivateKeyProviderFilter(Digest digest, Set<? extends String> keyTypes,
            Collection<? extends X500Principal> acceptableIssuers, Boolean encrypted, Boolean current, KeyUsage keyUsage)
    {
        myDigest = digest;
        myKeyTypes = New.unmodifiableSet(keyTypes);
        myAcceptableIssuers = New.unmodifiableCollection(acceptableIssuers);
        myEncrypted = encrypted;
        myCurrent = current;
        myKeyUsage = keyUsage;
    }

    /**
     * Gets the acceptable issuers.
     *
     * @return the acceptable issuers
     */
    public Collection<? extends X500Principal> getAcceptableIssuers()
    {
        return myAcceptableIssuers;
    }

    /**
     * Get a message that describes why the provider does not satisfy this
     * filter.
     *
     * @param provider The private key provider.
     * @return The error message.
     * @throws PrivateKeyProviderException If the certificates cannot be
     *             retrieved from the provider.
     */
    public String getErrorMessage(PrivateKeyProvider provider) throws PrivateKeyProviderException
    {
        if (myCurrent != null && myCurrent.booleanValue() != provider.isCurrent())
        {
            if (myCurrent.booleanValue())
            {
                return "That certificate is not current.";
            }
            return "That certificate is current.";
        }
        else if (!provider.hasKeyType(myKeyTypes))
        {
            return "That certificate has the wrong key type.";
        }
        else if (!provider.isAcceptable(myAcceptableIssuers))
        {
            return "That certificate was not issued by an authority recognized by this server.";
        }
        else if (!provider.hasKeyUsage(myKeyUsage))
        {
            if (myKeyUsage.allowsKeyOrDataEncipherment())
            {
                return "That certificate is not approved for encipherment.";
            }
            else if (myKeyUsage.allowsDigitalSignature())
            {
                return "That certificate is not approved for signing.";
            }
            else
            {
                return "That certificate is not approved for this KeyUsage: " + myKeyUsage;
            }
        }
        else if (myEncrypted != null && myEncrypted.booleanValue() != provider instanceof EncryptedPrivateKeyProvider)
        {
            if (myEncrypted.booleanValue())
            {
                return "The certificate must be encrypted, and that one is not.";
            }
            return "The certificate cannot be encrypted, but that one is.";
        }
        else
        {
            return "";
        }
    }

    /**
     * Determine if this filter is satisfied by a provider.
     *
     * @param provider The private key provider.
     * @return {@code true} if the filter is satisfied.
     * @throws PrivateKeyProviderException If the certificates cannot be
     *             retrieved from the provider.
     */
    public boolean isSatisfied(PrivateKeyProvider provider) throws PrivateKeyProviderException
    {
        if (myEncrypted != null && myEncrypted.booleanValue() != provider instanceof EncryptedPrivateKeyProvider)
        {
            return false;
        }
        if (myCurrent != null && myCurrent.booleanValue() != provider.isCurrent())
        {
            return false;
        }
        return provider.hasDigest(myDigest) && provider.isAcceptable(myKeyTypes, myAcceptableIssuers)
                && provider.hasKeyUsage(myKeyUsage);
    }
}
