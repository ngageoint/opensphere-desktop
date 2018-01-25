package io.opensphere.core.scenegraph;

/**
 * A parameter for use in a {@link SceneGraph}. Parameters are used to determine
 * similarities between objects. Objects with all the same parameters will be
 * grouped in the same leaf node of the scene graph.
 */
@FunctionalInterface
public interface SceneGraphParameter
{
    /**
     * Get the type of this parameter. Parameters of the same type are kept at
     * the same level in the scene graph.
     *
     * @return the type
     */
    Object getType();
}
