package io.opensphere.core.importer;

import java.io.File;
import java.net.URL;
import java.util.List;

/**
 * The Interface ImportCallback.
 */
public interface ImportCallback
{
    /**
     * File group import complete.
     *
     * @param success true if successful import
     * @param files the list of files imported.
     * @param responseObject the response object
     */
    void fileGroupImportComplete(boolean success, List<File> files, Object responseObject);

    /**
     * File import complete.
     *
     * @param success true if successful import
     * @param aFile the a file imported.
     * @param responseObject the response object
     */
    void fileImportComplete(boolean success, File aFile, Object responseObject);

    /**
     * URL import complete.
     *
     * @param success true if successful import
     * @param aURL the a url imported
     * @param responseObject the response object
     */
    void urlImportComplete(boolean success, URL aURL, Object responseObject);
}
