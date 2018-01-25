package io.opensphere.core.importer;

import javax.swing.JMenuItem;

/**
 * The Class FileOrURLImporterMenuItem.
 */
@SuppressWarnings("serial")
public class FileOrURLImporterMenuItem extends JMenuItem
{
    /** The my importer. */
    private final FileOrURLImporter myImporter;

    /** The my import type. */
    private final ImportType myImportType;

    /**
     * Instantiates a new file importer menu item.
     *
     * @param importer the importer
     * @param type the type
     */
    public FileOrURLImporterMenuItem(FileOrURLImporter importer, ImportType type)
    {
        super();
        myImporter = importer;
        myImportType = type;
        switch (type)
        {
            case FILE:
                setText(importer.getImportSingleFileMenuItemName());
                break;
            case FILE_GROUP:
                setText(importer.getImportMultiFileMenuItemName());
                break;
            case URL:
                setText(importer.getImportURLFileMenuItemName());
                break;
            default:
                setText("Unknown");
                break;
        }
    }

    /**
     * Gets the importer.
     *
     * @return the importer
     */
    public FileOrURLImporter getImporter()
    {
        return myImporter;
    }

    /**
     * Gets the import type.
     *
     * @return the import type
     */
    public ImportType getImportType()
    {
        return myImportType;
    }
}
