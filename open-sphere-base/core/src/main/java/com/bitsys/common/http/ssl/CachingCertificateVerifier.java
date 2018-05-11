package com.bitsys.common.http.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import com.bitsys.common.http.util.cache.CacheManager;

/**
 * This class is a certificate verifier that caches the result returned from a
 * delegated certificate verifier. Subsequent requests of the same type will be
 * returned from a cache rather than asking the delegated certificate verifier.
 */
public class CachingCertificateVerifier implements CertificateVerifier
{
    /**
     * This class contains all of the parameters to the
     * {@link CachingCertificateVerifier#allowCertificate(X509Certificate[], String, Collection, CertificateException)}
     * method.
     */
    private static class CacheKey
    {
        /** The certificate chain. */
        private final X509Certificate[] chain;

        /** The key exchange algorithm used. */
        private final String authType;

        /** The collection of verification issues. */
        private final Collection<CertificateVerificationIssue> issues;

        /**
         * The <code>CertificateException</code> thrown by the default trust
         * manager.
         */
        private final CertificateException certificateException;

        /**
         * Constructs a new {@linkplain CacheKey} from the given parameters.
         *
         * @param chain the certificate chain.
         * @param authType the key exchange algorithm used
         * @param issues the collection of verification issues.
         * @param certificateException the <code>CertificateException</code>
         *            thrown by the default trust manager.
         */
        public CacheKey(final X509Certificate[] chain, final String authType,
                final Collection<CertificateVerificationIssue> issues, final CertificateException certificateException)
        {
            this.chain = chain.clone();
            this.authType = authType;
            this.issues = Collections.unmodifiableCollection(issues);
            this.certificateException = certificateException;
        }

        /**
         * Returns the <code>CertificateException</code> thrown by the default
         * trust manager.
         *
         * @return the exception thrown by the default trust manager.
         */
        public CertificateException getCertificateException()
        {
            return certificateException;
        }

        /**
         * Returns the collection of verification issues.
         *
         * @return the collection of verification issues.
         */
        public Collection<CertificateVerificationIssue> getIssues()
        {
            return issues;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (authType == null ? 0 : authType.hashCode());
            result = prime * result + Arrays.hashCode(chain);
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
            if (authType == null)
            {
                if (other.authType != null)
                {
                    return false;
                }
            }
            else if (!authType.equals(other.authType))
            {
                return false;
            }
            if (!Arrays.equals(chain, other.chain))
            {
                return false;
            }
            return true;
        }
    }

    /** The delegated certificate verifier to be cached. */
    private final CertificateVerifier verifier;

    /** The cache manager. */
    private final CacheManager cacheManager = new CacheManager();

    /**
     * Constructs a new {@linkplain CachingCertificateVerifier} using the given
     * {@link CertificateVerifier}.
     *
     * @param verifier the delegated certificate verifier.
     */
    public CachingCertificateVerifier(final CertificateVerifier verifier)
    {
        if (verifier == null)
        {
            throw new IllegalArgumentException("The certificate verifier is null");
        }
        this.verifier = verifier;
    }

    /**
     *
     * @see CertificateVerifier#allowCertificate(X509Certificate[], String,
     *      Collection, CertificateException)
     */
    @Override
    public boolean allowCertificate(final X509Certificate[] chain, final String authType,
            final Collection<CertificateVerificationIssue> issues, final CertificateException certificateException)
    {
        final boolean result;
        final CacheKey key = new CacheKey(chain, authType, issues, certificateException);
        final Boolean cachedResult = cacheManager.get(key);
        if (cachedResult != null)
        {
            result = cachedResult.booleanValue();
        }
        else
        {
            result = verifier.allowCertificate(chain, authType, issues, certificateException);
            cacheManager.put(key, Boolean.valueOf(result));
        }
        return result;
    }

    /**
     * Clears any cached items since the given date. Data before this date will
     * be preserved. If the parameter is <code>null</code> then the entire cache
     * should be cleared.
     *
     * @param since the starting date when the cache will be cleared.
     */
    public void clearCache(final Date since)
    {
        cacheManager.clearCache(since);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("CachingCertificateVerifier [Verifier=");
        builder.append(verifier);
        builder.append("]");
        return builder.toString();
    }
}
