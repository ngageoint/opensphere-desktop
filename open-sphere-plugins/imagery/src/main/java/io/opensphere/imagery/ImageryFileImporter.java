package io.opensphere.imagery;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.TransferHandler.DropLocation;

import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.DataGroupImportCallbackResponse;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceCreator;

/**
 * The Class ImageryFileImporter.
 */
public class ImageryFileImporter implements FileOrURLImporter
{
    /** The Constant ourFileExtensions. */
    private static final List<String> ourFileExtensions = New.unmodifiableList("png", "gif", "jpg", "jpeg", "tif", "nitf", "ntf",
            "tiff", "geoTIFF", "WorldFile");

    /** The controller. */
    private final ImageryFileSourceController myController;

    /**
     * Instantiates a new CSV file importer.
     *
     * @param controller the controller
     */
    public ImageryFileImporter(ImageryFileSourceController controller)
    {
        myController = controller;
    }

    @Override
    public boolean canImport(File importFile, DropLocation dropLocation)
    {
        boolean canImportImageryFile = false;
        if (importFile != null && importFile.canRead())
        {
            String fileName = importFile.getAbsolutePath().toLowerCase();
            for (String ext : ourFileExtensions)
            {
                if (fileName.endsWith(ext.toLowerCase()))
                {
                    canImportImageryFile = true;
                    break;
                }
            }
        }
        return canImportImageryFile;
    }

    @Override
    public boolean canImport(URL aURL, DropLocation dropLocation)
    {
        return false;
    }

    @Override
    public String getDescription()
    {
        return "Importer for Imagery Files.";
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        return null;
    }

    @Override
    public String getImportMultiFileMenuItemName()
    {
        return "Import Imagery File Group";
    }

    @Override
    public String getImportSingleFileMenuItemName()
    {
        return "Import Imagery File";
    }

    @Override
    public String getImportURLFileMenuItemName()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return "Imagery File";
    }

    @Override
    public int getPrecedence()
    {
        return 400;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return ourFileExtensions;
    }

    @Override
    public void importFile(final File file, final ImportCallback impCallback)
    {
        importFiles(Collections.singletonList(file), impCallback);
    }

    @Override
    public void importFiles(final List<File> fileList, final ImportCallback callback)
    {
        EventQueue.invokeLater(() ->
        {
            Set<IDataSource> sourcesInUse = new HashSet<>(myController.getSourceList());

            JDialog wizardDialog = new JDialog(myController.getToolbox().getUIRegistry().getMainFrameProvider().get(), false);
            wizardDialog.setSize(new Dimension(900, 700));
            wizardDialog.setLayout(new BorderLayout());
            wizardDialog.setResizable(false);

            @SuppressWarnings("unused")
            ImagerySourceWizardPanel panel = new ImagerySourceWizardPanel(wizardDialog.getContentPane(),
                    myController.getToolbox(), fileList, sourcesInUse, new IDataSourceCreator()
                    {
                        @Override
                        public void sourceCreated(boolean successful, final IDataSource source)
                        {
                            try
                            {
                                if (successful)
                                {
                                    myController.addSource(source);
                                }
                                if (callback != null)
                                {
                                    callback.fileGroupImportComplete(successful, fileList, new DataGroupImportCallbackResponse()
                                    {
                                        @Override
                                        public DataGroupInfo getNewOrChangedGroup()
                                        {
                                            return ((ImagerySourceGroup)source).getDataGroupInfo();
                                        }
                                    });
                                }
                            }
                            finally
                            {
                                wizardDialog.setVisible(false);
                            }
                        }

                        @Override
                        public void sourcesCreated(boolean successful, List<IDataSource> sources)
                        {
                            // Do nothing we won't get here.
                        }
                    });
            wizardDialog.setLocationRelativeTo(myController.getToolbox().getUIRegistry().getMainFrameProvider().get());
            wizardDialog.setVisible(true);
        });
    }

    @Override
    public boolean importsFileGroups()
    {
        return true;
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
    public void importURL(URL aURL, Component component)
    {
    }

    @Override
    public void importURL(URL aURL, ImportCallback callback)
    {
        if (callback != null)
        {
            callback.urlImportComplete(false, aURL, null);
        }
    }

    /**
     * Reimport group.
     *
     * @param group the group
     * @param callback the callback
     */
    public void reimportGroup(final ImagerySourceGroup group, final ImportCallback callback)
    {
        Set<IDataSource> sourcesInUse = new HashSet<IDataSource>();
        for (IDataSource source : myController.getSourceList())
        {
            sourcesInUse.add(source);
        }
        sourcesInUse.remove(group);

        final JDialog wiz = new JDialog(myController.getToolbox().getUIRegistry().getMainFrameProvider().get(), false);
        wiz.setSize(new Dimension(900, 700));
        wiz.setLayout(new BorderLayout());
        wiz.setResizable(false);
        final ImagerySourceGroup backupGroup = new ImagerySourceGroup(group);

        @SuppressWarnings("unused")
        ImagerySourceWizardPanel panel = new ImagerySourceWizardPanel(wiz.getContentPane(), myController.getToolbox(), group,
                sourcesInUse, new IDataSourceCreator()
                {
                    @Override
                    public void sourceCreated(boolean successful, final IDataSource source)
                    {
                        try
                        {
                            myController.addSource(successful ? source : backupGroup);
                            if (callback != null)
                            {
                                final ImagerySourceGroup resultGroup = successful ? (ImagerySourceGroup)source : backupGroup;
                                DataGroupImportCallbackResponse responseObject = new DataGroupImportCallbackResponse()
                                {
                                    @Override
                                    public DataGroupInfo getNewOrChangedGroup()
                                    {
                                        return resultGroup.getDataGroupInfo();
                                    }
                                };
                                callback.fileGroupImportComplete(successful, resultGroup.getFileList(), responseObject);
                            }
                        }
                        finally
                        {
                            wiz.setVisible(false);
                        }
                    }

                    @Override
                    public void sourcesCreated(boolean successful, List<IDataSource> sources)
                    {
                        // Do nothing we won't get here.
                    }
                });
        wiz.setLocationRelativeTo(myController.getToolbox().getUIRegistry().getMainFrameProvider().get());
        wiz.setVisible(true);
    }
}
