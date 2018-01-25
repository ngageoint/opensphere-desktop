package io.opensphere.core.pipeline.renderer;

import javax.media.opengl.GL;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.TimeManager.Fade;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.pipeline.util.ShaderRendererUtilities;
import io.opensphere.core.util.MathUtil;

/**
 * Abstract base class for {@link ShaderRendererUtilities}.
 */
public abstract class AbstractShaderRendererUtilities implements ShaderRendererUtilities
{
    @Override
    public void initIntervalFilter(GL gl, TimeSpan activeSpan, TimeSpan groupTimeSpan, Fade fade, boolean pickEffect,
            boolean useTextures)
    {
        final TimeInstant start = activeSpan.isUnboundedStart() ? groupTimeSpan.getStartInstant() : activeSpan.getStartInstant();
        final TimeInstant end = activeSpan.isUnboundedEnd() ? groupTimeSpan.getEndInstant() : activeSpan.getEndInstant();
        float modulatedStart = MathUtil.getModulatedFloat(start.getEpochMillis(), groupTimeSpan.getStart(),
                groupTimeSpan.getEnd());
        float modulatedEnd = MathUtil.getModulatedFloat(end.getEpochMillis(), groupTimeSpan.getStart(), groupTimeSpan.getEnd());
        float modulatedFadeStart;
        float modulatedFadeEnd;
        if (fade == null)
        {
            modulatedFadeStart = modulatedStart;
            modulatedFadeEnd = modulatedEnd;
        }
        else
        {
            long fadeStart = start.minus(fade.getFadeOut()).getEpochMillis();
            modulatedFadeStart = MathUtil.getModulatedFloat(fadeStart, groupTimeSpan.getStart(), groupTimeSpan.getEnd());
            long fadeEnd = end.plus(fade.getFadeIn()).getEpochMillis();
            modulatedFadeEnd = MathUtil.getModulatedFloat(fadeEnd, groupTimeSpan.getStart(), groupTimeSpan.getEnd());
        }
        initIntervalFilter(gl, modulatedFadeStart, modulatedStart, modulatedEnd, modulatedFadeEnd, pickEffect, useTextures);
    }

    /**
     * Convert some texture coordinates to an array of floats.
     *
     * @param tc The coordinates.
     * @return The array.
     */
    protected float[] convertCoordsToFloatArray(TextureCoords tc)
    {
        float[] params = new float[4];
        if (tc == null)
        {
            params[0] = 0f;
            params[1] = 1f;
            params[2] = 0f;
            params[3] = 1f;
        }
        else
        {
            if (tc.left() < tc.right())
            {
                params[0] = tc.left();
                params[1] = tc.right();
            }
            else
            {
                params[1] = tc.left();
                params[0] = tc.right();
            }

            if (tc.bottom() < tc.top())
            {
                params[2] = tc.bottom();
                params[3] = tc.top();
            }
            else
            {
                params[3] = tc.bottom();
                params[2] = tc.top();
            }
        }
        return params;
    }

    /**
     * Initialize the shader used for interval filtering. Vertices will be
     * transparent at less than the fadeMin, have gradually more opacity between
     * fadeMin and min, be opaque between min and max, have gradually less
     * opacity between max and fadeMax, and be transparent at greater than
     * fadeMax.
     *
     * @param gl The OpenGL context.
     * @param fadeMin The minimum fade bound.
     * @param min The minimum bound.
     * @param max The maximum bound.
     * @param fadeMax The maximum fade bound.
     * @param pickEffect Indicates if the pick effect should be enabled in the
     *            shader, i.e., if the glColor rgb should be used rather than
     *            the texture rgb.
     * @param useTexture Indicates if texture sampling should be used for the
     *            fragment colors.
     */
    protected abstract void initIntervalFilter(GL gl, float fadeMin, float min, float max, float fadeMax, boolean pickEffect,
            boolean useTexture);
}
