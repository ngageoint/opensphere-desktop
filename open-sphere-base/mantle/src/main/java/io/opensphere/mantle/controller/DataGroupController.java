package io.opensphere.mantle.controller;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.tree.TreeNode;

import io.opensphere.core.Toolbox;
import io.opensphere.core.util.collections.New;
import io.opensphere.mantle.data.ActiveGroupEntry;
import io.opensphere.mantle.data.DataGroupInfo;
import io.opensphere.mantle.data.DataGroupInfoActiveHistoryRecord;
import io.opensphere.mantle.data.DataGroupInfoActiveSet;
import io.opensphere.mantle.data.DataTypeInfo;

/**
 * The Interface DataGroupController.
 *
 * Note that "Active" data groups are groups that are participating in the
 * current timeline or are otherwise setup to "display" to the user either on
 * the map or through the tools. Non-active groups may still show up in some
 * displays but their data would not be "visible".
 *
 * When a DataGroup is "activated" the underlying DataTypeInfo for that node (
 * not children nodes ) will be set to "inUse=true"
 */
public interface DataGroupController
{
    /**
     * Adds a listener to be notified whenever groups are activated or
     * deactivated.
     *
     * @param activationListener The listener.
     */
    void addActivationListener(Runnable activationListener);

    /**
     * Adds the data group info as a top level group.
     *
     * @param dgi the dgi
     * @param source the source of the add
     * @throws IllegalArgumentException if the DataGroupInfo.isRootNode() does
     *             not return true.
     * @return true if added, false if not ( or already in controller )
     */
    boolean addRootDataGroupInfo(DataGroupInfo dgi, Object source);

    /**
     * Clean up group entries that are held with the controller as the data
     * group is being removed for all time.
     *
     * @param dgi the DataGroupInfo to clean up.
     */
    void cleanUpGroup(DataGroupInfo dgi);

    /**
     * Creates a list that represents this all groups and their children. Child
     * nodes will be sorted using the comparator or in natural order if no
     * comparator is provided ( null ), nodes can be filtered using the supplied
     * filter. Child nodes that do not pass the filter criteria will not be
     * added to the tree ( including their children ). The filter will not apply
     * to the node on which this function is called only on children.
     *
     * @param comparator the comparator to use to order the child nodes (
     *            recursive )
     * @param nodeFilter a filter to filter down the children nodes ( and their
     *            children )
     * @return the tree node the TreeNode.
     */
    List<DataGroupInfo> createGroupList(Comparator<DataGroupInfo> comparator, Predicate<? super DataGroupInfo> nodeFilter);

    /**
     * Creates a TreeNode that represents this all groups and their children.
     * Children are sorted in "natural" order. No filter is performed.
     *
     * @return the {@link TreeNode}
     */
    TreeNode createTreeNode();

    /**
     * Creates a TreeNode that represents this all groups and their children.
     * Child nodes will be sorted using the comparator or in natural order if no
     * comparator is provided ( null ), nodes can be filtered using the supplied
     * filter. Child nodes that do not pass the filter criteria will not be
     * added to the tree ( including their children ). The filter will not apply
     * to the node on which this function is called only on children.
     *
     * @param comparator the comparator to use to order the child nodes (
     *            recursive )
     * @param nodeFilter a filter to filter down the children nodes ( and their
     *            children )
     * @return the tree node the TreeNode.
     */
    TreeNode createTreeNode(Comparator<? super DataGroupInfo> comparator, Predicate<? super DataGroupInfo> nodeFilter);

    /**
     * Searches recursively through all the DataGroupInfo that are part of the
     * active set and returns any that pass the provided filter.
     *
     * @param dgiFilter the DataGruopInfo filter for the search
     * @param stopOnFirstFound if true stops the search after the the first
     *            filter match
     * @return the found result set of DataGroupInfo that pass the filter.
     */
    Set<DataGroupInfo> findActiveDataGroupInfo(Predicate<? super DataGroupInfo> dgiFilter, boolean stopOnFirstFound);

    /**
     * Searches through all the DataGroupInfo that are part of the regular set
     * and adds any that pass the provided filter to the provided collection.
     *
     * @param dgiFilter the DataGruopInfo filter for the search
     * @param results return collection of DataGroupInfo that pass the filter.
     * @param stopOnFirstFound if true stops the search after the the first
     *            filter match
     * @return the results (for convenience)
     */
    Collection<DataGroupInfo> findDataGroupInfo(Predicate<? super DataGroupInfo> dgiFilter, Collection<DataGroupInfo> results,
            boolean stopOnFirstFound);

    /**
     * Searches through all the {@link DataTypeInfo} that are part of the
     * regular set and returns the first member that has an id that matches the
     * provided id or null if not found.
     *
     * @param dtiId the member id to search for
     * @return the found member {@link DataTypeInfo} or null if not found.
     */
    DataTypeInfo findMemberById(String dtiId);

    /**
     * Searches through all the DataGroupInfo that are part of the regular set
     * and returns any members of those groups that pass the filter.
     *
     * @param dtiFilter the {@link DataTypeInfo} filter for the search
     * @param stopOnFirstFound if true stops the search after the the first
     *            filter match
     * @return the found result set of DataTypeInfo that pass the filter.
     */
    Set<DataTypeInfo> findMembers(Predicate<? super DataTypeInfo> dtiFilter, boolean stopOnFirstFound);

    /**
     * Finds the members in active groups that match the filter.
     *
     * @param dtiFilter the {@link DataTypeInfo} filter for the search
     * @return the matching active members
     */
    default Collection<DataTypeInfo> findActiveMembers(Predicate<? super DataTypeInfo> dtiFilter)
    {
        return findDataGroupInfo(g -> g.activationProperty().isActive(), New.list(), false).stream()
                .flatMap(g -> g.findMembers(dtiFilter, false, true).stream()).collect(Collectors.toList());
    }

    /**
     * Gets the current active groups.
     *
     * @return the active groups
     */
    List<DataGroupInfo> getActiveGroups();

    /**
     * Gets the active history list.
     *
     * @return the active history list
     */
    List<DataGroupInfoActiveHistoryRecord> getActiveHistoryList();

    /**
     * Gets all the members of active DataGroupInfo(recursing children
     * optional).
     *
     * @param recurseChildren the recurse children
     * @return the active members
     */
    Set<DataTypeInfo> getActiveMembers(boolean recurseChildren);

    /**
     * Gets the active set by name.
     *
     * @param setName the set name
     * @return the active set
     */
    DataGroupInfoActiveSet getActiveSet(String setName);

    /**
     * Gets the list of active sets.
     *
     * @return the active set names
     */
    List<String> getActiveSetNames();

    /**
     * Gets the data group info by its key. Searches the hierarchy of all root
     * nodes for a node that has the specified key.
     *
     * @param key the key to search for
     * @return the data group info or null if not found
     */
    DataGroupInfo getDataGroupInfo(String key);

    /**
     * Gets the complete set of data group info ( top level nodes ).
     *
     * @return the data group info ( or empty set if there are none )
     */
    Set<DataGroupInfo> getDataGroupInfoSet();

    /**
     * Get the list of {@link DataGroupInfo}s that contain a
     * {@link DataTypeInfo}, ordered top to bottom.
     *
     * @param dti The {@link DataTypeInfo} key.
     * @return The list of {@link DataGroupInfo}s.
     */
    List<? extends DataGroupInfo> getDataGroupInfosWithDti(DataTypeInfo dti);

    /**
     * Get the type keys for the currently queryable data types.
     *
     * @return The type keys.
     */
    Collection<? extends String> getQueryableDataTypeKeys();

    /**
     * Gets the toolbox.
     *
     * @return the toolbox
     */
    Toolbox getToolbox();

    /**
     * Gets the top parent of a data group info.
     *
     * @param dgi the dgi
     * @return the top parent or itself it is is the root node or top parent.
     */
    DataGroupInfo getTopParent(DataGroupInfo dgi);

    /**
     * Gets the top parent of the node with the specified key ( which if it is a
     * root node could be itself) or null if there was no {@link DataGroupInfo}
     * associated with that key in this tree.
     *
     * @param dgiKey the dgi key
     * @return the top parent
     */
    DataGroupInfo getTopParent(String dgiKey);

    /**
     * Gets the user deactivated group IDs.
     *
     * @return the user deactivated group IDs
     */
    Set<String> getUserDeactivatedGroupIds();

    /**
     * Checks for data group info. Searches the hierarchy of all root nodes, if
     * this dgi occurs in the tree of any root node or is itself a root node it
     * will return true.
     *
     * @param dgi the dgi
     * @return true, if is in the hierarchy
     */
    boolean hasDataGroupInfo(DataGroupInfo dgi);

    /**
     * Searches the hierarchy of all root nodes, if this dgi occurs in the tree
     * of any root node or is itself a root node, this will return true.
     *
     *
     * @param key the key
     * @return true, if successful
     */
    boolean hasDataGroupInfo(String key);

    /**
     * Checks if a {@link DataTypeInfo} is part of an active group.
     *
     * @param dti the {@link DataTypeInfo} to check
     * @return true, if is part of an active group.
     */
    boolean isTypeActive(DataTypeInfo dti);

    /**
     * Load active set by name.
     *
     * @param setName the set name to load.
     * @param exclusive true to exclusively load the set, false to add to the
     *            existing set those that are not already active.
     * @return true, if successful, false if no set by that name.
     */
    boolean loadActiveSet(String setName, boolean exclusive);

    /**
     * Removes the listener.
     *
     * @param activationListener The listener.
     */
    void removeActivationListener(Runnable activationListener);

    /**
     * Removes the active set by name.
     *
     * @param setName the set name
     * @return true, if successful
     */
    boolean removeActiveSet(String setName);

    /**
     * Removes the data group info. ( root or child anywhere in a tree residing
     * in this controller)
     *
     * @param dgi the dgi
     * @param source the source of the remove
     * @return true if found and removed, false if not
     */
    boolean removeDataGroupInfo(DataGroupInfo dgi, Object source);

    /**
     * Removes the data group info. ( root or child anywhere in a tree residing
     * in this controller)
     *
     * @param key the key
     * @param source the source of the remove
     * @return true if found and removed, false if not
     */
    boolean removeDataGroupInfo(String key, Object source);

    /**
     * Restores the default active set.
     *
     * @param exclusive true to exclusively load the set, false to add to the
     *            existing set those that are not already active.
     * @return true, if successful, false if not.
     */
    boolean restoreDefaultActiveSet(boolean exclusive);

    /**
     * Save active set. Overwrites any existing set by that name.
     *
     * @param setName the set name
     */
    void saveActiveSet(String setName);

    /**
     * Save active set with specific ids.
     *
     * Note: Will overwrite any existing set by this name.
     *
     * @param name the name of the set to save.
     * @param groups the groups
     */
    void saveActiveSet(String name, Collection<? extends ActiveGroupEntry> groups);
}
