package io.opensphere.shapefile;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler.DropLocation;

import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupImportCallbackResponse;
import io.opensphere.mantle.datasources.IDataSource;
import io.opensphere.mantle.datasources.IDataSourceCreator;
import io.opensphere.shapefile.config.v1.ShapeFileSource;

/**
 * The Class CSVFileImporter.
 */
@SuppressWarnings("PMD.GodClass")
public class ShapeFileImporter implements FileOrURLImporter
{
    /** The supported file extensions. */
    private static final List<String> ourFileExtensions = New.unmodifiableList("shp", "zip");

    /**
     * A helper class to examine input sources to determine whether they can be
     * read as a shapefile.
     */
    private final ShapeFilePeeker myPeeker;

    /** The controller. */
    private final ShapeFileDataSourceController ctrl;

    /**
     * Instantiates a new CSV file importer.
     *
     * @param controller the controller
     */
    public ShapeFileImporter(ShapeFileDataSourceController controller)
    {
        ctrl = controller;
        myPeeker = new ShapeFilePeeker(controller.getToolbox());
    }

    @Override
    public boolean canImport(File importFile, DropLocation dropLocation)
    {
        return myPeeker.isShape(importFile);
    }

    @Override
    public boolean canImport(URL aURL, DropLocation dropLocation)
    {
        return myPeeker.isShape(aURL);
    }

    @Override
    public String getDescription()
    {
        return "Importer for ESRI Shapefiles.";
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        return null;
    }

    @Override
    public String getImportMultiFileMenuItemName()
    {
        return "Import Shape File Group";
    }

    @Override
    public String getImportSingleFileMenuItemName()
    {
        return "Import Shape File";
    }

    @Override
    public String getImportURLFileMenuItemName()
    {
        return "Import Shape File URL";
    }

    @Override
    public String getName()
    {
        return "ESRI Shapefile";
    }

    @Override
    public int getPrecedence()
    {
        return 300;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return ourFileExtensions;
    }

    @Override
    public void importFile(File file, ImportCallback cb)
    {
        if (!continueIfLoaded(file.getAbsolutePath()))
        {
            if (cb != null)
            {
                cb.fileImportComplete(false, file, null);
            }
            return;
        }

        String defaultName = file.getName();
        int dotIndex = defaultName.lastIndexOf('.');
        if (dotIndex != -1)
        {
            defaultName = defaultName.substring(0, dotIndex);
        }

        Set<String> importedNames = New.set();
        for (IDataSource source : ctrl.getSourceList())
        {
            importedNames.add(source.getName());
        }

        String selection = reuseSelection(importedNames);
        ShapeFileSource importSource = new ShapeFileSource();
        Ref<ImportState> startState = new Ref<>();
        if (selection != null)
        {
            ShapeFileSource other = ctrl.getSource(selection);
            importSource.setEqualTo(other);
            startState.val = ImportState.PROPERTIES;
        }

        importSource.setName(defaultName);
        importSource.setPath(file.getAbsolutePath());
        CallbackCaller cbc = ifn(cb != null, (suc, src) -> cb.fileImportComplete(suc, file, getDgicr(src)));
        SwingUtilities.invokeLater(() -> importSource(importSource, importedNames, cbc, startState.val));
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
    public void importURL(URL url, Component component)
    {
    }

    @Override
    public void importURL(URL url, ImportCallback cb)
    {
        if (!continueIfLoaded(url.toString()))
        {
            if (cb != null)
            {
                cb.urlImportComplete(false, url, null);
            }
            return;
        }

        String path = url.getPath();
        int lastSep = Math.max(path.lastIndexOf(File.pathSeparator), Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')));
        lastSep = lastSep < 0 ? 0 : lastSep;
        int dotIndex = path.lastIndexOf('.');
        int dot = dotIndex > lastSep ? dotIndex : path.length();
        String defaultName = path.substring(lastSep + 1, dot);

        Set<String> importedNames = New.set();
        for (IDataSource source : ctrl.getSourceList())
        {
            importedNames.add(source.getName());
        }

        String selection = reuseSelection(importedNames);
        ShapeFileSource importSource = new ShapeFileSource();
        Ref<ImportState> startState = new Ref<>();
        if (selection != null)
        {
            ShapeFileSource other = ctrl.getSource(selection);
            importSource.setEqualTo(other);
            startState.val = ImportState.PROPERTIES;
        }

        importSource.setName(defaultName);
        importSource.setPath(url.toString());
        CallbackCaller cbc = ifn(cb != null, (suc, src) -> cb.urlImportComplete(suc, url, getDgicr(src)));
        SwingUtilities.invokeLater(() -> importSource(importSource, importedNames, cbc, startState.val));
    }

    /**
     * Import from a file. This method bypasses some of the unnecessary
     * rigamarole when re-importing a previously imported file. It is called
     * only from the reImport method of the excessively named class
     * ShapeFileDataGroupInfoAssistant.
     *
     * @param source file source
     * @param namesInUse already used names
     * @param cb a callback for reporting status
     * @param state thingy
     */
    public void importFileSource(ShapeFileSource source, Set<String> namesInUse, ImportCallback cb, ImportState state)
    {
        CallbackCaller cbc = (suc, src) -> cb.fileImportComplete(suc, null, getDgicr(src));
        importSource(source, namesInUse, cbc, state);
    }

    /**
     * Call only on AWT thread.
     *
     * @param source the source
     * @param namesInUse the names in use
     * @param callback the callback, which may be null
     * @param state the state of the wizard
     */
    void importSource(ShapeFileSource source, Set<String> namesInUse, CallbackCaller callback, ImportState state)
    {
        JFrame mainFrame = getMainFrame();
        JDialog dialog = new JDialog(mainFrame, false);
        dialog.setSize(new Dimension(900, 700));
        dialog.setLayout(new BorderLayout());
        dialog.setResizable(false);
        LegacyShapeFileImportWizard wizard = new LegacyShapeFileImportWizard(dialog.getContentPane(), ctrl.getToolbox(), source,
                namesInUse, new Idsc(callback, dialog));
        if (state != null)
        {
            wizard.changeState(state);
        }
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    /**
     * Implements an interface whose documentation helpfully identifies it as
     * "The Interface IDataSourceCreator".
     */
    private class Idsc implements IDataSourceCreator
    {
        /** Stuff. */
        private final CallbackCaller callback;

        /** The dialog used by the import wizard. */
        private final JDialog wizardDialog;

        /**
         * Bla.
         *
         * @param c bla
         * @param d bla
         */
        public Idsc(CallbackCaller c, JDialog d)
        {
            callback = c;
            wizardDialog = d;
        }

        @Override
        public void sourceCreated(boolean successful, IDataSource src)
        {
            try
            {
                if (successful)
                {
                    ctrl.addSource(src);
                }
                if (callback != null)
                {
                    callback.call(successful, (ShapeFileSource)src);
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
            // Only one data source is created at a time.
        }
    }

    /**
     * Check to see if this shape file is already loaded and allow the user to
     * cancel if they do not wish to reload the file again.
     *
     * @param file The absolute path or URL which specifies the file location.
     * @return true if the file should be loaded.
     */
    private boolean continueIfLoaded(String file)
    {
        for (IDataSource source : ctrl.getSourceList())
        {
            if (((ShapeFileSource)source).getPath().equals(file))
            {
                int value = JOptionPane
                        .showConfirmDialog(getMainFrame(),
                                "The file has already been loaded under the name " + source.getName()
                                        + "\nDo you want to load the file again?",
                                "Duplicate File", JOptionPane.OK_CANCEL_OPTION);

                if (value == JOptionPane.CANCEL_OPTION)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine whether an existing source's configuration will be used as the
     * configuration for the new file.
     *
     * @param importedNames The names of the existing sources.
     * @return The name of the source whose configuration will be used or
     *         {@code null} if none will be used.
     */
    private String reuseSelection(Set<String> importedNames)
    {
        if (ctrl.getSourceList().isEmpty())
        {
            return null;
        }

        return EventQueueUtilities.happyOnEdt(() ->
        {
            JPanel p = new JPanel(new BorderLayout());
            JTextArea jta = new JTextArea("Do you want to use the same import settings\nas one of these other files?");
            jta.setBackground(p.getBackground());
            jta.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            jta.setEditable(false);
            p.add(jta, BorderLayout.NORTH);
            JComboBox<String> cb = new JComboBox<>(New.array(importedNames, String.class));
            p.add(cb, BorderLayout.CENTER);

            int value = JOptionPane.showConfirmDialog(getMainFrame(), p, "Reuse Import Settings", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null);
            String selectedItem = null;
            if (value == JOptionPane.YES_OPTION)
            {
                selectedItem = (String)cb.getSelectedItem();
            }
            return selectedItem;
        });
    }

    /**
     * Soon to be deleted.
     */
    @FunctionalInterface
    interface CallbackCaller
    {
        /**
         * Bla.
         *
         * @param successful bla
         * @param source bla
         */
        void call(boolean successful, ShapeFileSource source);
    }

    /**
     * Convert the needlessly lengthy expression into an elegant method call.
     *
     * @return bla
     */
    private JFrame getMainFrame()
    {
        return ctrl.getToolbox().getUIRegistry().getMainFrameProvider().get();
    }

    /**
     * Note that the body is shorter than the name of the return type.
     *
     * @param src bla
     * @return bla.
     */
    private static DataGroupImportCallbackResponse getDgicr(ShapeFileSource src)
    {
        return () -> src.getDataGroupInfo();
    }

    /**
     * Required because Java is stupid.
     *
     * @param <T> reference type
     */
    private static class Ref<T>
    {
        /** Bla. */
        public T val;
    }

    /**
     * Inline conditional, because I despise the "?:" operator. If <i>b</i> is
     * true, then return <i>t</i>; otherwise return null.
     *
     * @param b the truth value of the condition
     * @param t the return value, in case of <i>b</i>
     * @param <T> the type of <i>t</i>
     * @return either <i>t</i> or null
     */
    private static <T> T ifn(boolean b, T t)
    {
        if (b)
        {
            return t;
        }
        return null;
    }
}
