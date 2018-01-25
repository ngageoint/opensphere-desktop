package io.opensphere.core;

import java.io.File;

import io.opensphere.core.help.data.PluginHelpInfo;

/** This is the main interface class that manages the help system. */
public interface HelpManager
{
    /**
     * Add help files to the help system.
     *
     * @param name The name to associate with the help files.
     * @param directory The directory that will be recursively copied over to
     *            the help system location.
     * @return True if successful, false otherwise.
     */
    boolean addHelpFiles(String name, File directory);

    /**
     * Add the given plugin help information to the help system.
     *
     * @param helpInfo The plugin specific help information.
     * @return True if successful, false otherwise.
     */
    boolean addHelpInfo(PluginHelpInfo helpInfo);

    /**
     * Remove the help information associated with the given name from the help
     * system.
     *
     * @param name The name associated with the help information to remove.
     * @return True if successful, false otherwise.
     */
    boolean removeHelpInfo(String name);
}
