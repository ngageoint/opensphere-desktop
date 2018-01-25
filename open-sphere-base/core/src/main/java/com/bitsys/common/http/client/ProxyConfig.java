package com.bitsys.common.http.client;

import com.bitsys.common.http.auth.Credentials;
import com.bitsys.common.http.auth.UsernamePasswordCredentials;
import com.bitsys.common.http.proxy.ProxyResolver;

/**
 * This class defines the proxy configuration options.
 */
public class ProxyConfig
{
   /** The proxy configuration resolver for this client. */
   private ProxyResolver proxyResolver;

   /** The proxy credentials. */
   private Credentials credentials;

   /**
    * Sets the proxy server resolver.
    *
    * @param proxyConfig
    *           the proxy server resolver.
    */
   public void setProxyResolver(final ProxyResolver proxyConfig)
   {
      proxyResolver = proxyConfig;
   }

   /**
    * Returns the proxy server resolver.
    *
    * @return the proxy server resolver or <code>null</code>.
    */
   public ProxyResolver getProxyResolver()
   {
      return proxyResolver;
   }

   /**
    * Sets the credentials for the proxy server.
    *
    * @param credentials
    *           the authentication credentials.
    */
   public void setCredentials(Credentials credentials)
   {
      this.credentials = credentials;
   }

   /**
    * Returns the credentials for the proxy server.
    *
    * @return the authentication credentials or <code>null</code> if none should be used.
    */
   public Credentials getCredentials()
   {
      return credentials;
   }

   /**
    * Sets the default user name and password for proxy server authentication.
    * This is a convenience method for {@link #setCredentials(Credentials)}.
    *
    * @param userName
    *           the user name.
    * @param password
    *           the password.
    */
   public void setUserName(final String userName, final char[] password) {
      setCredentials(new UsernamePasswordCredentials(userName, password));
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("ProxyConfig [Proxy Resolver=");
      builder.append(getProxyResolver());
      builder.append(", Credentials=");
      builder.append(getCredentials());
      builder.append("]");
      return builder.toString();
   }
}
