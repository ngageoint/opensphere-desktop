package io.opensphere.server.source;

import javax.net.ssl.KeyManager;

import io.opensphere.core.authentication.UserInteractionX509KeyManager;
import io.opensphere.core.common.connection.CertificateConfiguration;
import io.opensphere.core.common.connection.ServerConfiguration;

/**
 * Helper for server authentication.
 */
public final class AuthenticationHelper
{
    /**
     * Reset authentication parameters for a server due to failed
     * authentication.
     *
     * @param serverConfig The server configuration.
     */
    public static void failedAuthentication(ServerConfiguration serverConfig)
    {
        CertificateConfiguration certConfig = serverConfig.getCertificateConfiguration();
        if (certConfig != null && certConfig.isUseCertificate())
        {
            for (KeyManager keyManager : certConfig.getKeyManagers())
            {
                if (keyManager instanceof UserInteractionX509KeyManager)
                {
                    ((UserInteractionX509KeyManager)keyManager).failedAuthentication();
                }
            }
        }
    }

    /** Disallow instantiation. */
    private AuthenticationHelper()
    {
    }
}
