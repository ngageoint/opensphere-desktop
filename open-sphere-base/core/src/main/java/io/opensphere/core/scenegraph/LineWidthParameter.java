package io.opensphere.core.scenegraph;

import com.jogamp.opengl.GL;

/**
 * This is a {@link SceneGraph} parameter that indicates a GL line width.
 */
public class LineWidthParameter extends GLParameter
{
    /** The width to use when drawing the line. */
    private final float myWidth;

    /**
     * Construct the parameter.
     *
     * @param width the line width
     */
    public LineWidthParameter(float width)
    {
        myWidth = width;
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
        LineWidthParameter other = (LineWidthParameter)obj;
        return Float.floatToIntBits(myWidth) == Float.floatToIntBits(other.myWidth);
    }

    @Override
    public void execute(GL gl)
    {
        gl.glLineWidth(myWidth);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(myWidth);
        return result;
    }
}
