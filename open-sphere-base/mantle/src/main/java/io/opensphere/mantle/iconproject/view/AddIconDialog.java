package io.opensphere.mantle.iconproject.view;

import java.awt.Dimension;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.util.filesystem.MnemonicFileChooser;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.icon.impl.IconProviderFactory;
import io.opensphere.mantle.iconproject.model.ImportProp;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.AddIconPane;

public class AddIconDialog extends JFXDialog
{
    /**
     * Wraps the Add Icon into a java FX pannel.
     */
    private static final long serialVersionUID = -4136694415228468073L;

    private PanelModel myPanelModel;

    private Window myOwner;

    private AddIconPane myAddIconPane;

    public AddIconDialog(Window owner, PanelModel thePanelModel)
    {
        super(owner, "Add Icon From Folder", false);
        myPanelModel = thePanelModel;
        myOwner = owner;
        setMinimumSize(new Dimension(500, 300));
        setLocationRelativeTo(owner);
        myAddIconPane = new AddIconPane(myPanelModel);
        setFxNode(myAddIconPane);
        setAcceptEar(() -> LoadfromFolder(myAddIconPane.getCollectionNamePane().getCollectionName(),
                myAddIconPane.getSubCollectPane().getSubCategory()));
    }

    private void LoadfromFolder(String collectionName, String subCollection)
    {
        String colName = collectionName;
        String subcategory = subCollection;
        System.out.println("collection is: " + collectionName + " subcollection is: " + subCollection);

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
                myPanelModel.getMyIconRegistry().addIcons(providerList, this);
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(myOwner, "Failed to load one or more icons: " + resultFile.getAbsolutePath(),
                        "Image Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }

//        File result = ImageUtil.showImageFileChooser("Choose Icon File", myOwner,
//                myPanelModel.getToolBox().getPreferencesRegistry());
//        if (result != null)
//        {
//            try
//            {
//                System.out.println("the Icon Url is : " + result.toURI().toURL());
//                System.out.println("loading from file under the name:  " + collectionName);
//                IconProvider provider = new DefaultIconProvider(result.toURI().toURL(), collectionName, subCollection, "User");
//                myPanelModel.getMyIconRegistry().addIcon(provider, this);
//            }
//            catch (MalformedURLException e)
//            {
//            }
//        }
        myPanelModel.getViewModel().getMainPanel().refresh();
    }
}
