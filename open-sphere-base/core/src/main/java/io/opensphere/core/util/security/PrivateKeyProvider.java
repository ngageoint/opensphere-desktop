package io.opensphere.core.util.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

/**
 * A provider for a private key and its certificate chain.
 */
public interface PrivateKeyProvider
{
    /** Comparator that compares providers by their aliases. */
    Comparator<PrivateKeyProvider> ALIAS_COMPARATOR = (o1, o2) -> o1.getAlias().compareTo(o2.getAlias());

    /**
     * Get the alias associated with this key.
     *
     * @return The alias.
     */
    String getAlias();

    /**
     * Get the X.509 certificate chain associated with the private key.
     *
     * @return The certificate chain.
     * @throws PrivateKeyProviderException If the certificate chain cannot be
     *             retrieved.
     */
    List<? extends X509Certificate> getCertificateChain() throws PrivateKeyProviderException;

    /**
     * Get a string that describes the details of this private key provider.
     *
     * @return The detail string.
     */
    String getDetailString();

    /**
     * Get the digest for the first X.509 certificate in the certificate chain.
     *
     * @param algorithm The digest algorithm to use.
     * @return The digest.
     * @throws PrivateKeyProviderException If the certificate cannot be
     *             retrieved.
     * @throws NoSuchAlgorithmException If the digest algorithm is not
     *             supported.
     * @throws CertificateEncodingException If the certificate cannot be
     *             encoded.
     */
    Digest getDigest(String algorithm) throws PrivateKeyProviderException, CertificateEncodingException, NoSuchAlgorithmException;

    /**
     * Get the fingerprint of the first certificate in the X.509 certificate
     * chain.
     *
     * @param algorithm The type of digest to use, for
     *            {@link MessageDigest#getInstance(String)}.
     * @return The fingerprint.
     * @throws PrivateKeyProviderException If the certificate cannot be
     *             retrieved.
     * @throws CertificateEncodingException If the certificate cannot be
     *             encoded.
     * @throws NoSuchAlgorithmException If the digest algorithm is not
     *             supported.
     * @see MessageDigest#getInstance(String)
     */
    String getFingerprint(String algorithm)
            throws PrivateKeyProviderException, CertificateEncodingException, NoSuchAlgorithmException;

    /**
     * Get the private key.
     *
     * @return The private key.
     * @throws PrivateKeyProviderException If the private key cannot be
     *             retrieved.
     */
    PrivateKey getPrivateKey() throws PrivateKeyProviderException;

    /**
     * Get the serial number of the first certificate in the X.509 certificate
     * chain.
     *
     * @return The serial number.
     * @throws PrivateKeyProviderException If the certificate cannot be
     *             retrieved.
     * @see MessageDigest#getInstance(String)
     */
    String getSerialNumber() throws PrivateKeyProviderException;

    /**
     * Get a string representation of the source of the private key.
     *
     * @return The source string.
     */
    String getSource();

    /**
     * Determine if the digest for the first X.509 certificate in the
     * certificate chain matches the given digest. If {@code null} is passed for
     * the digest, {@code true} is returned. If any errors occur, {@code false}
     * will be returned.
     *
     * @param digest The digest, or {@code null}.
     * @return {@code true} iff the digests match.
     */
    boolean hasDigest(Digest digest);

    /**
     * Get if this key has any of the specified key types.
     *
     * @param keyTypes The key types.
     * @return {@code true} if the key type matches.
     * @throws PrivateKeyProviderException If the provider is uninitialized.
     */
    boolean hasKeyType(Set<? extends String> keyTypes) throws PrivateKeyProviderException;

    /**
     * Get if the key has the required key usage.
     *
     * @param keyUsage The key usage.
     * @return {@code true} if the key has the required usage array.
     * @throws PrivateKeyProviderException If the provider is uninitialized.
     */
    boolean hasKeyUsage(KeyUsage keyUsage) throws PrivateKeyProviderException;

    /**
     * Get if this private key was issued by one of the provided
     * {@link Principal}s.
     *
     * @param acceptableIssuers The acceptable principals.
     * @return {@code true} if this key is acceptable.
     * @throws PrivateKeyProviderException If the provider is uninitialized.
     */
    boolean isAcceptable(Collection<? extends X500Principal> acceptableIssuers) throws PrivateKeyProviderException;

    /**
     * Get if this private key has one of the specified key types and was issued
     * by one of the provided {@link Principal}s.
     *
     * @param keyTypes The key types.
     * @param acceptableIssuers The acceptable principals.
     * @return {@code true} if this key is acceptable.
     * @throws PrivateKeyProviderException If the provider is uninitialized.
     */
    boolean isAcceptable(Set<? extends String> keyTypes, Collection<? extends X500Principal> acceptableIssuers)
            throws PrivateKeyProviderException;

    /**
     * Get if the certificate chain is current.
     *
     * @return {@code true} if the certificate chain is current.
     * @throws PrivateKeyProviderException If a certificate cannot be retrieved.
     */
    boolean isCurrent() throws PrivateKeyProviderException;
}
