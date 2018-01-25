package io.opensphere.core.pipeline.util;

import javax.media.opengl.GLAutoDrawable;

import org.apache.log4j.Logger;

import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * Manager for {@link Animator}s.
 */
public class AnimatorManager
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AnimatorManager.class);

    /** The animator for the GL Canvas. This may be <code>null</code>. */
    private AnimatorBase myAnimator;

    /** The drawable being animated. */
    private GLAutoDrawable myDrawable;

    /**
     * The target FPS for the animator.
     * <ul>
     * <li>{@code -1} indicates that the canvas should be rendered as many times
     * as possible.</li>
     * <li>{@code 0} indicates that the canvas should only be rendered when
     * required.</li>
     * <li>Any other number indicates a target number of frames per second.</li>
     * </ul>
     */
    private volatile int myFramesPerSecond;

    /**
     * Force the drawable to display.
     */
    public synchronized void displayNow()
    {
        if (myDrawable != null)
        {
            myDrawable.display();
        }
    }

    /**
     * Get the frames per second.
     *
     * @return The frames per second.
     */
    public int getFramesPerSecond()
    {
        return myFramesPerSecond;
    }

    /**
     * If there's an animator and it has a drawable, remove the drawable.
     */
    public synchronized void reset()
    {
        if (myDrawable != null && myAnimator != null)
        {
            myAnimator.remove(myDrawable);
        }
    }

    /**
     * Set the drawable in the animator if there is one active.
     *
     * @param drawable The drawable.
     */
    public synchronized void setDrawable(GLAutoDrawable drawable)
    {
        if (myAnimator != null)
        {
            myAnimator.add(drawable);
        }
        myDrawable = drawable;
    }

    /**
     * Set the target frame rate for the animator.
     *
     * <ul>
     * <li><tt>-1</tt> indicates that the canvas should be rendered as many
     * times as possible.</li>
     * <li><tt>0</tt> indicates that the canvas should only be rendered when
     * required.</li>
     * <li>Any other number indicates a target number of frames per second.</li>
     * </ul>
     *
     * @param framesPerSecond The frames per second.
     */
    public synchronized void setFrameRate(int framesPerSecond)
    {
        if (framesPerSecond == getFramesPerSecond())
        {
            return;
        }

        myFramesPerSecond = framesPerSecond;

        if (myAnimator != null)
        {
            myAnimator.remove(myDrawable);
            myAnimator.stop();
            myAnimator = null;
        }
        if (framesPerSecond != 0)
        {
            if (framesPerSecond > 0)
            {
                myAnimator = new FPSAnimator(framesPerSecond);
            }
            else
            {
                myAnimator = new Animator();
            }
            if (myDrawable != null)
            {
                myAnimator.add(myDrawable);
            }

            // Force the animator into its own thread group.
            Thread thread = new Thread(new ThreadGroup("Animator-group"), "Animator Starter")
            {
                @Override
                public void run()
                {
                    myAnimator.start();

                    ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                    Thread[] list = new Thread[threadGroup.activeCount()];
                    threadGroup.enumerate(list);
                    list[list.length - 1].setName("Animator");
                }
            };
            thread.start();
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                LOGGER.error("Failed to join animator thread: " + e, e);
            }
        }
    }
}
