package io.opensphere.mantle.icon.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconImageProvider;
import io.opensphere.mantle.icon.IconProvider;

/**
 * A factory for creating IconProvider objects.
 */
public final class IconProviderFactory
{
    /** The Constant iconFileTypeFilter. */
    private static final Predicate<File> ourIconFileTypeFilter = new Predicate<File>()
    {
        @Override
        public boolean test(File value)
        {
            boolean accept = false;
            if (value != null && value.isFile() && value.canRead())
            {
                String fileName = value.getName().toLowerCase();
                accept = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")
                        || fileName.endsWith(".gif");
            }
            return accept;
        }
    };

    /**
     * Creates the {@link IconProvider} with a basic ImageProvider from a file.
     *
     * @param iconFile the icon file
     * @param collectionName the collection name
     * @param subCategory the sub category
     * @param sourceKey the source key
     * @return the icon provider
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static IconProvider create(File iconFile, String collectionName, String subCategory, String sourceKey)
        throws IOException
    {
        Utilities.checkNull(iconFile, "iconFile");
        try
        {
            return create(iconFile.toURI().toURL(), collectionName, subCategory, sourceKey);
        }
        catch (MalformedURLException e)
        {
            throw new IOException("Could not load icon file: " + iconFile.getAbsolutePath(), e);
        }
    }

    /**
     * Creates the IconProvider.
     *
     * @param imageURL the image url
     * @param collectionName the collection name
     * @param subCategory the sub category
     * @param sourceKey the source key
     * @return the icon provider
     */
    public static IconProvider create(URL imageURL, String collectionName, String subCategory, String sourceKey)
    {
        return new DefaultIconProvider(imageURL, collectionName, subCategory, sourceKey);
    }

    /**
     * Given a directory reads in all image files ( gif, jpeg, png ) as image
     * providers, and optionally recurses into sub-directories, optionally using
     * sub directory paths as sub categories.
     *
     * @param iconDirectory the icon directory to read in.
     * @param collectionName the collection name for the directory.
     * @param sourceKey the source key for traceability to the provider.
     * @param recurseSubDirectories the recurse sub directories ( true to
     *            recurse into sub-directories )
     * @param subCatIfNotFromDirNames the sub cat if not from dir names
     * @return the list of {@link IconImageProvider}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static List<DefaultIconProvider> createFromDirectory(File iconDirectory, String collectionName, String sourceKey,
            boolean recurseSubDirectories, String subCatIfNotFromDirNames) throws IOException
    {
        List<DefaultIconProvider> result = New.linkedList();
        if (iconDirectory.isDirectory())
        {
            File[] fList = iconDirectory.listFiles();
            if (fList != null)
            {
                for (File aFile : fList)
                {
                    if (aFile.isDirectory() && recurseSubDirectories)
                    {
                        createProvidersFromDirectory(result, aFile, collectionName, sourceKey, recurseSubDirectories,
                                recurseSubDirectories, subCatIfNotFromDirNames);
                    }
                    else
                    {
                        if (ourIconFileTypeFilter.test(aFile))
                        {
                            result.add(new DefaultIconProvider(aFile.toURI().toURL(), collectionName, subCatIfNotFromDirNames,
                                    sourceKey));
                        }
                    }
                }
            }
        }
        else
        {
            if (ourIconFileTypeFilter.test(iconDirectory))
            {
                result.add(new DefaultIconProvider(iconDirectory.toURI().toURL(), collectionName, null, sourceKey));
            }
        }
        return result.isEmpty() ? Collections.<DefaultIconProvider>emptyList() : result;
    }

    /**
     * Utility function for create from directory to allow easy recursion.
     *
     * @param listToAddTo the list to add to
     * @param iconDirectory the icon directory
     * @param collectionName the collection name
     * @param sourceKey the source key
     * @param recurseSubDirectories the recurse sub directories
     * @param subDirsAsSubCategories the sub dirs as sub categories
     * @param subCatIfNotFromDirNames the sub cat if not from dir names
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void createProvidersFromDirectory(List<DefaultIconProvider> listToAddTo, File iconDirectory,
            String collectionName, String sourceKey, boolean recurseSubDirectories, boolean subDirsAsSubCategories,
            String subCatIfNotFromDirNames) throws IOException
    {
        if (iconDirectory.isDirectory())
        {
            File[] fList = iconDirectory.listFiles();
            if (fList != null)
            {
                for (File aFile : fList)
                {
                    if (aFile.isDirectory() && recurseSubDirectories)
                    {
                        createProvidersFromDirectory(listToAddTo, aFile, collectionName, sourceKey, recurseSubDirectories,
                                subDirsAsSubCategories, subCatIfNotFromDirNames);
                    }
                    else
                    {
                        if (ourIconFileTypeFilter.test(aFile))
                        {
                            listToAddTo.add(new DefaultIconProvider(aFile.toURI().toURL(), collectionName,
                                    subDirsAsSubCategories ? aFile.getParentFile().getName() : subCatIfNotFromDirNames,
                                    sourceKey));
                        }
                    }
                }
            }
        }
    }

    /**
     * Instantiates a new icon provider factory.
     */
    private IconProviderFactory()
    {
        // Don't allow instantiation.
    }
}
