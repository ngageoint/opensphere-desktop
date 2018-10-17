package io.opensphere.mantle.iconproject.impl;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.scene.control.TreeItem;

/**
 * The Class IconPopupMenuImpl.
 */
public class TreePopupMenuImpl
{
    /** The current UI model. */
    private PanelModel myPanelModel;

    /**
     * The constructor for IconPopupMenuImpl.
     *
     * @param panelModel the model used to get registry items.
     */
    public TreePopupMenuImpl(PanelModel panelModel)
    {
        myPanelModel = panelModel;
    }

    /**
     * Removes the tree from the display and registry.
     *
     * @param doDelete the toggle to delete the icon
     */
    public void remove(boolean doDelete)
    {
        for (int i = 0; i <= myPanelModel.getIconRegistry().getIconIds().max(); i++)
        {
            IconRecord iconRecord = myPanelModel.getIconRegistry().getIconRecordByIconId(i);
            if (!(iconRecord == null))
            {
                TreeItem<String> collectionName = (TreeItem<String>)myPanelModel.getTreeObject().getSelectedTree().getSelectionModel()
                        .selectedItemProperty().get();
                if (iconRecord.getCollectionName().equals(collectionName.getValue()))
                {
                    myPanelModel.getIconRegistry().removeIcon(iconRecord, this);
                    if (doDelete)
                    {
                        myPanelModel.getIconRegistry().deleteIcon(iconRecord, myPanelModel);
                    }
                }
            }
        }
        myPanelModel.getTreeObject().getSelectedTree().getRoot().getChildren()
                .remove(myPanelModel.getTreeObject().getSelectedTree().getSelectionModel().getSelectedItem());
    }
}
