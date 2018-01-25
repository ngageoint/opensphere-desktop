package io.opensphere.geopackage.importer;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.TransferHandler.DropLocation;

import io.opensphere.core.control.ui.UIRegistry;
import io.opensphere.core.data.DataRegistry;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.geopackage.importer.layer.LayerImporter;
import io.opensphere.geopackage.importer.tile.TileImporter;
import io.opensphere.geopackage.importer.tile.TileRowImporterImpl;
import io.opensphere.geopackage.model.GeoPackageLayer;
import io.opensphere.geopackage.progress.ProgressReporter;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageConstants;
import mil.nga.geopackage.manager.GeoPackageManager;
import mil.nga.geopackage.validate.GeoPackageValidate;

/**
 * Responsible for importing GeoPackage files.
 */
public class GeoPackageImporter implements FileOrURLImporter
{
    /**
     * Importer for layers.
     */
    private final LayerImporter myLayerImporter;

    /**
     * Used to import the tile data and images contained in a geopackage file.
     */
    private final TileImporter myTileImporter;

    /**
     * Used to display a cancellable task activity.
     */
    private final UIRegistry myUIRegistry;

    /**
     * Constructs a new {@link GeoPackageImporter}.
     *
     * @param registry Used to create the
     * @param uiRegistry Used to display a cancellable task activity.
     * @param alreadyImported The set of files that have already been imported
     *            into the system.
     */
    public GeoPackageImporter(DataRegistry registry, UIRegistry uiRegistry, Set<String> alreadyImported)
    {
        myLayerImporter = new LayerImporter(registry);
        myTileImporter = new TileImporter(new TileRowImporterImpl(registry));
        myUIRegistry = uiRegistry;
        ThreadUtilities.runBackground(() ->
        {
            new GeoPackageDataEnsurer(this, registry).ensureData(alreadyImported);
        });
    }

    @Override
    public boolean canImport(File file, DropLocation dropLocation)
    {
        return GeoPackageValidate.hasGeoPackageExtension(file);
    }

    @Override
    public boolean canImport(URL url, DropLocation dropLocation)
    {
        return false;
    }

    @Override
    public String getDescription()
    {
        return "Imports GeoPackage files that may contain features and/or tiles.";
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        return null;
    }

    @Override
    public String getImportMultiFileMenuItemName()
    {
        return null;
    }

    @Override
    public String getImportSingleFileMenuItemName()
    {
        return "Import " + GeoPackageConstants.GEOPACKAGE_EXTENSION.toUpperCase() + " File";
    }

    @Override
    public String getImportURLFileMenuItemName()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return GeoPackageConstants.GEOPACKAGE_EXTENSION.toUpperCase() + " File";
    }

    @Override
    public int getPrecedence()
    {
        return 11;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return New.list(GeoPackageConstants.GEOPACKAGE_EXTENSION, GeoPackageConstants.GEOPACKAGE_EXTENDED_EXTENSION);
    }

    @Override
    public void importFile(File file, ImportCallback callback)
    {
        ThreadUtilities.runBackground(() -> importInBackground(file, callback));
    }

    @Override
    public void importFiles(List<File> fileList, ImportCallback callback)
    {
        // Not supported.
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
        return false;
    }

    @Override
    public void importURL(URL url, Component component)
    {
        // Not supported.
    }

    @Override
    public void importURL(URL url, ImportCallback callback)
    {
        // Not supported.
    }

    /**
     * Imports the geo package file, this method is meant to be called on a
     * background thread.
     *
     * @param file The geo package file, it is assumed canImport was already
     *            called on this file.
     * @param callback The import callback.
     */
    private void importInBackground(File file, ImportCallback callback)
    {
        CancellableTaskActivity ta = new CancellableTaskActivity();
        try
        {
            ta.setLabelValue("Importing " + file);
            ta.setActive(true);
            ta.setProgress(-1);
            myUIRegistry.getMenuBarRegistry().addTaskActivity(ta);
            GeoPackage geopackage = GeoPackageManager.open(file);
            try
            {
                List<GeoPackageLayer> layers = New.list();
                ProgressReporter reporter = myLayerImporter.importLayers(geopackage, layers, ta);
                myTileImporter.importTiles(geopackage, layers, ta, reporter.getModel());
                reporter.close();
            }
            finally
            {
                geopackage.close();
            }
        }
        finally
        {
            ta.setComplete(true);
        }

        if (callback != null)
        {
            callback.fileImportComplete(true, file, null);
        }
    }
}
