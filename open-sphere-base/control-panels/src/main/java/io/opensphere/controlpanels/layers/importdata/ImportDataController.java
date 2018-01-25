package io.opensphere.controlpanels.layers.importdata;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.scene.Node;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler.DropLocation;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import io.opensphere.controlpanels.layers.event.AvailableGroupSelectionEvent;
import io.opensphere.core.Toolbox;
import io.opensphere.core.event.EventListener;
import io.opensphere.core.event.ImportDataEvent;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.importer.ImportType;
import io.opensphere.core.importer.ImporterRegistry;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.WeakChangeSupport;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.mantle.data.DataGroupImportCallbackResponse;
import io.opensphere.mantle.util.importer.URLDataSource;
import io.opensphere.mantle.util.importer.impl.URLDataLoader;

/**
 * The Class ImportDataController.
 */
@SuppressWarnings("PMD.GodClass")
public final class ImportDataController
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(ImportDataController.class);

    /** Prefix for overwrite confirmation message. */
    private static final String OW_PREFIX = "The file ";

    /** Suffix for overwrite confirmation message. */
    private static final String OW_SUFFIX = " already exists.  Do you want to over-write it?";

    /** The Constant ourEventExecutor. */
    private static final ThreadPoolExecutor ourDispatchExecutor = new ThreadPoolExecutor(1, 1, 500, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("ImportDataController::Dispatch"));

    /** The singleton instance. */
    private static ImportDataController ourInstance;

    /** The my change support. */
    private final WeakChangeSupport<ImportDataControllerListener> myChangeSupport = new WeakChangeSupport<>();

    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The ImporterRegistry. */
    private final ImporterRegistry importerRegistry;

    /** Listener for ImportDataEvents. */
    private final ImportEventListener importListener = new ImportEventListener();

    /** Listener for import events. */
    private final MyImportCallback importCallback = new MyImportCallback();

    /** Listener for registration of importers. */
    private final MyImporterRegistryListener importRegistryListener = new MyImporterRegistryListener();

    static
    {
        ourDispatchExecutor.allowCoreThreadTimeOut(true);
    }

    /**
     * Generate importer file filter description.
     *
     * @param importer the importer
     * @return the string
     */
    private static String getFileFilterDescription(FileOrURLImporter importer)
    {
        return getFileFilterDescription(importer.getName(), importer.getSupportedFileExtensions());
    }

    /**
     * Generate importer file filter description.
     *
     * @param name the name
     * @param extensions the extensions
     * @return the string
     */
    private static String getFileFilterDescription(String name, List<String> extensions)
    {
        if (extensions == null || extensions.isEmpty())
        {
            return name;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" [");
        final Iterator<String> it = extensions.iterator();
        sb.append("*.").append(it.next());
        while (it.hasNext())
        {
            sb.append(", ").append("*.").append(it.next());
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Gets the singleton instance of this class.
     *
     * @param tb The tool
     * @return the singleton instance
     */
    public static synchronized ImportDataController getInstance(Toolbox tb)
    {
        if (ourInstance == null)
        {
            ourInstance = new ImportDataController(tb);
        }
        return ourInstance;
    }

    /**
     * Instantiates a new import data controller.
     *
     * @param tb the {@link Toolbox}
     */
    private ImportDataController(Toolbox tb)
    {
        myToolbox = tb;
        importerRegistry = myToolbox.getImporterRegistry();
        importerRegistry.addListener(importRegistryListener);
        myToolbox.getEventManager().subscribe(ImportDataEvent.class, importListener);
    }

    /**
     * Adds the listener.
     *
     * @param listener the listener
     */
    public void addListener(ImportDataControllerListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Import file.
     */
    public void importFile()
    {
        assert EventQueue.isDispatchThread();
        importFileInternal(null);
    }

    /**
     * Import file group.
     */
    public void importFileGroup()
    {
        assert EventQueue.isDispatchThread();
        importFileGroupInternal(null);
    }

    /**
     * Import specific.
     *
     * @param importer the importer
     * @param importType the import type
     */
    public void importSpecific(FileOrURLImporter importer, ImportType importType)
    {
        assert EventQueue.isDispatchThread();
        switch (importType)
        {
            case FILE:
                importFileInternal(importer);
                break;
            case FILE_GROUP:
                importFileGroupInternal(importer);
                break;
            case URL:
                importURLInternal(importer);
                break;
            default:
                break;
        }
    }

    /**
     * Import url.
     */
    public void importURL()
    {
        assert EventQueue.isDispatchThread();
        importURLInternal(null);
    }

    /**
     * Removes the listener.
     *
     * @param listener the listener
     */
    public void removeListener(ImportDataControllerListener listener)
    {
        myChangeSupport.removeListener(listener);
    }

    /**
     * Creates the single file file chooser.
     *
     * @param multiFile the multi file
     * @param importerToUse the importer to use
     * @param importers the importers
     * @return the OpenSphere file chooser.
     */
    private MnemonicFileChooser createFileChooser(boolean multiFile, FileOrURLImporter importerToUse,
            final List<FileOrURLImporter> importers)
    {
        final MnemonicFileChooser chooser = new MnemonicFileChooser(myToolbox.getPreferencesRegistry(), "ImportDataController");
        chooser.setSize(800, 600);
        chooser.setMinimumSize(chooser.getSize());
        chooser.setDialogTitle("Import File" + (multiFile ? "s" : ""));
        chooser.setPreferredSize(chooser.getSize());
        chooser.setMultiSelectionEnabled(multiFile);
        if (importerToUse != null)
        {
            chooser.setDialogTitle("Import " + importerToUse.getName() + " File" + (multiFile ? "s" : ""));
            final List<String> extensions = importerToUse.getSupportedFileExtensions();
            if (extensions != null && !extensions.isEmpty())
            {
                final FileNameExtensionFilter filter = new FileNameExtensionFilter(getFileFilterDescription(importerToUse),
                        extensions.toArray(new String[extensions.size()]));
                chooser.addChoosableFileFilter(filter);
                chooser.setFileFilter(filter);
            }
            if (importerToUse.getFileChooserAccessory() != null)
            {
                chooser.setAccessory(importerToUse.getFileChooserAccessory());
            }
        }
        else
        {
            final List<String> allExtensions = New.list();
            for (final FileOrURLImporter importer : importers)
            {
                final List<String> extensions = importer.getSupportedFileExtensions();
                if (extensions != null && !extensions.isEmpty())
                {
                    allExtensions.addAll(extensions);
                    chooser.addChoosableFileFilter(new ImporterFileNameExtensionFilter(importer));
                }
            }
            final FileNameExtensionFilter everything = new FileNameExtensionFilter(getFileFilterDescription("All", allExtensions),
                    allExtensions.toArray(new String[allExtensions.size()]));
            chooser.addChoosableFileFilter(everything);
            chooser.setFileFilter(everything);
        }
        return chooser;
    }

    /**
     * Generic method for presenting a JavaFX Node in a JFXDialog container. If
     * the Node implements the Editor "magic" interface, then JFXDialog will
     * treat it as an Editor by calling the interface methods, as appropriate.
     *
     * @param root the Node to be displayed
     * @param w the JFXDialog width, in pixels
     * @param h the JFXDialog height, in pixels
     */
    private void showDialog(Node root, int w, int h)
    {
        final JFrame parent = getFrame();
        final JFXDialog dialog = new JFXDialog(parent, "Select Importer");
        dialog.setFxNode(root);
        dialog.setSize(w, h);
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        // Note: does not currently allow a confirmer to be added; when moved
        // out to a utility class, this feature should be added as an
        // overloaded method.
    }

    /**
     * Assembles File-specific tasks before delegating to the generic doImport
     * method, which invokes the tasks as required on the appropriate Threads.
     *
     * @param foui a FileOrURLImporter to use, if non-null
     * @param f the File to be imported
     * @param d in case of drag-and-drop
     */
    private void doImportFile(FileOrURLImporter foui, File f, DropLocation d)
    {
        // Note: unnecessary parentheses around the lambda prevent the style
        // checker from bitching
        doImport(f.getAbsolutePath(), foui, i -> i.importsFiles() && i.canImport(f, d), i -> i.importFile(f, importCallback),
            () -> unsupportedError(f));
    }

    /**
     * Assembles URL-specific tasks before delegating to the generic doImport
     * method, which invokes the tasks as required on the appropriate Threads.
     *
     * @param foui a FileOrURLImporter to use, if non-null
     * @param u the URL to be imported
     * @param d in case of drag-and-drop
     */
    private void doImportUrl(FileOrURLImporter foui, URL u, DropLocation d)
    {
        // Note: unnecessary parentheses around the lambda prevent the style
        // checker from bitching
        doImport(u.toString(), foui, i -> i.importsURLs() && i.canImport(u, d), i -> i.importURL(u, importCallback),
            () -> offerDownloadToFile(u, d));
    }

    /**
     * Assembles File group-specific tasks before delegating to the generic
     * doImport method, which invokes the tasks as required on the appropriate
     * Threads.
     *
     * @param foui a FileOrURLImporter to use, if non-null
     * @param files an array of files to be imported
     * @param d in case of drag-and-drop
     */
    private void doImportMultiFile(FileOrURLImporter foui, File[] files, DropLocation d)
    {
        // Note: unnecessary parentheses around the lambda prevent the style
        // checker from bitching
        doImport("Multiple files.", foui, i -> i.importsFileGroups() && canImportAll(i, files, d),
            i -> i.importFiles(Arrays.asList(files), importCallback), () -> unsupportedError(files));
    }

    /**
     * Get importers that can do the desired operation. Because <i>canDo</i> has
     * the potential to use networking, this method must be called on a worker
     * thread.
     *
     * @param canDo the test for valid importers
     * @return the list of matches
     */
    private List<FileOrURLImporter> findImp(Predicate<FileOrURLImporter> canDo)
    {
        return importerRegistry.getImporters(i -> i.getName() != null && canDo.test(i), FileOrURLImporter.LEX_ORDER);
    }

    /**
     * Generic import method that handles the threading issues related to
     * performing imports, but does not commit to a particular type of source
     * (i.e., File vs. URL). Note: while this method can be invoked on or off
     * the AWT thread, it immediately pawns the work off to a worker thread.
     *
     * @param src String representation of the source File or URL
     * @param foui a FileOrURLImporter to use, if non-null
     * @param canDo a test for the applicability of a FileOrURLImporter
     * @param impAction the method of import (e.g., importURL)
     * @param onErr what to do in case of error
     */
    private void doImport(String src, FileOrURLImporter foui, Predicate<FileOrURLImporter> canDo,
            Consumer<FileOrURLImporter> impAction, Runnable onErr)
    {
        // This method may be called on or off the AWT thread ...
        ThreadUtilities.runBackground(() ->
        {
            // if the importer was specified, use it; otherwise try to find one
            if (foui != null)
            {
                impAction.accept(foui);
            }
            else
            {
                selectAndImport(findImp(canDo), src, onErr, impAction);
            }
        });
    }

    /**
     * Select a single importer from the list of those available, and then use
     * it to import the specified source, if possible. This method must be
     * invoked on a worker thread. If necessary, the user will be queried to
     * choose the importer on the AWT thread, but regardless, the importer is
     * invoked on the worker thread.
     *
     * @param impList bla
     * @param src bla
     * @param onErr what to do in case of error
     * @param impAction the method of import (e.g., importURL)
     */
    private void selectAndImport(List<FileOrURLImporter> impList, String src, Runnable onErr,
            Consumer<FileOrURLImporter> impAction)
    {
        if (impList == null || impList.isEmpty())
        {
            return;
        }
        if (impList.size() == 1)
        {
            impAction.accept(impList.get(0));
        }
        else
        {
            guiToWork(() -> selectAmong(impList, src, onErr), i -> impAction.accept(i));
        }
    }

    /**
     * Select and return an importer from among those listed. If there is more
     * than one, the user must choose. Only invoke on the AWT thread.
     *
     * @param fouis the capable importers
     * @param src the source file or URL
     * @param onErr callback for error handling
     * @return bla
     */
    private FileOrURLImporter selectAmong(List<FileOrURLImporter> fouis, String src, Runnable onErr)
    {
        // if there is only one, just go with that; if more than one, the user
        // must choose; if there are none, handle the error and then punt
        if (fouis.size() == 1)
        {
            return fouis.get(0);
        }
        else if (fouis.size() > 1)
        {
            return userSelect(fouis, src);
        }
        else if (onErr != null)
        {
            onErr.run();
        }
        return null;
    }

    /**
     * Present the capable importers to the user to choose one. This is a GUI
     * thing, so it must be called on the GUI thread.
     *
     * @param fouis the list of capable importers
     * @param src the file or URL to be imported
     * @return the user's selection
     */
    private FileOrURLImporter userSelect(List<FileOrURLImporter> fouis, String src)
    {
        final ChooserPane cp = new ChooserPane();
        cp.setSource(src);
        cp.setFoui(fouis);
        showDialog(cp.getMainPane(), 350, 400);
        // if canceled, we are done
        // Note: in case of a URL we may still want to offer to download when
        // the user elected to cancel.
        if (!cp.isAccepted())
        {
            return null;
        }
        // return the user's chosen importer
        return cp.getSelected();
    }

    /**
     * Similar to its namesake, this method defaults <i>keepNull</i> to false.
     *
     * @param guiTask bla
     * @param workTask bla
     * @param <T> bla
     */
    private static <T> void guiToWork(Supplier<T> guiTask, Consumer<T> workTask)
    {
        guiToWork(guiTask, workTask, false);
    }

    /**
     * Execute a task on a worker thread and pass the result to a task running
     * on the GUI thread. This method optionally short-circuits the GUI task
     * when the worker task produces a null result.
     *
     * @param guiTask bla
     * @param workTask bla
     * @param keepNull if true, invoke the worker task even when the argument is
     *            null
     * @param <T> the type of data to be passed
     */
    private static <T> void guiToWork(Supplier<T> guiTask, Consumer<T> workTask, boolean keepNull)
    {
        final Ref<T> r = new Ref<>();
        SwingUtilities.invokeLater(() ->
        {
            r.val = guiTask.get();
            if (keepNull || r.val != null)
            {
                ThreadUtilities.runBackground(() -> workTask.accept(r.val));
            }
        });
    }

    /**
     * Mutable reference. This is needed because Java is stupid.
     *
     * @param <T> bla
     */
    private static class Ref<T>
    {
        /** Bla. */
        public T val;
    }

    /**
     * Check for multi-file importability.
     *
     * @param imp bla
     * @param files bla
     * @param d bla
     * @return true if and only if all files are importable
     */
    private static boolean canImportAll(FileOrURLImporter imp, File[] files, DropLocation d)
    {
        for (final File f : files)
        {
            if (!imp.canImport(f, d))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Download url to file.
     *
     * @param urlToDownload the url to download
     * @param outputFile the output file
     * @return true, if successful
     */
    private boolean downloadURLToFile(URL urlToDownload, File outputFile)
    {
        boolean success = true;
        final URLDataSource uds = new URLDataSource(urlToDownload);
        final URLDataLoader loader = new URLDataLoader(uds, myToolbox);
        InputStream is = null;
        ProgressMonitorInputStream monIs = null;
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(outputFile);
            is = loader.load();
            monIs = new ProgressMonitorInputStream(getFrame(), "Downloading: " + urlToDownload.toExternalForm(), is);

            byte[] buff = new byte[1024 * 100];
            while (monIs.available() > 0)
            {
                final int read = monIs.read(buff);
                fos.write(buff, 0, read);
            }
            buff = null;
        }
        catch (final IOException e)
        {
            LOGGER.error("Exception downloading : " + urlToDownload.toExternalForm(), e);
            success = false;
        }
        finally
        {
            Utilities.closeQuietly(monIs, is, fos);
        }
        return success;
    }

    /**
     * Import complete.
     *
     * @param success true if successful import
     * @param responseObject the response object
     */
    private void importComplete(boolean success, Object responseObject)
    {
        if (success && responseObject instanceof DataGroupImportCallbackResponse)
        {
            myToolbox.getEventManager().publishEvent(
                    new AvailableGroupSelectionEvent(((DataGroupImportCallbackResponse)responseObject).getNewOrChangedGroup()));
        }
    }

    /**
     * Import file group internal.
     *
     * If importerToUse is null will attempt to determine which to use.
     *
     * @param pImporterToUse the importer to use
     */
    private void importFileGroupInternal(final FileOrURLImporter pImporterToUse)
    {
        FileOrURLImporter importerToUse = pImporterToUse;
        final List<FileOrURLImporter> importers = importerRegistry
                .getImporters(value -> value.getName() != null && value.importsFileGroups(), FileOrURLImporter.LEX_ORDER);

        // Have the user choose a file
        final MnemonicFileChooser chooser = createFileChooser(true, importerToUse, importers);
        final int result = chooser.showOpenDialog(getFrame());
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File[] selFiles = chooser.getSelectedFiles();
            if (selFiles != null && selFiles.length > 0)
            {
                if (chooser.getFileFilter() instanceof ImporterFileNameExtensionFilter)
                {
                    importerToUse = ((ImporterFileNameExtensionFilter)chooser.getFileFilter()).getImporter();
                }

                // Do the import
                doImportMultiFile(importerToUse, selFiles, null);
            }
        }
    }

    /**
     * Import file internal.
     *
     * If importerToUse is null will attempt to determine which to use.
     *
     * @param pImporterToUse the importer to use
     */
    private void importFileInternal(final FileOrURLImporter pImporterToUse)
    {
        FileOrURLImporter importerToUse = pImporterToUse;
        final List<FileOrURLImporter> importers = importerRegistry.getImporters(value -> value.importsFiles(),
                FileOrURLImporter.PREC_ORDER);

        // Have the user choose a file
        final MnemonicFileChooser chooser = createFileChooser(false, importerToUse, importers);
        final int result = chooser.showOpenDialog(getFrame());
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File selFile = chooser.getSelectedFile();
            if (selFile != null)
            {
                if (chooser.getFileFilter() instanceof ImporterFileNameExtensionFilter)
                {
                    importerToUse = ((ImporterFileNameExtensionFilter)chooser.getFileFilter()).getImporter();
                }

                // Do the import
                doImportFile(importerToUse, selFile, null);
            }
        }
    }

    /**
     * Import URL internal.
     *
     * If importerToUse is null will attempt to determine which to use.
     *
     * @param pImporterToUse the importer to use
     */
    private void importURLInternal(final FileOrURLImporter pImporterToUse)
    {
        String title = "Import URL";
        if (pImporterToUse != null)
        {
            title = "Import " + pImporterToUse.getName() + " URL";
        }

        // Have the user enter a URL
        final URLImportPanel urlPanel = new URLImportPanel("");
        while (true)
        {
            if (!queryOkCan(title, urlPanel))
            {
                break;
            }
            if (urlPanel.isURLValid())
            {
                // Do the import
                doImportUrl(pImporterToUse, urlPanel.getURL(), null);
                break;
            }
        }
    }

    /**
     * Offer download to file.
     *
     * @param url the file or url
     * @param dropLocation The drop location for DND support.
     */
    private void offerDownloadToFile(final URL url, DropLocation dropLocation)
    {
        final String message = StringUtilities.concat("No importer was identified for the url:\n\n ", url.toExternalForm(),
                "\n\nWould you like to try downloading and importing the content as a file?");
        if (!queryYesNo("Download URL to file?", message))
        {
            return;
        }

        File aFile = null;
        while (aFile == null)
        {
            final MnemonicFileChooser chooser = new MnemonicFileChooser(myToolbox.getPreferencesRegistry(),
                    "ImportDataController");

            try
            {
                final String urlText = url.toExternalForm();
                final int index = urlText.lastIndexOf('/');
                if (index != -1)
                {
                    final String namePrefix = urlText.substring(index + 1);
                    final File currentDir = chooser.getCurrentDirectory();
                    final File defaultFile = new File(currentDir.getAbsolutePath() + File.separator + namePrefix);
                    chooser.setSelectedFile(defaultFile);
                }
            }
            catch (final RuntimeException e)
            {
                // Don't do anything.
                LOGGER.error(e);
            }

            chooser.showSaveDialog(getFrame());
            aFile = chooser.getSelectedFile();
            if (aFile == null)
            {
                return;
            }
            if (aFile.exists() && !queryOkCan("Confirm Over-write", OW_PREFIX + aFile.getName() + OW_SUFFIX))
            {
                aFile = null;
            }
        }
        if (downloadURLToFile(url, aFile))
        {
            doImportFile(null, aFile, dropLocation);
        }
        else
        {
            EventQueueUtilities.invokeLater(() -> showError("Download Error",
                    "An error was encountered while trying to download the url:\n" + url.toExternalForm()));
        }
    }

    /**
     * Query the user for acceptance.
     *
     * @param title the title of the popup
     * @param msg the message displayed to the user
     * @return true if and only if the user accepted
     */
    private boolean queryOkCan(String title, Object msg)
    {
        return queryUser(title, msg, JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Query the user for affirmation.
     *
     * @param title the title of the popup
     * @param msg the message displayed to the user
     * @return true if and only if the user affirmed
     */
    private boolean queryYesNo(String title, Object msg)
    {
        return queryUser(title, msg, JOptionPane.YES_NO_OPTION);
    }

    /**
     * Query the user for affirmation or acceptance.
     *
     * @param title the title of the popup
     * @param msg the message displayed to the user
     * @param opt either JOptionPane.YES_NO_OPTION or
     *            JOptionPane.OK_CANCEL_OPTION
     * @return true if and only if the user affirmed or accepted
     */
    private boolean queryUser(String title, Object msg, int opt)
    {
        final int result = JOptionPane.showConfirmDialog(getFrame(), msg, title, opt);
        // Note: currently, the values of YES_OPTION and OK_OPTION are the same
        return result == JOptionPane.YES_OPTION || result == JOptionPane.OK_OPTION;
    }

    /**
     * Show an error message indicating failure to import.
     *
     * @param fileOrUrl attempted import
     */
    private void unsupportedError(Object fileOrUrl)
    {
        final Object type = fileOrUrl instanceof URL ? "URL" : "file";
        final String message = "<html>Unable to import " + fileOrUrl + "<br>The " + type
                + " is either not readable or its type is not supported.</html>";
        showError("Import Error", message);
    }

    /**
     * Show an error message to the user.
     *
     * @param title the title of the popup
     * @param msg the message displayed to the user
     */
    private void showError(String title, String msg)
    {
        JOptionPane.showMessageDialog(getFrame(), msg, title, JOptionPane.ERROR_MESSAGE);
    }

    /** Listener for ImportDataEvents. */
    private class ImportEventListener implements EventListener<ImportDataEvent>
    {
        @Override
        public void notify(ImportDataEvent event)
        {
            final List<File> files = event.getFiles();
            if (files.isEmpty())
            {
                EventQueueUtilities.invokeLater(() -> importFile());
            }
            else
            {
                EventQueueUtilities.runOnEDT(() -> doImportFile(null, files.get(0), event.getDropLocation()));
            }
        }
    }

    /** Listener for import events. */
    private class MyImportCallback implements ImportCallback
    {
        @Override
        public void fileGroupImportComplete(boolean success, List<File> files, Object responseObject)
        {
            importComplete(success, responseObject);
            LOGGER.info("File Group Import Complete: " + success + " File count: " + files.size());
        }

        @Override
        public void fileImportComplete(boolean success, File aFile, Object responseObject)
        {
            importComplete(success, responseObject);
            LOGGER.info("File Import Complete: " + success + " File: " + aFile.getAbsolutePath());
        }

        @Override
        public void urlImportComplete(boolean success, URL aURL, Object responseObject)
        {
            importComplete(success, responseObject);
            LOGGER.info("URL Import Complete: " + success + " URL: " + aURL.toExternalForm());
        }
    }

    /** Listener for registration of importers. */
    private class MyImporterRegistryListener implements ImporterRegistry.ImporterRegistryListener
    {
        @Override
        public void importersChanged()
        {
            myChangeSupport.notifyListeners(listener -> listener.importersChanged(), ourDispatchExecutor);
        }
    }

    /**
     * The listener interface for receiving ImportDataController notifications.
     */
    @FunctionalInterface
    public interface ImportDataControllerListener
    {
        /**
         * Importers changed.
         */
        void importersChanged();
    }

    /** Bla. */
    private static class ImporterFileNameExtensionFilter extends FileFilter
    {
        /** The filter. */
        private final FileNameExtensionFilter myFilter;

        /** The importer. */
        private final FileOrURLImporter myImporter;

        /**
         * Instantiates a new importer file name extension filter.
         *
         * @param importer the importer
         */
        public ImporterFileNameExtensionFilter(FileOrURLImporter importer)
        {
            myImporter = importer;
            myFilter = new FileNameExtensionFilter(getFileFilterDescription(importer),
                    importer.getSupportedFileExtensions().toArray(new String[0]));
        }

        @Override
        public boolean accept(File f)
        {
            return myFilter.accept(f);
        }

        @Override
        public String getDescription()
        {
            return myFilter.getDescription();
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
    }

    /**
     * Replace the gigantic, crappy expression with a simple method call.
     *
     * @return the main JFrame
     */
    private JFrame getFrame()
    {
        return myToolbox.getUIRegistry().getMainFrameProvider().get();
    }
}
