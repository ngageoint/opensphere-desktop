package io.opensphere.mantle.mp;

/**
 * The Interface GroupInfoTreeNodeData.
 */
public interface MapAnnotationPointGroupTreeNodeData
{
    /**
     * Gets the group.
     *
     * @return the group
     */
    MutableMapAnnotationPointGroup getGroup();

    /**
     * Gets the name of the group ( also returned by toString() ).
     *
     * @return the display name
     */
    String getName();

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    String toString();
}
