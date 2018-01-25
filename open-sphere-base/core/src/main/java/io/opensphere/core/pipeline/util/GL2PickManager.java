package io.opensphere.core.pipeline.util;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES1;

import io.opensphere.core.control.ControlRegistry;
import io.opensphere.core.geometry.Geometry;

/**
 * Keeps track of pick colors and the geometries using them. This is designed to
 * be used in a single thread.
 */
public class GL2PickManager extends PickManager
{
    /**
     * Constructor.
     *
     * @param controlRegistry The control registry.
     */
    public GL2PickManager(ControlRegistry controlRegistry)
    {
        super(controlRegistry);
    }

    @Override
    public int glColor(GL gl, Geometry key)
    {
        int pickColor = getPickColor(key);
        byte red = (byte)(pickColor >> RED_SHIFT);
        byte green = (byte)(pickColor >> GREEN_SHIFT);
        byte blue = (byte)(pickColor >> BLUE_SHIFT);
        byte alpha = (byte)-1;
        gl.getGL2().glColor4ub(red, green, blue, alpha);
        return pickColor;
    }

    @Override
    public void glTexEnvColor(GL gl, Geometry key)
    {
        int pickColor = getPickColor(key);
        float[] colorArr = new float[4];
        colorArr[0] = (float)(pickColor >> RED_SHIFT & 0xff) / 0xff;
        colorArr[1] = (float)(pickColor >> GREEN_SHIFT & 0xff) / 0xff;
        colorArr[2] = (float)(pickColor >> BLUE_SHIFT & 0xff) / 0xff;
        colorArr[3] = 1f;
        gl.getGL2().glTexEnvfv(GL2ES1.GL_TEXTURE_ENV, GL2ES1.GL_TEXTURE_ENV_COLOR, colorArr, 0);
    }
}
