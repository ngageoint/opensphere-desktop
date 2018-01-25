package com.bitsys.common.http.ssl;

import java.net.Socket;
import java.util.Collection;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509KeyManager;

/**
 * This class provides the default implementation for a
 * {@link ClientCertificateSelector}. This class will use the first available
 * alias when asked for an answer. This mimics the default behavior in Java.
 */
public class DefaultClientCertificateSelector implements ClientCertificateSelector
{
   /**
    * @see com.bitsys.common.http.ssl.ClientCertificateSelector#chooseClientAlias(java.util.Collection,
    *      java.net.Socket, javax.net.ssl.X509KeyManager)
    */
   @Override
   public String chooseClientAlias(final Collection<String> aliases, final Socket socket,
                                   final X509KeyManager keyManager)
   {
      return aliases.isEmpty() ? null : aliases.iterator().next();
   }

   /**
    * @see com.bitsys.common.http.ssl.ClientCertificateSelector#chooseClientAlias(java.util.Collection,
    *      javax.net.ssl.SSLEngine, javax.net.ssl.X509KeyManager)
    */
   @Override
   public String chooseClientAlias(final Collection<String> aliases, final SSLEngine engine,
                                   final X509KeyManager keyManager)
   {
      return aliases.isEmpty() ? null : aliases.iterator().next();
   }
}
