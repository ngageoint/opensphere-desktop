package io.opensphere.core.importer;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.TransferHandler.DropLocation;

import io.opensphere.core.util.predicate.EndsWithPredicate;

/** Single file only importer. */
public abstract class SingleFileImporter extends AbstractImporter
{
    /** The supported file extensions. */
    private final List<String> myFileExtensions;

    /**
     * Constructor.
     *
     * @param name the name of the importer (used in menus)
     * @param fileExtensions the supported file extensions
     */
    public SingleFileImporter(String name, List<String> fileExtensions)
    {
        super(name);
        myFileExtensions = fileExtensions;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return myFileExtensions;
    }

    @Override
    public boolean importsFiles()
    {
        return true;
    }

    @Override
    public boolean importsFileGroups()
    {
        return false;
    }

    @Override
    public boolean importsURLs()
    {
        return false;
    }

    @Override
    public boolean canImport(File file, DropLocation dropLocation)
    {
        return file != null && file.canRead() && new EndsWithPredicate(myFileExtensions, true).test(file.getAbsolutePath());
    }

    @Override
    public boolean canImport(URL url, DropLocation dropLocation)
    {
        return false;
    }

    @Override
    public void importFiles(List<File> fileList, ImportCallback callback)
    {
    }

    @Override
    public void importURL(URL url, Component component)
    {
    }

    @Override
    public void importURL(URL url, ImportCallback callback)
    {
    }
}
