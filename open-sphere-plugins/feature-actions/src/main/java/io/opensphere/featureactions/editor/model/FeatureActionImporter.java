package io.opensphere.featureactions.editor.model;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler.DropLocation;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.opensphere.core.importer.FileOrURLImporter;
import io.opensphere.core.importer.ImportCallback;
import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.featureactions.model.FeatureAction;
import javafx.application.Platform;

/** Provides a import from file menu for feature actions. */
public class FeatureActionImporter implements FileOrURLImporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FeatureActionImporter.class);

    /** The list of supported file extensions. */
    private static final List<String> SUPPORTED_EXT = Collections.singletonList("xml");

    /** The model of the feature actions. */
    private SimpleFeatureActions myModel;

    /** The parent component of the import menu. */
    private Component myParent;

    /**
     * Constructor.
     *
     * @param model The model of feature actions for the layer we are importing to.
     * @param parent The parent component of the import menu.
     */
    public FeatureActionImporter(SimpleFeatureActions model, Component parent)
    {
        myModel = model;
        myParent = parent;
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

    @Override
    public void importFile(File file, ImportCallback callback)
    {
        boolean success = false;
        try
        {
            NodeList nodeList = getFeatureActionNodes(file);
            if (nodeList == null)
            {
                callback.fileImportComplete(success, file, null);
                EventQueueUtilities.invokeLater(() -> JOptionPane.showMessageDialog(myParent, "Selected file failed to import."));
                return;
            }
            for (int i = 0; i < nodeList.getLength(); i++)
            {
                FeatureAction featureAction = XMLUtilities.readXMLObject(nodeList.item(i), FeatureAction.class);
                boolean foundGroup = false;
                for (SimpleFeatureActionGroup simpleGroup : myModel.getFeatureGroups())
                {
                    if (simpleGroup.getGroupName().equals(featureAction.getGroupName()))
                    {
                        Platform.runLater(() -> simpleGroup.getActions().add(new SimpleFeatureAction(featureAction)));
                        foundGroup = true;
                    }
                }
                if (!foundGroup)
                {
                    SimpleFeatureActionGroup featureActionGroup = new SimpleFeatureActionGroup();
                    featureActionGroup.setGroupName(featureAction.getGroupName());
                    featureActionGroup.getActions().add(new SimpleFeatureAction(featureAction));
                    Platform.runLater(() -> myModel.getFeatureGroups().add(featureActionGroup));
                }
            }
            success = true;
        }
        catch (JAXBException e)
        {
            LOGGER.error("Failed to read Feature Action from list of imported nodes.", e);
        }
        if (callback != null)
        {
            callback.fileImportComplete(success, file, null);
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

    /**
     * Gets a list of feature action nodes from the file.
     * @param file The file to get feature actions from.
     * @return The list of nodes of feature actions.
     */
    private NodeList getFeatureActionNodes(File file)
    {
        NodeList nodes;
        try
        {
            DocumentBuilder docBuilder = XMLUtilities.newDocumentBuilderNS();
            Document doc = docBuilder.parse(file);
            nodes =  StateXML.getChildNodes(doc, "/:featureActions/featureAction");
        }
        catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e)
        {
            nodes = null;
            LOGGER.error("Failed to parse input file by Feature Actions. "
                    + "File may not be of correct structure, or an error in I/O may have occured.", e);
        }
        return nodes;
    }
}
