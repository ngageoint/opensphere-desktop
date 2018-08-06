package io.opensphere.mantle.iconproject.impl;

import java.awt.Dimension;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.fx.JFXDialog;
import io.opensphere.mantle.icon.IconProvider;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

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
        mySelectedIcon = myPanelModel.getIconRecord();
    }

    /**
     * Adds the selected icon to favorites.
     *
     */
    public void addToFav()
    {
        Set<IconRecord> recordSet = myPanelModel.getSelectedIcons().keySet();
        if (recordSet.isEmpty() && myPanelModel.getIconRecord() == null)
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
        JFXDialog test = new JFXDialog(myPanelModel.getOwner(), "Error Rotating Icons", true);
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.schedule(new Runnable()
        {
            public void run()
            {
                test.setVisible(false); // should be invoked on the EDT
                test.dispose();
            }
        }, 2, TimeUnit.SECONDS);

        test.setMinimumSize(new Dimension(200, 250));
        test.setLocationRelativeTo(myPanelModel.getOwner());

        AnchorPane test2 = new AnchorPane();
        test2.getChildren().add(new Label("Multiple Icons selected. Please select one icon then try again."));
        test.setFxNode(test2);
        test.setVisible(true);

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

        if (recordSet.isEmpty() && myPanelModel.getIconRecord() == null)
        {
            JOptionPane.showMessageDialog(myPanelModel.getOwner(),
                    "There are currently no icons selected.\nSelect at least one icon and try again.",
                    "No Icons Selected Warning", JOptionPane.WARNING_MESSAGE);
        }
        else if (recordSet.size() >= 2)
        {
            for (IconRecord rec : recordSet)
            {
                String filename = rec.getImageURL().toString();
                myPanelModel.getIconRegistry().removeIcon(rec, this);
//                System.out.println("should have multiple:  " +  myPanelModel.getIconRegistry()));
                if (doDelete)
                {
                    filename = filename.replace("file:", "");
                    filename = filename.replace("%20", " ");
                    File iconActual = new File(filename);
                    iconActual.delete();
                }
            }
        }
        else
        {
            String filename = myPanelModel.getIconRecord().getImageURL().toString();
            myPanelModel.getIconRegistry().removeIcon(myPanelModel.getIconRecord(), this);
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
