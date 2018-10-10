package io.opensphere.mantle.data.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.collections.StreamUtilities;
import io.opensphere.core.util.lang.Pair;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.mantle.data.MapVisualizationType;

/**
 * The Class DataGroupInfoGroupByUtility.
 */
@SuppressWarnings("PMD.GodClass")
public final class DataGroupInfoGroupByUtility
{
    /** The Constant myDGIComparator. */
    public static final DGIComparator ourDGIComparator = new DGIComparator();

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(DataGroupInfoGroupByUtility.class);

    /** The Constant ourDTINameAndTypeComparator. */
    private static final DTIDisplayNameAndFeatureTypeComparator ourDTINameAndTypeComparator = new DTIDisplayNameAndFeatureTypeComparator();

    /**
     * Creates the group by tree for the collection of data group info. The tree
     * will be composed of root nodes provided by the given Categorizer
     * parameter, each DataGroupInfo in the passed in collection will first be
     * filtered
     *
     * @param builder the {@link GroupByTreeBuilder}
     * @param userObjGen the {@link NodeUserObjectGenerator}
     * @param dataGroups the {@link Collection} of {@link DataGroupInfo} to be
     *            considered for inclusion.
     * @return the tree node
     */
    public static TreeNode createGroupByTree(GroupByTreeBuilder builder, NodeUserObjectGenerator userObjGen,
            Collection<DataGroupInfo> dataGroups)
    {
        Utilities.checkNull(dataGroups, "dataGroups");
        Utilities.checkNull(builder, "builder");
        Utilities.checkNull(builder.getGroupCategorizer(), "builder.getGroupCategorizer()");

        DefaultMutableTreeNode result = new DefaultMutableTreeNode();

        NodeUserObjectGenerator uoGen = userObjGen == null ? new DefaultNodeUserObjectGenerator() : userObjGen;
        TreeOptions treeOptions = builder.getTreeOptions();

        dataGroups.removeIf(e -> e.getId().equals("Area"));
        if (treeOptions == null || treeOptions.isFlattenTree())
        {
            createFlattenedTree(builder, uoGen, dataGroups, result);
        }
        else
        {
            createFullTree(builder.getDataCategoryFilter(), builder.getGroupFilter(), uoGen, dataGroups, result);
        }

        return result;
    }

    /**
     * Adds a data group node to the passed in cat node.
     *
     * @param dgi The data group to add the node for.
     * @param treeOptions The tree options.
     * @param catNode The node to add to.
     * @param userObjGen Generates the user object to give to the node.
     * @param subNodeCount keeps track of sub node count.
     * @return The new sub not count.
     */
    private static int addDataGroupNode(DataGroupInfo dgi, TreeOptions treeOptions, DefaultMutableTreeNode catNode,
            NodeUserObjectGenerator userObjGen, int subNodeCount)
    {
        int newSubNodeCount = subNodeCount;
        DefaultMutableTreeNode dgiNode;
        if (dgi.getChildren().isEmpty() && dgi.getMembers(false).size() == 1 && dgi.isFlattenable())
        {
            dgiNode = new DefaultMutableTreeNode(userObjGen.createNodeUserObject(dgi, dgi.getMembers(false).iterator().next()));
        }
        else
        {
            dgiNode = new DefaultMutableTreeNode(userObjGen.createNodeUserObject(dgi));
        }
        catNode.add(dgiNode);

        if (dgi.isFlattenable())
        {
            newSubNodeCount++;
        }

        if (treeOptions != null && (dgi.numMembers(false) > 1 || !dgi.isFlattenable())
                && treeOptions.isSubNodesForMultiMemberGroups())
        {
            for (DataTypeInfo dti : getSortedMemberList(dgi.getMembers(false)))
            {
                dgiNode.add(new DefaultMutableTreeNode(userObjGen.createNodeUserObject(dgi, dti)));
            }
        }

        for (DataGroupInfo child : dgi.getChildren())
        {
            newSubNodeCount = addDataGroupNode(child, treeOptions, dgiNode, userObjGen, newSubNodeCount);
        }

        return newSubNodeCount;
    }

    /**
     * Adds the to group if passes filter.
     *
     * @param dataCategoryFilter the data category filter
     * @param groupFilter the group filter
     * @param addToList the add to list
     * @param dgi the dgi
     */
    private static void addToGroupIfPassesFilter(Predicate<DataGroupInfo> dataCategoryFilter,
            Predicate<DataGroupInfo> groupFilter, List<DataGroupInfo> addToList, DataGroupInfo dgi)
    {
        if ((dataCategoryFilter == null || dataCategoryFilter.test(dgi)) && (groupFilter == null || groupFilter.test(dgi)))
        {
            addToList.add(dgi);
        }
        if (dgi.hasChildren() && dgi.isFlattenable())
        {
            for (DataGroupInfo child : dgi.getChildren())
            {
                addToGroupIfPassesFilter(dataCategoryFilter, groupFilter, addToList, child);
            }
        }
    }

    /**
     * Determines if the group or any of it's descendants passes the filter.
     *
     * @param groupFilter the group filter
     * @param dgi the dgi
     * @return whether the group or any of it's descendants passes the filter
     */
    private static boolean anyGroupPassesFilter(Predicate<DataGroupInfo> groupFilter, DataGroupInfo dgi)
    {
        boolean passes = false;
        if (dgi.hasChildren())
        {
            for (DataGroupInfo child : dgi.getChildren())
            {
                if (anyGroupPassesFilter(groupFilter, child))
                {
                    passes = true;
                    break;
                }
            }
        }
        else
        {
            passes = groupFilter == null || groupFilter.test(dgi);
        }
        return passes;
    }

    /**
     * Creates the flattened tree.
     *
     * @param builder the {@link GroupByTreeBuilder}
     * @param userObjGen the {@link NodeUserObjectGenerator}
     * @param dataGroups the {@link Collection} of {@link DataGroupInfo} to be
     *            considered for inclusion.
     * @param parentNode the parent node
     */
    private static void createFlattenedTree(GroupByTreeBuilder builder, NodeUserObjectGenerator userObjGen,
            Collection<DataGroupInfo> dataGroups, DefaultMutableTreeNode parentNode)
    {
        TreeOptions treeOptions = builder.getTreeOptions();
        if (treeOptions == null || !treeOptions.isBuildWithTypesInsteadOfGroups())
        {
            Map<String, List<DataGroupInfo>> catToGroupsMap = createGroupBy(builder.getDataCategoryFilter(),
                    builder.getGroupFilter(), builder.getGroupComparator(), builder.getGroupCategorizer(), dataGroups);

            List<String> categories = builder.getGroupCategorizer().getAllCategories();

            for (String cat : categories)
            {
                GroupByNodeUserObject catNodeUserObj = userObjGen.createNodeUserObject(cat);
                DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(catNodeUserObj);
                List<DataGroupInfo> dgiList = catToGroupsMap.get(cat);
                int subNodeCount = 0;
                if (dgiList != null && !dgiList.isEmpty())
                {
                    for (DataGroupInfo dgi : dgiList)
                    {
                        subNodeCount = addDataGroupNode(dgi, treeOptions, catNode, userObjGen, subNodeCount);
                    }
                }
                catNodeUserObj.setLabel(catNodeUserObj.getLabel());
                catNodeUserObj.setCategoryCount(subNodeCount);

                if (catNode.getChildCount() == 1)
                {
                    TreeNode onlyChild = catNode.getChildAt(0);
                    if (onlyChild.toString().equals(catNode.toString()) && onlyChild instanceof DefaultMutableTreeNode)
                    {
                        catNode.remove(0);
                        for (int i = 0; i < onlyChild.getChildCount();)
                        {
                            Object nextElement = onlyChild.getChildAt(i);
                            if (nextElement instanceof MutableTreeNode)
                            {
                                catNode.add((MutableTreeNode)nextElement);
                            }
                        }
                    }
                }

                if (catNode.getChildCount() > 0)
                {
                    parentNode.add(catNode);
                }
            }
        }
        else
        {
            Map<String, List<Pair<DataGroupInfo, DataTypeInfo>>> catToTypeMap = createGroupByDataType(
                    builder.getDataCategoryFilter(), builder.getGroupFilter(), builder.getTypeComparator(),
                    builder.getGroupCategorizer(), dataGroups);

            List<String> categories = builder.getGroupCategorizer().getAllCategories();

            for (String cat : categories)
            {
                GroupByNodeUserObject catNodeUserObj = userObjGen.createNodeUserObject(cat);
                DefaultMutableTreeNode catNode = new DefaultMutableTreeNode(catNodeUserObj);
                int subNodeCount = 0;
                parentNode.add(catNode);
                List<Pair<DataGroupInfo, DataTypeInfo>> typeList = catToTypeMap.get(cat);

                if (typeList != null && !typeList.isEmpty())
                {

                    for (Pair<DataGroupInfo, DataTypeInfo> pair : typeList)
                    {

                        DefaultMutableTreeNode dgiNode = new DefaultMutableTreeNode(
                                userObjGen.createNodeUserObject(pair.getFirstObject(), pair.getSecondObject()));
                        catNode.add(dgiNode);
                        subNodeCount++;

                    }

                }
                catNodeUserObj.setLabel(catNodeUserObj.getLabel());
                catNodeUserObj.setCategoryCount(subNodeCount);
            }
        }
    }

    /**
     * Creates the fully structured tree.
     *
     * @param dataCategoryFilter the data category filter
     * @param groupFilter the group filter ( if null "all" are selected).
     * @param userObjGen the {@link NodeUserObjectGenerator}
     * @param dataGroups the {@link Collection} of {@link DataGroupInfo} to be
     *            considered for inclusion.
     * @param parentNode the parent node
     */
    private static void createFullTree(final Predicate<DataGroupInfo> dataCategoryFilter,
            final Predicate<DataGroupInfo> groupFilter, NodeUserObjectGenerator userObjGen, Collection<DataGroupInfo> dataGroups,
            DefaultMutableTreeNode parentNode)
    {
        if (CollectionUtilities.hasContent(dataGroups))
        {
            boolean preserveChildOrder = false;
            if (parentNode.getUserObject() instanceof GroupByNodeUserObject)
            {
                GroupByNodeUserObject userObject = (GroupByNodeUserObject)parentNode.getUserObject();
                preserveChildOrder = userObject.getDataGroupInfo().isPreserveChildOrder();
            }

            if (preserveChildOrder)
            {
                for (DataGroupInfo group : dataGroups)
                {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(userObjGen.createNodeUserObject(group));
                    parentNode.add(node);
                    createFullTree(dataCategoryFilter, groupFilter, userObjGen, group.getChildren(), node);
                }
            }
            else
            {
                // Add the groups with children that have descendants that pass
                // the group filter
                List<DataGroupInfo> folderGroups = StreamUtilities.filter(dataGroups,
                        group -> group.hasChildren() && anyGroupPassesFilter(groupFilter, group));
                Collections.sort(folderGroups, DefaultDataGroupInfo.CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR);

                for (DataGroupInfo group : folderGroups)
                {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(userObjGen.createNodeUserObject(group));
                    parentNode.add(node);
                    createFullTree(dataCategoryFilter, groupFilter, userObjGen, group.getChildren(), node);
                }

                // Add the groups with members that pass the data category and
                // group filters
                List<DataGroupInfo> memberGroups = StreamUtilities.filter(dataGroups,
                        group -> group.hasMembers(false) && (dataCategoryFilter == null || dataCategoryFilter.test(group))
                                && (groupFilter == null || groupFilter.test(group)));
                Collections.sort(memberGroups, DefaultDataGroupInfo.CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR);

                for (DataGroupInfo group : memberGroups)
                {
                    parentNode.add(new DefaultMutableTreeNode(userObjGen.createNodeUserObject(group)));
                }
            }
        }
    }

    /**
     * Groups the data into a map of categories to lists of data group info.
     * Sorts the resultant list by the provided comparator or by name if no
     * comparator is provided. Groups are selected from the data group
     * controller by the provided filter and categorized ( multiple categories
     * are allowed per group) by the GroupCatagorizer.
     *
     * @param dataCategoryFilter the data category filter
     * @param groupFilter the group filter ( if null "all" are selected).
     * @param groupComparator the group comparator a {@link Comparator} that
     *            helps sort the resultant lists. (if null sorted in natural
     *            name order).
     * @param categorizer the categorizer a {@link GroupCategorizer} that
     *            provides the categories for each entry.
     * @param collection the {@link Collection} of {@link DataGroupInfo} to use
     *            to create the group by.
     * @return the result {@link Map} of category to {@link List} of
     *         {@link DataGroupInfo}
     */
    private static Map<String, List<DataGroupInfo>> createGroupBy(Predicate<DataGroupInfo> dataCategoryFilter,
            Predicate<DataGroupInfo> groupFilter, Comparator<? super DataGroupInfo> groupComparator, GroupCategorizer categorizer,
            Collection<DataGroupInfo> collection)
    {
        Utilities.checkNull(collection, "collection");
        Utilities.checkNull(categorizer, "categorizer");
        Map<String, List<DataGroupInfo>> result = New.map();

        List<DataGroupInfo> dgiList = New.linkedList();

        for (DataGroupInfo dgi : collection)
        {
            addToGroupIfPassesFilter(dataCategoryFilter, groupFilter, dgiList, dgi);
        }

        Set<String> categories = null;
        for (DataGroupInfo dgi : dgiList)
        {
            categories = categorizer.getGroupCategories(dgi);
            if (categories != null && !categories.isEmpty())
            {
                for (String category : categories)
                {
                    List<DataGroupInfo> list = result.get(category);
                    if (list == null)
                    {
                        list = New.list();
                        result.put(category, list);
                    }
                    list.add(dgi);
                }
            }
        }

        // Sort each list by the provided comparator, or by display name if no
        // comparator is provided.
        for (Map.Entry<String, List<DataGroupInfo>> entry : result.entrySet())
        {
            if (groupComparator == null)
            {
                Collections.sort(entry.getValue(), DataGroupInfo.CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR);
            }
            else
            {
                Collections.sort(entry.getValue(), groupComparator);
            }
        }

        return result;
    }

    /**
     * Groups the data into a map of categories to lists of data type info.
     * Sorts the resultant list by the provided comparator or by name if no
     * comparator is provided. Groups are selected from the data group
     * controller by the provided filter and categorized ( multiple categories
     * are allowed per group) by the GroupCatagorizer.
     *
     * @param dataCategoryFilter the filter used to narrow the set of data
     *            categories.
     * @param groupFilter the group filter ( if null "all" are selected).
     * @param typeComparator the group comparator a {@link Comparator} that
     *            helps sort the resultant lists. (if null sorted in natural
     *            name order).
     * @param categorizer the categorizer a {@link GroupCategorizer} that
     *            provides the categories for each entry.
     * @param collection the {@link Collection} of {@link DataGroupInfo} to use
     *            to create the group by.
     * @return the result {@link Map} of category to {@link List} of
     *         {@link DataGroupInfo}
     */
    private static Map<String, List<Pair<DataGroupInfo, DataTypeInfo>>> createGroupByDataType(
            Predicate<DataGroupInfo> dataCategoryFilter, Predicate<DataGroupInfo> groupFilter,
            Comparator<? super DataTypeInfo> typeComparator, GroupCategorizer categorizer, Collection<DataGroupInfo> collection)
    {
        Utilities.checkNull(collection, "collection");
        Utilities.checkNull(categorizer, "categorizer");
        Map<String, List<Pair<DataGroupInfo, DataTypeInfo>>> result = New.map();

        List<DataGroupInfo> dgiList = New.linkedList();

        for (DataGroupInfo dgi : getSortedDGIList(collection))
        {
            addToGroupIfPassesFilter(dataCategoryFilter, groupFilter, dgiList, dgi);
        }

        Set<String> categories = null;
        for (DataGroupInfo dgi : dgiList)
        {
            for (DataTypeInfo dti : dgi.getMembers(true))
            {
                categories = categorizer.getTypeCategories(dti);
                if (categories != null && !categories.isEmpty())
                {
                    for (String category : categories)
                    {
                        List<Pair<DataGroupInfo, DataTypeInfo>> list = result.get(category);
                        if (list == null)
                        {
                            list = New.list();
                            result.put(category, list);
                        }
                        list.add(new Pair<>(dgi, dti));
                    }
                }
            }
        }

        // Sort each list by the provided comparator, or by display name if no
        // comparator is provided taking feature type into account.
        for (Map.Entry<String, List<Pair<DataGroupInfo, DataTypeInfo>>> entry : result.entrySet())
        {
            if (typeComparator == null)
            {
                Collections.sort(entry.getValue(), new Comparator<Pair<DataGroupInfo, DataTypeInfo>>()
                {
                    private final Comparator<DataTypeInfo> myDtiComp = DataTypeInfo.CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR;

                    @Override
                    public int compare(Pair<DataGroupInfo, DataTypeInfo> o1, Pair<DataGroupInfo, DataTypeInfo> o2)
                    {
                        MapVisualizationType mvt1 = getMapVisType(o1.getSecondObject());
                        MapVisualizationType mvt2 = getMapVisType(o2.getSecondObject());
                        int comparedByType = Integer.compare(mvt1.ordinal(), mvt2.ordinal());
                        return comparedByType == 0 ? myDtiComp.compare(o1.getSecondObject(), o2.getSecondObject())
                                : comparedByType;
                    }
                });
            }
            else
            {
                final Comparator<? super DataTypeInfo> dtiComp = typeComparator;
                Collections.sort(entry.getValue(), (o1, o2) -> dtiComp.compare(o1.getSecondObject(), o2.getSecondObject()));
            }
        }

        return result;
    }

    /**
     * Gets the map vis type.
     *
     * @param dti the dti
     * @return the map vis type
     */
    private static MapVisualizationType getMapVisType(DataTypeInfo dti)
    {
        MapVisualizationType type = MapVisualizationType.UNKNOWN;
        if (dti != null && dti.getMapVisualizationInfo() != null)
        {
            type = dti.getMapVisualizationInfo().getVisualizationType();
        }
        return type;
    }

    /**
     * Gets the sorted dgi list.
     *
     * @param dgiCollection the dgi collection
     * @return the sorted dgi list
     */
    private static List<DataGroupInfo> getSortedDGIList(Collection<DataGroupInfo> dgiCollection)
    {
        List<DataGroupInfo> dgiList = null;
        if (CollectionUtilities.hasContent(dgiCollection))
        {
            dgiList = New.list(dgiCollection);
            try
            {
                Collections.sort(dgiList, ourDGIComparator);
            }
            catch (IllegalArgumentException e)
            {
                LOGGER.warn(e);
                Collections.sort(dgiList, DataGroupInfo.CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR);
            }
        }
        return dgiList == null ? Collections.<DataGroupInfo>emptyList() : dgiList;
    }

    /**
     * Gets the sorted member list.
     *
     * @param dtiCollection the dti collection
     * @return the sorted member list
     */
    private static List<DataTypeInfo> getSortedMemberList(Collection<DataTypeInfo> dtiCollection)
    {
        List<DataTypeInfo> dtiList = null;
        if (CollectionUtilities.hasContent(dtiCollection))
        {
            dtiList = New.list(dtiCollection);
            Collections.sort(dtiList, ourDTINameAndTypeComparator);
        }
        return dtiList == null ? Collections.<DataTypeInfo>emptyList() : dtiList;
    }

    /**
     * Instantiates a new data group info group by utility.
     */
    private DataGroupInfoGroupByUtility()
    {
    }

    /**
     * The Class DefaultNodeUserObjectGenerator.
     */
    public static class DefaultNodeUserObjectGenerator implements NodeUserObjectGenerator
    {
        @Override
        public GroupByNodeUserObject createNodeUserObject(DataGroupInfo dgi)
        {
            return new GroupByNodeUserObject(dgi);
        }

        @Override
        public GroupByNodeUserObject createNodeUserObject(DataGroupInfo dgi, DataTypeInfo dti)
        {
            return new GroupByNodeUserObject(dgi, dti);
        }

        @Override
        public GroupByNodeUserObject createNodeUserObject(String label)
        {
            return new GroupByNodeUserObject(label);
        }
    }

    /**
     * The Class TreeOptions.
     */
    public static class TreeOptions
    {
        /** The my build with types instead of groups. */
        private final boolean myBuildWithTypesInsteadOfGroups;

        /** The sub nodes for multi member groups. */
        private final boolean mySubNodesForMultiMemberGroups;

        /** Whether to flatten the tree. */
        private final boolean myFlattenTree;

        /**
         * Instantiates a new tree options.
         *
         * @param subNodesForMultiMemberGroups the sub nodes for multi member
         *            groups
         */
        public TreeOptions(boolean subNodesForMultiMemberGroups)
        {
            this(subNodesForMultiMemberGroups, false);
        }

        /**
         * Instantiates a new tree options.
         *
         * @param subNodesForMultiMemberGroups the sub nodes for multi member
         *            groups
         * @param buildWithTypesInsteadOfGroups the build with types instead of
         *            groups
         */
        public TreeOptions(boolean subNodesForMultiMemberGroups, boolean buildWithTypesInsteadOfGroups)
        {
            this(subNodesForMultiMemberGroups, buildWithTypesInsteadOfGroups, true);
        }

        /**
         * Instantiates a new tree options.
         *
         * @param subNodesForMultiMemberGroups the sub nodes for multi member
         *            groups
         * @param buildWithTypesInsteadOfGroups the build with types instead of
         *            groups
         * @param flattenTree whether to flatten the tree
         */
        public TreeOptions(boolean subNodesForMultiMemberGroups, boolean buildWithTypesInsteadOfGroups, boolean flattenTree)
        {
            mySubNodesForMultiMemberGroups = subNodesForMultiMemberGroups;
            myBuildWithTypesInsteadOfGroups = buildWithTypesInsteadOfGroups;
            myFlattenTree = flattenTree;
        }

        /**
         * Checks if is builds the with types instead of groups.
         *
         * @return true, if is builds the with types instead of groups
         */
        public boolean isBuildWithTypesInsteadOfGroups()
        {
            return myBuildWithTypesInsteadOfGroups;
        }

        /**
         * Returns whether to flatten the tree.
         *
         * @return whether to flatten the tree
         */
        public boolean isFlattenTree()
        {
            return myFlattenTree;
        }

        /**
         * Checks if is sub nodes for multi member groups.
         *
         * @return true, if is sub nodes for multi member groups
         */
        public boolean isSubNodesForMultiMemberGroups()
        {
            return mySubNodesForMultiMemberGroups;
        }
    }

    /**
     * The Class DTIDisplayNameAndFeatureTypeComparator. This comparator handles
     * some different cases to try and keep the feature types ordered above tile
     * types for most views that are used. It does regular case insensitive
     * display name comparisons for most cases, however if the two items being
     * compared are both single member types without children we compare them
     * using their underlying data types, which takes feature type into account
     * then displyaname.
     */
    private static class DGIComparator implements Comparator<DataGroupInfo>
    {
        /** The Dti comp. */
        private final Comparator<DataGroupInfo> myDGIComp = DataGroupInfo.CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR;

        /** The DTI comp. */
        private final DTIDisplayNameAndFeatureTypeComparator myDTIComp = new DTIDisplayNameAndFeatureTypeComparator();

        @Override
        public int compare(DataGroupInfo o1, DataGroupInfo o2)
        {
            boolean o1IsSingleTypeDGI = !o1.hasChildren() && o1.numMembers(false) == 1;
            boolean o2IsSingleTypeDGI = !o2.hasChildren() && o2.numMembers(false) == 1;
            if (o1IsSingleTypeDGI && o2IsSingleTypeDGI)
            {
                return myDTIComp.compare(o1.getMembers(false).iterator().next(), o2.getMembers(false).iterator().next());
            }
            else if (o1IsSingleTypeDGI && !o2IsSingleTypeDGI)
            {
                return 1;
            }
            else if (!o1IsSingleTypeDGI && o2IsSingleTypeDGI)
            {
                return -1;
            }
            else
            {
                return myDGIComp.compare(o1, o2);
            }
        }
    }

    /**
     * The Class DTIDisplayNameAndFeatureTypeComparator.
     */
    private static class DTIDisplayNameAndFeatureTypeComparator implements Comparator<DataTypeInfo>
    {
        /** The Dti comp. */
        private final Comparator<DataTypeInfo> myDtiComp = DataTypeInfo.CASE_INSENSITIVE_DISPLAY_NAME_COMPARATOR;

        @Override
        public int compare(DataTypeInfo o1, DataTypeInfo o2)
        {
            MapVisualizationType mvt1 = getMapVisType(o1);
            MapVisualizationType mvt2 = getMapVisType(o2);

            int comparedByType = Integer.compare(mvt1.ordinal(), mvt2.ordinal());
            return comparedByType == 0 ? myDtiComp.compare(o1, o2) : comparedByType;
        }
    }
}
