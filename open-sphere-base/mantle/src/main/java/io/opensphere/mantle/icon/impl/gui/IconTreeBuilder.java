package io.opensphere.mantle.icon.impl.gui;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.DefaultIconRecordTreeNodeUserObject;

/** Builds the icon tree. */
public class IconTreeBuilder
{
    /** The icon registry. */
    private final IconRegistry myIconRegistry;

    /**
     * Constructor.
     *
     * @param iconRegistry The icon registry
     */
    public IconTreeBuilder(IconRegistry iconRegistry)
    {
        myIconRegistry = iconRegistry;
    }

    /**
     * Creates a tree structure with the icon records from the registry that
     * match the filter criteria. The tree is structured by collection name,
     * sub-category, and icon record. The user object for each leaf-node is the
     * {@link IconRecordTreeNodeUserObject}
     *
     * @param filter the filter for selecting records to be included.
     * @return the root node of the tree.
     */
    public TreeNode getIconRecordTree(Predicate<IconRecord> filter)
    {
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        List<IconRecord> records = myIconRegistry.getIconRecords(filter);
        Collections.sort(records, (r1, r2) -> AlphanumComparator.compareNatural(r1.getImageURL().toString(), r2.getImageURL().toString()));
        Set<String> collectionSet = New.set();
        Map<String, Map<String, List<IconRecord>>> iconRecordMapCollection = New.map();
        String defaultSubCategory = "DEFAULT";
        for (IconRecord record : records)
        {
            String collectionName = record.getCollectionName() == null ? IconRecord.DEFAULT_COLLECTION : record.getCollectionName();
            if (collectionName == null)
            {
                collectionName = IconRecord.DEFAULT_COLLECTION;
            }

            collectionSet.add(collectionName);
            String subCategory = record.getSubCategory() == null ? defaultSubCategory : record.getSubCategory();

            Map<String, List<IconRecord>> iconRecordMap = iconRecordMapCollection.get(collectionName);
            if (iconRecordMap == null)
            {
                iconRecordMap = New.map();
                iconRecordMapCollection.put(collectionName, iconRecordMap);
            }

            List<IconRecord> recordList = iconRecordMap.get(subCategory);
            if (recordList == null)
            {
                recordList = New.linkedList();
                iconRecordMap.put(subCategory, recordList);
            }
            recordList.add(record);
        }

        buildTreeFromMaps(rootNode, collectionSet, iconRecordMapCollection, defaultSubCategory);

        return rootNode;
    }

    /**
     * Builds the tree from maps.
     *
     * @param rootNode the root node
     * @param collectionSet the collection set
     * @param iconRecordMapCollection the collection to sub cat icon rec
     *            map
     * @param defaultSubCategory the default sub cat
     */
    private void buildTreeFromMaps(DefaultMutableTreeNode rootNode, Set<String> collectionSet,
            Map<String, Map<String, List<IconRecord>>> iconRecordMapCollection, String defaultSubCategory)
    {
        List<String> collectionList = New.list(collectionSet);
        Collections.sort(collectionList);
        collectionList.remove(IconRecord.DEFAULT_COLLECTION);
        collectionList.add(0, IconRecord.DEFAULT_COLLECTION);
        if (collectionList.remove(IconRecord.USER_ADDED_COLLECTION))
        {
            collectionList.add(0, IconRecord.USER_ADDED_COLLECTION);
        }
        if (collectionList.remove(IconRecord.FAVORITES_COLLECTION))
        {
            collectionList.add(0, IconRecord.FAVORITES_COLLECTION);
        }

        for (String collection : collectionList)
        {
            DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode();
            Map<String, List<IconRecord>> iconRecordMap = iconRecordMapCollection.get(collection);
            if (iconRecordMap != null)
            {
                rootNode.add(collectionNode);
                List<String> subCategoryList = New.list(iconRecordMap.keySet());
                Collections.sort(subCategoryList);
                if (subCategoryList.remove(defaultSubCategory))
                {
                    List<IconRecord> defaultRecList = iconRecordMap.get(defaultSubCategory);
                    collectionNode.setUserObject(DefaultIconRecordTreeNodeUserObject.createLeafNode(collectionNode, collection, defaultRecList,
                            IconRecordTreeNodeUserObject.NameType.COLLECTION));
                }
                else
                {
                    collectionNode.setUserObject(DefaultIconRecordTreeNodeUserObject.createFolderNode(collectionNode, collection,
                            IconRecordTreeNodeUserObject.NameType.COLLECTION));
                }

                for (String subCategory : subCategoryList)
                {
                    DefaultMutableTreeNode subNode = new DefaultMutableTreeNode();
                    subNode.setUserObject(DefaultIconRecordTreeNodeUserObject.createLeafNode(subNode, subCategory,
                            iconRecordMap.get(subCategory), IconRecordTreeNodeUserObject.NameType.SUBCATEGORY));
                    collectionNode.add(subNode);
                }
            }
        }
    }
}
