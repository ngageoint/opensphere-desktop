package io.opensphere.controlpanels.recording.gif;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import io.opensphere.core.AnimationChangeAdapter;
import io.opensphere.core.AnimationManager;
import io.opensphere.core.FrameBufferCaptureManager.FrameBufferCaptureListener;
import io.opensphere.core.FrameBufferCaptureManager.FrameBufferCaptureProvider;
import io.opensphere.core.animation.AnimationPlan;
import io.opensphere.core.animation.AnimationPlanModificationException;
import io.opensphere.core.animation.AnimationState;
import io.opensphere.core.common.util.AnimatedGifEncoder;
import io.opensphere.core.common.util.AnimatedGifEncoder.QUANTIZATION;
import io.opensphere.core.control.ui.InternalComponentRegistry;
import io.opensphere.core.hud.awt.HUDFrame;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.PhasedChangeArbitrator;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.swing.EventQueueUtilities;

/** Record the animation of a timeline to an animated GIF. */
public class GIFRecorder
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(GIFRecorder.class);

    /** Animation listener used during recording. */
    private RecorderAnimationListener myAnimationListener;

    /** The system wide animation manager. */
    private final AnimationManager myAnimationManager;

    /** Listener for captured images. */
    private GIFCaptureListener myCanvasCaptureListener;

    /** The system provider for captured frames from main canvas. */
    private final FrameBufferCaptureProvider myCaptureProvider;

    /** The registry for HUD frames. */
    private final InternalComponentRegistry myFrameRegistry;

    /**
     * When true, the do not attempt to add frames to the writer because it is
     * being closed.
     */
    private boolean myIsWriterClosed;

    /** The options selected by the user for this animation. */
    private final AnimationOptions myOptions;

    /**
     * Arbitrator which requires phased commits for animation plan changes while
     * recording.
     */
    private final PhasedChangeArbitrator myPhasedChangeArbitrator = new PhasedChangeArbitrator()
    {
        @Override
        public boolean isPhasedCommitRequired()
        {
            return !myIsWriterClosed;
        }
    };

    /**
     * The phaser used to notify the animation manager when pre-commit is
     * completed.
     */
    private final AtomicReference<Phaser> myPreCommitPhaser = new AtomicReference<>();

    /** When recording is complete the latch will be counted down. */
    private CountDownLatch myRecordingCompleteLatch;

    /** Writer for actually generating the animated GIF. */
    private final AnimatedGifEncoder myWriter;

    /**
     * Constructor.
     *
     * @param options The options selected by the user for this animation.
     * @param animationManager The system wide animation manager.
     * @param frameRegistry The registry for HUD frames.
     * @param captureProvider The system provider for captured frames from main
     *            canvas.
     */
    public GIFRecorder(AnimationOptions options, AnimationManager animationManager, InternalComponentRegistry frameRegistry,
            FrameBufferCaptureProvider captureProvider)
    {
        myOptions = options;
        myAnimationManager = animationManager;
        myFrameRegistry = frameRegistry;
        myCaptureProvider = captureProvider;
        myWriter = new AnimatedGifEncoder(myOptions.getFile(), myOptions.getFrameIntervalMS(), myOptions.getWidth(),
                myOptions.getHeight());

        // When recording animated GIFs color accuracy is more important than
        // performance, so use the maximum sampling.
        myWriter.setQuality(1);
        // Set the dispose to 1 so the GIF file can be read correctly by Power
        // Point 2013. Vortex-3307.
        myWriter.setDispose(1);
    }

    /**
     * Get the options.
     *
     * @return the options
     */
    public AnimationOptions getOptions()
    {
        return myOptions;
    }

    /**
     * Record one pass through the animation plan.
     *
     * @param recordingLatch When recording is complete the latch will be
     *            counted down.
     */
    // TODO This records one pass from the first frame forward to the last
    // frame. It seems like it would be easy to allow backwards animation.
    public void recordSinglePass(CountDownLatch recordingLatch)
    {
        myRecordingCompleteLatch = recordingLatch;
        EventQueueUtilities.runOnEDTAndWait(this::hideHUDWindows);
        myIsWriterClosed = false;
        myCanvasCaptureListener = new GIFCaptureListener();

        AnimationPlan plan = myAnimationManager.getCurrentPlan();
        final List<? extends TimeSpan> spans = plan.getAnimationSequence();
        if (spans.isEmpty())
        {
            myRecordingCompleteLatch.countDown();
            return;
        }

        AnimationState initialState = plan.getInitialState();
        AnimationState currentState = myAnimationManager.getAnimationState();
        if (!initialState.equals(currentState))
        {
            try
            {
                myAnimationManager.stepFirst(plan, false);
            }
            catch (AnimationPlanModificationException e)
            {
                LOGGER.error("Failed to change to first animation frame." + e, e);
            }
        }

        myAnimationManager.addPhasedChangeArbitrator(myPhasedChangeArbitrator);
        // Create the animation changed listener
        myAnimationListener = new RecorderAnimationListener();
        myAnimationManager.addAnimationChangeListener(myAnimationListener);
    }

    /**
     * Remove any windows the user has selected to not display. This should be
     * called just before recording the animation.
     */
    private void hideHUDWindows()
    {
        List<HUDFrame> frames = myFrameRegistry.getObjects();
        for (HUDFrame frame : frames)
        {
            if (myOptions.getHiddenComponents().contains(frame.getTitle()))
            {
                frame.setVisible(false);
            }
        }

        /* Because some window managers allow fade-out or other window close
         * animations, allow some time for those to complete. */
        ThreadUtilities.sleep(100);
    }

    /** Restore any windows which we have hidden. */
    private void restoreHUDWindows()
    {
        List<HUDFrame> frames = myFrameRegistry.getObjects();
        for (HUDFrame frame : frames)
        {
            if (myOptions.getHiddenComponents().contains(frame.getTitle()))
            {
                frame.setVisible(true);
            }
        }
    }

    /** Listener for handling captured buffer frames. */
    private class GIFCaptureListener implements FrameBufferCaptureListener
    {
        /**
         * The pixel height of the first frame captured. All future frames must
         * be the same size.
         */
        private int myFirstFrameHeight = -1;

        /**
         * The pixel width of the first frame captured. All future frames must
         * be the same size.
         */
        private int myFirstFrameWidth = -1;

        @Override
        public void handleFrameBufferCaptured(int width, int height, byte[] screenCapture)
        {
            if (myFirstFrameWidth == -1)
            {
                myFirstFrameWidth = width;
            }
            if (myFirstFrameHeight == -1)
            {
                myFirstFrameHeight = height;
            }

            if (width != myFirstFrameWidth || height != myFirstFrameHeight)
            {
                LOGGER.error("Animated GIF frame skipped because canvas was resized.");
            }
            else if (!myIsWriterClosed)
            {
                // The y direction for the GL canvas is the opposite of Swing,
                // so we must flip the image.
                byte[] flip = new byte[screenCapture.length];
                int lineByteWidth = width * 3;
                for (int i = 0; i < height; ++i)
                {
                    System.arraycopy(screenCapture, (height - i - 1) * lineByteWidth, flip, i * lineByteWidth, lineByteWidth);
                }

                BufferedImage screen = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                WritableRaster wr = screen.getRaster();
                wr.setDataElements(0, 0, width, height, flip);

                myWriter.addFrame(screen, QUANTIZATION.OCTAL_TREE);
            }

            Utilities.ifNotNull(myPreCommitPhaser.getAndSet(null), p -> p.arriveAndDeregister());
        }
    }

    /** Animation listener used during recording. */
    private class RecorderAnimationListener extends AnimationChangeAdapter
    {
        /**
         * Indicates if the animation has actually started.
         */
        private boolean myHasAnimationStarted;

        @Override
        public void commit(AnimationState state, Phaser phaser)
        {
            /* When the state being committed is the first frame, we are done
             * recording. This means we are about to step to the first frame, so
             * the last frame will still have been recorded during the
             * pre-commit phase.
             *
             * VORTEX-3833 checking to see if state equals initial state isn't
             * good enough because the animation manager will sometimes commit
             * the initial state first thing. So add an additional check to see
             * if we have actually started the animation. */
            if (myHasAnimationStarted && myAnimationManager.getCurrentPlan().getInitialState().equals(state))
            {
                myHasAnimationStarted = false;
                myAnimationManager.removePhasedChangeArbitrator(myPhasedChangeArbitrator);
                myAnimationManager.removeAnimationChangeListener(myAnimationListener);
                myAnimationListener = null;
                myIsWriterClosed = true;
                myWriter.finish();
                myCanvasCaptureListener = null;
                myRecordingCompleteLatch.countDown();

                EventQueueUtilities.runOnEDT(GIFRecorder.this::restoreHUDWindows);
            }
            else
            {
                myHasAnimationStarted = true;
            }
        }

        @Override
        public boolean preCommit(AnimationState changeInfo, Phaser phaser)
        {
            phaser.register();
            if (!myPreCommitPhaser.compareAndSet(null, phaser))
            {
                LOGGER.warn("Request for next GIF frame occurred before last one completed.");
            }
            myCaptureProvider.captureSingleFrame(myCanvasCaptureListener);
            return true;
        }
    }
}
