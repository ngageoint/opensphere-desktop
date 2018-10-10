package com.bitsys.common.http.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

/**
 * This class decorates an {@link X509ExtendedKeyManager}.
 */
public class X509ExtendedKeyManagerDecorator extends X509ExtendedKeyManager
{
    /** The decorated X509ExtendedKeyManager. */
    private final X509ExtendedKeyManager keyManager;

    /**
     * Constructs a new instance with the given key manager.
     *
     * @param keyManager the decorated key manager.
     */
    public X509ExtendedKeyManagerDecorator(final X509ExtendedKeyManager keyManager)
    {
        if (keyManager == null)
        {
            throw new IllegalArgumentException("The X509KeyManager instance is null");
        }
        this.keyManager = keyManager;
    }

    /**
     * Returns the decorated key manager.
     *
     * @return the decorated key manager.
     */
    public X509ExtendedKeyManager getKeyManager()
    {
        return keyManager;
    }

    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers)
    {
        return keyManager.getClientAliases(keyType, issuers);
    }

    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket)
    {
        return keyManager.chooseClientAlias(keyTypes, issuers, socket);
    }

    @Override
    public String chooseEngineClientAlias(final String[] keyTypes, final Principal[] issuers, final SSLEngine engine)
    {
        return keyManager.chooseEngineClientAlias(keyTypes, issuers, engine);
    }

    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers)
    {
        return keyManager.getServerAliases(keyType, issuers);
    }

    @Override
    public String chooseServerAlias(final String alias, final Principal[] issuers, final Socket socket)
    {
        return keyManager.chooseServerAlias(alias, issuers, socket);
    }

    @Override
    public String chooseEngineServerAlias(final String alias, final Principal[] issuers, final SSLEngine engine)
    {
        return keyManager.chooseEngineServerAlias(alias, issuers, engine);
    }

    @Override
    public X509Certificate[] getCertificateChain(final String alias)
    {
        return keyManager.getCertificateChain(alias);
    }

    @Override
    public PrivateKey getPrivateKey(final String alias)
    {
        return keyManager.getPrivateKey(alias);
    }
}
