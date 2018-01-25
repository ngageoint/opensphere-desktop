package io.opensphere.server.serverprovider;

import java.awt.Component;
import java.security.Principal;
import java.util.function.Supplier;

import org.apache.http.auth.BasicUserPrincipal;

import com.bitsys.common.http.auth.Credentials;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.authentication.UserInteractionUsernamePasswordProvider;
import io.opensphere.core.util.PausingTimeBudget;

/**
 * Extension of httpclient class that overrides the username/password retrieval.
 */
public class UserInteractionUsernamePasswordCredentials implements Credentials
{
    /** The username/password provider from Core. */
    private final UserInteractionUsernamePasswordProvider myProvider;

    /**
     * Constructor.
     *
     * @param serverName The server name to present to the user in dialog text.
     * @param serverKey The unique key to use for the server in the preferences.
     * @param parentProvider The parent component provider.
     * @param securityManager The system security manager.
     * @param timeBudget Optional time budget.
     */
    public UserInteractionUsernamePasswordCredentials(String serverName, String serverKey,
            Supplier<? extends Component> parentProvider, SecurityManager securityManager, PausingTimeBudget timeBudget)
    {
        myProvider = new UserInteractionUsernamePasswordProvider(serverName, serverKey, parentProvider, securityManager,
                timeBudget);
    }

    @Override
    public Principal getUserPrincipal()
    {
        return new BasicUserPrincipal(myProvider.getUsername());
    }

    @Override
    public char[] getPassword()
    {
        return myProvider.getPassword();
    }
}
