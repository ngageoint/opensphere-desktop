package com.bitsys.common.http.ssl;

import java.net.Socket;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;

/**
 * This class is an X.509 key manager that provides the ability for another
 * class to make decisions in which client keys are used.
 */
public class InteractiveX509KeyManager extends X509ExtendedKeyManagerDecorator
{
    /**
     * The instance that decides which client certificate to use for a
     * connection.
     */
    private final ClientCertificateSelector certificateSelector;

    /**
     * Constructor.
     *
     * @param keyManager the underlying key manager.
     * @param certificateSelector the client certificate selector.
     */
    public InteractiveX509KeyManager(final X509ExtendedKeyManager keyManager, final ClientCertificateSelector certificateSelector)
    {
        super(keyManager);
        if (certificateSelector == null)
        {
            throw new IllegalArgumentException("The client certificate selector is null");
        }
        this.certificateSelector = certificateSelector;
    }

    /**
     * Chooses an alias to authenticate the client side of a secure socket given
     * the public key type and the list of certificate issuer authorities
     * recognized by the peer (if any). The final decision of which certificate
     * will be used is determined by the call to
     * {@link ClientCertificateSelector#chooseClientAlias(Collection, Socket, X509KeyManager)}
     * .
     *
     * @see X509KeyManager#chooseClientAlias(String[], Principal[], Socket)
     */
    @Override
    public String chooseClientAlias(final String[] keyTypes, final Principal[] issuers, final Socket socket)
    {
        final String recommendedAlias = super.chooseClientAlias(keyTypes, issuers, socket);
        final Set<String> aliases = new LinkedHashSet<>();
        aliases.add(recommendedAlias);
        aliases.addAll(getClientAliases(keyTypes, issuers));
        final String selectedAlias = certificateSelector.chooseClientAlias(aliases, socket, this);
        return selectedAlias;
    }

    /**
     * Chooses an alias to authenticate the client side of a secure socket given
     * the public key type and the list of certificate issuer authorities
     * recognized by the peer (if any). The final decision of which certificate
     * will be used is determined by the call to
     * {@link ClientCertificateSelector#chooseClientAlias(Collection, SSLEngine, X509KeyManager)}
     * .
     *
     * @see X509ExtendedKeyManager#chooseEngineClientAlias(String[],
     *      Principal[], SSLEngine)
     */
    @Override
    public String chooseEngineClientAlias(final String[] keyTypes, final Principal[] issuers, final SSLEngine engine)
    {
        final String recommendedAlias = super.chooseEngineClientAlias(keyTypes, issuers, engine);
        final Set<String> aliases = new LinkedHashSet<>();
        aliases.add(recommendedAlias);
        aliases.addAll(getClientAliases(keyTypes, issuers));
        final String selectedAlias = certificateSelector.chooseClientAlias(aliases, engine, this);
        return selectedAlias;
    }

    /**
     * Determines the full set of client aliases appropriate for the given key
     * types and certificate issuer authorities.
     *
     * @param keyTypes the key algorithm type name(s) ordered with the
     *            most-preferred key type first.
     * @param issuers the certificate issuer authorities.
     * @return the set of client aliases (may be empty).
     */
    protected Set<String> getClientAliases(final String[] keyTypes, final Principal[] issuers)
    {
        final Set<String> aliases = new LinkedHashSet<>();
        for (final String keyType : keyTypes)
        {
            final String[] array = getClientAliases(keyType, issuers);
            if (array != null)
            {
                aliases.addAll(Arrays.asList(array));
            }
        }
        return aliases;
    }
}
