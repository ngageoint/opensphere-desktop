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
     * @param doDelete the toggle to create
     */
    public void remove(boolean doDelete)
    {
        for (int i = 0; i <= myPanelModel.getIconRegistry().getIconIds().max(); i++)
        {
            IconRecord temp = myPanelModel.getIconRegistry().getIconRecordByIconId(i);
            if (!(temp == null))
            {
                @SuppressWarnings("unchecked")
                TreeItem<String> samp = (TreeItem<String>)myPanelModel.getTreeObj().getMyObsTree().get().getSelectionModel()
                        .selectedItemProperty().get();
                if (temp.getCollectionName().equals(samp.getValue()))
                {
                    myPanelModel.getIconRegistry().removeIcon(temp, this);
                    if (doDelete)
                    {
                        myPanelModel.getIconRegistry().deleteIcon(temp);
                    }
                }
            }
        }
        myPanelModel.getTreeObj().getMyObsTree().get().getRoot().getChildren()
                .remove(myPanelModel.getTreeObj().getMyObsTree().get().getSelectionModel().getSelectedItem());
    }
}
