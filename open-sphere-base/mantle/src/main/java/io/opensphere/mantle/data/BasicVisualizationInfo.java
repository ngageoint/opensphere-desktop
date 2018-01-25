package io.opensphere.mantle.data;

import java.awt.Color;
import java.util.Collection;
import java.util.Set;

import io.opensphere.core.util.ObservableValue;
import io.opensphere.mantle.data.element.DataElement;

/**
 * Basic Visualization Info for a data type.
 */
public interface BasicVisualizationInfo
{
    /**
     * Gets the default type color.
     *
     * @return the default {@link Color}
     */
    Color getDefaultTypeColor();

    /**
     * Gets the loads to designation for this type.
     *
     * @return the loads to
     */
    LoadsTo getLoadsTo();

    /**
     * Gets the supported loads to types.
     *
     * @return the supported loads to types
     */
    Set<LoadsTo> getSupportedLoadsToTypes();

    /**
     * Gets the type color.
     *
     * @return the type {@link Color}
     */
    Color getTypeColor();

    /**
     * Gets the type opacity.
     *
     * Note: This retrieves the opacity channel of the type color, it is not an
     * independent of the color.
     *
     * Range 0-255.
     *
     * @return the type opacity ( 0 - 255 )
     */
    int getTypeOpacity();

    /**
     * Restores the current type color to the default type color.
     *
     * @param source - the calling object
     */
    void restoreDefaultColor(Object source);

    /**
     * Sets the loads to designation for this type.
     *
     * @param l the new loads to
     * @param source the source of the change.
     */
    void setLoadsTo(LoadsTo l, Object source);

    /**
     * Sets the supported loads to types.
     *
     * @param types the new supported loads to types
     */
    void setSupportedLoadsToTypes(Collection<LoadsTo> types);

    /**
     * Sets the color for this type of data.
     *
     * @param c - the color to set to
     * @param source - the calling object
     */
    void setTypeColor(Color c, Object source);

    /**
     * Sets the type opacity. Range 0 - 255.
     *
     * Note that this sets the alpha channel of the type color, it is not an
     * independent parameter.
     *
     * @param alpha the new type opacity
     * @param source - the calling object
     */
    void setTypeOpacity(int alpha, Object source);

    /**
     * Sets whether the DataTypeInfo utilizes {@link DataElement}s.
     *
     * @param usesDataElements whether the DataTypeInfo utilizes
     *            {@link DataElement}s.
     */
    void setUsesDataElements(boolean usesDataElements);

    /**
     * Returns whether the loads to is supported.
     *
     * @param loadsTo the loads to
     * @return true, if successful
     */
    boolean supportsLoadsTo(LoadsTo loadsTo);

    /**
     * True if this DataTypeInfo utilizes {@link DataElement}s.
     *
     * @return true if uses, false if not
     */
    boolean usesDataElements();

    /**
     * Gets the loads to property.
     *
     * @return the loads to property
     */
    ObservableValue<LoadsTo> loadsToProperty();

    /**
     * Gets the type color property.
     *
     * @return the type color property
     */
    ObservableValue<Color> typeColorProperty();
}
