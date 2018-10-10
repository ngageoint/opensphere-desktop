package io.opensphere.mantle.iconproject.impl;

import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.PanelModel;
import javafx.scene.control.TreeItem;

/**
 * The Class IconPopupMenuImpl.
 */
public class TreePopupMenuImpl
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
    public TreePopupMenuImpl(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
        mySelectedIcon = myPanelModel.getSelectedRecord().get();
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
                @SuppressWarnings("unchecked")
                TreeItem<String> collectionName = (TreeItem<String>)myPanelModel.getTreeObject().getMyObsTree().get().getSelectionModel()
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
        myPanelModel.getTreeObject().getMyObsTree().get().getRoot().getChildren()
                .remove(myPanelModel.getTreeObject().getMyObsTree().get().getSelectionModel().getSelectedItem());
    }
}
