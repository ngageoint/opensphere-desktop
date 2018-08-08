package io.opensphere.mantle.iconproject.impl;

import java.io.File;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.ErrorPane;
import javafx.scene.control.Button;

/**
 * The Class IconPopupMenuImpl.
 */
public class IconPopupMenuImpl
{
    /** The selected icon. */
    IconRecord mySelectedIcon;

    /** The current UI model. */
    private PanelModel myPanelModel;

    /**
     * The constructor for IconPopupMenuImpl.
     *
     * @param thePanelModel the model used to get registry items.
     */
    public IconPopupMenuImpl(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        mySelectedIcon = myPanelModel.getSelectedRecord().get();
    }

    /**
     * Adds the selected icon to favorites.
     *
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
        else if (recordSet.size() >= 2)
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
     * Rotates the selected icon.
     */
    public void rotate()
    {
        if (myPanelModel.getSelectedIcons().size() >= 2)
        {
            showMultiSelectMessage();
        }
        else
        {
            IconProjRotDialog dialog = new IconProjRotDialog(myPanelModel.getOwner(), myPanelModel);
            dialog.setVisible(true);
        }
        myPanelModel.getViewModel().getMainPanel().refresh();
    }

    /**
     * Shows a generic error message in JavaFX if there are multiple icons
     * selected when trying to perform a single icon event.
     */
    private void showMultiSelectMessage()
    {
        ErrorPane errorLoader = new ErrorPane();
        JFXDialog test2 = errorLoader.createErrorPane(2, "Multiple Icons selected. Please select one icon then try again.",
                "Error Loading Icons", myPanelModel);
        test2.setLocationRelativeTo(myPanelModel.getOwner());
        System.out.println("Dialog closed");
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
        else if (recordSet.size() >= 2)
        {
            for (IconRecord rec : recordSet)
            {
                myPanelModel.getIconRegistry().removeIcon(rec, this);
                if (doDelete)
                {
                    myPanelModel.getIconRegistry().deleteIcon(rec);
                }
            }
        }
        else
        {
            String filename = myPanelModel.getSelectedRecord().get().getImageURL().toString();
            myPanelModel.getIconRegistry().removeIcon(myPanelModel.getSelectedRecord().get(), this);
            if (doDelete)
            {
                filename = filename.replace("file:", "");
                filename = filename.replace("%20", " ");
                File iconActual = new File(filename);
                iconActual.delete();
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
}
