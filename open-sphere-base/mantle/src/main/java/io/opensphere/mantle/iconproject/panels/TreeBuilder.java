package io.opensphere.mantle.iconproject.panels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javafx.scene.control.TreeItem;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.gui.AlphanumComparator;
import io.opensphere.mantle.iconproject.impl.DefaultIconRecordTreeItemObject;
import io.opensphere.mantle.iconproject.model.IconRecordTreeItemUserObject;
import io.opensphere.mantle.iconproject.model.PanelModel;

/**
 * The TreeBuilder class.
 * Builds the tree in the icon panel
 */
public class TreeBuilder extends TreeItem<String>
{
    /** The icon registry. */
    private final IconRegistry myIconRegistry;

    /** The object that holds main treeItem info. */
    private DefaultIconRecordTreeItemObject myIconTreeObject;

    /** The icon record map with the collection name string as the key and the list of the icon record as the value. */
    private final Map<String, List<IconRecord>> myRecordMap = new HashMap<>();

    /** The model for the main panel. */
    private final PanelModel myPanelModel;

    /**
     * Creates a tree structure with the icon records from the registry that
     * match the filter criteria. The tree is structured by collection name,
     * sub-category, and icon record. The user object for each leaf-node is the
     * {@link IconRecordTreeNodeUserObject}
     *
     * @param panelModel the model for the main panel
     * @param filter the filter for selecting records to be included.
     */
    public TreeBuilder(PanelModel panelModel, Predicate<IconRecord> filter)
    {
        myPanelModel = panelModel;
        myIconRegistry = myPanelModel.getIconRegistry();

        List<IconRecord> records = myIconRegistry.getIconRecords(filter);
        Collections.sort(records, (r1, r2) -> AlphanumComparator.compareNatural(r1.getImageURL().toString(), r2.getImageURL().toString()));
        Set<String> collectionSet = New.set();
        Map<String, Map<String, List<IconRecord>>> iconRecordMapCollection = New.map();
        String defaultSubCategory = "DEFAULT";
        for (IconRecord record : records)
        {
            String collection = record.getCollectionName() == null ? IconRecord.DEFAULT_COLLECTION : record.getCollectionName();
            if (collection == null)
            {
                collection = IconRecord.DEFAULT_COLLECTION;
            }

            collectionSet.add(collection);
            String subCategory = record.getSubCategory() == null ? defaultSubCategory : record.getSubCategory();

            Map<String, List<IconRecord>> iconRecordMap = iconRecordMapCollection.get(collection);
            if (iconRecordMap == null)
            {
                iconRecordMap = New.map();
                iconRecordMapCollection.put(collection, iconRecordMap);
            }

            List<IconRecord> recordList = iconRecordMap.get(subCategory);
            if (recordList == null)
            {
                recordList = New.linkedList();
                iconRecordMap.put(subCategory, recordList);
            }
            recordList.add(record);
        }

        buildTreeFromMaps(this, collectionSet, iconRecordMapCollection, defaultSubCategory);
        setExpanded(true);
    }

    /**
     * Builds the tree from maps.
     *
     * @param rootNode the root node
     * @param collectionSet the collection set
     * @param iconRecordMapCollection the collection of icon record collections
     * @param defaultSubCategory the default sub category
     */
    private void buildTreeFromMaps(TreeItem<String> rootNode, Set<String> collectionSet,
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
            TreeItem<String> mainNode = new TreeItem<>();
            Map<String, List<IconRecord>> iconRecordMap = iconRecordMapCollection.get(collection);
            if (iconRecordMap != null)
            {
                List<String> subCategoryList = New.list(iconRecordMap.keySet());
                Collections.sort(subCategoryList);

                if (subCategoryList.remove(defaultSubCategory))
                {
                    List<IconRecord> defaultRecList = iconRecordMap.get(defaultSubCategory);
                    myIconTreeObject = DefaultIconRecordTreeItemObject.createLeafNode(mainNode, collection, defaultRecList,
                            IconRecordTreeItemUserObject.NameType.COLLECTION, null);
                    myRecordMap.put(collection, myIconTreeObject.getRecords(true));
                }
                else
                {
                    myIconTreeObject = DefaultIconRecordTreeItemObject.createFolderNode(mainNode, collection,
                            IconRecordTreeItemUserObject.NameType.COLLECTION, null);
                    myRecordMap.put(collection, myIconTreeObject.getRecords(true));
                }

                getChildren().add(myIconTreeObject.getMyTreeItem().get());
                for (String subCategory : subCategoryList)
                {
                    TreeItem<String> newNode = new TreeItem<>();
                    myIconTreeObject = DefaultIconRecordTreeItemObject.createLeafNode(newNode, subCategory, iconRecordMap.get(subCategory),
                            IconRecordTreeItemUserObject.NameType.SUBCATEGORY, collection);
                    mainNode.getChildren().add(myIconTreeObject.getMyTreeItem().get());
                    myRecordMap.put(subCategory, myIconTreeObject.getRecords(true));
                    ArrayList<IconRecord> iconRecords = new ArrayList<>(myRecordMap.get(collection));
                    iconRecords.addAll(myIconTreeObject.getRecords(true));
                    myRecordMap.put(collection, iconRecords);
                }
            }
        }
        myPanelModel.setTreeObject(myIconTreeObject);
    }

    /**
     * Gets the record map.
     *
     * @return the record map
     */
    public Map<String, List<IconRecord>> getRecordMap()
    {
        return myRecordMap;
    }
}
