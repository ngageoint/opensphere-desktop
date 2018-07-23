package io.opensphere.mantle.iconproject.view;

import java.util.Collections;
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

/**
 * The TreeBuilder class.
 *
 */
public class TreeBuilder extends TreeItem<String>
{
    /** The icon registry. */
    private final IconRegistry myIconRegistry;

    /**
     * Creates a tree structure with the icon records from the registry that
     * match the filter criteria. The tree is structured by collection name,
     * sub-category, and icon record. The user object for each leaf-node is the
     * {@link IconRecordTreeNodeUserObject}
     *
     * @param iconReg the iconRegistry
     * @param filter the filter for selecting records to be included.
     */
    public TreeBuilder(IconRegistry iconReg, Predicate<IconRecord> filter)
    {
        myIconRegistry = iconReg;

        // new TreeItem<String>("Icon Registry"); // not sure about this line
        // tbh
        // DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

        List<IconRecord> records = myIconRegistry.getIconRecords(filter);
        Collections.sort(records, (r1, r2) -> AlphanumComparator.compareNatural(r1.getImageURL().toString(), r2.getImageURL().toString()));
        Set<String> collectionSet = New.set();
        Map<String, Map<String, List<IconRecord>>> collectionToSubCatIconRecMap = New.map();
        String defaultSubCat = "DEFAULT";
        for (IconRecord rec : records)
        {
            String collection = rec.getCollectionName() == null ? IconRecord.DEFAULT_COLLECTION : rec.getCollectionName();
            if (collection == null)
            {
                collection = IconRecord.DEFAULT_COLLECTION;
            }

            collectionSet.add(collection);
            String subCat = rec.getSubCategory() == null ? defaultSubCat : rec.getSubCategory();

            Map<String, List<IconRecord>> subCatToRecListMap = collectionToSubCatIconRecMap.get(collection);
            if (subCatToRecListMap == null)
            {
                subCatToRecListMap = New.map();
                collectionToSubCatIconRecMap.put(collection, subCatToRecListMap);
            }

            List<IconRecord> recList = subCatToRecListMap.get(subCat);
            if (recList == null)
            {
                recList = New.linkedList();
                subCatToRecListMap.put(subCat, recList);
            }
            recList.add(rec);
        }

        buildTreeFromMaps(this, collectionSet, collectionToSubCatIconRecMap, defaultSubCat);
        setExpanded(true);
    }

    /**
     * Builds the tree from maps.
     *
     * @param rootNode the root node
     * @param collectionSet the collection set
     * @param collectionToSubCatIconRecMap the collection to sub cat icon rec
     *            map
     * @param defaultSubCat the default sub cat
     */
    private void buildTreeFromMaps(TreeItem<String> rootNode, Set<String> collectionSet,
            Map<String, Map<String, List<IconRecord>>> collectionToSubCatIconRecMap, String defaultSubCat)
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
            // DefaultMutableTreeNode colNode = new DefaultMutableTreeNode();
            TreeItem<String> colNode = new TreeItem<String>(collection);
            Map<String, List<IconRecord>> subToRecListMap = collectionToSubCatIconRecMap.get(collection);
            if (subToRecListMap != null)
            {
                List<String> subCatList = New.list(subToRecListMap.keySet());
                Collections.sort(subCatList);

                if (subCatList.remove(defaultSubCat))
                {
                    // getChildren().add(new TreeItem<String>(collection));
                    getChildren().add(colNode);
                    // set tree object thingy (colNode) to name collection and
                    // the reclist defaultRecList
                    // this is the leaf one
                }
                else // like !found from example
                {
                    // set "folder" with collection name
                    getChildren().add(colNode);
                }
                for (String subCat : subCatList)
                {
                    TreeItem<String> depNode = new TreeItem<String>(subCat);
                    colNode.getChildren().add(depNode);
                }
            }
        }
        // setExpanded(true);
    }
}
