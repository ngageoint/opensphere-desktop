package io.opensphere.mantle.iconproject.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.icon.IconRecord;
import io.opensphere.mantle.icon.IconRecordTreeNodeUserObject;
import io.opensphere.mantle.icon.IconRegistry;
import io.opensphere.mantle.icon.impl.gui.AlphanumComparator;
import io.opensphere.mantle.iconproject.model.PanelModel;
import io.opensphere.mantle.iconproject.panels.TreeBuilder;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class RegistryMap
{
    private PanelModel myPanelModel;

    private IconRegistry myIconRegistry;

    /** The object that holds main treeItem info. */
    private DefaultIconRecordTreeItemObject iconTreeObject;

    /**
     * The icon record map with the collection name string as the key and the
     * list of the icon record as the value.
     */
    private Map<String, List<IconRecord>> recordMap = new HashMap<>();

    private TreeBuilder treeBuilder;

    private TreeView myTreeView;

    public RegistryMap(PanelModel thePanelModel)
    {
        myPanelModel = thePanelModel;
    }

    public void refreshRegistry()
    {
        TreeBuilder(myPanelModel, null);
    }

    /**
     * Creates a tree structure with the icon records from the registry that
     * match the filter criteria. The tree is structured by collection name,
     * sub-category, and icon record. The user object for each leaf-node is the
     * {@link IconRecordTreeNodeUserObject}
     *
     * @param thePanelModel the model for the main panel
     * @param filter the filter for selecting records to be included.
     * @return
     */
    public void TreeBuilder(PanelModel thePanelModel, Predicate<IconRecord> filter)
    {
        myPanelModel = thePanelModel;
        myIconRegistry = myPanelModel.getMyIconRegistry();

        treeBuilder = new TreeBuilder(myPanelModel, null);
        myTreeView = new TreeView<>(treeBuilder);
        myTreeView.setShowRoot(false);
        myTreeView.getSelectionModel().select(myTreeView.getRow((myTreeView.getTreeItem(2))));
        recordMap = new HashMap<>(treeBuilder.getRecordMap());
        // myPanelModel.setIconRecordList(recordMap.get("Default"));
        myPanelModel.setIconRecordList(recordMap.get(myPanelModel.getIconRecord().getCollectionName()));

//
//        List<IconRecord> records = myIconRegistry.getIconRecords(filter);
//        Collections.sort(records,
//                (r1, r2) -> AlphanumComparator.compareNatural(r1.getImageURL().toString(), r2.getImageURL().toString()));
//        Set<String> collectionSet = New.set();
//        Map<String, Map<String, List<IconRecord>>> collectionToSubCatIconRecMap = New.map();
//        String defaultSubCat = "DEFAULT";
//        for (IconRecord rec : records)
//        {
//            String collection = rec.getCollectionName() == null ? IconRecord.DEFAULT_COLLECTION : rec.getCollectionName();
//            if (collection == null)
//            {
//                collection = IconRecord.DEFAULT_COLLECTION;
//            }
//
//            collectionSet.add(collection);
//            String subCat = rec.getSubCategory() == null ? defaultSubCat : rec.getSubCategory();
//
//            Map<String, List<IconRecord>> subCatToRecListMap = collectionToSubCatIconRecMap.get(collection);
//            if (subCatToRecListMap == null)
//            {
//                subCatToRecListMap = New.map();
//                collectionToSubCatIconRecMap.put(collection, subCatToRecListMap);
//            }
//
//            List<IconRecord> recList = subCatToRecListMap.get(subCat);
//            if (recList == null)
//            {
//                recList = New.linkedList();
//                subCatToRecListMap.put(subCat, recList);
//            }
//            recList.add(rec);
//        }
//    }
        // myPanelModel.setIconRecordList(recList);
    }
}
