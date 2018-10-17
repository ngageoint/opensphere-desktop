package io.opensphere.mantle.iconproject.model;

import java.util.List;

import javafx.scene.control.TreeItem;

import io.opensphere.mantle.icon.IconRecord;

/**
 * The interface IconRecordTreeItemUserObject.
 */
public interface IconRecordTreeItemUserObject
{
    /**
     * Gets the treeItem.
     *
     * @return the treeItem
     */
    TreeItem<String> getTreeItem();

    /**
     * Gets the label.
     *
     * @return the label
     */
    String getLabel();

    /**
     * Gets the name type.
     *
     * @return the name type
     */
    NameType getNameType();

    /**
     * Gets icon records for this node.
     *
     * @return the child {@link IconRecord}s.
     */
    List<IconRecord> getRecords();

    /**
     * Gets the type.
     *
     * @return the type
     */
    Type getType();

    /**
     * Gets the parent collection.
     *
     * @return the parent collection
     */
    String getParent();

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    String toString();

    /**
     * The Enum NameType.
     */
    enum NameType
    {
        /** The COLLECTION. */
        COLLECTION,

        /** The SUBCATEGORY. */
        SUBCATEGORY
    }

    /**
     * The Enum Type.
     */
    enum Type
    {
        /** The FOLDER. */
        FOLDER,

        /** The LEAF. */
        LEAF
    }
}
