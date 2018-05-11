package com.bitsys.common.http.ssl;

import java.security.cert.X509Certificate;

/**
 * This interface describes a certificate verification error.
 */
public class CertificateVerificationIssue
{
    /**
     * Indicates the severity of an issue.
     */
    public enum SeverityType
    {
        INFORMATION, WARNING, ERROR
    }

    /**
     * This enumeration describes the different type of certificate verification
     * issues that may occur. Issues have a {@link SeverityType severity}
     * associated with them.
     */
    public enum IssueType
    {
        /**
         * Indicates that the certificate is not yet valid meaning that its
         * <code>Validity</code> period is in the future.
         */
        CERTIFICATE_NOT_YET_VALID(SeverityType.ERROR, "The certificate is not yet valid"),

        /**
         * Indicates that the certificate has expired meaning that its
         * <code>Validity</code> period is in the past.
         */
        CERTIFICATE_EXPIRED(SeverityType.ERROR, "The certificate has expired"),

        /**
         * Indicates that the certificate chain is malformed. A well-formed
         * certificate chain starts with the lowest level of issued certificate
         * and continues with each subsequent certificate being the issuer of
         * the certificate before it and finally ending it a self-signed
         * certificate.
         */
        MALFORMED_CERTIFICATE_CHAIN(SeverityType.WARNING, "The certificate chain is malformed"),

        /**
         * Indicates that certificate chain is incomplete. It could be that only
         * the first certificate in the chain is present or the chain does not
         * terminate with a self-signed certificate. The certificate may still
         * be trusted but this indicates an improperly configured server.
         */
        INCOMPLETE_CERTIFICATE_CHAIN(SeverityType.WARNING,
                "The certificate is not trusted because no issuer chain was provided."),
        /**
         * Indicates that a received certificate is either not in the trust
         * store or, by following the certificate chain, not an issued ancestor
         * of a certificate in the trust store.
         */
        CERTIFICATE_NOT_TRUSTED(SeverityType.ERROR, "The certificate is not trusted");

        /** The severity. */
        private final SeverityType severity;

        /** The default message. */
        private final String defaultMessage;

        /**
         * Constructs a new {@linkplain IssueType} with the given severity.
         *
         * @param severity the severity.
         * @param defaultMessage the default message.
         */
        IssueType(final SeverityType severity, final String defaultMessage)
        {
            this.severity = severity;
            this.defaultMessage = defaultMessage;
        }

        /**
         * Returns the issue's severity.
         *
         * @return the issue's severity.
         */
        public SeverityType getSeverity()
        {
            return severity;
        }

        /**
         * Returns the default message.
         *
         * @return the default message.
         */
        public String getDefaultMessage()
        {
            return defaultMessage;
        }
    }

    /** The issue type. */
    private final IssueType issueType;

    /** The certificate. */
    private final X509Certificate certificate;

    /** The verification issue. */
    private final String message;

    /**
     * Constructs a new {@linkplain CertificateVerificationIssue} with the given
     * issue type, certificate and message.
     *
     * @param issueType the issue type.
     * @param certificate the certificate with a verification issue.
     */
    public CertificateVerificationIssue(final IssueType issueType, final X509Certificate certificate)
    {
        this(issueType, certificate, issueType != null ? issueType.getDefaultMessage() : null);
    }

    /**
     * Constructs a new {@linkplain CertificateVerificationIssue} with the given
     * issue type, certificate and message.
     *
     * @param issueType the issue type.
     * @param certificate the certificate with a verification issue.
     * @param message the message.
     */
    public CertificateVerificationIssue(final IssueType issueType, final X509Certificate certificate, final String message)
    {
        if (issueType == null)
        {
            throw new IllegalArgumentException("The issue type is null");
        }
        if (certificate == null)
        {
            throw new IllegalArgumentException("The certificate is null");
        }
        if (message == null)
        {
            throw new IllegalArgumentException("The message is null");
        }
        this.issueType = issueType;
        this.certificate = certificate;
        this.message = message;
        // == null ? issueType.getDefaultMessage() : message;
    }

    /**
     * Returns the issue type.
     *
     * @return the issue type.
     */
    public IssueType getIssueType()
    {
        return issueType;
    }

    /**
     * Returns the X.509 certificate with which there is a problem.
     *
     * @return the X.509 certificate with an error.
     */
    public X509Certificate getCertificate()
    {
        return certificate;
    }

    /**
     * Returns the verification message.
     *
     * @return the verification message.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("CertificateVerificationIssue [issueType=");
        builder.append(issueType);
        builder.append(", certificate=");
        builder.append(certificate);
        builder.append(", message=");
        builder.append(message);
        builder.append("]");
        return builder.toString();
    }
}
