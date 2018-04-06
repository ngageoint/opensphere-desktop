package io.opensphere.core.pipeline.util;

import java.awt.Color;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;

import io.opensphere.core.geometry.AbstractGeometry;
import io.opensphere.core.geometry.AbstractGeometry.RenderMode;
import io.opensphere.core.geometry.ColorGeometry;
import io.opensphere.core.geometry.renderproperties.ColorRenderProperties;
import io.opensphere.core.geometry.renderproperties.StippleModelConfig;
import io.opensphere.core.math.Matrix4d;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.UnexpectedEnumException;
import io.opensphere.core.viewer.Viewer;

/**
 * OpenGL drawing utilities.
 */
public final class GL2Utilities
{
    /**
     * Set the current GL color using immediate mode.
     *
     * @param gl The OpenGL interface.
     * @param color The new color, as RGBA bytes.
     */
    public static void glColor(GL2 gl, Color color)
    {
        gl.glColor4ub((byte)color.getRed(), (byte)color.getGreen(), (byte)color.getBlue(), (byte)color.getAlpha());
    }

    /**
     * Set the current GL color to a geometry's color (or the highlight color if
     * the geometry is picked) unless the color is the same as the last color.
     *
     * @param rc The render context.
     * @param pickManager Determines if the geometry is picked.
     * @param geometry The geometry.
     */
    public static void glColor(RenderContext rc, PickManager pickManager, ColorGeometry geometry)
    {
        glColor(rc, pickManager, geometry, geometry.getRenderProperties());
    }

    /**
     * Set the current GL color to a geometry's color (or the highlight color if
     * the geometry is picked) unless the color is the same as the last color.
     *
     * @param rc The render context.
     * @param pickManager Determines if the geometry is picked.
     * @param geometry The geometry.
     * @param colorProperties The color properties to use.
     */
    public static void glColor(RenderContext rc, PickManager pickManager, ColorGeometry geometry,
            ColorRenderProperties colorProperties)
    {
        if (rc.getRenderMode() == AbstractGeometry.RenderMode.DRAW)
        {
            int color;
            if (geometry.getRenderProperties().isPickable() && pickManager.getPickedGeometries().contains(geometry))
            {
                color = colorProperties.getHighlightColorARGB();
            }
            else
            {
                color = colorProperties.getColorARGB();
            }
            rc.glColorARGB(color);
        }
        else if (rc.getRenderMode() == AbstractGeometry.RenderMode.PICK)
        {
            pickManager.glColor(rc.getGL(), geometry);
        }
        else
        {
            throw new UnexpectedEnumException(rc.getRenderMode());
        }
    }

    /**
     * Set the current GL color using immediate mode.
     *
     * @param gl The OpenGL interface.
     * @param argbColor The new color, as an ARGB integer.
     */
    public static void glColorARGB(GL2 gl, int argbColor)
    {
        gl.glColor4ub((byte)(argbColor >> 16), (byte)(argbColor >> 8), (byte)argbColor, (byte)(argbColor >> 24));
    }

    /**
     * Set the line stipple if the new config is different from the last
     * stipple.
     *
     * @param gl The GL context.
     * @param mode The render mode.
     * @param stipple The new stipple model config, or {@code null} for no
     *            stipple.
     * @param lastStipple The last stipple model config, or {@code null} for no
     *            stipple.
     * @return The (possibly new) current stipple model config.
     */
    @SuppressWarnings("PMD.AvoidUsingShortType")
    public static StippleModelConfig glLineStipple(GL2 gl, RenderMode mode, StippleModelConfig stipple,
            StippleModelConfig lastStipple)
    {
        if (mode == RenderMode.DRAW && !Utilities.sameInstance(lastStipple, stipple))
        {
            if (stipple == null)
            {
                gl.glLineStipple(1, (short)0xFFFF);
                gl.glDisable(GL2.GL_LINE_STIPPLE);
            }
            else
            {
                if (lastStipple == null)
                {
                    gl.glEnable(GL2.GL_LINE_STIPPLE);
                }
                gl.glLineStipple(stipple.getFactor(), stipple.getPattern());
            }
            return stipple;
        }
        else
        {
            return lastStipple;
        }
    }

    /**
     * Call a runnable after adjusting the model-view matrix with the given
     * transform, if the current viewer supports it.
     *
     * @param rc The render context.
     * @param transform The transform to multiply with the current model-view
     *            matrix (multiplied from the right).
     * @param r The runnable.
     */
    public static void renderWithTransform(RenderContext rc, Matrix4d transform, Runnable r)
    {
        if (transform == null)
        {
            r.run();
        }
        else if (rc.getMapContext().getStandardViewer().supportsAdjustedModelView())
        {
            try
            {
                rc.getGL2().glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
                rc.getGL2().glPushMatrix();
                rc.getGL2().glLoadMatrixf(((Viewer)rc.getMapContext().getStandardViewer()).getAdjustedModelViewMatrix(transform),
                        0);
                r.run();
            }
            finally
            {
                rc.getGL2().glPopMatrix();
            }
        }
    }

    /** Disallow class instantiation. */
    private GL2Utilities()
    {
    }
}
