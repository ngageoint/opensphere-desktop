package io.opensphere.analysis.export.controller;

import java.awt.Component;
import java.io.File;

/**
 * Interface to an object wanting to be notified when an export has successfully
 * completed.
 */
public interface ExportCompleteListener
{
    /**
     * Asks the user if the file can be overwritten.
     *
     * @param parent The component initiating the export.
     * @param file The file to overwrite, this function assumes the file exists.
     *
     * @return True if it is ok to overwrite the file, otherwise false.
     */
    boolean askUserForOverwrite(Component parent, File file);

    /**
     * Called when an export has completed successfully.
     *
     * @param parent The parent component the export was initiated from.
     * @param file The exported file.
     * @param exportedCount The number of records exported to the file.
     */
    void exportComplete(Component parent, File file, int exportedCount);
}
