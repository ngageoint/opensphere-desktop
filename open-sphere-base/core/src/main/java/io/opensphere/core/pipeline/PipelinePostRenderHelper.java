package io.opensphere.core.pipeline;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;

import io.opensphere.core.FrameBufferCaptureManager.FrameBufferCaptureListener;
import io.opensphere.core.FrameBufferCaptureManager.FrameBufferCaptureProvider;
import io.opensphere.core.Toolbox;
import io.opensphere.core.pipeline.cache.CacheProvider;
import io.opensphere.core.pipeline.util.DisposalHelper;
import io.opensphere.core.pipeline.util.RepaintListener;
import io.opensphere.core.util.collections.CollectionUtilities;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.concurrent.CommonTimer;

/** Helper for handling post render operations. */
public class PipelinePostRenderHelper
{
    /** Provider which manages requests for captured images. */
    private final FrameBufferCaptureProvider myCaptureProvider = new FrameBufferCaptureProvider()
    {
        @Override
        public void cancelCapture(FrameBufferCaptureListener listener)
        {
            // In case this listener has been added to multiple lists, make sure
            // to remove it from all of them.
            mySingleFrameListeners.remove(listener);
            synchronized (myStreamListeners)
            {
                myStreamListeners.remove(listener);
            }

            ScheduledFuture<?> future = myTimedListeners.remove(listener);
            if (future != null)
            {
                future.cancel(false);
            }
        }

        @Override
        public void captureScheduled(final FrameBufferCaptureListener externalListener, int interval)
        {
            Runnable task = new Runnable()
            {
                @Override
                public void run()
                {
                    // When the task is executed, capture a single frame to send
                    // to the real listener.
                    captureSingleFrame(new FrameBufferCaptureListener()
                    {
                        @Override
                        public void handleFrameBufferCaptured(int width, int height, byte[] screenCapture)
                        {
                            externalListener.handleFrameBufferCaptured(width, height, screenCapture);
                        }
                    });
                }
            };

            ScheduledFuture<?> future = CommonTimer.scheduleAtFixedRate(task, 0, interval);
            myTimedListeners.put(externalListener, future);
            myRepaintListener.repaint();
        }

        @Override
        public void captureSingleFrame(FrameBufferCaptureListener listener)
        {
            mySingleFrameListeners.add(listener);
            myRepaintListener.repaint();
        }

        @Override
        public void captureStream(FrameBufferCaptureListener listener)
        {
            synchronized (myStreamListeners)
            {
                myStreamListeners.add(listener);
            }
            myRepaintListener.repaint();
        }
    };

    /** The last captured width of the frame buffer. */
    private int myCaptureWidth;

    /**
     * A list of all helpers for disposing on-card memory allocated by the
     * renderers.
     */
    private List<? extends DisposalHelper> myDisposalHelpers;

    /** The most recently captured image of the frame buffer. */
    private ByteBuffer myFrameBufferCapture;

    /** The last captured height of the frame buffer. */
    private int myFrameBufferHeight;

    /** Lock for reading/writing to the captured image. */
    private final ReentrantReadWriteLock myFrameBufferLock = new ReentrantReadWriteLock();

    /**
     * Listener for repaint events. This may be used to force a repaint in order
     * to trigger capturing the frame buffer.
     */
    private final RepaintListener myRepaintListener;

    /** Listeners for waiting for a single frame buffer capture. */
    private final Queue<FrameBufferCaptureListener> mySingleFrameListeners = new ConcurrentLinkedQueue<>();

    /** Executor for sending frame buffer captures to the listeners. */
    private final Executor myStreamExecutor = CommonTimer.createProcrastinatingExecutor(0);

    /** Listeners for multiple frame buffer captures. */
    private final List<FrameBufferCaptureListener> myStreamListeners = new ArrayList<>();

    /**
     * Listeners for multiple frame buffer captures at specific time intervals.
     */
    private final Map<FrameBufferCaptureListener, ScheduledFuture<?>> myTimedListeners = Collections
            .synchronizedMap(new HashMap<FrameBufferCaptureListener, ScheduledFuture<?>>());

    /**
     * Constructor.
     *
     * @param toolbox Toolbox containing the geometry registry, map manager,
     *            control registry, etc.
     * @param listener Listener for repaint requests.
     * @param cache The geometry cache.
     */
    public PipelinePostRenderHelper(Toolbox toolbox, RepaintListener listener, CacheProvider cache)
    {
        myRepaintListener = listener;
        toolbox.getFrameBufferCaptureManager().setCaptureProvider(myCaptureProvider);
    }

    /**
     * Capture an image of the current frame buffer. If there are no listeners
     * waiting to receive the image, the frame buffer will not be captured.
     *
     * @param gl The GL context.
     */
    public void capture(GL gl)
    {
        if (!mySingleFrameListeners.isEmpty() || !myStreamListeners.isEmpty())
        {
            // Record the current row alignment...
            IntBuffer alignment = IntBuffer.allocate(1);
            gl.glGetIntegerv(GL.GL_PACK_ALIGNMENT, alignment);

            // If the GL_PACK_ALIGNMENT is not set to "BYTE" (1) or "EVEN BYTE"
            // (2), the JVM will core when the row width doesn't fall exactly on
            // a word or double-word boundary in memory. "EVEN BYTE" works since
            // we're already enforcing that the width is a multiple of two at
            // this point, but using BYTE(1) since it is always aligned, no
            // matter what.
            gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);

            WriteLock wLock = myFrameBufferLock.writeLock();
            wLock.lock();
            try
            {
                GLDrawable drawable = GLContext.getCurrent().getGLDrawable();
                myCaptureWidth = drawable.getWidth();
                myFrameBufferHeight = drawable.getHeight();

                // We are getting the image as RGB, so 3 bytes per pixel.
                int bytesPerPixel = 3;
                int bufferSize = myCaptureWidth * myFrameBufferHeight * bytesPerPixel;

                if (myFrameBufferCapture == null || myFrameBufferCapture.capacity() != bufferSize)
                {
                    myFrameBufferCapture = ByteBuffer.allocateDirect(bufferSize);
                }
                myFrameBufferCapture.rewind();
                gl.glReadPixels(0, 0, myCaptureWidth, myFrameBufferHeight, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, myFrameBufferCapture);
            }
            finally
            {
                wLock.unlock();
            }

            // Restore the old row alignment..
            gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, alignment.get(0));

            // Use an executor to move listener notification off of the GL
            // thread.
            Runnable notify = new Runnable()
            {
                @Override
                public void run()
                {
                    notifyListeners();
                }
            };
            myStreamExecutor.execute(notify);
        }
    }

    /**
     * Dispose of all resources being tracked by my disposal helpers.
     *
     * @param gl The GL context.
     */
    public void forceDispose(GL gl)
    {
        for (DisposalHelper helper : myDisposalHelpers)
        {
            helper.forceDispose(gl);
        }
    }

    /**
     * Handle any post render activities.
     *
     * @param gl The GL context.
     */
    public void postRender(GL gl)
    {
        capture(gl);

        if (!myDisposalHelpers.isEmpty())
        {
            for (DisposalHelper disposal : myDisposalHelpers)
            {
                disposal.cleanOncardMemory(gl);
            }
        }
    }

    /**
     * Set the disposal helpers.
     *
     * @param disposalHelpers The disposal helpers.
     */
    public void setDisposalHelpers(Collection<? extends DisposalHelper> disposalHelpers)
    {
        if (CollectionUtilities.hasContent(myDisposalHelpers))
        {
            for (DisposalHelper disposalHelper : disposalHelpers)
            {
                disposalHelper.close();
            }
        }
        myDisposalHelpers = New.unmodifiableList(disposalHelpers);
        for (DisposalHelper disposalHelper : disposalHelpers)
        {
            disposalHelper.open();
        }
    }

    /**
     * Provide the captured frame buffer to the listeners who only wish to
     * receive a single image.
     */
    private void notifyListeners()
    {
        // All listeners will receive the same copy of the buffer.
        byte[] capture = null;
        int width = 0;
        int height = 0;
        ReadLock rLock = myFrameBufferLock.readLock();
        rLock.lock();
        try
        {
            capture = new byte[myFrameBufferCapture.capacity()];
            myFrameBufferCapture.rewind();
            myFrameBufferCapture.get(capture);
            width = myCaptureWidth;
            height = myFrameBufferHeight;
        }
        finally
        {
            rLock.unlock();
        }

        FrameBufferCaptureListener listener = null;
        while ((listener = mySingleFrameListeners.poll()) != null)
        {
            listener.handleFrameBufferCaptured(width, height, capture);
        }

        List<FrameBufferCaptureListener> streamListeners = new ArrayList<>(myStreamListeners.size());
        synchronized (myStreamListeners)
        {
            streamListeners.addAll(myStreamListeners);
        }

        for (FrameBufferCaptureListener streamListener : streamListeners)
        {
            streamListener.handleFrameBufferCaptured(width, height, capture);
        }
    }
}
