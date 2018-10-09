package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.icon.impl.IconProviderFactory;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.AddIconPane;

/** Creates window for importing icons from folder and handles registry. */
public class AddIconDialog extends JFXDialog
{
    /** Serial ID. */
    private static final long serialVersionUID = -4136694415228468073L;

    /** The model for UI elements. */
    private PanelModel myPanelModel;

    /** The panel calling this window. */
    private Window myOwner;

    /** The pane displaying UI elements. */
    private AddIconPane myAddIconPane;

    /**
     * Creates a new window containing the collection name and sub collection
     * name choices.
     *
     * @param owner the calling window.
     * @param panelModel the model.
     */
    public AddIconDialog(Window owner, PanelModel panelModel)
    {
        super(owner, "Add Icon From Folder", true);
        myPanelModel = panelModel;
        myOwner = owner;
        setMinimumSize(new Dimension(400, 250));
        setLocationRelativeTo(owner);
        myAddIconPane = new AddIconPane(myPanelModel);
        setFxNode(myAddIconPane);
        setAcceptListener(() -> loadFromFolder(myAddIconPane.getCollectionNamePane().getCollectionName(),
                myAddIconPane.getSubCollectPane().getSubCategory()));
    }

    /**
     * The back end function to display the file chooser and load the icon/icons
     * into the registry.
     *
     * @param collectionName the collection icons will be added to.
     * @param subCollectionName the sub collection icons will be added to. (if
     *            applicable)
     */
    private void loadFromFolder(String collectionName, String subCollectionName)
    {
        String colName = collectionName;
        String subcategory = subCollectionName;

        MnemonicFileChooser chooser = new MnemonicFileChooser(myPanelModel.getToolBox().getPreferencesRegistry(),
                "IconManagerFrame");

        chooser.setDialogTitle("Choose Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(myOwner);

        if (result == JFileChooser.APPROVE_OPTION)
        {
            File resultFile = chooser.getSelectedFile();
            try
            {
                List<DefaultIconProvider> providerList = IconProviderFactory.createFromDirectory(resultFile, colName,
                        "IconManager", true, subcategory);
                myPanelModel.getIconRegistry().addIcons(providerList, this);
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(myOwner, "Failed to load one or more icons: " + resultFile.getAbsolutePath(),
                        "Image Load Error", JOptionPane.ERROR_MESSAGE);
            }
            myPanelModel.getViewModel().getMainPanel().refresh();
        }
    }
}
