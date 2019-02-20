package io.opensphere.core.pipeline;

import java.awt.Canvas;
import java.awt.Dimension;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;

import org.apache.log4j.Logger;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;

import io.opensphere.core.Toolbox;

/**
 * Helper for creating and managing the GL canvas.
 */
public class PipelineCanvasHelper
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(PipelineCanvasHelper.class);

    /**
     * The GL canvas. The implementing class for this can vary based on whether
     * native windows are used.
     */
    private final Canvas myCanvas;

    /**
     * The drawable element. The implementing class for this can vary based on
     * whether native windows are used.
     */
    private final GLAutoDrawable myDrawable;

    /** Helper for handling NEWT events. */
    private NewtEventHelper myNewtHelper;

    /**
     * Constructor.
     *
     * @param pipeline The pipeline.
     * @param toolbox The toolbox.
     * @param preferredSize The preferred size of the canvas.
     */
    public PipelineCanvasHelper(GLEventListener pipeline, Toolbox toolbox, Dimension preferredSize)
    {
        final boolean newt = Boolean.getBoolean("opensphere.pipeline.jogl.nativeWindows");

        final GLCapabilities caps = new GLCapabilities(GLProfile.getDefault());

        // TODO setSampleBuffers(true) fixes the black outlines around dots on
        // Linux, but causes picking problems on all platforms.
        //        caps.setSampleBuffers(true);
        //        caps.setNumSamples(Math.min(4, caps.getNumSamples()));

        if (newt)
        {
            myDrawable = GLWindow.create(caps);
            myCanvas = new NewtCanvasAWT((GLWindow)myDrawable);

            // TODO there used to be a deadlock condition related to this
            // setting, but it seems to have been fixed. More
            // testing/investigation should probably be done.
            myCanvas.setFocusable(false);
        }
        else
        {
            myCanvas = new PipelineGLCanvas(caps);
            myDrawable = (GLAutoDrawable)myCanvas;
        }

        LOGGER.info("Created " + myCanvas.getClass().getSimpleName());

        myCanvas.setPreferredSize(preferredSize);

        myDrawable.addGLEventListener(pipeline);

        if (newt)
        {
            myNewtHelper = new NewtEventHelper((GLWindow)myDrawable, toolbox, myCanvas);
        }
    }

    /** Perform any required cleanup before shutting down the pipeline. */
    public synchronized void close()
    {
        if (myNewtHelper != null)
        {
            myNewtHelper.close();
        }
    }

    /**
     * Get the canvas.
     *
     * @return the canvas
     */
    public Canvas getCanvas()
    {
        return myCanvas;
    }

    /**
     * Get the drawable.
     *
     * @return the drawable
     */
    public GLAutoDrawable getDrawable()
    {
        return myDrawable;
    }
}
