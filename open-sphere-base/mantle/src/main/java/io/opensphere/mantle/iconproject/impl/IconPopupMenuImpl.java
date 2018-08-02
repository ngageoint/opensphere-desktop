package io.opensphere.mantle.iconproject.impl;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.icon.impl.IconProjRotDialog;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.view.IconCustomizerDialog;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * The Class IconPopupMenuImpl.
 */

public class IconPopupMenuImpl
{
    /** The selected icon. */
    IconRecord mySelectedIcon;
    private PanelModel myPanelModel;
    /**
     * The constructor for IconPopupMenuImpl.
     *
     * @param choice the chosen Icon
     */
    public IconPopupMenuImpl(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        mySelectedIcon = myPanelModel.getIconRecord();
    }

    /**
     * Adds the selected icon to favorites.
     */
    public void addToFav(PanelModel myPanel)
    {
        System.out.println("Adding: " + mySelectedIcon + " to favorites");

//            Set<IconRecord> recordSet = myPanelModel.get
//            if (recordSet.isEmpty())
//            {
//                JOptionPane.showMessageDialog(this, "There are currently no icons selected.\nSelect at least one icon and try again.",
//                        "No Icons Selected Warning", JOptionPane.WARNING_MESSAGE);
//            }
//            else
//            {
//                List<IconProvider> providerList = New.list(recordSet.size());
//                for (IconRecord rec : recordSet)
//                {
        DefaultIconProvider provider = new DefaultIconProvider(mySelectedIcon.getImageURL(), IconRecord.FAVORITES_COLLECTION, null,
                "User");
//                    providerList.add(provider);

        myPanelModel.getMyIconRegistry().addIcon(provider, this);
    }

    /**
     * Rotates the selected icon.
     */
    public void rotate()
    {
        System.out.println("Rotating: " + mySelectedIcon);
        IconProjRotDialog dialog = new IconProjRotDialog(myPanelModel.getOwner(), myPanelModel);
        dialog.setVisible(true);
    }

    /**
     * Deletes the selected icon.
     */
    public void delete(boolean choice)
    {
        if (choice)
        {
            System.out.println("Deleting actual file: " + mySelectedIcon);
            String filename = mySelectedIcon.getImageURL().toString();
            filename = filename.replace("file:", "");
            filename = filename.replace("%20", " ");
            myPanelModel.getMyIconRegistry().removeIcon(mySelectedIcon, this);
            File iconActual = new File(filename);
            iconActual.delete();
        }
        else
        {
            System.out.println("Removing from registry: " + mySelectedIcon);
            myPanelModel.getMyIconRegistry().removeIcon(mySelectedIcon, this);
        }
        myPanelModel.getViewModel().getMainPanel().refresh();
        
    }
}
