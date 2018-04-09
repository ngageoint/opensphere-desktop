package com.bitsys.common.http.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * This class is a certificate verifier that rejects any certificate with trust
 * issues.
 */
public final class StrictCertificateVerifier implements CertificateVerifier
{
    @Override
    public boolean allowCertificate(final X509Certificate[] chain, final String authType,
            final Collection<CertificateVerificationIssue> issues, final CertificateException certificateException)
    {
        return false;
    }
}
