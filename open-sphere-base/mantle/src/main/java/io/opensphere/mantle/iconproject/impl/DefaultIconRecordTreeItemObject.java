package io.opensphere.mantle.iconproject.impl;

import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.iconproject.model.IconRecordTreeItemUserObject;

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
    private final ObjectProperty<TreeItem<String>> myItem = new SimpleObjectProperty<TreeItem<String>>();

    /** The Type. */
    private final Type myType;

    /** The Parent Collection. */
    private final String myParent;

    /** The Observable Selected Tree Node. */
    @SuppressWarnings("rawtypes")
    private ObjectProperty<TreeView> mySelectedTreeNode = new SimpleObjectProperty<TreeView>();

    /**
     * Creates the folder node.
     *
     * @param item the treeItem
     * @param label the label
     * @param nametype the nametype
     * @param parent the name of the parent treeItem
     *
     * @return the default icon record tree node user object
     */
    public static DefaultIconRecordTreeItemObject createFolderNode(TreeItem<String> item, String label, NameType nametype,
            String parent)
    {
        return new DefaultIconRecordTreeItemObject(item, label, null, Type.FOLDER, nametype, parent);
    }

    /**
     * Creates the leaf node.
     *
     * @param item the treeItem
     * @param label the label
     * @param recs the list of icon records
     * @param nametype the nametype
     * @param parent the name of the parent treeItem
     *
     * @return the default icon record tree node user object
     */
    public static DefaultIconRecordTreeItemObject createLeafNode(TreeItem<String> item, String label, List<IconRecord> recs,
            NameType nametype, String parent)
    {
        return new DefaultIconRecordTreeItemObject(item, label, recs, Type.LEAF, nametype, parent);
    }

    /**
     * Instantiates a new default icon record tree node user object.
     *
     * @param item the treeItem
     * @param label the label
     * @param records the list of icon records
     * @param type the type
     * @param nametype the nametype
     * @param parent the name of the parent treeItem
     */
    public DefaultIconRecordTreeItemObject(TreeItem<String> item, String label, List<IconRecord> records, Type type, NameType nametype,
            String parent)
    {
        myType = type;
        myNameType = nametype;
        myLabel = label;
        myItem.set(item);
        myIconRecords = records;
        myParent = parent;
        myItem.get().setValue((myLabel));
    }

    @Override
    public ObjectProperty<TreeItem<String>> getMyTreeItem()
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
        getChildrenRecords(subList, myItem.get(), recurse);
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
        if (item != null)
        {
            if (myType == Type.LEAF)
            {
                addToList.addAll(myIconRecords);
            }
        }
    }

    /**
     * Gets the value of the {@link #mySelectedTreeNode} field.
     *
     * @return the value stored in the {@link #mySelectedTreeNode} field.
     */
    @SuppressWarnings("rawtypes")
    public ObjectProperty<TreeView> getMyObsTree()
    {
        return mySelectedTreeNode;
    }
}
