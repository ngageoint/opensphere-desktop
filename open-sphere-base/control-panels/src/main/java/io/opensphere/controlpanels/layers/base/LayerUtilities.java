package io.opensphere.controlpanels.layers.base;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.tree.TreePath;

import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * Layer utilities.
 */
public final class LayerUtilities
{
    /**
     * Find nodes.
     *
     * @param setToAddTo the set to add to
     * @param node the node
     * @param selector the selector
     */
    public static void findNodes(Set<TreeTableTreeNode> setToAddTo, TreeTableTreeNode node,
            final Predicate<GroupByNodeUserObject> selector)
    {
        if (node.getPayload() != null && node.getPayload().getPayloadData() instanceof GroupByNodeUserObject)
        {
            GroupByNodeUserObject uo = (GroupByNodeUserObject)node.getPayload().getPayloadData();
            if (selector.test(uo))
            {
                setToAddTo.add(node);
            }
        }
        if (node.getChildCount() > 0)
        {
            node.getChildren().forEach(child -> findNodes(setToAddTo, child, selector));
        }
    }

    /**
     * Recursively add all data groups to set.
     *
     * Note that even if a node is not selected by the filter to be included
     * itself its children will still be recursively checked.
     *
     * @param groupsToDeactivate the groups to deactivate
     * @param node the node
     * @param dgiFilter the optional {@link Predicate} for the
     *            {@link DataGroupInfo}
     */
    public static void recursivelyAddJustDataGroups(Set<DataGroupInfo> groupsToDeactivate, TreeTableTreeNode node,
            Predicate<DataGroupInfo> dgiFilter)
    {
        GroupByNodeUserObject userObject = getUserObject(node);
        if (userObject != null)
        {
            DataGroupInfo dataGroup = userObject.getDataGroupInfo();
            DataTypeInfo dataType = userObject.getDataTypeInfo();
            if (dataGroup != null && dataType == null && (dgiFilter == null || dgiFilter.test(dataGroup)))
            {
                groupsToDeactivate.add(dataGroup);
            }
        }
        if (node.getChildCount() > 0)
        {
            node.getChildren().forEach(child -> recursivelyAddAllDataGroupsToSet(groupsToDeactivate, child, dgiFilter));
        }
    }

    /**
     * Recursively add all data groups to set.
     *
     * Note that even if a node is not selected by the filter to be included
     * itself its children will still be recursively checked.
     *
     * @param groupsToDeactivate the groups to deactivate
     * @param node the node
     * @param dgiFilter the optional {@link Predicate} for the
     *            {@link DataGroupInfo}
     */
    public static void recursivelyAddAllDataGroupsToSet(Set<DataGroupInfo> groupsToDeactivate, TreeTableTreeNode node,
            Predicate<DataGroupInfo> dgiFilter)
    {
        DataGroupInfo dataGroup = getDataGroup(node);
        if (dataGroup != null && (dgiFilter == null || dgiFilter.test(dataGroup)))
        {
            groupsToDeactivate.add(dataGroup);
        }
        if (node.getChildCount() > 0)
        {
            node.getChildren().forEach(child -> recursivelyAddAllDataGroupsToSet(groupsToDeactivate, child, dgiFilter));
        }
    }

    /**
     * Recursively add all data types to the map.
     *
     * Note that even if a node is not selected by the filter to be included
     * itself its children will still be recursively checked.
     *
     * @param typesToGroups the selected data types mapped to their group.
     * @param node the node
     * @param dgiFilter the optional {@link Predicate} for the
     *            {@link DataGroupInfo}
     */
    public static void recursivelyAddAllDataTypesToMap(Map<DataTypeInfo, DataGroupInfo> typesToGroups, TreeTableTreeNode node,
            Predicate<DataGroupInfo> dgiFilter)
    {
        GroupByNodeUserObject userObject = getUserObject(node);
        if (userObject != null)
        {
            DataGroupInfo dataGroup = userObject.getDataGroupInfo();
            DataTypeInfo dataType = userObject.getDataTypeInfo();
            if (dataGroup != null && dataType != null && (dgiFilter == null || dgiFilter.test(dataGroup)))
            {
                typesToGroups.put(dataType, dataGroup);
            }
        }
        if (node.getChildCount() > 0)
        {
            node.getChildren().forEach(child -> recursivelyAddAllDataTypesToMap(typesToGroups, child, dgiFilter));
        }
    }

    /**
     * Gets the DataGroupInfo from the given node.
     *
     * @param node the node
     * @return the DataGroupInfo or null
     */
    public static DataGroupInfo getDataGroup(TreeTableTreeNode node)
    {
        DataGroupInfo dataGroup = null;
        GroupByNodeUserObject userObject = getUserObject(node);
        if (userObject != null)
        {
            dataGroup = userObject.getDataGroupInfo();
        }
        return dataGroup;
    }

    /**
     * Converts an array of TreePaths to a collection of GroupByNodeUserObjects.
     *
     * @param paths the tree paths
     * @return the GroupByNodeUserObjects
     */
    public static Collection<GroupByNodeUserObject> userObjectsFromTreePaths(TreePath[] paths)
    {
        Collection<GroupByNodeUserObject> userObjects = New.list(paths.length);
        for (TreePath path : paths)
        {
            GroupByNodeUserObject userObject = userObjectFromTreePath(path);
            if (userObject != null)
            {
                userObjects.add(userObject);
            }
        }
        return userObjects;
    }

    /**
     * Converts a TreePath to a GroupByNodeUserObject.
     *
     * @param path the tree path
     * @return the GroupByNodeUserObject
     */
    public static GroupByNodeUserObject userObjectFromTreePath(TreePath path)
    {
        GroupByNodeUserObject userObject = null;
        Object o = path.getLastPathComponent();
        if (o instanceof TreeTableTreeNode)
        {
            TreeTableTreeNode node = (TreeTableTreeNode)o;
            userObject = getUserObject(node);
        }
        return userObject;
    }

    /**
     * Gets the GroupByNodeUserObject from the given node.
     *
     * @param node the node
     * @return the GroupByNodeUserObject or null
     */
    public static GroupByNodeUserObject getUserObject(TreeTableTreeNode node)
    {
        GroupByNodeUserObject userObject = null;
        Object payloadData = node.getPayloadData();
        if (payloadData instanceof GroupByNodeUserObject)
        {
            userObject = (GroupByNodeUserObject)payloadData;
        }
        return userObject;
    }

    /** Private constructor. */
    private LayerUtilities()
    {
    }
}
