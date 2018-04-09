package io.opensphere.core.importer;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.TransferHandler.DropLocation;

/**
 * The Interface FileOrURLImporter.
 */
public interface FileOrURLImporter
{
    /** The COMPARE_BY_NAME. */
    Comparator<FileOrURLImporter> LEX_ORDER = ((o1, o2) -> o1.getName().compareTo(o2.getName()));

    /** A comparator for ordering importers by precedence. */
    Comparator<FileOrURLImporter> PREC_ORDER = ((o1, o2) -> Integer.compare(o1.getPrecedence(), o2.getPrecedence()));

    /**
     * Provides a file to the importer so that the importer can determine if it
     * can import the file. This may be as simple as an extension verification,
     * or as complicated as a file analysis.
     *
     * @param file the a file to test
     * @param dropLocation the drop location, if the file is the object in a
     *            drag and drop operation
     * @return true, if the importer reasonably believes it can import the file.
     */
    boolean canImport(File file, @Nullable DropLocation dropLocation);

    /**
     * Provides a URL to the importer so that the importer can determine if it
     * can import the URL.
     *
     * @param url the a url to test
     * @param dropLocation the drop location, if the URL is the object in a drag
     *            and drop operation
     * @return true, if the importer reasonably believes it can import the URL.
     */
    boolean canImport(URL url, @Nullable DropLocation dropLocation);

    /**
     * Gets the description of the importer.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Gets the file chooser accessory if there is one for this importer's file
     * types.
     *
     * @return the file chooser accessory
     */
    JComponent getFileChooserAccessory();

    /**
     * Gets the import multi file menu item name.
     *
     * @return the import multi file menu item name
     */
    String getImportMultiFileMenuItemName();

    /**
     * Gets the import single file menu item name.
     *
     * @return the import single file menu item name
     */
    String getImportSingleFileMenuItemName();

    /**
     * Gets the import url file menu item name.
     *
     * @return the import url file menu item name
     */
    String getImportURLFileMenuItemName();

    /**
     * Gets the name of the importer.
     *
     * @return the name
     */
    String getName();

    /**
     * Provide a precedence number for the importer used to determine the order
     * in which importers are applied (lower numbers go first).
     *
     * @return the precedence number
     */
    int getPrecedence();

    /**
     * Gets the list of supported file extensions if this importer supports file
     * importation.
     *
     * @return the supported file extensions
     */
    List<String> getSupportedFileExtensions();

    /**
     * Requests the importer import a file.
     *
     * @param file the file to import.
     * @param callback the callback that provides the outcome of the import.
     */
    void importFile(File file, ImportCallback callback);

    /**
     * Requests the importer import a list of files as a group.
     *
     * @param fileList the list of files to import.
     * @param callback the callback that provides the outcome of the import.
     */
    void importFiles(List<File> fileList, ImportCallback callback);

    /**
     * Indicates if this importer can import file groups.
     *
     * @return true, if it imports file groups.
     */
    boolean importsFileGroups();

    /**
     * Indicates if this importer can import files.
     *
     * @return true, if can import files.
     */
    boolean importsFiles();

    /**
     * Indicates if this importer can import URLs.
     *
     * @return true, if it supports URL imports.
     */
    boolean importsURLs();

    /**
     * Import url.
     *
     * @param url The url.
     * @param component The parent component for import UI's.
     */
    void importURL(URL url, Component component);

    /**
     * Import url.
     *
     * @param url the a url
     * @param callback the callback that provides the outcome of the import.
     */
    void importURL(URL url, ImportCallback callback);
}
