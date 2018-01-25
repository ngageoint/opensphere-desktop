package io.opensphere.mantle.mp;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Predicate;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * The Interface MapAnnotationPointGroup.
 */
public interface MutableMapAnnotationPointGroup extends MapAnnotationPointGroup
{
    /**
     * Adds the child data group info.
     *
     * @param group MapAnnotationPointGroup.
     * @param source the source of the change
     * @throws IllegalStateException if the MapAnnotationPointGroup being added
     *             would become its own parent ancestor.
     */
    void addChild(MutableMapAnnotationPointGroup group, Object source);

    /**
     * Adds the point.
     *
     * @param pt the {@link MutableMapAnnotationPoint} to add.
     * @param source the source
     */
    void addPoint(MutableMapAnnotationPoint pt, Object source);

    /**
     * Clears all points and all sub-group nodes. (recursive)
     *
     * @param source the source of the change
     */
    void clearAll(Object source);

    /**
     * Clears all children group nodes. ( recursively clears )
     *
     * @param source the source of the change
     */
    void clearChildren(Object source);

    /**
     * Clear points from the group.
     *
     * @param recursive the recursive
     * @param source the source
     */
    void clearPoints(boolean recursive, Object source);

    /**
     * Creates a copy of this group and its contents but filters members and
     * children using the provided filters.
     *
     * @param pointFilter the point filter
     * @param groupFilter the group filter
     * @return the copy of the group
     */
    MutableMapAnnotationPointGroup createFilteredCopy(Predicate<? super MutableMapAnnotationPoint> pointFilter,
            Predicate<? super MutableMapAnnotationPointGroup> groupFilter);

    /**
     * Creates the group set.
     *
     * @param nodeFilter the node filter
     * @return the sets the
     */
    Set<MutableMapAnnotationPointGroup> createGroupSet(Predicate<? super MutableMapAnnotationPointGroup> nodeFilter);

    /**
     * Creates a TreeNode that represents this
     * {@link MutableMapAnnotationPointGroup} and its children. Children are
     * sorted in "natural" order. No filter is performed.
     *
     * @return the {@link TreeNode}
     */
    MutableTreeNode createTreeNode();

    /**
     * Creates a tree node that represents this
     * {@link MutableMapAnnotationPointGroup} and its children. child nodes will
     * be sorted using the comparator or in natural order if no comparator is
     * provided ( null ), nodes can be filtered using the supplied filter. Child
     * nodes that do not pass the filter criteria will not be added to the tree
     * ( including their children ). The filter will not apply to the node on
     * which this function is called only on children.
     *
     * @param comparator the comparator to use to order the child nodes (
     *            recursive )
     * @param nodeFilter a filter to filter down the children nodes ( and their
     *            children )
     * @return the tree node the TreeNode.
     */
    MutableTreeNode createTreeNode(Comparator<? super MutableMapAnnotationPointGroup> comparator,
            Predicate<? super MutableMapAnnotationPointGroup> nodeFilter);

    /**
     * Creates a TreeNode that represents this
     * {@link MutableMapAnnotationPointGroup} and its children. Children are
     * sorted in "preferred" order using the values returned by
     * getPreferredOrder.
     *
     * @return the {@link TreeNode}
     */
    MutableTreeNode createTreeNodeWithChildrenInPrefferedOrder();

    /**
     * Creates a TreeNode that represents this MapAnnotationPointGroup and its
     * children. Children are sorted in "preferred" order using the values
     * returned by getPreferredOrder. Children are filtered using the provided
     * node filter.
     *
     * @param nodeFilter - the filter for children ( and their children ) or no
     *            filter if null.
     * @return the {@link TreeNode}
     */
    MutableTreeNode createTreeNodeWithChildrenInPrefferedOrder(Predicate<? super MutableMapAnnotationPointGroup> nodeFilter);

    /**
     * Searches for points that matches the filter, optionally recursively
     * searches the children to see if any child members match the filter.
     *
     * @param filter the {@link MutableMapAnnotationPoint} filter to use to
     *            select members.
     * @param recursive if true recurses into children to check
     * @param stopOnFirstFound if true stops search on the first point to match
     *            the filter criteria, if false exhaustively searches member
     *            set.
     *
     * @return the set of points that matched the filter criteria or an empty
     *         set if none found.
     */
    Set<MutableMapAnnotationPoint> findPoints(Predicate<? super MutableMapAnnotationPoint> filter, boolean recursive,
            boolean stopOnFirstFound);

    /**
     * Fires off a {@link AbstractMapAnnotationPointGroupChangeEvent} to any
     * listeners.
     *
     * @param e - the event to fire.
     */
    void fireGroupInfoChangeEvent(AbstractMapAnnotationPointGroupChangeEvent e);

    /**
     * Gets the parent.
     *
     * @return the parent
     */
    MutableMapAnnotationPointGroup getParent();

    /**
     * Gets the top parent.
     *
     * @return the top parent
     */
    MutableMapAnnotationPointGroup getTopParent();

    /**
     * Checks if the MapAnnotationPointGroup specified a parent ancestor working
     * up the tree.
     *
     * @param group the group to check.
     * @return true, if is above in hierarchy
     */
    boolean isParentAncestor(MutableMapAnnotationPointGroup group);

    /**
     * Checks if is root node.
     *
     * @return true, if is root node
     */
    boolean isRootNode();

    /**
     * Removes the child group.
     *
     * @param group the group to remove
     * @param source the source of the change
     * @return true if removed, false if not.
     */
    boolean removeChild(MutableMapAnnotationPointGroup group, Object source);

    /**
     * Removes the point.
     *
     * @param pt the {@link MutableMapAnnotationPoint} to add.
     * @param recursive the recursive
     * @param source the source
     * @return true, if successful
     */
    boolean removePoint(MutableMapAnnotationPoint pt, boolean recursive, Object source);

    /**
     * Sets the name.
     *
     * @param name the new name
     * @param source the source of the change.
     */
    void setName(String name, Object source);

    /**
     * Sets the parent.
     *
     * @param parent the new parent
     */
    void setParent(MutableMapAnnotationPointGroup parent);

    /**
     * Sets the preferred order to be used by a comparator to sort into a
     * "default" non-natural order. Where low is first, and high is last.
     *
     * @param order - the order value
     */
    void setPreferredOrder(int order);
}
