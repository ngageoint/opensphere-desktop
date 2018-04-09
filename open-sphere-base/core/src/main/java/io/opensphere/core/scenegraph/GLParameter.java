package io.opensphere.core.scenegraph;

import com.jogamp.opengl.GL;

/**
 * A {@link SceneGraphParameter} that can be used to group objects that need the
 * same GL command(s) executed.
 */
public abstract class GLParameter implements SceneGraphParameter
{
    /**
     * Method to execute the GL commands associated with this parameter.
     *
     * @param gl the OpenGL interface
     */
    public abstract void execute(GL gl);

    @Override
    public Object getType()
    {
        return getClass();
    }
}
