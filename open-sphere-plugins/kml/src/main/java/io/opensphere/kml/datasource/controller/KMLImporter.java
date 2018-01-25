package io.opensphere.kml.datasource.controller;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler.DropLocation;

import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.predicate.EndsWithPredicate;
import io.opensphere.kml.common.model.KMLDataSource;
import io.opensphere.kml.common.model.KMLDataSource.Type;
import io.opensphere.kml.common.util.KMLToolbox;
import io.opensphere.mantle.data.DataGroupImportCallbackResponse;
import io.opensphere.mantle.datasources.IDataSource;

/**
 * KML File/URL Importer.
 */
public class KMLImporter implements FileOrURLImporter
{
    /** The supported file extensions. */
    private static final List<String> ourFileExtensions = New.unmodifiableList("kml", "kmz");

    /** The data source controller. */
    private final KMLDataSourceControllerImpl myController;

    /**
     * Peeks at url's to see if they contain kml data.
     */
    private final KMLPeeker myPeeker;

    /**
     * Constructor.
     *
     * @param controller The data source controller
     */
    public KMLImporter(KMLDataSourceControllerImpl controller)
    {
        myController = controller;
        myPeeker = new KMLPeeker(myController.getToolbox());
    }

    @Override
    public boolean canImport(File file, DropLocation dropLocation)
    {
        return file != null && file.canRead() && new EndsWithPredicate(ourFileExtensions, true).test(file.getAbsolutePath());
    }

    @Override
    public boolean canImport(URL url, DropLocation dropLocation)
    {
        // There's not an easy way to know if this is a KML URL.
        // We could guess based on the URL string, or download part of the file
        // and see if it looks like KML.
        boolean canImport = false;
        if (url != null)
        {
            String path = url.getPath().toLowerCase();
            if (!(path.endsWith(".csv") || path.endsWith(".txt") || path.endsWith(".dat") || path.endsWith(".shp")))
            {
                if (path.endsWith(".kml") || path.endsWith(".kmz"))
                {
                    canImport = true;
                }
                else
                {
                    canImport = myPeeker.isKml(url);
                }
            }
        }
        return canImport;
    }

    @Override
    public String getDescription()
    {
        return "Importer for KML files and URLs.";
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        return null;
    }

    @Override
    public String getImportMultiFileMenuItemName()
    {
        return "Import KML File Group";
    }

    @Override
    public String getImportSingleFileMenuItemName()
    {
        return "Import KML File";
    }

    @Override
    public String getImportURLFileMenuItemName()
    {
        return "Import KML URL";
    }

    @Override
    public String getName()
    {
        return "KML";
    }

    @Override
    public int getPrecedence()
    {
        return 1300;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return ourFileExtensions;
    }

    /**
     * Imports a KML data source.
     *
     * @param dataSource The data source
     * @param isReImport True if this is a re-import
     * @param fileOrUrl The file or URL
     * @param callback The callback
     * @param parent The parent UI for the import dialog or null if the main
     *            window should be used.
     */
    public void importDataSource(final KMLDataSource dataSource, final boolean isReImport, final Object fileOrUrl,
            final ImportCallback callback, final Component parent)
    {
        dataSource.setActive(true);
        KMLToolbox kmlToolbox = myController.getToolbox().getPluginToolboxRegistry().getPluginToolbox(KMLToolbox.class);
        kmlToolbox.getPluginExecutor().execute(() -> handleImportComplete(dataSource, isReImport, fileOrUrl, callback));
    }

    @Override
    public void importFile(File file, ImportCallback callback)
    {
        KMLDataSource dataSource = new KMLDataSource();
        dataSource.setName(getSourceName(file));
        dataSource.setType(Type.FILE);
        dataSource.setPath(file.getAbsolutePath());
        importDataSource(dataSource, false, file, callback, null);
    }

    @Override
    public void importFiles(List<File> fileList, ImportCallback callback)
    {
        if (callback != null)
        {
            callback.fileGroupImportComplete(false, fileList, null);
        }
    }

    @Override
    public boolean importsFileGroups()
    {
        return false;
    }

    @Override
    public boolean importsFiles()
    {
        return true;
    }

    @Override
    public boolean importsURLs()
    {
        return true;
    }

    @Override
    public void importURL(URL url, Component parent)
    {
        KMLDataSource dataSource = new KMLDataSource();
        dataSource.setName(getSourceName(url));
        dataSource.setType(Type.URL);
        dataSource.setPath(url.toExternalForm());
        importDataSource(dataSource, false, url, null, parent);
    }

    @Override
    public void importURL(URL url, ImportCallback callback)
    {
        KMLDataSource dataSource = new KMLDataSource();
        dataSource.setName(getSourceName(url));
        dataSource.setType(Type.URL);
        dataSource.setPath(url.toExternalForm());
        importDataSource(dataSource, false, url, callback, null);
    }

    /**
     * Handle adding the source when the user clicks OK.
     *
     * @param dataSource The data source
     * @param isReImport True if this is a re-import
     * @param fileOrUrl The file or URL
     * @param callback The callback
     */
    private void handleImportComplete(final KMLDataSource dataSource, boolean isReImport, Object fileOrUrl,
            ImportCallback callback)
    {
        if (isReImport)
        {
            boolean isActive = dataSource.isActive();
            myController.removeSource(dataSource);
            dataSource.setActive(isActive);
        }
        myController.addSource(dataSource);

        if (callback != null)
        {
            DataGroupImportCallbackResponse responseObject = () -> dataSource.getDataGroupInfo();
            if (fileOrUrl instanceof File)
            {
                callback.fileImportComplete(true, (File)fileOrUrl, responseObject);
            }
            else if (fileOrUrl instanceof URL)
            {
                callback.urlImportComplete(true, (URL)fileOrUrl, responseObject);
            }
        }
    }

    /**
     * Gets the initial data source name for the given URL.
     *
     * @param path The path
     * @return The initial data source name
     */
    private String getSourceName(Object path)
    {
        // Get an initial name from the File/URL
        String name = "New KML";
        if (path instanceof File)
        {
            name = ((File)path).getName();
        }
        else if (path instanceof URL)
        {
            name = ((URL)path).getPath();

            // Get everything after the last slash
            int shashIndex = name.lastIndexOf('/');
            if (shashIndex != -1)
            {
                name = name.substring(shashIndex + 1);
            }
        }

        // Get everything before the last dot
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1)
        {
            name = name.substring(0, dotIndex);
        }

        // Resolve name conflicts
        boolean inConflict = true;
        int i = 1;
        String newName = name;
        while (inConflict)
        {
            inConflict = false;
            for (IDataSource source : myController.getSourceList())
            {
                if (newName.equals(source.getName()))
                {
                    inConflict = true;
                    newName = new StringBuilder(name).append(' ').append(i++).toString();
                    break;
                }
            }
        }
        name = newName;

        return name;
    }
}
