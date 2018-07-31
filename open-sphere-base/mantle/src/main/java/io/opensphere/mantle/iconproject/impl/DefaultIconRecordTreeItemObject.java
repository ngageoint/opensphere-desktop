package io.opensphere.mantle.iconproject.impl;

import java.util.Collections;
import java.util.List;

import javafx.scene.control.TreeItem;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;

/**
 * The Class DefaultIconRecordTreeItemUserObject.
 */
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

    /** The Parent Collection. */
    private final String myParent;

    /**
     * Creates the folder node.
     *
     * @param item the treeItem
     * @param label the label
     * @param nt the nametype
     * @param parent the name of the parent treeItem
     *
     * @return the default icon record tree node user object
     */
    public static DefaultIconRecordTreeItemObject createFolderNode(TreeItem<String> item, String label, NameType nt, String parent)
    {
        return new DefaultIconRecordTreeItemObject(item, label, null, Type.FOLDER, nt, parent);
    }

    /**
     * Creates the leaf node.
     *
     * @param item the treeItem
     * @param label the label
     * @param recs the recs
     * @param nt the nt
     * @param parent the name of the parent treeItem
     *
     * @return the default icon record tree node user object
     */
    public static DefaultIconRecordTreeItemObject createLeafNode(TreeItem<String> item, String label,
            List<IconRecord> recs, NameType nt, String parent)
    {
        return new DefaultIconRecordTreeItemObject(item, label, recs, Type.LEAF, nt, parent);
    }

    /**
     * Instantiates a new default icon record tree node user object.
     *
     * @param item the treeItem
     * @param label the label
     * @param recs the recs
     * @param type the type
     * @param nt the nt
     * @param parent the parent treeItem
     */
    private DefaultIconRecordTreeItemObject(TreeItem<String> item, String label, List<IconRecord> recs, Type type,
            NameType nt, String parent)
    {
        //
        myType = type;
        myNameType = nt;
        myLabel = label;
        myItem = item;
        myIconRecords = recs;
        myParent = parent;
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
    public String getParent()
    {
        return myParent;
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
     * @param item the treeItem
     * @param recurse the recurse
     */
    private void getChildrenRecords(List<IconRecord> addToList, TreeItem<String> item, boolean recurse)
    {
        if (item != null)// && item.getUserObject() instanceof DefaultIconRecordTreeItemObject)
        {
            //DefaultIconRecordTreeItemObject nodeObj = (DefaultIconRecordTreeItemObject)node.getUserObject();
            if (myType == Type.LEAF)
            {
                addToList.addAll(myIconRecords);
            }
        }
    }
}
