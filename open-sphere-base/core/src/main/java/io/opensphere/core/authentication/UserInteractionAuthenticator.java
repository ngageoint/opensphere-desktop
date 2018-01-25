package io.opensphere.core.authentication;

import java.awt.Component;
import java.util.function.Supplier;

import io.opensphere.core.SecurityManager;
import io.opensphere.core.util.PausingTimeBudget;
import io.opensphere.core.util.Utilities;

/**
 * Common functionality for authenticators.
 */
public class UserInteractionAuthenticator
{
    /**
     * The system security manager.
     */
    private final SecurityManager mySecurityManager;

    /** The unique key to identify the server. */
    private final String myServerKey;

    /** The server name to present to the user in dialog text. */
    private final String myServerName;

    /** The provider of a parent component for GUIs. */
    private final Supplier<? extends Component> myParentProvider;

    /**
     * Optional time budget which should be paused while waiting for user
     * interaction.
     */
    private final PausingTimeBudget myTimeBudget;

    /**
     * Constructor.
     *
     * @param serverName The server name to present to the user in dialog text.
     * @param serverKey The unique key to use for the server in the preferences.
     * @param parentProvider The parent component.
     * @param securityManager The system security manager.
     * @param timeBudget Optional time budget.
     */
    public UserInteractionAuthenticator(String serverName, String serverKey, Supplier<? extends Component> parentProvider,
            SecurityManager securityManager, PausingTimeBudget timeBudget)
    {
        myParentProvider = parentProvider;
        mySecurityManager = Utilities.checkNull(securityManager, "securityManager");
        myServerKey = serverKey;
        myServerName = serverName;
        myTimeBudget = timeBudget;
    }

    /**
     * Get the parent component.
     *
     * @return The parent component.
     */
    protected Component getParent()
    {
        return myParentProvider.get();
    }

    /**
     * Get the security manager.
     *
     * @return The security manager.
     */
    protected SecurityManager getSecurityManager()
    {
        return mySecurityManager;
    }

    /**
     * Accessor for the serverKey.
     *
     * @return The serverKey.
     */
    protected String getServerKey()
    {
        return myServerKey;
    }

    /**
     * Accessor for the serverName.
     *
     * @return The serverName.
     */
    protected String getServerName()
    {
        return myServerName;
    }

    /**
     * Accessor for the timeBudget.
     *
     * @return The timeBudget.
     */
    protected PausingTimeBudget getTimeBudget()
    {
        return myTimeBudget;
    }
}
