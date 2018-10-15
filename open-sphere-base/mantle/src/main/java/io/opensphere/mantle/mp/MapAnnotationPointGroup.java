package io.opensphere.mantle.mp;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * The Interface MapAnnotationPointGroup.
 */
public interface MapAnnotationPointGroup
{
    /**
     * Comparator that orders {@link MutableMapAnnotationPointGroup}s by their
     * display names.
     */
    Comparator<MapAnnotationPointGroup> NAME_COMPARATOR = (o1, o2) -> o1.getName().compareTo(o2.getName());

    /**
     * Gets the {@link Set} of {@link MapAnnotationPointGroup} that represent
     * the children of this node.
     *
     * @return the immutable list of children
     */
    List<MapAnnotationPointGroup> getChildren();

    /**
     * Gets the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the points for this group ( and optional all sub-groups ).
     *
     * @param recurseChildren - recurse into children nodes.
     * @return the data types
     */
    List<MapAnnotationPoint> getPoints(boolean recurseChildren);

    /**
     * Gets the value for the preferred order to be used by a comparator to sort
     * into a "default" non-natural order. Where low is first, and high is last.
     *
     * @return the order
     */
    int getPreferredOrder();

    /**
     * Checks for children.
     *
     * @return true, if successful
     */
    boolean hasChildren();

    /**
     * Checks for point.
     *
     * @param pt the {@link MapAnnotationPoint} to check for.
     * @param recursive the recursive
     * @return true, if successful
     */
    boolean hasPoint(MapAnnotationPoint pt, boolean recursive);

    /**
     * Checks for points.
     *
     * @return true, if group has points, false if not.
     */
    boolean hasPoints();
}
