package com.bitsys.common.http.ssl;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import com.bitsys.common.http.util.cache.CacheManager;

/**
 * This class is a host name verifier that caches the result returned from a
 * delegated host name verifier. Subsequent requests of the same type will be
 * returned from a cache rather than asking the delegated host name verifier.
 */
public class CachingHostNameVerifier implements HostNameVerifier
{
   /**
    * This class contains all of the parameters needed to identify an entry in
    * the cache.
    */
   private static class CacheKey
   {
      /** The host name. */
      private final String host;

      /** The Common Names from the X.509 certificate. */
      private final String[] cns;

      /** The Subject Alternate Names. */
      private final String[] subjectAlts;

      /**
       * Constructs a new {@link CacheKey} from the given parameters.
       *
       * @param host
       *           the host name.
       * @param cns
       *           the Common Names from the X.509 certificate.
       * @param subjectAlts
       *           the Subject Alternative Names.
       */
      public CacheKey(final String host, final String[] cns, final String[] subjectAlts)
      {
         this.host = host;
         this.cns = ArrayUtils.clone(cns);
         this.subjectAlts = ArrayUtils.clone(subjectAlts);
      }

      /**
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + Objects.hashCode(host);
         result = prime * result + Arrays.hashCode(cns);
         result = prime * result + Arrays.hashCode(subjectAlts);
         return result;
      }

      /**
       * @see java.lang.Object#equals(java.lang.Object)
       */
      @Override
      public boolean equals(final Object obj)
      {
         if (this == obj)
         {
            return true;
         }
         if (obj == null)
         {
            return false;
         }
         if (getClass() != obj.getClass())
         {
            return false;
         }
         final CacheKey other = (CacheKey)obj;
         if (host == null)
         {
            if (other.host != null)
            {
               return false;
            }
         }
         else if (!host.equals(other.host))
         {
            return false;
         }
         if (!Arrays.equals(cns, other.cns))
         {
            return false;
         }
         if (!Arrays.equals(subjectAlts, other.subjectAlts))
         {
            return false;
         }
         return true;
      }
   }

   /** The cached host name verifier. */
   private final HostNameVerifier verifier;

   /** The cache manager. */
   private final CacheManager cacheManager = new CacheManager();

   /**
    * Constructs a new {@link CachingHostNameVerifier} with the given
    * {@link HostNameVerifier}.
    *
    * @param verifier
    *           the verifier to cache.
    */
   public CachingHostNameVerifier(final HostNameVerifier verifier)
   {
      if (verifier == null)
      {
         throw new IllegalArgumentException("The host name verifier is null");
      }
      this.verifier = verifier;
   }

   /**
    * @see com.bitsys.common.http.ssl.HostNameVerifier#allowInvalidHostName(java.lang.String,
    *      java.lang.String[], java.lang.String[], java.lang.String)
    */
   @Override
   public boolean allowInvalidHostName(final String host, final String[] cns,
                                       final String[] subjectAlts, final String reason)
   {
      final boolean result;
      final CacheKey key = new CacheKey(host, cns, subjectAlts);
      final Boolean cachedResult = cacheManager.get(key);
      if (cachedResult != null)
      {
         result = cachedResult;
      }
      else
      {
         result = verifier.allowInvalidHostName(host, cns, subjectAlts, reason);
         cacheManager.put(key, result);
      }

      return result;
   }

   /**
    * Clears any cached items since the given date. Data before this date will
    * be preserved. If the parameter is <code>null</code> then the entire cache
    * should be cleared.
    *
    * @param since
    *           the starting date when the cache will be cleared.
    */
   public void clearCache(final Date since)
   {
      cacheManager.clearCache(since);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("CachingHostNameVerifier [Verifier=");
      builder.append(verifier);
      builder.append("]");
      return builder.toString();
   }
}
