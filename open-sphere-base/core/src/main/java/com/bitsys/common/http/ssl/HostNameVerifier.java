package com.bitsys.common.http.ssl;

/**
 * This interface defines the method(s) to aid in SSL host name verification.
 */
public interface HostNameVerifier
{
   /**
    * Called when a host name does not match its certificate.
    *
    * @param host
    *           the host name.
    * @param cns
    *           CN fields, in order, as extracted from the X.509 certificate.
    * @param subjectAlts
    *           Subject-Alt fields of type 2 ("DNS"), as extracted from the
    *           X.509 certificate.
    * @param reason
    *           a message indicating the reason why the host name is invalid.
    * @return <code>true</code> if the invalid host name should be allowed.
    *         Otherwise, returns <code>false</code>.
    */
   boolean allowInvalidHostName(final String host, final String[] cns,
                                final String[] subjectAlts, final String reason);
}
