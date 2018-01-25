package io.opensphere.myplaces.controllers;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JSeparator;

import de.micromata.opengis.kml.v_2_2_0.Placemark;
import io.opensphere.core.Toolbox;
import io.opensphere.core.control.action.ContextMenuProvider;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.myplaces.constants.Constants;
import io.opensphere.myplaces.models.MyPlacesDataGroupInfo;
import io.opensphere.myplaces.models.MyPlacesDataTypeInfo;
import io.opensphere.myplaces.models.MyPlacesModel;
import io.opensphere.myplaces.specific.MyPointsMenuItemProvider;
import io.opensphere.myplaces.specific.PointGoer;

/**
 * Provides the context menu for single group selection.
 */
public class SingleGroupContextMenuProvider extends MyPointsMenuItemProvider
        implements ContextMenuProvider<DataGroupInfo.DataGroupContextKey>
{
    /** The toolbox. */
    private final Toolbox myToolbox;

    /** The Model. */
    protected MyPlacesModel myPlacesModel;

    /** The Controller. */
    private final PointGoer myController;

    /** The Current placemark. */
    private Placemark myPlacemark;

    /** The Group. */
    private MyPlacesDataGroupInfo myGroup;

    /** The Type. */
    private MyPlacesDataTypeInfo myType;

    /**
     * Constructs a new points context menu provider.
     *
     * @param toolbox The toolbox.
     * @param model The my places model.
     * @param controller the controller
     */
    public SingleGroupContextMenuProvider(Toolbox toolbox, MyPlacesModel model, PointGoer controller)
    {
        myPlacesModel = model;
        myToolbox = toolbox;
        myController = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();
        if (menuItems.get(ItemType.CENTER_ON) == src)
        {
            if (myPlacemark != null)
            {
                myController.gotoPoint(myType);
            }
        }
        else if (menuItems.get(ItemType.EDIT) == src)
        {
            myType.launchEditor(myGroup, Collections.singletonList(myType));
        }
        else if (menuItems.get(ItemType.DELETE) == src)
        {
            int response = JOptionPane.showConfirmDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                    "Are you sure you want to delete " + myType.getDisplayName() + "?", "Delete Confirmation",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.OK_OPTION)
            {
                myGroup.removeMember(myType, false, this);
            }
        }
        else if (menuItems.get(ItemType.HIDE_POINT) == src)
        {
            doMenuItemAction(myType, myPlacemark, Constants.IS_FEATURE_ON_ID, false);
        }
        else if (menuItems.get(ItemType.SHOW_POINT) == src)
        {
            doMenuItemAction(myType, myPlacemark, Constants.IS_FEATURE_ON_ID, true);
        }
        else if (menuItems.get(ItemType.SHOW_BUBBLE) == src)
        {
            doMenuItemAction(myType, myPlacemark, Constants.IS_ANNOHIDE_ID, false);
        }
        else if (menuItems.get(ItemType.HIDE_BUBBLE) == src)
        {
            doMenuItemAction(myType, myPlacemark, Constants.IS_ANNOHIDE_ID, true);
        }
    }

    @Override
    public List<? extends Component> getMenuItems(String contextId, DataGroupInfo.DataGroupContextKey key)
    {
        if (!(key.getDataGroup() instanceof MyPlacesDataGroupInfo))
        {
            return new LinkedList<>();
        }

        myGroup = (MyPlacesDataGroupInfo)key.getDataGroup();
        myType = (MyPlacesDataTypeInfo)key.getDataType();

        if (myType != null)
        {
            myPlacemark = myType.getKmlPlacemark();
        }

        List<Component> menuItems = new LinkedList<>();
        if (myType == null)
        {
            menuItems.add(new CreateFolderMenuItem(myToolbox, myGroup));

            JMenuItem configItem = new JMenuItem("Rename...");
            configItem.addActionListener(e -> renameGroup(myGroup));
            menuItems.add(configItem);
        }

        for (ItemType type : ItemType.values())
        {
            if (type.isNeedsDataType() && myType != null
                    || myType == null && type.isNeedsDataGroup() && !type.isNeedsDataType())
            {
                if (type.isRequired())
                {
                    menuItems.add(getMenuItems().get(type));
                }
                else
                {
                    addMenuItems(menuItems, myPlacemark, type);
                }

                if (type.isGroup())
                {
                    menuItems.add(new JSeparator(Separator.HORIZONTAL));
                }
            }
        }

        return menuItems;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    /**
     * Renames the group.
     *
     * @param group The group to rename.
     */
    private void renameGroup(MyPlacesDataGroupInfo group)
    {
        Component mainFrame = myToolbox.getUIRegistry().getMainFrameProvider().get();
        String folderName = JOptionPane.showInputDialog(mainFrame, "Name", "Rename", JOptionPane.QUESTION_MESSAGE);
        if (folderName == null || folderName.isEmpty())
        {
            return;
        }
        group.getKmlFolder().setName(folderName);
        group.setDisplayName(folderName, this);
        myPlacesModel.notifyObservers();
    }

    // - - - - - - - Junk Pile - - - - - - - - - - - - - - - - - - - - - - - - -

//  /**
//   * Deletes the group or type.
//   *
//   * @param group The group to delete if type is null.
//   * @param dataType The type to delete or null if there isn't one.
//   */
//  private void deleteGroupOrType(MyPlacesDataGroupInfo group, MyPlacesDataTypeInfo dataType)
//  {
//      if (dataType != null)
//      {
//          group.removeMember(dataType, false, this);
//      }
//      else
//      {
//          MantleToolboxUtils.getMantleToolbox(myToolbox).getDataGroupController().removeDataGroupInfo(group, this);
//      }
//  }
}
