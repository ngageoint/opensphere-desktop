package io.opensphere.auxiliary.video;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.log4j.Logger;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;

import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.video.VideoContentHandler;
import io.opensphere.core.video.VideoDecoder;
import io.opensphere.core.video.VideoDecoderException;

/**
 * A video decoder that uses Xuggler. Except for
 * {@link #suspendDecoding(boolean)} and {@link #close()}, all method calls must
 * be thread-confined.
 */
@ThreadSafe
@SuppressWarnings("PMD.GodClass")
public class XugglerVideoDecoder implements VideoDecoder
{
    /**
     * The number of images that can be in the system at once for this decoder.
     */
    private static final int IMAGE_POOL_SIZE = 10;

    /** Used to log messages. */
    private static final Logger LOGGER = Logger.getLogger(XugglerVideoDecoder.class);

    /** The coder pack currently being used to decode video et al. */
    @GuardedBy("this")
    private XugglerCoderPack myCoderPack;

    /** The first PTS in the stream (in milliseconds). */
    @GuardedBy("this")
    private long myFirstPtsMillis = Global.NO_PTS;

    /** True when the log message for bad video packets has been shown. */
    @GuardedBy("myHasLoggedBadPacket")
    private volatile boolean myHasLoggedBadPacket;

    /**
     * Indicates if images being resized should keep their aspect ratio.
     */
    private boolean myKeepAspectRatio = true;

    /** Collection of handlers for non-video content. */
    private final Collection<VideoContentHandler<? super ByteBuffer>> myNonVideoContentHandlers = new CopyOnWriteArrayList<>();

    /** The dimensions for the decoded video frames. */
    @Nullable
    private Vector2i myOutputDimensions;

    /** The start time. */
    @GuardedBy("this")
    private TimeInstant myStartTime;

    /**
     * When true, video decoding is not done, but resources are not cleaned up.
     */
    private volatile boolean mySuspended;

    /** The video content handler. */
    @GuardedBy("this")
    private VideoContentHandler<? super ImageIOImage> myVideoContentHandler;

    /** The packet decoder which may be re-used with multiple containers. */
    // @GuardedBy("this") for write access, but not for read
    private volatile VideoPacketDecoder myVideoDecoder;

    static
    {
        XugglerNativeUtilities.explodeXugglerNatives();
    }

    @Override
    public void close() throws IOException
    {
        suspendDecoding(true);
        myNonVideoContentHandlers.clear();
        VideoPacketDecoder videoDecoder;
        XugglerCoderPack coderPack;
        synchronized (this)
        {
            videoDecoder = myVideoDecoder;
            coderPack = myCoderPack;
            myVideoDecoder = null;
            myCoderPack = null;
        }
        Utilities.close(videoDecoder, coderPack);
    }

    @Override
    public synchronized boolean decode() throws VideoDecoderException
    {
        if (myCoderPack == null)
        {
            // Assume the decoder has been closed
            return false;
        }

        IPacket packet = IPacket.make();
        byte[] buf = null;
        try
        {
            int status = 0;
            while (myCoderPack.isPackValid() && status >= 0)
            {
                if (mySuspended)
                {
                    /* Return without cleaning up or generating any exceptions.
                     * A continue may occur following this action. */
                    return false;
                }

                if ((status = myCoderPack.getContainer().readNextPacket(packet)) >= 0)
                {
                    if (packet.getStreamIndex() == myCoderPack.getVideoStreamId() && myVideoContentHandler != null)
                    {
                        if (myFirstPtsMillis == Global.NO_PTS)
                        {
                            myFirstPtsMillis = XugglerUtilities.getPtsMillis(packet);
                        }
                        try
                        {
                            myVideoDecoder.decodePacket(packet, myVideoContentHandler);
                        }
                        catch (VideoDecoderException e)
                        {
                            logBadPacket(e);
                        }
                    }
                    else if (packet.getStreamIndex() == myCoderPack.getMetadataStreamId() && !myNonVideoContentHandlers.isEmpty())
                    {
                        buf = XugglerUtilities.handleMetadataPacket(packet, buf, myNonVideoContentHandlers);
                    }
                }
            }

            /* If the stream is cancelled or the coder pack was closed, do not
             * generate an exception. */
            if (!myCoderPack.isPackValid())
            {
                Utilities.close(this);
                IError err = IError.make(status);
                throw new VideoDecoderException("Failed to read stream: " + err);
            }
        }
        finally
        {
            packet.delete();
        }

        return true;
    }

    @Override
    public synchronized boolean decodeToTime(long[] keyframes, TimeInstant time, boolean startOnKeyFrame)
        throws VideoDecoderException
    {
        Utilities.checkNull(myCoderPack, "myCoderPack");
        Utilities.checkNull(myStartTime, "myStartTime");

        IPacket packet = IPacket.make();
        byte[] buf = null;
        long precedingKeyMS = -1;
        try
        {
            int status = 0;
            boolean imageProduced = false;
            while (myCoderPack.isPackValid() && status >= 0)
            {
                if ((status = myCoderPack.getContainer().readNextPacket(packet)) >= 0)
                {
                    // Use DTS instead of PTS here to be consistent with the
                    // packet decoder.
                    long packetPtsMS = XugglerUtilities.getDtsMillis(packet);
                    if (myFirstPtsMillis == Global.NO_PTS)
                    {
                        myFirstPtsMillis = packetPtsMS;
                    }
                    /* If we are not starting on a key frame, don't bother
                     * looking for the preceding key. */
                    if (startOnKeyFrame && precedingKeyMS == -1)
                    {
                        precedingKeyMS = findPrecedingKey(time.getEpochMillis() - myStartTime.getEpochMillis(), keyframes,
                                myFirstPtsMillis);
                    }
                    long ptsOffsetMS = packetPtsMS - myFirstPtsMillis;
                    if (!startOnKeyFrame || packetPtsMS >= precedingKeyMS)
                    {
                        boolean reachedPTSMinusBuffer = myStartTime.getEpochMillis() + ptsOffsetMS >= time.getEpochMillis() - 200;
                        if (packet.getStreamIndex() == myCoderPack.getVideoStreamId() && myVideoContentHandler != null)
                        {
                            try
                            {
                                VideoContentHandler<? super ImageIOImage> contentHandler = reachedPTSMinusBuffer
                                        ? myVideoContentHandler : null;
                                if (myVideoDecoder.decodePacket(packet, contentHandler))
                                {
                                    imageProduced = true;
                                    if (myStartTime.getEpochMillis() + ptsOffsetMS >= time.getEpochMillis())
                                    {
                                        break;
                                    }
                                }
                            }
                            catch (VideoDecoderException e)
                            {
                                logBadPacket(e);
                            }
                        }
                        else if (packet.getStreamIndex() == myCoderPack.getMetadataStreamId()
                                && !myNonVideoContentHandlers.isEmpty() && reachedPTSMinusBuffer)
                        {
                            buf = XugglerUtilities.handleMetadataPacket(packet, buf, myNonVideoContentHandlers);
                            /* If this is metadata only, then we are done,
                             * otherwise continue until a video frame is
                             * produced. */
                            if (myCoderPack.getVideoCoder() == null)
                            {
                                return false;
                            }
                        }
                    }

                    // Update the time in the content handler even when an image
                    // is not produced.
                    if (!imageProduced)
                    {
                        myVideoContentHandler.handleContent((ImageIOImage)null, packetPtsMS);
                    }
                }

                /* If the stream is cancelled or the coder pack was closed, do
                 * not generate an exception. */
                if (!myCoderPack.isPackValid())
                {
                    IError err = IError.make(status);
                    throw new VideoDecoderException("Failed to seek in stream: " + err);
                }
            }
            return imageProduced;
        }
        finally
        {
            packet.delete();
        }
    }

    @Override
    public void deregisterNonVideoContentHandler(VideoContentHandler<? super ByteBuffer> handler)
    {
        myNonVideoContentHandlers.remove(handler);
    }

    @Override
    public synchronized void deregisterVideoContentHandler(VideoContentHandler<? super ImageIOImage> handler)
    {
        if (Utilities.sameInstance(handler, myVideoContentHandler))
        {
            myVideoContentHandler = null;
        }
    }

    @Override
    public synchronized TimeInstant getStartTime()
    {
        return myStartTime;
    }

    @Override
    public boolean isKeepAspectRatio()
    {
        return myKeepAspectRatio;
    }

    @Override
    public boolean isSuspended()
    {
        return mySuspended;
    }

    @Override
    public void registerNonVideoContentHandler(VideoContentHandler<? super ByteBuffer> handler)
    {
        myNonVideoContentHandlers.add(handler);
    }

    @Override
    public synchronized void registerVideoContentHandler(VideoContentHandler<? super ImageIOImage> handler,
            @Nullable Vector2i outputDimensions)
    {
        if (myVideoContentHandler != null)
        {
            throw new IllegalStateException("A video content handler is already registered.");
        }
        myVideoContentHandler = handler;
        myOutputDimensions = outputDimensions;
    }

    @Override
    public synchronized void replaceInputStream(CancellableInputStream is, TimeInstant startTime) throws VideoDecoderException
    {
        if (myCoderPack == null)
        {
            throw new IllegalStateException("Cannot replace input stream with no coder pack.");
        }
        else
        {
            myCoderPack.replaceStream(is);
            myStartTime = startTime;
            myFirstPtsMillis = Global.NO_PTS;
        }
    }

    @Override
    public void setInputFile(String file, TimeInstant startTime) throws VideoDecoderException
    {
        setInputFile(file, null, null);
    }

    /**
     * Sets the input to decode, including an already opened input container.
     *
     * @param file Contains the video data.
     * @param startTime The time corresponding to the beginning of the stream,
     *            which can be {@code null} if
     *            {@link #decodeToTime(long[], TimeInstant, boolean)} will not
     *            be called.
     * @param inputContainer An already opened {@link IContainer} reading from
     *            the specified stream.
     * @throws VideoDecoderException If we had trouble opening the decoder.
     */
    public synchronized void setInputFile(String file, @Nullable TimeInstant startTime, IContainer inputContainer)
        throws VideoDecoderException
    {
        Utilities.close(myCoderPack, myVideoDecoder);

        myCoderPack = new XugglerCoderPack();
        if (inputContainer == null)
        {
            myCoderPack.init(file);
        }
        else
        {
            myCoderPack.init(file, inputContainer);
        }

        myVideoDecoder = createPacketDecoder(myCoderPack, myOutputDimensions);
        myStartTime = startTime;
        myFirstPtsMillis = Global.NO_PTS;
    }

    @Override
    public synchronized void setInputStream(CancellableInputStream is, TimeInstant startTime) throws VideoDecoderException
    {
        setInputStream(is, startTime, (IContainer)null);
    }

    /**
     * Sets the input to decode, including an already opened input container.
     *
     * @param is Contains the video data.
     * @param startTime The time corresponding to the beginning of the stream,
     *            which can be {@code null} if
     *            {@link #decodeToTime(long[], TimeInstant, boolean)} will not
     *            be called.
     * @param inputContainer An already opened {@link IContainer} reading from
     *            the specified stream.
     * @throws VideoDecoderException If we had trouble opening the decoder.
     */
    public synchronized void setInputStream(CancellableInputStream is, @Nullable TimeInstant startTime, IContainer inputContainer)
        throws VideoDecoderException
    {
        Utilities.close(myCoderPack, myVideoDecoder);

        myCoderPack = new XugglerCoderPack();
        if (inputContainer == null)
        {
            myCoderPack.init(is);
        }
        else
        {
            myCoderPack.init(is, inputContainer);
        }

        myVideoDecoder = createPacketDecoder(myCoderPack, myOutputDimensions);
        myStartTime = startTime;
        myFirstPtsMillis = Global.NO_PTS;
    }

    @Override
    public void setKeepAspectRatio(boolean keepAspectRatio)
    {
        myKeepAspectRatio = keepAspectRatio;
    }

    @Override
    public void suspendDecoding(boolean suspend)
    {
        VideoPacketDecoder videoDecoder = myVideoDecoder;
        if (videoDecoder != null)
        {
            videoDecoder.suspendDecoding(suspend);
        }
        mySuspended = suspend;
    }

    @Override
    public synchronized long videoDuration()
    {
        long duration = 0;

        if (myCoderPack != null && myCoderPack.getContainer() != null)
        {
            duration = myCoderPack.getContainer().getDuration();
        }

        return duration / 1000;
    }

    /**
     * Create the packet decoder from the current coder pack.
     *
     * @param coderPack The coder pack.
     * @param outputDimensions The output dimensions for the packet decoder.
     * @return The packet decoder.
     */
    private synchronized VideoPacketDecoder createPacketDecoder(XugglerCoderPack coderPack, @Nullable Vector2i outputDimensions)
    {
        IStreamCoder videoCoder = coderPack.getVideoCoder();
        if (videoCoder == null)
        {
            throw new IllegalStateException("Video coder cannot be null.");
        }
        int width;
        int height;
        if (outputDimensions == null)
        {
            width = videoCoder.getWidth();
            height = videoCoder.getHeight();
        }
        else
        {
            width = outputDimensions.getX();
            // Maintain aspect ratio.
            if (myKeepAspectRatio)
            {
                height = (int)Math.round((double)outputDimensions.getX() * videoCoder.getHeight() / videoCoder.getWidth());
            }
            else
            {
                height = outputDimensions.getY();
            }
        }
        return new VideoPacketDecoder(videoCoder, width, height, IMAGE_POOL_SIZE);
    }

    /**
     * Find the key frame which precedes the display offset.
     *
     * @param displayOffsetMS The offset of the display time from the stream
     *            start time.
     * @param keyframes The time stamps at which the key frames occur, these
     *            values are in the streams native start time, but converted to
     *            milliseconds.
     * @param firstPacketMS The native time stamp of the first packet in the
     *            stream converted to milliseconds.
     * @return The time of the desired key frame.
     */
    private long findPrecedingKey(long displayOffsetMS, long[] keyframes, long firstPacketMS)
    {
        long preceding = firstPacketMS;
        for (long key : keyframes)
        {
            long keyOffsetMS = key - firstPacketMS;
            /* When the offset to the key is larger than the offset to the
             * display time, then we have passed the key frame that we are
             * looking for, so use the previous one. */
            if (displayOffsetMS < keyOffsetMS)
            {
                break;
            }
            preceding = key;
        }
        return preceding;
    }

    /**
     * Display a long message that the bad packet has been encountered.
     *
     * @param e The error which occurred for the bad packet.
     */
    private void logBadPacket(VideoDecoderException e)
    {
        if (!myHasLoggedBadPacket)
        {
            myHasLoggedBadPacket = true;
            LOGGER.error(e.getMessage());
        }
        else if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(e.getMessage(), e);
        }
    }
}
