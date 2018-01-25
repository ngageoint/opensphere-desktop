package com.bitsys.common.http.auth;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;

/**
 * Implementations of this interface have the ability to specifically clear
 * entries from their credential's store.
 */
public interface ClearableCredentialsProvider extends CredentialsProvider
{
   /**
    * Clears credentials that exactly match the given authentication scope.
    *
    * @param authScope
    *           the authentication scope.
    */
   void clearCredentials(AuthScope authScope);

   /**
    * Sets the {@link Credentials credentials} for the given authentication
    * scope. Any previous credentials for the given scope will be overwritten.
    * If the credentials are <code>null</code>, the credentials will be removed
    * from this provider.
    *
    * @see org.apache.http.client.CredentialsProvider#setCredentials(org.apache.http.auth.AuthScope,
    *      org.apache.http.auth.Credentials)
    */
   @Override
   void setCredentials(AuthScope authScope, Credentials credentials);
}
