package io.opensphere.core.scenegraph;

import java.awt.Color;

import javax.media.opengl.GL;

/**
 * This is a {@link SceneGraph} parameter that indicates a color.
 */
public class ColorParameter extends GLParameter
{
    /** The color in RGBA. */
    private final int myColor;

    /**
     * Construct the parameter.
     *
     * @param color the color
     */
    public ColorParameter(Color color)
    {
        myColor = color.getRGB();
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
        return myColor == ((ColorParameter)obj).myColor;
    }

    @Override
    public void execute(GL gl)
    {
        final int redshift = 16;
        final int greenshift = 8;
        final int alphashift = 24;
        final int mask = 0xFF;
        gl.getGL2().glColor4ub((byte)(myColor >> redshift & mask), (byte)(myColor >> greenshift & mask), (byte)(myColor & mask),
                (byte)(myColor >> alphashift & mask));
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + myColor;
        return result;
    }
}
