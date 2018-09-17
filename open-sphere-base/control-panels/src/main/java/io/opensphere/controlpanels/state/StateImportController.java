package io.opensphere.controlpanels.state;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler.DropLocation;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.bitsys.fade.mist.state.v4.StateType;

import io.opensphere.core.Toolbox;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.modulestate.ModuleStateManager;
import io.opensphere.core.modulestate.StateV4ReaderWriter;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.modulestate.TagList;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.core.util.net.HttpUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Importer for state files. */
@SuppressWarnings("PMD.GodClass")
public class StateImportController implements FileOrURLImporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(StateImportController.class);

    /**
     * The beginning of the could not read error message.
     */
    private static final String ourCouldNotReadError = "Could not read URL [";

    /**
     * The beginning of the could not parse error message.
     */
    private static final String ourFailedToParseError = "Failed to parse state document: ";

    /** The manager for system states. */
    private final ModuleStateManager myModuleStateManager;

    /** The parent component provider. */
    private final Supplier<? extends Component> myParentProvider;

    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Checks the version of the state file and notifies the user if the version
     * is an unexpected version.
     */
    private final StateVersionChecker myVersionChecker = new StateVersionChecker();

    /**
     * Constructor.
     *
     * @param provider The parent component.
     * @param moduleStateManager The module state manager.
     * @param toolbox The system toolbox.
     */
    public StateImportController(Supplier<? extends JFrame> provider, ModuleStateManager moduleStateManager, Toolbox toolbox)
    {
        myParentProvider = provider;
        myModuleStateManager = moduleStateManager;
        myToolbox = toolbox;
    }

    @Override
    public boolean canImport(File aFile, DropLocation dropLocation)
    {
        boolean canImport = false;
        try (InputStream inputStream = readFile(aFile))
        {
            canImport = inputStream != null && canImport(inputStream);
        }
        catch (IOException e)
        {
            LOGGER.warn(e);
        }
        return canImport;
    }

    @Override
    public boolean canImport(URL aURL, DropLocation dropLocation)
    {
        boolean canImport = false;
        try (InputStream inputStream = readUrl(aURL))
        {
            canImport = inputStream != null && canImport(inputStream);
        }
        catch (IOException e)
        {
            LOGGER.warn(e);
        }
        return canImport;
    }

    @Override
    public String getDescription()
    {
        return "Importer for state files and URLs.";
    }

    @Override
    public JComponent getFileChooserAccessory()
    {
        return null;
    }

    @Override
    public String getImportMultiFileMenuItemName()
    {
        return "Import State File Group";
    }

    @Override
    public String getImportSingleFileMenuItemName()
    {
        return "Import State File";
    }

    @Override
    public String getImportURLFileMenuItemName()
    {
        return "Import State URL";
    }

    @Override
    public String getName()
    {
        return "State";
    }

    @Override
    public int getPrecedence()
    {
        return 200;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return Arrays.asList("xml");
    }

    @Override
    public void importFile(File aFile, ImportCallback callback)
    {
        boolean success = false;
        try (InputStream inputStream = readFile(aFile))
        {
            if (inputStream != null)
            {
                try
                {
                    importStream(inputStream, null);
                    success = true;
                }
                catch (SAXException | IOException | ParserConfigurationException e)
                {
                    LOGGER.error(ourFailedToParseError + e, e);
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.warn(e);
        }

        if (callback != null)
        {
            callback.fileImportComplete(success, aFile, null);
        }
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
    public void importURL(URL aURL, Component component)
    {
        importURL(aURL, component, null);
    }

    @Override
    public void importURL(URL aURL, ImportCallback callback)
    {
        importURL(aURL, null, callback);
    }

    /**
     * Determines if the input stream can be imported.
     *
     * @param inputStream the input stream
     * @return whether it can be imported
     */
    private boolean canImport(InputStream inputStream)
    {
        boolean canImport = false;
        try (BufferedInputStream bufferedStream = new BufferedInputStream(inputStream))
        {
            // peek at the first few bytes of the file, to determine if the text could be
            // XML (this avoids the XML parsing exception if the file is blatantly not XML).
            bufferedStream.mark(1024);
            byte[] firstCharacters = new byte[256];
            int read = bufferedStream.read(firstCharacters);
            String peek = new String(firstCharacters, 0, read, StringUtilities.DEFAULT_CHARSET.toString());

            if (peek.charAt(0) == '<')
            {
                bufferedStream.reset();

                Pair<InputStream, String> result = myVersionChecker.checkVersion(bufferedStream, false);
                String versionNamespace = result.getSecondObject();
                canImport = ModuleStateController.STATE_NAMESPACE_V4.equals(versionNamespace);
                if (!canImport)
                {
                    InputStream normalized = result.getFirstObject();
                    Document doc = XMLUtilities.newDocumentBuilderNS().parse(normalized);
                    canImport = StateXML.newXPath().evaluate("/" + ModuleStateController.STATE_QNAME, doc,
                            XPathConstants.NODE) != null;
                }
            }
        }
        catch (IOException | SAXException | ParserConfigurationException e)
        {
            LOGGER.error(e, e);
        }
        catch (XPathExpressionException e)
        {
            // When this happens we do not need to report an error;
            // this just means that the data cannot be imported.
            LOGGER.warn("Input data does not contain valid XML");
        }
        return canImport;
    }

    /**
     * Imports a URL.
     *
     * @param aURL the URL to import
     * @param component The optional parent component for import UI's.
     * @param callback the optional callback that provides the outcome of the
     *            import.
     */
    private void importURL(URL aURL, Component component, ImportCallback callback)
    {
        boolean success = false;
        try (InputStream inputStream = readUrl(aURL))
        {
            if (inputStream != null)
            {
                try
                {
                    importStream(inputStream, component);
                    success = true;
                }
                catch (SAXException | IOException | ParserConfigurationException e)
                {
                    LOGGER.error(ourFailedToParseError + e, e);
                }
            }
        }
        catch (IOException e)
        {
            LOGGER.warn(e);
        }

        if (callback != null)
        {
            callback.urlImportComplete(success, aURL, null);
        }
    }

    /**
     * Import an input stream that contains application state.
     *
     * @param is The input stream.
     * @param component The parent component of the dialog or null if the main
     *            window should be used.
     * @throws ParserConfigurationException If the DOM parser is not configured.
     * @throws IOException If there is an error reading the stream.
     * @throws SAXException If there is a problem with the data.
     */
    private void importStream(InputStream is, Component component) throws SAXException, IOException, ParserConfigurationException
    {
        Pair<InputStream, String> result = myVersionChecker.checkVersion(is, true);
        InputStream normalized = result.getFirstObject();
        String versionNamespace = result.getSecondObject();
        if (ModuleStateController.STATE_NAMESPACE_V4.equals(versionNamespace))
        {
            try
            {
                StateType state = new StateV4ReaderWriter().read(normalized);
                SwingUtilities.invokeLater(() -> showDialog(component, state));
            }
            catch (JAXBException e)
            {
                throw new IOException(e);
            }
        }
        else
        {
            Element element = XMLUtilities.newDocumentBuilderNS().parse(normalized).getDocumentElement();
            SwingUtilities.invokeLater(() -> showDialog(component, element));
        }
    }

    /**
     * As the name suggests. It must be called on the AWT thread.
     *
     * @param par parent component
     * @param elt the XML DOM Element
     */
    private void showDialog(Component par, Element elt)
    {
        try
        {
            Collection<String> modules = myModuleStateManager.detectModules(elt);
            ImportStateDialog dialog = new ImportStateDialog(par, modules,
                    myModuleStateManager.getStateDependenciesForModules(modules), myModuleStateManager.getRegisteredStateIds());
            XPath xpath = StateXML.newXPath();
            dialog.setStateId(getStateId(xpath, elt));
            dialog.setDescription(getStateDesc(xpath, elt));
            dialog.setTags(getTags(xpath, elt));
            dialog.buildAndShow();
            if (dialog.getSelection() != JOptionPane.OK_OPTION)
            {
                return;
            }

            String id = dialog.getStateId();
            String desc = dialog.getDescription();
            Collection<? extends String> tags = dialog.getTags();
            Collection<? extends String> sel = dialog.getSelectedModules();
            EventQueueUtilities.waitCursorRun(myParentProvider.get(), () ->
            {
                myModuleStateManager.registerState(id, desc, tags, sel, elt);
                myModuleStateManager.toggleState(id);
            });
        }
        catch (XPathExpressionException e)
        {
            LOGGER.error(e, e);
        }
    }

    /**
     * As the name suggests. It must be called on the AWT thread.
     *
     * @param par parent component
     * @param state the state object
     */
    private void showDialog(Component par, StateType state)
    {
        Collection<String> modules = myModuleStateManager.detectModules(state);
        ImportStateDialog dialog = new ImportStateDialog(par, modules,
                myModuleStateManager.getStateDependenciesForModules(modules), myModuleStateManager.getRegisteredStateIds());
        dialog.setStateId(state.getTitle());
        dialog.setDescription(state.getDescription());
        dialog.setTags(state.getTags() != null ? state.getTags().getTag() : Collections.emptyList());
        dialog.buildAndShow();
        if (dialog.getSelection() != JOptionPane.OK_OPTION)
        {
            return;
        }

        String id = dialog.getStateId();
        String desc = dialog.getDescription();
        Collection<? extends String> tags = dialog.getTags();
        Collection<? extends String> sel = dialog.getSelectedModules();
        EventQueueUtilities.waitCursorRun(myParentProvider.get(), () ->
        {
            myModuleStateManager.registerState(id, desc, tags, sel, state);
            myModuleStateManager.toggleState(id);
        });
    }

    /**
     * Reads the file to an input stream.
     *
     * @param file the file
     * @return the input stream, or null if the file could not be read
     */
    private InputStream readFile(File file)
    {
        InputStream inputStream = null;
        if (file != null && file.canRead())
        {
            try
            {
                inputStream = new BufferedInputStream(new FileInputStream(file));
            }
            catch (FileNotFoundException e)
            {
                LOGGER.warn(ourCouldNotReadError + file + "]: " + e, e);
            }
        }
        return inputStream;
    }

    /**
     * Reads the URL to an input stream.
     *
     * @param url the URL
     * @return the input stream, or null if the URL could not be read
     */
    private InputStream readUrl(URL url)
    {
        InputStream inputStream = null;
        if (url != null)
        {
            try
            {
                inputStream = HttpUtilities.sendGet(url, myToolbox.getServerProviderRegistry());
            }
            catch (IOException e)
            {
                LOGGER.warn(ourCouldNotReadError + url + "]: " + e, e);
            }
        }
        return inputStream;
    }

    /**
     * Get the "tags" from a State XML DOM Element.
     *
     * @param xp thingy
     * @param elt the root Element
     * @return a List of the String "tags"
     * @throws XPathExpressionException bla
     */
    private static List<String> getTags(XPath xp, Element elt) throws XPathExpressionException
    {
        try
        {
            Node tagsNode = (Node)xp.evaluate("/" + ModuleStateController.STATE_QNAME + "/:tags", elt, XPathConstants.NODE);
            if (tagsNode == null)
            {
                return Collections.<String>emptyList();
            }
            TagList tl = XMLUtilities.readXMLObject(tagsNode, TagList.class);
            if (tl == null)
            {
                return Collections.<String>emptyList();
            }
            return tl.getTags();
        }
        catch (JAXBException | XPathExpressionException e)
        {
            LOGGER.error("Failed to unmarshal tag list from state file: " + e, e);
        }
        return Collections.<String>emptyList();
    }

    /**
     * Get the id (called "title") from a State XML DOM Element.
     *
     * @param xp thingy
     * @param elt the root Element
     * @return big surprise: the State ID
     * @throws XPathExpressionException bla
     */
    private static String getStateId(XPath xp, Element elt) throws XPathExpressionException
    {
        return xp.evaluate("/" + ModuleStateController.STATE_QNAME + "/:title", elt);
    }

    /**
     * Get the description String from a State XML DOM Element.
     *
     * @param xp thingy
     * @param elt the root Element
     * @return the description, you bet
     * @throws XPathExpressionException bla
     */
    private static String getStateDesc(XPath xp, Element elt) throws XPathExpressionException
    {
        return xp.evaluate("/" + ModuleStateController.STATE_QNAME + "/:description", elt);
    }
}
