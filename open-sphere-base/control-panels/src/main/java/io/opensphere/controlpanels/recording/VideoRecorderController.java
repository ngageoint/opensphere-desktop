package io.opensphere.controlpanels.recording;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import io.opensphere.core.FrameBufferCaptureManager.FrameBufferCaptureListener;
import io.opensphere.core.Toolbox;
import io.opensphere.core.util.ChangeSupport;
import io.opensphere.core.util.StrongChangeSupport;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.util.swing.EventQueueUtilities;
import io.opensphere.core.util.taskactivity.TaskActivity;
import io.opensphere.core.video.VideoEncoder;
import io.opensphere.core.video.VideoEncoderException;

/**
 * Controls for starting and stopping video recording.
 */
public final class VideoRecorderController
{
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(VideoRecorderController.class);

    /** Listener for captured images. */
    private transient VideoCaptureListener myCanvasCaptureListener;

    /** The change support for recording complete listeners. */
    private final ChangeSupport<RecordingCompleteListener> myChangeSupport = new StrongChangeSupport<>();

    /** The encoder which produces the video file. */
    private transient VideoEncoder myEncoder;

    /** The Record activity. */
    private final transient TaskActivity myRecordActivity;

    /**
     * The height of the video. This may not match the canvas size if we are
     * resizing the frames, or if the frames do not have even height.
     */
    private int myRecordHeight;

    /**
     * The width of the video. This may not match the canvas size if we are
     * resizing the frames, or if the frames do not have even width.
     */
    private int myRecordWidth;

    /** The tool box. */
    private final transient Toolbox myToolbox;

    /** Lock access to the video writer. */
    private final Lock myWriterLock = new ReentrantLock();

    /**
     * Constructor.
     *
     * @param toolbox the toolbox
     */
    public VideoRecorderController(Toolbox toolbox)
    {
        myRecordActivity = new TaskActivity();
        myToolbox = toolbox;
        myToolbox.getUIRegistry().getMenuBarRegistry().addTaskActivity(myRecordActivity);
    }

    /**
     * Add a listener to be notified when recording is complete.
     *
     * @param listener The listener.
     */
    public void addListener(RecordingCompleteListener listener)
    {
        myChangeSupport.addListener(listener);
    }

    /**
     * Get the acceptable file extensions for video output.
     *
     * @return The list of file extensions.
     */
    public List<String> getAcceptableFileExtensions()
    {
        return Arrays.asList(".mp4", ".flv", ".wmv", ".mpeg", ".mov");
    }

    /**
     * Start recording.
     *
     * @param file The file to record to.
     * @return true, if successful
     */
    public boolean startRecording(File file)
    {
        boolean recordingStarted = false;
        recordingStarted = handleStartRecording(file);
        if (recordingStarted)
        {
            if (!myRecordActivity.isActive())
            {
                myRecordActivity.setActive(true);
            }
            myRecordActivity.setLabelValue("Recording... (Press video button to stop)");
        }
        return recordingStarted;
    }

    /**
     * Stop recording.
     */
    public void stopRecording()
    {
        myWriterLock.lock();
        try
        {
            if (myEncoder != null)
            {
                myRecordActivity.setLabelValue("Processing Recording");
                myToolbox.getFrameBufferCaptureManager().getCaptureProvider().cancelCapture(myCanvasCaptureListener);
                myCanvasCaptureListener = null;
                myEncoder.close();
            }
        }
        catch (RuntimeException e)
        {
            // Xuggler throws RuntimeException whenever anything goes wrong.
            // Since we can't know what really happened, just log the exception.
            LOGGER.error("Failed to close the video writer." + e, e);
        }
        finally
        {
            myWriterLock.unlock();
            myEncoder = null;
            myRecordActivity.setLabelValue("Record complete");
            myRecordActivity.setActive(false);

            myChangeSupport.notifyListeners(new ChangeSupport.Callback<VideoRecorderController.RecordingCompleteListener>()
            {
                @Override
                public void notify(RecordingCompleteListener listener)
                {
                    listener.recordingComplete();
                }
            });
        }
    }

    /**
     * Start recording video.
     *
     * @param file The file to record to.
     *
     * @return true if recording was started.
     */
    private boolean handleStartRecording(File file)
    {
        Utilities.checkNull(file, "file");
        myWriterLock.lock();
        try
        {
            // In order to encode the frames, the width and height must be
            // even.
            int vpw = myToolbox.getMapManager().getScreenViewer().getViewportWidth();
            myRecordWidth = vpw - vpw % 2;
            int vph = myToolbox.getMapManager().getScreenViewer().getViewportHeight();
            myRecordHeight = vph - vph % 2;

            myWriterLock.lock();
            try
            {
                myEncoder = null;
                VideoEncoderException error = null;
                Iterator<VideoEncoder> encodersIterator = ServiceLoader.load(VideoEncoder.class).iterator();
                if (encodersIterator.hasNext())
                {
                    VideoEncoder encoder = encodersIterator.next();
                    try
                    {
                        encoder.open(file, myRecordWidth, myRecordHeight);
                        myEncoder = encoder;
                    }
                    catch (VideoEncoderException e)
                    {
                        LOGGER.error("Error starting recording: " + e, e);
                        error = e;
                    }
                }

                if (error != null)
                {
                    JOptionPane.showMessageDialog(null, "Failed to create video: " + error.getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            finally
            {
                myWriterLock.unlock();
            }
            myCanvasCaptureListener = new VideoCaptureListener();
            myToolbox.getFrameBufferCaptureManager().getCaptureProvider().captureStream(myCanvasCaptureListener);
            return true;
        }
        catch (UnsatisfiedLinkError e)
        {
            LOGGER.error("Video recording not supported on this platform.");
        }
        finally
        {
            myWriterLock.unlock();
        }
        return false;
    }

    /** Interface for listeners interested in when recording is complete. */
    @FunctionalInterface
    public interface RecordingCompleteListener
    {
        /** Called when recording is complete. */
        void recordingComplete();
    }

    /** Listener for handling captured buffer frames. */
    private class VideoCaptureListener implements FrameBufferCaptureListener
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

        /**
         * When no frames are written for too long of a time period, Zuggler
         * will produce an error on the next frame write. This executor is used
         * to ensure that periodic writes occur.
         */
        private final Executor myRecaptureExecutor = CommonTimer.createProcrastinatingExecutor(750);

        @Override
        public void handleFrameBufferCaptured(int width, int height, byte[] screenCapture)
        {
            // The lock would prevent multiple writes at the same time, but what
            // we really want is to skip frames when we cannot write them fast
            // enough.
            if (myWriterLock.tryLock())
            {
                try
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
                        stopRecording();
                        LOGGER.error("Recording aborted because of resize.");
                        EventQueueUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                JOptionPane.showMessageDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(),
                                        "The application window cannot be resized once recording is started.", "Recording Aborted",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                        return;
                    }

                    // The y direction for the GL canvas is the opposite of
                    // Swing, so we must flip the image.
                    byte[] flip = new byte[myRecordWidth * myRecordHeight * 3];
                    int srcLineByteWidth = width * 3;
                    int destLineByteWidth = myRecordWidth * 3;
                    for (int i = 0; i < myRecordHeight; ++i)
                    {
                        System.arraycopy(screenCapture, (myRecordHeight - i - 1) * srcLineByteWidth, flip, i * destLineByteWidth,
                                destLineByteWidth);
                    }

                    BufferedImage frame = new BufferedImage(myRecordWidth, myRecordHeight, BufferedImage.TYPE_3BYTE_BGR);
                    WritableRaster wr = frame.getRaster();
                    wr.setDataElements(0, 0, myRecordWidth, myRecordHeight, flip);

                    writeFrame(frame);
                }
                catch (RuntimeException e)
                {
                    // Xuggler throws RuntimeException whenever anything goes
                    // wrong. The message is a guess to help the user, but may
                    // not indicate the real problem.
                    stopRecording();
                    LOGGER.error("Failed to write frame." + e, e);
                    EventQueueUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String message = "Frame encoding failed.\n"
                                    + "This problem typically occurs when data cannot be written fast enough,\n"
                                    + "especially when writing to a network location. Writing to a local\n"
                                    + "file system, changing the video format or decreasing the application \n"
                                    + "window size may alleviate this problem.";
                            JOptionPane.showMessageDialog(myToolbox.getUIRegistry().getMainFrameProvider().get(), message,
                                    "Recording Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
                finally
                {
                    myWriterLock.unlock();
                }
            }
        }

        /**
         * Encode a frame into the video stream.
         *
         * @param frame The frame to encode.
         */
        public void writeFrame(BufferedImage frame)
        {
            if (myEncoder != null && myEncoder.isOpen())
            {
                try
                {
                    myEncoder.encode(frame);
                }
                catch (VideoEncoderException e)
                {
                    LOGGER.error("Failed to encode video frame: " + e, e);
                }
            }

            if (myEncoder != null && myEncoder.isOpen())
            {
                Runnable runner = new WriteRunnable(frame);
                myRecaptureExecutor.execute(runner);
            }
        }

        /** A runnable which writes a frame to the video stream. */
        private class WriteRunnable implements Runnable
        {
            /** The frame which is to be written to the video stream. */
            private final BufferedImage myFrame;

            /**
             * Constructor.
             *
             * @param frame The frame which is to be written to the video
             *            stream.
             */
            public WriteRunnable(BufferedImage frame)
            {
                myFrame = frame;
            }

            @Override
            public void run()
            {
                if (myWriterLock.tryLock())
                {
                    try
                    {
                        if (myEncoder != null && myEncoder.isOpen())
                        {
                            writeFrame(myFrame);
                        }
                    }
                    finally
                    {
                        myWriterLock.unlock();
                    }
                }
            }
        }
    }
}
