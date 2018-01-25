package io.opensphere.core.geometry.renderproperties;

import java.io.Serializable;
import java.util.Collection;

/**
 * Properties which affect how a geometry is rendered. The purpose of this class
 * is to separate the fundamental geometry from the information which is
 * processing or rendering specific. In particular, properties which are likely
 * to be the same for sets of geometries and can be shared by those geometries
 * may be good candidates for render properties. Mutable members of the render
 * properties should change the appearance of the geometry without requiring the
 * creation of a new geometry. For immutable fields, a new geometry should be
 * created when the property needs to be changed. Processors interested in
 * changes to these properties should register for notification.
 */
public interface RenderProperties extends Cloneable, Serializable
{
    /**
     * Add a listener for changes to my properties.
     *
     * @param listen The listener to add.
     */
    void addListener(RenderPropertyChangeListener listen);

    /**
     * Clone this set of render properties.
     *
     * @return The cloned render properties.
     */
    RenderProperties clone();

    /**
     * Get a collection that comprises this properties object plus any
     * descendants.
     *
     * @return The properties.
     */
    Collection<? extends RenderProperties> getThisPlusDescendants();

    /**
     * Remove a listener for changes to my properties..
     *
     * @param listen The listener to remove.
     */
    void removeListener(RenderPropertyChangeListener listen);
}
