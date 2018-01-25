package io.opensphere.mantle.mp;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.tree.TreeNode;

import io.opensphere.core.Toolbox;

/**
 * The Interface MapAnnotationPointRegistry.
 */
public interface MapAnnotationPointRegistry
{
    /**
     * Adds the data group info as a top level group.
     *
     * @param group the {@link MutableMapAnnotationPointGroup} to add as a root
     *            group.
     * @param source the source of the add
     * @throws IllegalArgumentException if the
     *             MapAnnotationPointGroup.isRootNode() does not return true.
     * @return true if added, false if not ( or already in controller )
     */
    boolean addRootGroup(MutableMapAnnotationPointGroup group, Object source);

    /**
     * Clear registry.
     *
     * @param source the source
     */
    void clearRegistry(Object source);

    /**
     * Create a copy of the groups and points in this registry, filtering the
     * groups and points using the provided filters.
     *
     * @param pointFilter the point filter ( if null all points will pass )
     * @param groupFilter the group filter ( if null all points will pass )
     * @return the sets the
     */
    Set<MutableMapAnnotationPointGroup> createFilteredCopy(Predicate<? super MutableMapAnnotationPoint> pointFilter,
            Predicate<? super MutableMapAnnotationPointGroup> groupFilter);

    /**
     * Creates a list that represents this all point groups and their children.
     * Child nodes will be sorted using the comparator or in natural order if no
     * comparator is provided ( null ), nodes can be filtered using the supplied
     * filter. Child nodes that do not pass the filter criteria will not be
     * added to the tree ( including their children nodes ). The filter will not
     * apply to the node on which this function is called only on children.
     *
     * @param comparator the comparator to use to order the child nodes (
     *            recursive )
     * @param nodeFilter a filter to filter down the children nodes ( and their
     *            children )
     * @return the tree node the TreeNode.
     */
    List<MutableMapAnnotationPointGroup> createGroupList(Comparator<MutableMapAnnotationPointGroup> comparator,
            Predicate<? super MutableMapAnnotationPointGroup> nodeFilter);

    /**
     * Creates a TreeNode that represents this all map point groups and their
     * children. Children are sorted in "natural" order. No filter is performed.
     *
     * @return the {@link TreeNode}
     */
    TreeNode createTreeNode();

    /**
     * Creates a TreeNode that represents this all map point groups and their
     * children. Child nodes will be sorted using the comparator or in natural
     * order if no comparator is provided ( null ), nodes can be filtered using
     * the supplied filter. Child nodes that do not pass the filter criteria
     * will not be added to the tree ( including their children nodes ). The
     * filter will not apply to the node on which this function is called only
     * on children.
     *
     * @param comparator the comparator to use to order the child nodes (
     *            recursive )
     * @param nodeFilter a filter to filter down the children nodes ( and their
     *            children )
     * @return the tree node the TreeNode.
     */
    TreeNode createTreeNode(Comparator<? super MutableMapAnnotationPointGroup> comparator,
            Predicate<? super MutableMapAnnotationPointGroup> nodeFilter);

    /**
     * Creates a TreeNode that represents this all map point groups and their
     * children. Children are sorted in "preferred" order using the values
     * returned by getPreferredOrder.
     *
     * @return the {@link TreeNode}
     */
    TreeNode createTreeNodeWithChildrenInPrefferedOrder();

    /**
     * Creates a TreeNode that represents this all groups and their children.
     * Children are sorted in "preferred" order using the values returned by
     * getPreferredOrder. Children are filtered using the provided node filter.
     *
     * @param nodeFilter - the filter for children ( and their children ) or no
     *            filter if null.
     * @return the {@link TreeNode}
     */
    TreeNode createTreeNodeWithChildrenInPrefferedOrder(Predicate<? super MutableMapAnnotationPointGroup> nodeFilter);

    /**
     * Searches through all the MapAnnotationPointGroup that are part of the
     * regular set and adds any that pass the provided filter to the provided
     * collection.
     *
     * @param filter the MapAnnotationPointGroup filter for the search
     * @param results return collection of MapAnnotationPointGroup that pass the
     *            filter.
     * @param stopOnFirstFound if true stops the search after the the first
     *            filter match
     */
    void findGroup(Predicate<MapAnnotationPointGroup> filter, Collection<MapAnnotationPointGroup> results,
            boolean stopOnFirstFound);

    /**
     * Searches the {@link MutableMapAnnotationPointGroup} for the first
     * MapAnnotationPointGroup that has the provided
     * {@link MutableMapAnnotationPoint} as a member. Returns the
     * {@link MutableMapAnnotationPointGroup} found or null if none found.
     *
     * @param point the {@link MutableMapAnnotationPoint}
     * @return the data group info
     */
    MapAnnotationPointGroup findGroupWithPoint(MapAnnotationPoint point);

    /**
     * Searches through all the MapAnnotationPointGroup that are part of the
     * regular set and returns any points of those groups that pass the filter.
     *
     * @param filter the {@link MutableMapAnnotationPoint} filter for the search
     * @param stopOnFirstFound if true stops the search after the the first
     *            filter match
     * @return the found result set of MapAnnotationPoint that pass the filter.
     */
    Set<MutableMapAnnotationPoint> findPoints(Predicate<? super MutableMapAnnotationPoint> filter, boolean stopOnFirstFound);

    /**
     * Gets the complete set of groups ( top level nodes ).
     *
     * @return the groups ( or empty set if there are none )
     */
    Set<MutableMapAnnotationPointGroup> getGroupSet();

    /**
     * Get the list of {@link MutableMapAnnotationPointGroup}s that contain a.
     *
     * @param point the point
     * @return The list of {@link MutableMapAnnotationPointGroup}s.
     *         {@link MutableMapAnnotationPoint}, ordered top to bottom.
     */
    List<? extends MapAnnotationPointGroup> getGroupsWithPoint(MutableMapAnnotationPoint point);

    /**
     * Gets the top parent of a MapAnnotationPointGroup.
     *
     * @param group the group
     * @return the top parent or itself it is is the root node or top parent.
     */
    MutableMapAnnotationPointGroup getTopParent(MutableMapAnnotationPointGroup group);

    /**
     * Gets the user default point.
     *
     * @return the {@link MapAnnotationPoint} user default point.
     */
    MapAnnotationPoint getUserDefaultPoint();

    /**
     * Checks for group. Searches the hierarchy of all root nodes, if this group
     * occurs in the tree of any root node or is itself a root node it will
     * return true.
     *
     * @param group the group
     * @return true, if is in the hierarchy
     */
    boolean hasGroup(MutableMapAnnotationPointGroup group);

    /**
     * Loads a {@link MapAnnotationPointGroup} from a file.
     *
     * @param tb the {@link Toolbox}
     * @param aFile the a file
     * @return the map annotation point group or null.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    MapAnnotationPointGroup loadFromXMLFile(Toolbox tb, File aFile) throws IOException;

    /**
     * Removes the MapAnnotationPointGroup. ( root or child anywhere in a tree
     * residing in this controller)
     *
     * @param group the group to remove
     * @param source the source of the remove
     * @return true if found and removed, false if not
     */
    boolean removeGroup(MutableMapAnnotationPointGroup group, Object source);

    /**
     * Saves a MapAnnotationPointGroup to an XML file.
     *
     * @param aFile the a file to save to.
     * @param group the group to save.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void saveToXMLFile(File aFile, MapAnnotationPointGroup group) throws IOException;

    /**
     * Sets the user default point.
     *
     * @param userDefaultPoint the {@link MapAnnotationPoint} to set as a the
     *            user default point.
     * @param source the source of the add
     */
    void setUserDefaultPoint(MapAnnotationPoint userDefaultPoint, Object source);

    /** The actions that will be performed on the file. */
    enum FileAction
    {
        /** Read action. */
        READ,

        /** Write action. */
        WRITE
    }

    /** The file types that we use. */
    enum FileType
    {
        /** The csv file type. */
        CSV,

        /** The xml file type. */
        XML
    }
}
