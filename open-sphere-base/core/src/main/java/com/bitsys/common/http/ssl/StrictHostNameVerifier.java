package com.bitsys.common.http.ssl;

/**
 * This class is a {@link HostNameVerifier} that rejects any invalid host name.
 */
public final class StrictHostNameVerifier implements HostNameVerifier
{
   @Override
   public boolean allowInvalidHostName(final String host, final String[] cns,
                                       final String[] subjectAlts, final String reason)
   {
      return false;
   }
}
