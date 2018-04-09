package com.bitsys.common.http.ssl;

import javax.net.ssl.SSLException;

import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * This class delegates calls to Apache's <code>X509HostnameVerifier</code>. If
 * the verifier raises a verification exception, the judge (i.e.
 * {@link HostNameVerifier}) has the ability to overrule the verification
 * exception.
 */
public class X509HostNameVerifierCourtRoom extends AbstractVerifier
{
    /** The delegated X509 host name verifier. */
    private final X509HostnameVerifier verifier;

    /** The host name verifier that can overrule the delegated verifier. */
    private final HostNameVerifier judge;

    /**
     * Constructs a <code>X509HostNameVerifierDelegate</code>.
     *
     * @param verifier the delegated host name verifier.
     */
    public X509HostNameVerifierCourtRoom(final X509HostnameVerifier verifier, final HostNameVerifier judge)
    {
        if (verifier == null)
        {
            throw new IllegalArgumentException("The Apache X509 host name verifier is null");
        }
        this.verifier = verifier;
        if (judge == null)
        {
            throw new IllegalArgumentException("The host name verifier is null");
        }
        this.judge = judge;
    }

    @Override
    public void verify(final String host, final String[] cns, final String[] subjectAlts) throws SSLException
    {
        try
        {
            verifier.verify(host, cns, subjectAlts);
        }
        catch (final SSLException e)
        {
            final boolean allow = judge.allowInvalidHostName(host, cns, subjectAlts, e.getMessage());

            // If the judge sustained the objection, throw the exception.
            if (!allow)
            {
                throw e;
            }
        }
    }
}
