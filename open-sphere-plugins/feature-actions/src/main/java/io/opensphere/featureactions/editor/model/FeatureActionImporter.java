package io.opensphere.featureactions.editor.model;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler.DropLocation;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.bitsys.fade.mist.state.v4.FeatureActionArrayType;
import com.bitsys.fade.mist.state.v4.ObjectFactory;

import io.opensphere.controlpanels.state.NonCloseableInputStream;
import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.modulestate.ModuleStateController;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.fx.FXUtilities;
import io.opensphere.core.util.io.StreamReader;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.core.util.lang.StringUtilities;
import io.opensphere.featureactions.model.FeatureAction;
import io.opensphere.featureactions.model.FeatureActions;
import io.opensphere.featureactions.model.FeatureActionsFactory;

/** Provides a import from file menu for feature actions. */
public class FeatureActionImporter implements FileOrURLImporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FeatureActionImporter.class);

    /** The list of supported file extensions. */
    private static final List<String> SUPPORTED_EXT = Collections.singletonList("xml");

    /** The model of the feature actions. */
    private SimpleFeatureActions myModel;

    /**
     * Constructor.
     *
     * @param model The model of feature actions for the layer we are importing
     *            to.
     */
    public FeatureActionImporter(SimpleFeatureActions model)
    {
        myModel = model;
    }

    @Override
    public boolean canImport(File file, DropLocation dropLocation)
    {
        if (file != null && file.canRead())
        {
            String fileName = file.getAbsolutePath().toLowerCase();
            return fileName.endsWith(SUPPORTED_EXT.get(0));
        }
        return false;
    }

    @Override
    public boolean canImport(URL url, DropLocation dropLocation)
    {
        return false;
    }

    @Override
    public String getDescription()
    {
        return "Importer for Feature Actions.";
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
        return "Import Feature Actions File";
    }

    @Override
    public String getImportURLFileMenuItemName()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return "Feature Actions";
    }

    @Override
    public int getPrecedence()
    {
        return 875;
    }

    @Override
    public List<String> getSupportedFileExtensions()
    {
        return SUPPORTED_EXT;
    }

    /**
     * Gets the state namespace from the supplied input stream, returning a pair
     * of objects with the namespace as the left hand object and the input
     * stream as the right hand side. Data is read from the supplied stream. If
     * the supplied stream supports {@link InputStream#mark(int)} and
     * {@link InputStream#reset()}, then the input stream is marked, read, and
     * reset to its position. Otherwise, the stream is read in its entirety,
     * then wrapped in a new stream and returned.
     * <p>
     * This method has side-effects of massaging the namespaces of known faults
     * from other applications. Specifically:
     * <ul>
     * <li>The namespace http://www.bit-sys.com/mist/state/v4 is not valid for
     * feature actions, and is translated to
     * http://www.bit-sys.com/state/v4</li>
     * <li>The namespace http://www.bit-sys.com/state/v3 is unrecognized by
     * parsers, but is functionally equivalent to
     * http://www.bit-sys.com/state/v2 for feature actions. The former is
     * translated to the latter before return.</li>
     * </ul>
     *
     * @param stream the stream from which to read the namespace.
     * @return a {@link Pair} of objects, with the Left hand side containing the
     *         namespace and the right hand side containing the input stream
     *         from which data should be extracted.
     * @throws IOException if data cannot be read from the supplied stream, or
     *             cannot be parsed, or the supplied stream cannot be reset.
     * @throws SAXException if the supplied stream contains un-parsable XML.
     * @throws ParserConfigurationException if the parser cannot be set up.
     */
    public Pair<String, InputStream> getNamespaceVersion(InputStream stream)
        throws IOException, SAXException, ParserConfigurationException
    {
        InputStream workingStream = new NonCloseableInputStream(stream);
        if (workingStream.markSupported())
        {
            workingStream.mark(1024);
        }
        else
        {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);
            new StreamReader(workingStream).readStreamToOutputStream(outStream);
            workingStream = new ByteArrayInputStream(outStream.toByteArray());
        }

        Document doc = XMLUtilities.newDocumentBuilderNS().parse(workingStream);
        String namespace = doc.getFirstChild().getNamespaceURI();

        workingStream.reset();

        // in this context, the alt namespace should be used:
        InputStream returnStream = workingStream;
        if (ModuleStateController.STATE_NAMESPACE_V4.equals(namespace))
        {
            String fileString = new StreamReader(workingStream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
            fileString = fileString.replaceAll(namespace, ModuleStateController.STATE_NAMESPACE_V4_ALT);
            returnStream = new ByteArrayInputStream(fileString.getBytes(StringUtilities.DEFAULT_CHARSET));
        }
        else if (ModuleStateController.STATE_NAMESPACE_V3.equals(namespace))
        {
            String fileString = new StreamReader(workingStream).readStreamIntoString(StringUtilities.DEFAULT_CHARSET);
            fileString = fileString.replaceAll(namespace, ModuleStateController.STATE_NAMESPACE);
            returnStream = new ByteArrayInputStream(fileString.getBytes(StringUtilities.DEFAULT_CHARSET));
        }

        return new Pair<>(namespace, returnStream);
    }

    @Override
    public void importFile(File file, ImportCallback callback)
    {
        boolean success = true;
        try (InputStream in = new BufferedInputStream(new FileInputStream(file)))
        {
            Pair<String, InputStream> namespacePair = getNamespaceVersion(in);
            String namespace = namespacePair.getFirstObject();
            if (ModuleStateController.STATE_NAMESPACE.equals(namespace))
            {
                readV2Stream(in);
            }
            else if (ModuleStateController.STATE_NAMESPACE_V4_ALT.equals(namespace))
            {
                readV4Stream(in);
            }
        }
        catch (JAXBException e)
        {
            LOGGER.error("Failed to read Feature Action from list of imported nodes.", e);
            success = false;
        }
        catch (FileNotFoundException e)
        {
            LOGGER.error("Unable to read file (file not found) '" + file.getAbsolutePath() + "'", e);
            success = false;
        }
        catch (IOException e)
        {
            LOGGER.error("Unable to read file '" + file.getAbsolutePath() + "'", e);
            success = false;
        }
        catch (SAXException e)
        {
            LOGGER.error("Malformed XML In file '" + file.getAbsolutePath() + "'", e);
            success = false;
        }
        catch (ParserConfigurationException e)
        {
            LOGGER.error("Unable to configure XML parser to read file '" + file.getAbsolutePath() + "'", e);
            success = false;
        }
        if (callback != null)
        {
            callback.fileImportComplete(success, file, null);
        }
    }

    /**
     * Reads the supplied stream as a V4 State, converting it to the internal
     * feature action model.
     *
     * @param in the stream from which to read the XML state.
     * @throws JAXBException if the parser cannot read valid XML from the
     *             supplied stream.
     */
    private void readV4Stream(InputStream in) throws JAXBException
    {
        FXUtilities.runOnFXThreadAndWait(() -> myModel.getFeatureGroups().clear());
        Object featureActionsArray = XMLUtilities.readXMLObject(in, FeatureActionArrayType.class,
                Arrays.asList(ObjectFactory.class, oasis.names.tc.ciq.xsdschema.xal._2.ObjectFactory.class));

        SimpleFeatureActionGroup convertedFeatureActions = VersionConverter
                .convert(((JAXBElement<FeatureActionArrayType>)featureActionsArray).getValue());

        FXUtilities.runOnFXThreadAndWait(() -> myModel.getFeatureGroups().add(convertedFeatureActions));
    }

    /**
     * Reads the supplied stream as a V2 State, converting it to the internal
     * feature action model.
     *
     * @param in the stream from which to read the XML state.
     * @throws JAXBException if the parser cannot read valid XML from the
     *             supplied stream.
     */
    private void readV2Stream(InputStream in) throws JAXBException
    {
        Object featureActionsElement = XMLUtilities.readXMLObject(in, FeatureActions.class,
                Arrays.asList(FeatureActionsFactory.class));

        FeatureActions featureActions = ((JAXBElement<FeatureActions>)featureActionsElement).getValue();

        FXUtilities.runOnFXThreadAndWait(() -> myModel.getFeatureGroups().clear());
        List<FeatureAction> actions = featureActions.getActions();
        for (FeatureAction featureAction : actions)
        {
            boolean foundGroup = false;
            for (SimpleFeatureActionGroup simpleGroup : myModel.getFeatureGroups())
            {
                if (simpleGroup.getGroupName().equals(featureAction.getGroupName()))
                {
                    FXUtilities.runOnFXThreadAndWait(() -> simpleGroup.getActions().add(new SimpleFeatureAction(featureAction)));
                    foundGroup = true;
                }
            }
            if (!foundGroup)
            {
                SimpleFeatureActionGroup featureActionGroup = new SimpleFeatureActionGroup();
                featureActionGroup.setGroupName(featureAction.getGroupName());
                featureActionGroup.getActions().add(new SimpleFeatureAction(featureAction));
                FXUtilities.runAndWait(() -> myModel.getFeatureGroups().add(featureActionGroup));
            }
        }
    }

    @Override
    public void importFiles(List<File> fileList, ImportCallback callback)
    {
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
    }

    @Override
    public void importURL(URL url, ImportCallback callback)
    {
    }
}
