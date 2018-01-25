package io.opensphere.csv;

import java.awt.Component;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler.DropLocation;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.predicate.EndsWithPredicate;
import io.opensphere.core.util.swing.wizard.WizardCallback;
import io.opensphere.core.util.swing.wizard.WizardController;
import io.opensphere.csv.config.v2.CSVDataSource;
import io.opensphere.csv.ui.controller.CSVImportWizardController;
import io.opensphere.mantle.data.DataGroupImportCallbackResponse;

/**
 * The Class CSVFileImporter.
 */
@SuppressWarnings("PMD.GodClass")
public class CSVFileImporter implements FileOrURLImporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(CSVFileImporter.class);

    /** Bla. */
    private static final String CONFIRM_MSG_PREFIX = "The file has already been loaded under the name ";
    /** Bla. */
    private static final String CONFIRM_MSG_SUFFIX = "\nDo you want to load the file again?";

    /** The allowable file extensions. */
    private static final List<String> ourFileExtensions = New.unmodifiableList("csv", "txt");

    /** The my controller. */
    private final CSVFileDataSourceController myController;

    /**
     * Instantiates a new CSV file importer.
     *
     * @param controller the controller
     */
    public CSVFileImporter(CSVFileDataSourceController controller)
    {
        myController = controller;
    }

    @Override
    public boolean canImport(File file, DropLocation dropLocation)
    {
        return file != null && file.canRead() && new EndsWithPredicate(ourFileExtensions, true).test(file.getAbsolutePath());
    }

    @Override
    public boolean canImport(URL url, DropLocation dropLocation)
    {
        ServerProvider<HttpServer> provider = myController.getToolbox().getServerProviderRegistry()
                .getProvider(HttpServer.class);
        ResponseValues response = new ResponseValues();
        // If it looks like plain text data, then return true
        try (InputStream inputStream = provider.getServer(url).sendGet(url, response))
        {
            if (response.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                return false;
            }

            int in = 0;
            int total = 0;
            DataInputStream dataStream = new DataInputStream(inputStream);
            try
            {
                for (int i = 0; i < 10000; ++i)
                {
                    ++total;
                    if (isOrdinaryCharacter(dataStream.readByte()))
                    {
                        ++in;
                    }
                }
            }
            catch (EOFException e)
            {
                LOGGER.info("End of file reached");
            }

            double tolerance = (10 - .5) / 10;
            return in / (double)total > tolerance;
        }
        catch (IOException | URISyntaxException e)
        {
            LOGGER.error("Could not read shape file." + e, e);
        }
        return false;
    }

    @Override
    public String getDescription()
    {
        return "Importer for delimited or fixed with column based text files with one record per line.";
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        return null;
    }

    @Override
    public String getImportMultiFileMenuItemName()
    {
        return "Import CSV File Group";
    }

    @Override
    public String getImportSingleFileMenuItemName()
    {
        return "Import CSV File";
    }

    @Override
    public String getImportURLFileMenuItemName()
    {
        return "Import CSV URL";
    }

    @Override
    public String getName()
    {
        return "CSV";
    }

    @Override
    public int getPrecedence()
    {
        return 1350;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return ourFileExtensions;
    }

    @Override
    public void importURL(URL url, ImportCallback cb)
    {
        URI u = toUri(url);
        if (u == null)
        {
            cb.urlImportComplete(false, url, null);
        }
        else
        {
            proceedIfOkay(u, () -> doImport(u, s -> cb.urlImportComplete(true, url, dgicbr(s))),
                    () -> cb.urlImportComplete(false, url, null));
        }
    }

    @Override
    public void importFile(File file, ImportCallback cb)
    {
        URI u = file.toURI();
        // extra parentheses added to keep the style checker from bitching
        proceedIfOkay(u, () -> doImport(u, s -> cb.fileImportComplete(true, file, dgicbr(s))),
                () -> cb.fileImportComplete(false, file, null));
    }

    /**
     * Please note that the body of this method is shorter than the name of the
     * return type.
     * @param s bla
     * @return bla
     */
    private DataGroupImportCallbackResponse dgicbr(CSVDataSource s)
    {
        return () -> s.getDataGroupInfo();
    }

    /**
     * Bla.
     * @param uri bla
     * @param cb bla
     */
    private void doImport(URI uri, Consumer<CSVDataSource> cb)
    {
        CSVDataSource src = new CSVDataSource(uri);
        Set<String> importedNames = New.set();
        for (CSVDataSource s : myController.getCsvSourcesAlready())
        {
            importedNames.add(s.getName());
        }
        if (cb == null)
        {
            importSource(src, importedNames, null);
        }
        else
        {
            importSource(src, importedNames, () -> cb.accept(src));
        }
    }

    /**
     * Use the AWT thread to query the user before proceeding with work on the
     * worker thread.  In case of failure, invoke the error handler.
     * @param uri request URI
     * @param work the work to be done
     * @param onErr error handler
     */
    private void proceedIfOkay(URI uri, Runnable work, Runnable onErr)
    {
        SwingUtilities.invokeLater(() ->
        {
            if (continueIfLoaded(uri))
            {
                ThreadUtilities.runBackground(work);
            }
            else
            {
                ThreadUtilities.runBackground(onErr);
            }
        });
    }

    /**
     * Check to see if this CSV file is already loaded and allow the user to
     * cancel if they do not wish to reload the file again.
     *
     * @param uri The URI which specifies the file location.
     * @return true if the file should be loaded.
     */
    private boolean continueIfLoaded(URI uri)
    {
        for (CSVDataSource s : myController.getCsvSourcesAlready())
        {
            if (s.getSourceUri().equals(uri) && !confirmLoad(s.getName()))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Show a dialog requesting the user to confirm reloading a CSV.  Only call
     * on the AWT thread.
     * @param srcName name of the already loaded CSV
     * @return true in case of user confirmation
     */
    private boolean confirmLoad(String srcName)
    {
        int value = JOptionPane.showConfirmDialog(mainFrame(),
                CONFIRM_MSG_PREFIX + srcName + CONFIRM_MSG_SUFFIX,
                "Duplicate File", JOptionPane.OK_CANCEL_OPTION);
        return value == JOptionPane.OK_OPTION;
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

    /**
     * Perform the import, etc., and then use the provided callback to report
     * the eventual completion of the import.
     * @param source the thing to be imported (a File or URL)
     * @param namesInUse bla
     * @param callback used to report completion
     */
    public void importSource(CSVDataSource source, Set<String> namesInUse, Runnable callback)
    {
        assert !SwingUtilities.isEventDispatchThread();

        Toolbox toolbox = myController.getToolbox();

        // Update the data source
        source.setSourceUri(new File(source.getFileLocalPath(toolbox)).toURI());
        source.setActive(true);

        // Run the wizard
        WizardCallback wizardCallback = new WizardCallback()
        {
            @Override
            public void run(WizardController controller)
            {
                assert SwingUtilities.isEventDispatchThread();
                // Remove the source if it exists (i.e. this is a re-import)
                if (myController.hasSource(source))
                {
                    myController.removeSource(source, true, mainFrame());
                }
                myController.addSource(source);
                if (callback != null)
                {
                    callback.run();
                }
            }
        };
        SwingUtilities.invokeLater(() ->
        {
            try
            {
                new CSVImportWizardController(toolbox).startWizard(
                        mainFrame(), source, namesInUse, wizardCallback);
            }
            catch (FileNotFoundException e)
            {
                LOGGER.error(e, e);
            }
        });
    }

    /**
     * Convert a nasty, ugly expression into an elegant method call.
     * @return bla
     */
    private JFrame mainFrame()
    {
        return myController.getToolbox().getUIRegistry().getMainFrameProvider().get();
    }

    @Override
    public boolean importsURLs()
    {
        return true;
    }

    @Override
    public void importURL(URL url, Component component)
    {
        importURL(url, (ImportCallback)null);
    }

    /**
     * Tell whether the given byte represents a ordinary character.
     *
     * @param ascii The character to check.
     * @return true, when this is an ordinary character.
     */
    private boolean isOrdinaryCharacter(byte ascii)
    {
        return ascii > 31 && ascii < 127 || ascii == 9 || ascii == 10 || ascii == 13;
    }

    /**
     * Bla.
     * @param u bla
     * @return bla
     */
    private static URI toUri(URL u)
    {
        try
        {
            if (u != null)
            {
                return u.toURI();
            }
        }
        catch (URISyntaxException eek)
        {
            LOGGER.error(eek, eek);
        }
        return null;
    }
}
