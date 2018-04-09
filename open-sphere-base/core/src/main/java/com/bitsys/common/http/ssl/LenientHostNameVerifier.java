package com.bitsys.common.http.ssl;

/**
 * This class is a {@link HostNameVerifier} that allows any invalid host name.
 */
public final class LenientHostNameVerifier implements HostNameVerifier
{
    @Override
    public boolean allowInvalidHostName(final String host, final String[] cns, final String[] subjectAlts, final String reason)
    {
        return true;
    }
}
