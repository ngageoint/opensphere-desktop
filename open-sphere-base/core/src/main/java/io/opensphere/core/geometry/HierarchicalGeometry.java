package io.opensphere.core.geometry;

import java.util.Collection;

/**
 * Interface for geometries that can have parents and children.
 *
 * @param <T> The type of this geometry's children.
 */
public interface HierarchicalGeometry<T extends Geometry> extends Geometry
{
    /**
     * Get the immediate children of this geometry.
     *
     * @param allowDivide Indicates if the geometry should divide if possible.
     *
     * @return the children of this geometry or an empty collection if there are
     *         none
     */
    Collection<? extends T> getChildren(boolean allowDivide);

    /**
     * Get the descendants of this geometry. This includes all geometries with
     * this geometry as an ancestor.
     *
     * @param result The set used to store the descendants.
     */
    void getDescendants(Collection<? super T> result);

    /**
     * Gets the division hold generation.
     *
     * @return the division hold generation
     */
    int getDivisionHoldGeneration();

    /**
     * Get the generation of this geometry. The 0th generation has no parent,
     * the 1st generation has a parent of the 0th generation, etc.
     *
     * @return The generation of this geometry.
     */
    int getGeneration();

    /**
     * Get the parent of this tile, or <code>null</code>.
     *
     * @return the parent or <code>null</code>
     */
    HierarchicalGeometry<T> getParent();

    /**
     * Get the top-level ancestor of this geometry, which may be this geometry
     * itself if this geometry has no parent.
     *
     * @return The top-level ancestor.
     */
    HierarchicalGeometry<T> getTopAncestor();

    /**
     * Get if this geometry has children currently.
     *
     * @return {@code true} if the geometry has children.
     */
    boolean hasChildren();

    /**
     * Determine whether this geometry is a descendant of the given geometry.
     *
     * @param geometry The geometry from which this geometry might be descended.
     * @return true when this geometry is a descendant of the given geometry.
     */
    boolean isDescendant(T geometry);

    /**
     * Determine if this geometry is divisible.
     *
     * @return <code>true</code> if divisible
     */
    boolean isDivisible();

    /**
     * Checks if is division is paused for this geometry.
     *
     * @return true, if is division paused
     */
    boolean isDivisionOverride();
}
