package io.opensphere.core.importer;

import javax.swing.JComponent;

/** Abstract importer. */
public abstract class AbstractImporter implements FileOrURLImporter
{
    /** The name of the importer. */
    private final String myName;

    /**
     * Constructor.
     *
     * @param name the name of the importer (used in menus)
     */
    public AbstractImporter(String name)
    {
        myName = name;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        return null;
    }

    @Override
    public String getImportMultiFileMenuItemName()
    {
        return "Import " + myName + " File Group";
    }

    @Override
    public String getImportSingleFileMenuItemName()
    {
        return "Import " + myName + " File";
    }

    @Override
    public String getImportURLFileMenuItemName()
    {
        return "Import " + myName + " URL";
    }

    @Override
    public String getName()
    {
        return myName;
    }
}
