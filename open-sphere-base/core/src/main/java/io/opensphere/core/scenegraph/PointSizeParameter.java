package io.opensphere.core.scenegraph;

import javax.media.opengl.GL;

/**
 * This is a {@link SceneGraph} parameter that indicates a GL point size.
 */
public class PointSizeParameter extends GLParameter
{
    /** The point size in pixels. */
    private final float mySize;

    /**
     * Construct the parameter.
     *
     * @param size the point size
     */
    public PointSizeParameter(float size)
    {
        mySize = size;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        PointSizeParameter other = (PointSizeParameter)obj;
        return Float.floatToIntBits(mySize) == Float.floatToIntBits(other.mySize);
    }

    @Override
    public void execute(GL gl)
    {
        gl.getGL2().glPointSize(mySize);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(mySize);
        return result;
    }
}
