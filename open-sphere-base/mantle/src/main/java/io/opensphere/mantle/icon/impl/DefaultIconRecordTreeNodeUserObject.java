package io.opensphere.mantle.icon.impl;

import java.util.Collections;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject;

/**
 * The Class DefaultIconRecordTreeNodeUserObject.
 */
public final class DefaultIconRecordTreeNodeUserObject implements IconRecordTreeNodeUserObject
{
    /** The Icon record. */
    private final List<IconRecord> myIconRecords;

    /** The Label. */
    private final String myLabel;

    /** The Name type. */
    private final NameType myNameType;

    /** The Node. */
    private final DefaultMutableTreeNode myNode;

    /** The Type. */
    private final Type myType;

    /**
     * Creates the folder node.
     *
     * @param node the node
     * @param label the label
     * @param nt the nt
     * @return the default icon record tree node user object
     */
    public static DefaultIconRecordTreeNodeUserObject createFolderNode(DefaultMutableTreeNode node, String label, NameType nt)
    {
        return new DefaultIconRecordTreeNodeUserObject(node, label, null, Type.FOLDER, nt);
    }

    /**
     * Creates the leaf node.
     *
     * @param node the node
     * @param label the label
     * @param recs the recs
     * @param nt the nt
     * @return the default icon record tree node user object
     */
    public static DefaultIconRecordTreeNodeUserObject createLeafNode(DefaultMutableTreeNode node, String label,
            List<IconRecord> recs, NameType nt)
    {
        return new DefaultIconRecordTreeNodeUserObject(node, label, recs, Type.LEAF, nt);
    }

    /**
     * Instantiates a new default icon record tree node user object.
     *
     * @param node the node
     * @param label the label
     * @param recs the recs
     * @param type the type
     * @param nt the nt
     */
    private DefaultIconRecordTreeNodeUserObject(DefaultMutableTreeNode node, String label, List<IconRecord> recs, Type type,
            NameType nt)
    {
        myType = type;
        myNameType = nt;
        myLabel = label;
        myNode = node;
        myIconRecords = recs;
    }

    @Override
    public String getLabel()
    {
        return myLabel;
    }

    @Override
    public NameType getNameType()
    {
        return myNameType;
    }

    @Override
    public List<IconRecord> getRecords(boolean recurse)
    {
        List<IconRecord> subList = New.linkedList();
        getChildrenRecords(subList, myNode, recurse);
        return subList.isEmpty() ? Collections.<IconRecord>emptyList() : New.list(subList);
    }

    @Override
    public Type getType()
    {
        return myType;
    }

    @Override
    public String toString()
    {
        return myLabel;
    }

    /**
     * Gets the children records.
     *
     * @param addToList the add to list
     * @param node the node
     * @param recurse the recurse
     */
    private void getChildrenRecords(List<IconRecord> addToList, DefaultMutableTreeNode node, boolean recurse)
    {
        if (node != null && node.getUserObject() instanceof DefaultIconRecordTreeNodeUserObject)
        {
            DefaultIconRecordTreeNodeUserObject nodeObj = (DefaultIconRecordTreeNodeUserObject)node.getUserObject();
            if (nodeObj.getType() == Type.LEAF)
            {
                addToList.addAll(nodeObj.myIconRecords);
            }
            else
            {
                if (node.getChildCount() > 0)
                {
                    for (int i = 0; i < node.getChildCount(); i++)
                    {
                        TreeNode tn = node.getChildAt(i);
                        if (tn instanceof DefaultMutableTreeNode)
                        {
                            DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)tn;
                            if (dmtn.getUserObject() instanceof IconRecordTreeNodeUserObject)
                            {
                                IconRecordTreeNodeUserObject childUO = (IconRecordTreeNodeUserObject)dmtn.getUserObject();
                                if (childUO.getType() == Type.LEAF || childUO.getType() == Type.FOLDER && recurse)
                                {
                                    getChildrenRecords(addToList, dmtn, recurse);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
