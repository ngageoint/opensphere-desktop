package io.opensphere.mantle.iconproject.impl;

import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.view.IconCustomizerDialog;
import javafx.scene.control.Button;

/**
 * The Class IconPopupMenuImpl.
 */
public class IconPopupMenuImpl
{
    /** The selected icon. */
    private IconRecord mySelectedIcon;

    /** The current UI model. */
    private PanelModel myPanelModel;

    /**
     * The constructor for IconPopupMenuImpl.
     *
     * @param panelModel the model used to get registry items.
     */
    public IconPopupMenuImpl(PanelModel panelModel)
    {
        myPanelModel = panelModel;
        mySelectedIcon = myPanelModel.getSelectedRecord().get();
    }

    /**
     * Adds the selected icon to favorites.
     */
    public void addToFav()
    {
        Set<IconRecord> recordSet = myPanelModel.getSelectedIcons().keySet();
        if (recordSet.isEmpty() && mySelectedIcon == null)
        {
            JOptionPane.showMessageDialog(myPanelModel.getOwner(),
                    "There are currently no icons selected.\nSelect at least one icon and try again.",
                    "No Icons Selected Warning", JOptionPane.WARNING_MESSAGE);
        }
        else if (recordSet.size() > 1)
        {
            List<IconProvider> providerList = New.list(recordSet.size());
            for (IconRecord rec : recordSet)
            {
                DefaultIconProvider provider = new DefaultIconProvider(rec.getImageURL(), IconRecord.FAVORITES_COLLECTION, null,
                        "User");
                providerList.add(provider);

                myPanelModel.getIconRegistry().addIcon(provider, this);
            }
        }
        else
        {
            DefaultIconProvider provider = new DefaultIconProvider(mySelectedIcon.getImageURL(), IconRecord.FAVORITES_COLLECTION,
                    null, "User");
            myPanelModel.getIconRegistry().addIcon(provider, this);
        }
        myPanelModel.getViewModel().getMainPanel().refresh();
    }

    /**
     * Customize the selected icon.
     */
    public void customize()
    {
        IconCustomizerDialog dialog = new IconCustomizerDialog(myPanelModel.getOwner(), myPanelModel);
        dialog.setVisible(true);
    }

    /**
     * Deletes the selected icon.
     *
     * @param doDelete the indicator of if the file should be deleted.
     */
    public void delete(boolean doDelete)
    {
        Set<IconRecord> recordSet = myPanelModel.getSelectedIcons().keySet();

        if (recordSet.isEmpty() && myPanelModel.getSelectedRecord().get() == null)
        {
            JOptionPane.showMessageDialog(myPanelModel.getOwner(),
                    "There are currently no icons selected.\nSelect at least one icon and try again.",
                    "No Icons Selected Warning", JOptionPane.WARNING_MESSAGE);
        }
        else if (recordSet.size() > 1)
        {
            for (IconRecord rec : recordSet)
            {
                myPanelModel.getIconRegistry().removeIcon(rec, this);
                if (doDelete)
                {
                    myPanelModel.getIconRegistry().deleteIcon(rec, myPanelModel);
                }
            }
        }
        else
        {
            myPanelModel.getIconRegistry().removeIcon(myPanelModel.getSelectedRecord().get(), this);
            if (doDelete)
            {
                myPanelModel.getIconRegistry().deleteIcon(myPanelModel.getSelectedRecord().get(), myPanelModel);
            }
        }
        myPanelModel.getViewModel().getMainPanel().refresh();
    }

    /** Un-Selects the Icons visually and in the registry. */
    public void unSelectIcons()
    {
        for (Button recordindex : myPanelModel.getSelectedIcons().values())
        {
            int idx = myPanelModel.getViewModel().getMainPanel().getIconGrid().getChildren().indexOf(recordindex);
            myPanelModel.getViewModel().getMainPanel().getIconGrid().getChildren().get(idx).setStyle("");
        }
        myPanelModel.getSelectedIcons().clear();
    }

    /** Un-Selects the Icons visually and in the registry. */
    public void unSelectIcon()
    {
        for (Button recordindex : myPanelModel.getSelectedIconMap().values())
        {
            int idx = myPanelModel.getViewModel().getMainPanel().getIconGrid().getChildren().indexOf(recordindex);
            myPanelModel.getViewModel().getMainPanel().getIconGrid().getChildren().get(idx).setStyle("");
        }
        myPanelModel.getSelectedIconMap().clear();
    }
}
