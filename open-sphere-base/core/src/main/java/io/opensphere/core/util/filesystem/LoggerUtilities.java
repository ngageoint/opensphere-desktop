package io.opensphere.core.util.filesystem;

import java.io.File;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import io.opensphere.core.util.lang.StringUtilities;

/**
 * This class holds utilities associated with the logger.
 */
public final class LoggerUtilities
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(LoggerUtilities.class);

    /**
     * Remove all files ending in ".log" in the logs directory which is located
     * in the opensphere.path.runtime specified directory.
     *
     * @return True if all log files were removed successfully, false otherwise
     *         or if no log files currently exist in the directory.
     */
    public static boolean removeLogFile()
    {
        // Create the directory where the logs should be found.
        final StringBuilder logPath = new StringBuilder();
        logPath.append(StringUtilities.expandProperties(System.getProperty("opensphere.path.runtime"), System.getProperties()));
        logPath.append(StringUtilities.FILE_SEP);
        logPath.append("logs");

        final File logDir = new File(logPath.toString());

        // Find all files that end with ".log" in the logs directory.
        final List<File> logsToRemove = FileUtilities.getFilesFromDirectory(logDir, ".log");

        // Return if there are no logs to remove
        if (logsToRemove == null || logsToRemove.isEmpty())
        {
            LOGGER.warn("There are no log files to remove in directory:" + logPath.toString());
            return false;
        }

        // Display confirmation dialog
        final StringBuilder msg = new StringBuilder(128);
        msg.append("Are you sure you want to delete all the log files from:");
        msg.append(StringUtilities.LINE_SEP);
        msg.append(logPath.toString());
        msg.append(StringUtilities.LINE_SEP);
        msg.append("Once they are gone, they are not coming back!");

        final int confirm = JOptionPane.showConfirmDialog(null, msg.toString(), "Confirm Log File Deletion",
                JOptionPane.OK_CANCEL_OPTION);
        if (confirm == JOptionPane.CANCEL_OPTION)
        {
            return false;
        }

        // Remove the log files
        boolean status = true;
        for (final File log : logsToRemove)
        {
            status &= log.delete();
        }

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(" The return value for removing log files = " + status);
        }
        return status;
    }

    /** Disallow instantiation. */
    private LoggerUtilities()
    {
    }
}
