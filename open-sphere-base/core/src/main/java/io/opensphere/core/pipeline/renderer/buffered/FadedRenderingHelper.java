package io.opensphere.core.pipeline.renderer.buffered;

import com.jogamp.opengl.util.texture.TextureCoords;

import io.opensphere.core.TimeManager;
import io.opensphere.core.TimeManager.ActiveTimeSpanChangeListener;
import io.opensphere.core.TimeManager.Fade;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.model.time.TimeSpanList;
import io.opensphere.core.pipeline.util.RenderContext;
import io.opensphere.core.pipeline.util.ShaderRendererUtilities.TileShader;
import io.opensphere.core.util.Utilities;

/**
 * Helper for fading geometries based on their relationship to the current time
 * frame.
 */
public class FadedRenderingHelper
{
    /** The active time span change listener. */
    private final ActiveTimeSpanChangeListener myActiveTimeSpanChangeListener = active -> setFade(
            active.getFade() == null || active.getDirection() >= 0 ? active.getFade() : active.getFade().reverse());

    /** The current fade setting. */
    private volatile Fade myFade;

    /** The group time span. */
    private TimeSpan myGroupTimeSpan;

    /** The time manager. */
    private volatile TimeManager myTimeManager;

    /** The current time span. */
    private TimeSpan myTimeSpan;

    /**
     * Initialize the shader used for interval filtering. This is intended to be
     * called from the {@link Runnable} passed to
     * {@link #renderEachTimeSpan(RenderContext, TimeSpan, Runnable)}. It uses
     * the time spans set by that method.
     *
     * @param rc The render context.
     * @param pickEffect If the pick effect should be enabled in the shader,
     *            i.e., if the glColor rgb should be used rather than the
     *            texture rgb.
     * @param textureCoords The texture coordinates, if a texture is enabled.
     */
    public void initIntervalFilter(RenderContext rc, boolean pickEffect, TextureCoords textureCoords)
    {
        if (myTimeSpan != null && myGroupTimeSpan != null)
        {
            rc.getShaderRendererUtilities().initIntervalFilter(rc.getGL(), myTimeSpan, myGroupTimeSpan, getFade(), pickEffect,
                    textureCoords != null);
        }
        else if (textureCoords != null && pickEffect)
        {
            rc.getShaderRendererUtilities().enableShaderByName(rc.getGL(), TileShader.PICK, null, textureCoords);
        }
    }

    /**
     * Execute the given runnable for each time span, initializing the interval
     * filter shader before each call.
     *
     * @param rc The render context.
     * @param groupTimeSpan The group time span.
     * @param r The rendering runnable.
     */
    public void renderEachTimeSpan(RenderContext rc, TimeSpan groupTimeSpan, Runnable r)
    {
        myGroupTimeSpan = groupTimeSpan;
        TimeManager timeManager = getTimeManager();
        if (groupTimeSpan == null || timeManager == null)
        {
            myTimeSpan = null;
            r.run();
        }
        else
        {
            TimeSpanList primaryActiveTimeSpans = timeManager.getPrimaryActiveTimeSpans();
            for (TimeSpan span : primaryActiveTimeSpans)
            {
                myTimeSpan = span;
                r.run();
            }
        }
    }

    /**
     * Set the time manager.
     *
     * @param timeManager The time manager, or {@code null}.
     */
    public void setTimeManager(TimeManager timeManager)
    {
        TimeManager oldTimeManager = myTimeManager;
        if (!Utilities.sameInstance(timeManager, oldTimeManager))
        {
            myTimeManager = timeManager;
            if (oldTimeManager != null)
            {
                oldTimeManager.removeActiveTimeSpanChangeListener(myActiveTimeSpanChangeListener);
            }
            if (timeManager != null)
            {
                timeManager.addActiveTimeSpanChangeListener(myActiveTimeSpanChangeListener);
            }
        }
    }

    /**
     * Accessor for the fade.
     *
     * @return The fade.
     */
    protected Fade getFade()
    {
        return myFade;
    }

    /**
     * Get the time manager.
     *
     * @return The time manager.
     */
    protected TimeManager getTimeManager()
    {
        return myTimeManager;
    }

    /**
     * Mutator for the fade.
     *
     * @param fade The fade to set.
     */
    protected void setFade(Fade fade)
    {
        myFade = fade;
    }
}
