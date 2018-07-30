package io.opensphere.featureactions.editor.ui;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import io.opensphere.core.modulestate.StateXML;
import io.opensphere.core.preferences.PreferencesRegistry;
import io.opensphere.core.util.XMLUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.swing.OptionDialog;
import io.opensphere.core.util.swing.RadioButtonPanel;
import io.opensphere.featureactions.editor.model.SimpleFeatureAction;
import io.opensphere.featureactions.editor.model.SimpleFeatureActionGroup;
import io.opensphere.featureactions.editor.ui.DetailEditor.StackLayout;

/** Provides an export to file menu for feature actions. */
public class FeatureActionExporter
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(FeatureActionExporter.class);

    /** Option to export only active feature actions. */
    private static final String EXPORT_ACTIVE = "Export Active (Checked)";

    /** Option to export all feature actions. */
    private static final String EXPORT_ALL = "Export All";

    /** The panel for the selection of radio buttons. */
    private RadioButtonPanel<String> myButtonPanel;

    /** The name of the layer for the feature actions. */
    private final String myLayerName;

    /** The name of the file to be saved. */
    private JTextField myName;

    /** The list of feature actions groups in a layer. */
    private final List<SimpleFeatureActionGroup> myGroupList;

    /** Preferences registry reference. */
    private final PreferencesRegistry myPrefsRegistry;

    /**
     * Constructor.
     *
     * @param prefRegistry The preferences registry.
     * @param layerName The name of the layer that has feature actions being exported
     * @param groupList The list of feature action groups in the layer.
     */
    public FeatureActionExporter(PreferencesRegistry prefRegistry, String layerName, List<SimpleFeatureActionGroup> groupList)
    {
        myPrefsRegistry = prefRegistry;
        myLayerName = layerName;
        myGroupList = groupList;
    }

    /**
     * Launches the export feature action menu.
     *
     * @param parentDialog The parent dialog.
     */
    public void launch(Component parentDialog)
    {
        OptionDialog dialog = new OptionDialog(parentDialog);
        dialog.setTitle("Export Feature Actions");
        JPanel root = new JPanel();
        StackLayout stack = new StackLayout();
        stack.attach(root);
        myName = new JTextField(myLayerName + " Feature Actions");
        stack.add(new JLabel("Name:"));
        stack.add(myName);
        myButtonPanel = new RadioButtonPanel<>(New.list(EXPORT_ACTIVE, EXPORT_ALL), EXPORT_ACTIVE);
        stack.add(myButtonPanel);
        dialog.setComponent(root);
        dialog.setModal(true);
        dialog.buildAndShow();
        if (dialog.getSelection() == JOptionPane.OK_OPTION)
        {
            try
            {
                Document doc = XMLUtilities.newDocument();
                for (SimpleFeatureActionGroup featureActionGroup : myGroupList)
                {
                    for (SimpleFeatureAction featureAction : featureActionGroup.getActions())
                    {
                        if (checkExportValidity(featureAction))
                        {
                            Node baseNode = StateXML.createChildNode(doc, doc, doc, "/:featureActions", "featureActions");
                            XMLUtilities.marshalJAXBObjectToElement(featureAction.getFeatureAction(), baseNode);
                        }
                    }
                }
                saveExportToFile(parentDialog, doc);
            }
            catch (ParserConfigurationException | XPathExpressionException | JAXBException e)
            {
                LOGGER.error("Failure to export Feature Actions to file.", e);
            }
        }
    }

    /**
     * Saves all the valid feature actions to the xml file.
     *
     * @param parentComponent The parent component.
     * @param doc The document to save the feature actions to.
     */
    private void saveExportToFile(Component parentComponent, Document doc)
    {
        OutputStream outputStream;
        MnemonicFileChooser chooser = new MnemonicFileChooser(myPrefsRegistry, getClass().getName());
        chooser.setSelectedFile(new File(myName.getText() + ".xml"));
        while (true)
        {
            int result = chooser.showSaveDialog(parentComponent, Collections.singleton(".xml"));
            if (result == JFileChooser.APPROVE_OPTION)
            {
                File saveFile = chooser.getSelectedFile();
                try
                {
                    outputStream = new FileOutputStream(saveFile);
                    break;
                }
                catch (FileNotFoundException e)
                {
                    LOGGER.error("Failed to write to selected file [" + saveFile.getAbsolutePath() + "]: " + e, e);
                    JOptionPane.showMessageDialog(parentComponent, "Failed to write to file: " + e.getMessage(),
                            "Error writing to file", JOptionPane.ERROR_MESSAGE);
                }
            }
            else
            {
                outputStream = null;
                break;
            }
        }
        if (outputStream != null)
        {
            try
            {
                XMLUtilities.format(doc, outputStream, null);
                outputStream.close();
            }
            catch (IOException e)
            {
                LOGGER.error("Error in closing the file [" + chooser.getSelectedFile().getAbsolutePath() + "].", e);
            }
        }
    }

    /**
     * Checks whether the feature action is going to be exported.
     *
     * @param featureAction The feature action to check.
     * @return True if the feature action is allowed to be exported, false otherwise.
     */
    private boolean checkExportValidity(SimpleFeatureAction featureAction)
    {
        return myButtonPanel.getSelection().equals(EXPORT_ALL) || (myButtonPanel.getSelection().equals(EXPORT_ACTIVE)
               && featureAction.getFeatureAction().isEnabled());
    }
}
