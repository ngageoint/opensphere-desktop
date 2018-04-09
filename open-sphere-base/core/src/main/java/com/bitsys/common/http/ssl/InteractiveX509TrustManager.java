package com.bitsys.common.http.ssl;

import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.bitsys.common.http.ssl.CertificateVerificationIssue.IssueType;

/**
 * This class is an X.509 trust manager that provides greater control in the
 * certificate verification process. A {@link CertificateVerifier} can be
 * provided to override the default, strict certificate verification.
 */
public class InteractiveX509TrustManager implements X509TrustManager
{
    /** The X.509 trust manager. */
    private final X509TrustManager trustManager;

    /** The certificate verifier. */
    private final CertificateVerifier verifier;

    /**
     * Constructs an {@linkplain InteractiveX509TrustManager} with the given
     * trust manager. The certificate verifier defaults to
     * {@link StrictCertificateVerifier}.
     *
     * @param trustManager the underlying trust manager.
     */
    public InteractiveX509TrustManager(final X509TrustManager trustManager)
    {
        this(trustManager, new StrictCertificateVerifier());
    }

    /**
     * Constructs an {@linkplain InteractiveX509TrustManager} with the given
     * trust manager and certificate verifier.
     *
     * @param trustManager the underlying trust manager.
     * @param verifier the certificate verifier.
     */
    public InteractiveX509TrustManager(final X509TrustManager trustManager, final CertificateVerifier verifier)
    {
        if (trustManager == null)
        {
            throw new IllegalArgumentException("The trust manager is null");
        }
        if (verifier == null)
        {
            throw new IllegalArgumentException("The certificate verifier is null");
        }
        this.trustManager = trustManager;
        this.verifier = verifier;
    }

    /**
     * Returns the X.509 trust manager.
     *
     * @return the X.509 trust manager.
     */
    public X509TrustManager getTrustManager()
    {
        return trustManager;
    }

    /**
     * Returns the certificate verifier.
     *
     * @return the certificate verifier.
     */
    public CertificateVerifier getVerifier()
    {
        return verifier;
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[],
     *      java.lang.String)
     */
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException
    {
        trustManager.checkClientTrusted(chain, authType);
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[],
     *      java.lang.String)
     */
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException
    {
        if (chain == null)
        {
            throw new CertificateException("The certificate chain is null");
        }
        if (chain.length == 0)
        {
            throw new CertificateException("The certificate chain is empty");
        }
        if (authType == null)
        {
            throw new IllegalArgumentException("The authentication type is null");
        }
        if (authType.length() == 0)
        {
            throw new IllegalArgumentException("The authentication type is empty");
        }

        // First see if the default trust manager has an issue.
        CertificateException defaultException = null;
        try
        {
            trustManager.checkServerTrusted(chain, authType);
        }
        catch (final CertificateException e)
        {
            defaultException = e;
        }

        // If the default trust manager found an issue, try to find it and any
        // other issues. Ask the verifier for guidance.
        if (defaultException != null)
        {
            final Collection<CertificateVerificationIssue> issues = new ArrayList<>();

            final X509Certificate serverCertificate = chain[0];
            final CertificateVerificationIssue issue = checkValidity(serverCertificate, chain);
            if (issue != null)
            {
                issues.add(issue);
            }

            issues.addAll(verifyChain(chain));

            // If the verifier doesn't allow the certificate, throw an
            // exception.
            if (!verifier.allowCertificate(chain, authType, issues, defaultException))
            {
                throw new CertificateException("The exception was sustained: " + defaultException.getLocalizedMessage(),
                        defaultException);
            }
        }
    }

    /**
     * Determines if the given certificate is valid. The certificate is valid if
     * the current date/time fall between the certificate's
     * <code>notBefore</code> and <code>notAfter</code> dates.
     *
     * @param certificate the certificate to check.
     * @param chain the full certificate chain. This parameter is for
     *            informational purposes.
     * @return the certificate verification issue or <code>null</code>.
     * @throws CertificateNotYetValidException if the certificate is not yet
     *             valid and verifier rejects the certificate.
     * @throws CertificateExpiredException if the certificate has expired and
     *             the verifier rejects the certificate.
     */
    protected CertificateVerificationIssue checkValidity(final X509Certificate certificate, final X509Certificate[] chain)
        throws CertificateExpiredException, CertificateNotYetValidException
    {
        CertificateVerificationIssue error = null;
        final Date now = new Date();
        if (now.before(certificate.getNotBefore()))
        {
            final String message = "The certificate '" + certificate.getSubjectDN() + "' will not be valid until "
                    + DateFormatUtils.ISO_DATETIME_FORMAT.format(certificate.getNotAfter());
            error = new CertificateVerificationIssue(IssueType.CERTIFICATE_NOT_YET_VALID, certificate, message);
        }
        else if (now.after(certificate.getNotAfter()))
        {
            final String message = "The certificate '" + certificate.getSubjectDN() + "' expired on "
                    + DateFormatUtils.ISO_DATETIME_FORMAT.format(certificate.getNotAfter());
            error = new CertificateVerificationIssue(IssueType.CERTIFICATE_EXPIRED, certificate, message);
        }
        return error;
    }

    /**
     * Analyzes the certificate chain for issues. Checks for a well-formed chain
     * as well as certificate trust. Any issues found are reported in the
     * returned collection.
     *
     * @param chain the certificate chain to verify.
     * @return the collection of issues. The result may be zero-length but never
     *         <code>null</code>.
     */
    protected Collection<? extends CertificateVerificationIssue> verifyChain(final X509Certificate[] chain)
    {
        final Collection<CertificateVerificationIssue> errors = new ArrayList<>();
        final List<X509Certificate> trustedCertificates = Arrays.asList(getAcceptedIssuers());
        final Map<X500Principal, Set<PublicKey>> trustedSubjects = createTrustedSubjects(trustedCertificates);
        CertificateVerificationIssue issue = null;
        boolean trusted = false;

        // Verify each link in the chain.
        for (int ii = 0; ii < chain.length; ii++)
        {
            final X509Certificate certificate = chain[ii];
            final X500Principal subject = certificate.getSubjectX500Principal();
            final Set<PublicKey> publicKeys = trustedSubjects.get(subject);
            if (ii > 0)
            {
                final X509Certificate lastCertificate = chain[ii - 1];
                issue = verifyLink(lastCertificate, certificate);
                if (issue != null)
                {
                    break;
                }
            }

            // Verify that the last certificate in the chain is self-signed.
            if (ii == chain.length - 1 && !certificate.getIssuerX500Principal().equals(certificate.getSubjectX500Principal()))
            {
                errors.add(new CertificateVerificationIssue(IssueType.INCOMPLETE_CERTIFICATE_CHAIN, certificate));
            }

            // If the current certificate is in the trusted certificates or the
            // certificate's public key is in the set of trusted public keys,
            // the
            // certificate chain is trusted.
            if (trustedCertificates.contains(certificate)
                    || publicKeys != null && publicKeys.contains(certificate.getPublicKey()))
            {
                trusted = true;
                break;
            }
        }

        // Report any issues found.
        if (issue != null)
        {
            errors.add(issue);
        }
        else if (!trusted)
        {
            errors.add(new CertificateVerificationIssue(IssueType.CERTIFICATE_NOT_TRUSTED, chain[0]));
        }
        return errors;
    }

    /**
     * Verifies the link between the two certificates. This method will first
     * verify that the previous certificate's issue principal matches the
     * current certificate's principal. If they match, this method
     * {@link X509Certificate#verify(PublicKey) verifies} that the current
     * certificate issues the previous certificate.
     *
     * @param previousCertificate the previous certificate in the chain.
     * @param certificate the current certificate in the chain.
     * @return the verification issue or <code>null</code> if no issue was
     *         found.
     */
    protected CertificateVerificationIssue verifyLink(final X509Certificate previousCertificate,
            final X509Certificate certificate)
    {
        CertificateVerificationIssue issue = null;

        // If the next certificate in the chain doesn't have the same
        // subject as the previous certificate's issuer, the chain is
        // invalid.
        final X500Principal subject = certificate.getSubjectX500Principal();
        final X500Principal issuerSubject = previousCertificate.getIssuerX500Principal();
        if (!subject.equals(issuerSubject))
        {
            issue = new CertificateVerificationIssue(IssueType.MALFORMED_CERTIFICATE_CHAIN, certificate,
                    "The certificate '" + previousCertificate.getSubjectX500Principal() + "' was not issued by '"
                            + certificate.getSubjectX500Principal() + "'");
        }
        else
        {
            try
            {
                previousCertificate.verify(certificate.getPublicKey());
            }
            catch (final Exception e)
            {
                issue = new CertificateVerificationIssue(IssueType.MALFORMED_CERTIFICATE_CHAIN, certificate,
                        "Failed to verify that '" + previousCertificate.getSubjectX500Principal() + "' was issued by '"
                                + certificate.getSubjectX500Principal() + "'");
            }
        }
        return issue;
    }

    /**
     * Returns the mapping of trusted certificate's subject to set of trusted
     * certificate's public keys from the given list of trusted certificates.
     *
     * @param trustedCertificates the list of trusted certificates.
     * @return the mapping of subjects to sets of public keys.
     */
    protected Map<X500Principal, Set<PublicKey>> createTrustedSubjects(final List<X509Certificate> trustedCertificates)
    {
        final Map<X500Principal, Set<PublicKey>> trustedSubjects = new HashMap<>();
        for (final X509Certificate cert : trustedCertificates)
        {
            final X500Principal subject = cert.getSubjectX500Principal();
            Set<PublicKey> publicKeys = trustedSubjects.get(subject);
            if (publicKeys == null)
            {
                publicKeys = new HashSet<>();
                trustedSubjects.put(subject, publicKeys);
            }
            publicKeys.add(cert.getPublicKey());
        }
        return trustedSubjects;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        X509Certificate[] trustedCerts = null;
        trustedCerts = trustManager.getAcceptedIssuers();
        return trustedCerts;
    }
}
