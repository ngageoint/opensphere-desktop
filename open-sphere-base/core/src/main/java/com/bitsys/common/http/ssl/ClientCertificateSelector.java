package com.bitsys.common.http.ssl;

import java.net.Socket;
import java.security.Principal;
import java.util.Collection;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

/**
 * This interface defines methods that can be used to override the default
 * {@link X509KeyManager} certificate selection.
 */
public interface ClientCertificateSelector
{
   /**
    * Selects a certificate alias to use for the given {@link Socket}.
    *
    * @param aliases
    *           the allowable certificate aliases order with the most-preferred
    *           alias first.
    * @param socket
    *           the <code>Socket</code> for which the certificate is needed.
    * @param keyManager
    *           the manager of client certificates.
    * @return the chosen alias or <code>null</code> if none were provided or
    *         none could be selected.
    * @see X509KeyManager#chooseClientAlias(String[], Principal[], Socket)
    */
   String chooseClientAlias(Collection<String> aliases, Socket socket,
                            X509KeyManager keyManager);

   /**
    * Selects a certificate alias to use for the given {@link SSLEngine}.
    *
    * @param aliases
    *           the allowable certificate aliases order with the most-preferred
    *           alias first.
    * @param engine
    *           the <code>SSLEngine</code> for which the certificate is needed.
    * @param keyManager
    *           the manager of client certificates.
    * @return the chosen alias or <code>null</code> if none were provided or
    *         none could be selected.
    * @see X509ExtendedKeyManager#chooseEngineClientAlias(String[], Principal[],
    *      SSLEngine)
    */
   String chooseClientAlias(Collection<String> aliases, SSLEngine engine,
                            X509KeyManager keyManager);
}
