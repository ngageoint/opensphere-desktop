package io.opensphere.controlpanels.layers.activedata.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;

import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.controlpanels.layers.base.LayerUtilities;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.swing.tree.ListCheckBoxTree;
import io.opensphere.core.viewbookmark.ViewBookmark;
import io.opensphere.core.viewbookmark.util.ViewBookmarkUtil;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;
import io.opensphere.mantle.data.util.impl.DataTypeActionUtils;

/**
 * The mouse listener, flys to a data type that contains lat lon info when user
 * double clicks it.
 */
public class ActiveDataMouseListener implements MouseListener
{
    /**
     * The toolbox.
     */
    private final Toolbox myToolbox;

    /**
     * Constructs a new mouse listener.
     *
     * @param toolbox The toolbox.
     */
    public ActiveDataMouseListener(Toolbox toolbox)
    {
        myToolbox = toolbox;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2)
        {
            ListCheckBoxTree tree = (ListCheckBoxTree)e.getSource();
            TreePath path = tree.getPathForLocation(e.getX(), e.getY());

            tree.setSelectionRows(new int[0]);

            DataTypeInfo dataType = path != null ? getDataType(path) : null;
            if (dataType != null)
            {
                boolean wentToView = gotoBookmarkView(dataType);
                if (!wentToView)
                {
                    DataTypeActionUtils.gotoDataType(dataType, myToolbox.getMapManager().getStandardViewer());
                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    /**
     * Gets the data type from the tree path.
     *
     * @param path the tree path
     * @return the data type
     */
    private DataTypeInfo getDataType(TreePath path)
    {
        GroupByNodeUserObject userObject = LayerUtilities.userObjectFromTreePath(path);
        DataTypeInfo dataType = userObject.getDataTypeInfo();
        if (dataType == null)
        {
            DataGroupInfo dataGroup = userObject.getDataGroupInfo();
            if (dataGroup != null)
            {
                Set<DataTypeInfo> typesWithLocation = dataGroup.findMembers(t -> t.getBoundingBox() != null, true, true);
                if (!typesWithLocation.isEmpty())
                {
                    dataType = typesWithLocation.iterator().next();
                }
            }
        }
        return dataType;
    }

    /**
     * Goes to the bookmark view if one exists.
     *
     * @param dataType the data type
     * @return whether the bookmark view was went to
     */
    private boolean gotoBookmarkView(DataTypeInfo dataType)
    {
        boolean wentToView = false;
        String associatedView = dataType.getAssociatedView();
        if (!StringUtils.isBlank(associatedView))
        {
            ViewBookmark vbm = myToolbox.getMapManager().getViewBookmarkRegistry().getViewBookmarkByName(associatedView);
            if (vbm != null)
            {
                wentToView = true;
                ViewBookmarkUtil.gotoView(myToolbox, vbm);
            }
        }
        return wentToView;
    }
}
