package io.opensphere.controlpanels.layers.base;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import io.opensphere.controlpanels.layers.base.cfg.v1.TreePathConfig;
import io.opensphere.controlpanels.layers.base.cfg.v1.TreePathList;
import io.opensphere.core.preferences.Preferences;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.swing.TreePathIterator;
import io.opensphere.core.util.swing.tree.TreeTableTreeNode;
import io.opensphere.mantle.data.impl.GroupByNodeUserObject;

/**
 * The Class DiscoveryTreeExpansionHelper.
 */
@SuppressWarnings("PMD.GodClass")
public class DiscoveryTreeExpansionHelper
{
    /** The Constant ourCompare. */
    private static final ComparePathByDepthLeafFirst ourCompare = new ComparePathByDepthLeafFirst();

    /** The Map lock. */
    private final ReentrantLock myMapLock;

    /** The Mode. */
    private final Mode myMode;

    /** The Preferences. */
    private final Preferences myPreferences;

    /** The View by type to expanded path set map. */
    private final Map<String, Set<String>> myViewByTypeToPathSetMap;

    /**
     * Creates the path identifier.
     *
     * @param path the path
     * @return the string
     */
    private static String createPathIdentifier(TreePath path)
    {
        StringBuilder sb = new StringBuilder();
        for (Object pc : path.getPath())
        {
            sb.append('[');
            sb.append(getNodeId((TreeNode)pc));
            sb.append(']');
        }
        return sb.toString();
    }

    /**
     * Gets the node name.
     *
     * @param node the node
     * @return the node name
     */
    private static String getNodeId(TreeNode node)
    {
        String value = "???";
        if (node instanceof TreeTableTreeNode)
        {
            TreeTableTreeNode tttn = (TreeTableTreeNode)node;
            if (tttn.getPayloadData() instanceof GroupByNodeUserObject)
            {
                value = ((GroupByNodeUserObject)tttn.getPayloadData()).getId();
            }
            else if (tttn.getPayloadData() != null)
            {
                value = tttn.getPayloadData().toString();
            }
            else if (tttn.getPayload() != null)
            {
                value = tttn.getPayload().getButton().getText();
            }
        }
        return value;
    }

    /**
     * Instantiates a new discovery tree expansion helper.
     *
     * @param prefs the prefs
     * @param mode the mode
     */
    public DiscoveryTreeExpansionHelper(Preferences prefs, Mode mode)
    {
        myMapLock = new ReentrantLock();
        myViewByTypeToPathSetMap = New.map();
        myMode = mode;
        myPreferences = prefs;
    }

    /**
     * Adds the expansion path.
     *
     * @param viewByType the view by type
     * @param path the path
     */
    public void addExpansionPath(String viewByType, TreePath path)
    {
        if (expandByDefault(path))
        {
            removePath(viewByType, path);
        }
        else
        {
            addPath(viewByType, path);
        }
    }

    /**
     * Load from preferences.
     */
    public void loadFromPreferences()
    {
        TreePathConfig config = myPreferences.getJAXBObject(TreePathConfig.class, "ExpansionHelper", new TreePathConfig());
        myMapLock.lock();
        try
        {
            for (TreePathList tpl : config.getPathLists())
            {
                Set<String> storedSet = myViewByTypeToPathSetMap.get(tpl.getViewByType());
                if (storedSet == null)
                {
                    storedSet = New.set();
                    myViewByTypeToPathSetMap.put(tpl.getViewByType(), storedSet);
                }
                storedSet.addAll(tpl.getTreePaths());
            }
        }
        finally
        {
            myMapLock.unlock();
        }
    }

    /**
     * Removes the expansion path.
     *
     * @param viewByType the view by type
     * @param path the path
     */
    public void removeExpansionPath(String viewByType, TreePath path)
    {
        if (expandByDefault(path))
        {
            addPath(viewByType, path);
        }
        else
        {
            removePath(viewByType, path);
        }
    }

    /**
     * Restore expansion paths.
     *
     * Traverses a tree and using the list of expansion paths in storage
     * restores the trees previous expansion state for all nodes that are
     * presently in the tree.
     *
     * @param viewByType the view by type
     * @param tree the tree
     */
    public void restoreExpansionPaths(String viewByType, JTree tree)
    {
        Set<String> expandedSet = null;
        myMapLock.lock();
        try
        {
            expandedSet = myViewByTypeToPathSetMap.get(viewByType);
            if (expandedSet != null)
            {
                expandedSet = New.set(expandedSet);
            }
        }
        finally
        {
            myMapLock.unlock();
        }

        TreeNode node = (TreeNode)tree.getModel().getRoot();
        TreePathIterator iterator = new TreePathIterator(node);
        List<TreePathOperation> operationList = New.list();
        while (iterator.hasNext())
        {
            TreePath path = iterator.next();
            if (!((TreeNode)path.getLastPathComponent()).isLeaf())
            {
                boolean expand = expandByDefault(path);
                // If there's a user preference, that means we need to use the
                // opposite of the default
                if (expandedSet != null && expandedSet.contains(createPathIdentifier(path)))
                {
                    expand = !expand;
                }
                operationList.add(new TreePathOperation(path, expand));
            }
        }

        if (operationList != null && !operationList.isEmpty())
        {
            Collections.sort(operationList, ourCompare);

            for (TreePathOperation tp : operationList)
            {
                tp.execute(tree);
            }
        }
    }

    /**
     * Determines if the given path should be expanded by default.
     *
     * @param path the tree path
     * @return true to expand, false to collapse
     */
    private boolean expandByDefault(TreePath path)
    {
        // Figure out the data group's thought on the matter
        Boolean isExpandedByDefault = null;
        if (path.getLastPathComponent() instanceof TreeTableTreeNode)
        {
            Object payload = ((TreeTableTreeNode)path.getLastPathComponent()).getPayloadData();
            if (payload instanceof GroupByNodeUserObject)
            {
                GroupByNodeUserObject userObject = (GroupByNodeUserObject)payload;
                if (userObject.getDataGroupInfo() != null)
                {
                    isExpandedByDefault = userObject.getDataGroupInfo().getExpandedByDefault();
                }
            }
        }

        // If the data group has an opinion, use it, otherwise do whatever the
        // UI wants
        return isExpandedByDefault != null ? isExpandedByDefault.booleanValue() : myMode == Mode.STORE_CONTRACTIONS;
    }

    /**
     * Adds the expansion path.
     *
     * @param viewByType the view by type
     * @param path the path
     */
    private void addPath(String viewByType, TreePath path)
    {
        if (path != null)
        {
            String pathId = createPathIdentifier(path);
            myMapLock.lock();
            try
            {
                Set<String> expandedSet = myViewByTypeToPathSetMap.get(viewByType);
                if (expandedSet == null)
                {
                    expandedSet = New.set();
                    myViewByTypeToPathSetMap.put(viewByType, expandedSet);
                }
                expandedSet.add(pathId);
            }
            finally
            {
                myMapLock.unlock();
            }
            saveToPreferences(viewByType, pathId);
        }
    }

    /**
     * Modifies the specified path list.
     *
     * @param pathList The path list to modify.
     * @param pathId The id of the path to add.
     */
    private void modifyPathList(TreePathList pathList, String pathId)
    {
        Set<String> ensureUnique = New.set(pathList.getTreePaths());
        ensureUnique.add(pathId);

        List<String> paths = New.list(ensureUnique);
        Collections.sort(paths);

        pathList.setTreePaths(paths);
    }

    /**
     * Removes the path from the preferences.
     *
     * @param viewByType The view by type to remove the path from.
     * @param pathId The path to remove.
     */
    private void removeFromPreferences(String viewByType, String pathId)
    {
        TreePathConfig config = myPreferences.getJAXBObject(TreePathConfig.class, "ExpansionHelper", new TreePathConfig());
        myMapLock.lock();
        try
        {
            for (TreePathList tpl : config.getPathLists())
            {
                if (tpl.getViewByType().equals(viewByType))
                {
                    List<String> paths = tpl.getTreePaths();
                    boolean wasContained = paths.remove(pathId);
                    if (wasContained)
                    {
                        myPreferences.putJAXBObject("ExpansionHelper", config, false, this);
                    }

                    break;
                }
            }
        }
        finally
        {
            myMapLock.unlock();
        }
    }

    /**
     * Removes the expansion path.
     *
     * @param viewByType the view by type
     * @param path the path
     */
    private void removePath(String viewByType, TreePath path)
    {
        if (path != null)
        {
            boolean changed = false;
            String pathId = createPathIdentifier(path);
            myMapLock.lock();
            try
            {
                Set<String> expandedSet = myViewByTypeToPathSetMap.get(viewByType);
                if (expandedSet != null)
                {
                    changed = expandedSet.remove(pathId);
                }
            }
            finally
            {
                myMapLock.unlock();
            }
            if (changed)
            {
                removeFromPreferences(viewByType, pathId);
            }
        }
    }

    /**
     * Save to preferences.
     *
     * @param viewType The type of the view.
     * @param pathId The id of the path.
     */
    private void saveToPreferences(String viewType, String pathId)
    {
        TreePathConfig cfg = myPreferences.getJAXBObject(TreePathConfig.class, "ExpansionHelper", new TreePathConfig());
        myMapLock.lock();
        try
        {
            boolean wasPathListFound = false;
            for (TreePathList pathList : cfg.getPathLists())
            {
                if (pathList.getViewByType().equals(viewType))
                {
                    modifyPathList(pathList, pathId);
                    wasPathListFound = true;
                    break;
                }
            }

            if (!wasPathListFound)
            {
                TreePathList pathList = new TreePathList(viewType);
                cfg.getPathLists().add(pathList);
                modifyPathList(pathList, pathId);
            }
        }
        finally
        {
            myMapLock.unlock();
        }
        myPreferences.putJAXBObject("ExpansionHelper", cfg, false, this);
    }

    /**
     * The Class ComparePathByDepthLeafFirst.
     */
    @SuppressWarnings("serial")
    private static class ComparePathByDepthLeafFirst implements Comparator<TreePathOperation>, Serializable
    {
        @Override
        public int compare(TreePathOperation o1, TreePathOperation o2)
        {
            return o1.getPath().getPathCount() > o2.getPath().getPathCount() ? -1
                    : o1.getPath().getPathCount() == o2.getPath().getPathCount() ? 0 : 1;
        }
    }

    /**
     * The Enum Mode.
     */
    public enum Mode
    {
        /** The STORE_CONTRACTIONS. */
        STORE_CONTRACTIONS,

        /** The STORE_EXPANSIONS. */
        STORE_EXPANSIONS
    }

    /**
     * The Class TreePathOperation.
     */
    private static class TreePathOperation
    {
        /** Expand if true, collapse if false. */
        private final boolean myExpand;

        /** The Path. */
        private final TreePath myPath;

        /**
         * Instantiates a new tree path operation.
         *
         * @param path the path
         * @param expand the expand
         */
        public TreePathOperation(TreePath path, boolean expand)
        {
            myExpand = expand;
            myPath = path;
        }

        /**
         * Execute.
         *
         * @param tree the tree
         */
        public void execute(JTree tree)
        {
            if (myExpand)
            {
                tree.expandPath(myPath);
            }
            else
            {
                tree.collapsePath(myPath);
            }
        }

        /**
         * Gets the path.
         *
         * @return the path
         */
        public TreePath getPath()
        {
            return myPath;
        }

        /**
         * Checks if is expand.
         *
         * @return true, if is expand
         */
        @SuppressWarnings("unused")
        public boolean isExpand()
        {
            return myExpand;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(myExpand ? "{EXPAND}" : "{COLLAPSE}");
            sb.append(createPathIdentifier(myPath));
            return sb.toString();
        }
    }
}
