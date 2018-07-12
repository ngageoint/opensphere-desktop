package io.opensphere.mantle.iconproject.impl;

import java.awt.Window;
import java.io.File;
import java.net.MalformedURLException;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.lang3.StringUtils;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.image.ImageUtil;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.swing.JTreeUtilities;
import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconProvider;
import io.opensphere.mantle.icon.impl.gui.IconCollectionNamePanel;
import io.opensphere.mantle.icon.impl.gui.IconTreeBuilder;
import io.opensphere.mantle.icon.impl.gui.SubCategoryPanel;
import io.opensphere.mantle.util.MantleToolboxUtils;

/**
 * Instantiates a new add icon from file dialog.
 *
 * @param tb the {@link Toolbox}
 */
public class FileAddBuilder
{
    /** The owner window. */
    private final Window owner;

    /** The Toolbox. */
    private final Toolbox myToolbox;

    /** The Icon registry. */
    private final IconRegistry myIconRegistry;

    /** The Tree. */
    private final JTree myTree;

    /** The Last selected tree node user object. */
    private IconRecordTreeNodeUserObject myLastSelectedTreeNodeUserObject;

    /** The Root tree node. */
    @SuppressWarnings("PMD.SingularField")
    private TreeNode myRootTreeNode;

    /** The Tree model. */
    private DefaultTreeModel myTreeModel;

    /** The optional selected icon URL. */
    private String mySelectedUrl;

    public FileAddBuilder(Toolbox tb)
    {
        myToolbox = tb;
        myIconRegistry = MantleToolboxUtils.getMantleToolbox(myToolbox).getIconRegistry();
        owner = myToolbox.getUIRegistry().getMainFrameProvider().get();
        // JPopupMenu iconPopupMenu = new JPopupMenu();
        // JPopupMenu treePopupMenu = new JPopupMenu();
        // JButton buildIcon = new JButton("Build New Icon");
        // myTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());

        myTree = new JTree();

        addIconFromFile();
    }

    /**
     * Adds the icon from file.
     */
    private void addIconFromFile()
    {
        String colName = getCollectionNameFromUser();
        if (!StringUtils.isBlank(colName))
        {
            SubCategoryPanel pnl = getSubCategoryFromUser(colName, false);
            if (pnl != null)
            {
                loadFromFile(colName, pnl.getCategory());
            }
        }
    }

    /**
     * Load from file.
     *
     * @param collectionName the collection name
     * @param subCatName the sub cat name
     */
    public void loadFromFile(String collectionName, String subCatName)
    {
        File result = ImageUtil.showImageFileChooser("Choose Icon File", owner, myToolbox.getPreferencesRegistry());

        if (result != null)
        {
            try
            {
                myIconRegistry.addIcon(new DefaultIconProvider(result.toURI().toURL(), collectionName, subCatName, "User"),
                        owner);
                refreshFromRegistry(collectionName);
            }
            catch (MalformedURLException e)
            {
                JOptionPane.showMessageDialog(owner, "Failed to load image: " + result.getAbsolutePath(), "Image Load Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Refresh from registry.
     *
     * @param collectionToShow the name of the collection to show, or null
     */
    public final void refreshFromRegistry(String collectionToShow)
    {
        EventQueueUtilities.runOnEDT(new Runnable()
        {
            @Override
            public void run()
            {
                if (myTree != null)
                {
                    myLastSelectedTreeNodeUserObject = null;
                    myRootTreeNode = new IconTreeBuilder(myIconRegistry).getIconRecordTree(null);
                    if (myTreeModel == null)
                    {
                        myTreeModel = new DefaultTreeModel(myRootTreeNode);
                    }
                    else
                    {
                        myTreeModel.setRoot(myRootTreeNode);
                    }
                    myTree.setModel(myTreeModel);
                    myTree.revalidate();
                    JTreeUtilities.expandOrCollapseAll(myTree, true);

                    TreeNode nodeToSelect = getNodeToSelect(collectionToShow);
                    if (nodeToSelect != null)
                    {
                        TreeNode[] nodeArray = myTreeModel.getPathToRoot(nodeToSelect);
                        TreePath path = new TreePath(nodeArray);
                        myTree.getSelectionModel().setSelectionPath(path);
                    }
                }
            }
        });
    }

    /**
     * Gets the collection name from user.
     *
     * @return the collection name from user
     */
    private String getCollectionNameFromUser()
    {
        String resultCollectionName = null;
        IconCollectionNamePanel pnl = new IconCollectionNamePanel(myIconRegistry.getCollectionNames());

        boolean done = false;
        while (!done)
        {
            int result = JOptionPane.showConfirmDialog(owner, pnl, "Collection Name Selection", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION)
            {
                String colName = pnl.getCollectionName();
                if (StringUtils.isBlank(colName))
                {
                    JOptionPane.showMessageDialog(owner, "The collection name can not be blank.", "Collection Name Error",
                            JOptionPane.PLAIN_MESSAGE);
                }
                else
                {
                    resultCollectionName = colName;
                    done = true;
                }
            }
            else
            {
                done = true;
            }
        }
        return resultCollectionName;
    }

    /**
     * Gets the sub category from user.
     *
     * @param collectionName the collection name
     * @param subCatsFromDirName the sub cats from dir name
     * @return the sub category from user ( null if cancelled )
     */
    private SubCategoryPanel getSubCategoryFromUser(String collectionName, boolean subCatsFromDirName)
    {
        SubCategoryPanel pnl = new SubCategoryPanel(myIconRegistry.getSubCategoiresForCollection(collectionName),
                subCatsFromDirName);

        boolean done = false;
        boolean cancelled = false;
        while (!done)
        {
            int result = JOptionPane.showConfirmDialog(owner, pnl, "Sub-Category Selection", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION)
            {
                String colName = pnl.getCategory();
                if (!pnl.isSubCatsFromDirNames() && !pnl.isNoCategory() && StringUtils.isBlank(colName))
                {
                    JOptionPane.showMessageDialog(owner, "The sub-category name can not be blank.", "Sub-category Name Error",
                            JOptionPane.PLAIN_MESSAGE);
                }
                else
                {
                    done = true;
                }
            }
            else
            {
                done = true;
                cancelled = true;
            }
        }
        return cancelled ? null : pnl;
    }

    /**
     * Gets the node to select.
     *
     * @param collectionToShow the requested collection to show
     * @return the node to select, or null
     */
    private TreeNode getNodeToSelect(String collectionToShow)
    {
        TreeNode nodeToSelect = null;

        // If there is a requested collection to show, find its node
        if (collectionToShow != null)
        {
            for (int i = 0; i < myRootTreeNode.getChildCount(); i++)
            {
                if (collectionToShow.equals(myRootTreeNode.getChildAt(i).toString()))
                {
                    nodeToSelect = myRootTreeNode.getChildAt(i);
                    break;
                }
            }
        }

        if (nodeToSelect == null && myRootTreeNode.getChildCount() > 0)
        {
            // If there is a selected icon, find its node
            if (mySelectedUrl != null)
            {
                for (int i = 0; i < myRootTreeNode.getChildCount(); i++)
                {
                    TreeNode child = myRootTreeNode.getChildAt(i);
                    if (child instanceof DefaultMutableTreeNode)
                    {
                        DefaultMutableTreeNode mtn = (DefaultMutableTreeNode)child;
                        Object userObj = mtn.getUserObject();
                        if (userObj instanceof IconRecordTreeNodeUserObject)
                        {
                            IconRecordTreeNodeUserObject irNode = (IconRecordTreeNodeUserObject)userObj;
                            boolean hasIcon = irNode.getRecords(true).stream()
                                    .anyMatch(r -> mySelectedUrl.equals(r.getImageURL().toString()));
                            if (hasIcon)
                            {
                                nodeToSelect = mtn;
                                break;
                            }
                        }
                    }
                }
            }
            // Default to the first one
            else
            {
                nodeToSelect = myRootTreeNode.getChildAt(0);
            }
        }
        return nodeToSelect;
    }

    /**
     * Gets the last selected tree node user object.
     *
     * @return the last selected tree node user object
     */
    public IconRecordTreeNodeUserObject getLastSelectedTreeNodeUserObject()
    {
        return myLastSelectedTreeNodeUserObject;
    }

    /**
     * Sets the selected icon URL.
     *
     * @param selectedUrl the icon URL
     */
    public void setSelectedUrl(String selectedUrl)
    {
        mySelectedUrl = selectedUrl;
    }

    public String getSelectedURL()
    {
        return mySelectedUrl;
    }

}
