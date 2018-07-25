package io.opensphere.mantle.iconproject.impl;

import java.util.Collections;
import java.util.List;

import javafx.scene.control.TreeItem;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;

public final class DefaultIconRecordTreeItemObject implements IconRecordTreeItemUserObject
{
    /** The Icon record. */
    private final List<IconRecord> myIconRecords;

    /** The Label. */
    private final String myLabel;

    /** The Name type. */
    private final NameType myNameType;

    /** The TreeItem. */
    private final TreeItem<String> myItem;

    /** The Type. */
    private final Type myType;

    /**
     * Creates the folder node.
     *
     * @param node the node
     * @param label the label
     * @param nt the nametype
     * @return the default icon record tree node user object
     */
    public static DefaultIconRecordTreeItemObject createFolderNode(TreeItem<String> item, String label, NameType nt)
    {
        System.out.println("folder created!!!!!  " + label);

        return new DefaultIconRecordTreeItemObject(item, label, null, Type.FOLDER, nt);
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
    public static DefaultIconRecordTreeItemObject createLeafNode(TreeItem<String> item, String label,
            List<IconRecord> recs, NameType nt)
    {
        System.out.println("leaf created!!!!!  " + label);
        return new DefaultIconRecordTreeItemObject(item, label, recs, Type.LEAF, nt);
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
    private DefaultIconRecordTreeItemObject(TreeItem<String> item, String label, List<IconRecord> recs, Type type,
            NameType nt)
    {
        //
        myType = type;
        myNameType = nt;
        myLabel = label;
        myItem = item;
        myIconRecords = recs;
        myItem.setValue(myLabel);
    }

    @Override
    public TreeItem<String> getMyTreeItem()
    {
        return myItem;
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
        getChildrenRecords(subList, myItem, recurse);
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
    private void getChildrenRecords(List<IconRecord> addToList, TreeItem<String> item, boolean recurse)
    {
        /*if (item != null && item.getUserObject() instanceof DefaultIconRecordTreeItemObject)
        {
            DefaultIconRecordTreeItemObject nodeObj = (DefaultIconRecordTreeItemObject)node.getUserObject();
            if (nodeObj.getType() == Type.LEAF)
            {
                addToList.addAll(nodeObj.myIconRecords);
            }
            else
            {
                if (item.getChildCount() > 0)
                {
                    for (int i = 0; i < item.getChildCount(); i++)
                    {
                        TreeNode tn = item.getChildAt(i);
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
        }*/
        System.out.println("ahhhhhh");
    }

}
