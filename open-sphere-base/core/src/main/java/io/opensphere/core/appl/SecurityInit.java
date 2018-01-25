package io.opensphere.core.appl;

import java.net.InetAddress;
import java.net.SocketPermission;
import java.net.UnknownHostException;
import java.security.Permission;

import org.apache.log4j.Logger;

/**
 * Initializer for security. This installs a custom security manager to strongly
 * encourage plug-ins to create envoys to do their network connections.
 */
class SecurityInit
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(SecurityInit.class);

    /** The local IP address. */
    private static final String ourLocalAddress;

    static
    {
        String localAddress = "";
        try
        {
            localAddress = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            LOGGER.warn("Failed to determine local host address: " + e, e);
        }
        finally
        {
            ourLocalAddress = localAddress;
        }
    }

    /**
     * Set up the custom security manager.
     */
    public void setupSecurity()
    {
        System.setSecurityManager(new OpenSphereSecurityManager());
    }

    /**
     * Custom security manager.
     */
    private static class OpenSphereSecurityManager extends SecurityManager
    {
        @Override
        public void checkPermission(Permission perm)
        {
            if (perm instanceof SocketPermission)
            {
                checkPermission((SocketPermission)perm);
            }
            super.checkPermission(perm);
        }

        /**
         * Determine if the current thread is allowed the requested socket
         * permission, based on its container thread group.
         *
         * @param perm The socket permission.
         */
        private void checkPermission(SocketPermission perm)
        {
            if (!perm.getActions().contains("connect"))
            {
                return;
            }
            else if (inThreadGroup("Envoy-group"))
            {
                return;
            }

            // Allow connections to the local host from the cache thread pool.
            else if (inThreadGroup("Cache-group") && perm.getName().startsWith(ourLocalAddress))
            {
                return;
            }

            throw new SecurityException("Socket access must be performed from an Envoy.");
        }

        /**
         * Determine if the current thread is in a particular thread group.
         *
         * @param name The name of the thread group.
         * @return <code>true</code> if the current thread is part of the group.
         */
        private boolean inThreadGroup(String name)
        {
            ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
            do
            {
                if (threadGroup.getName().equals(name))
                {
                    return true;
                }
                threadGroup = threadGroup.getParent();
            }
            while (threadGroup != null);

            return false;
        }
    }
}
