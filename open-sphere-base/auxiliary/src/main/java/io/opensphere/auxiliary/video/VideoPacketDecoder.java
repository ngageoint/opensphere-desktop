package io.opensphere.auxiliary.video;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.function.Consumer;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.collections.LazyMap.Factory;
import io.opensphere.core.util.collections.MappedObjectPool;
import io.opensphere.core.util.concurrent.CommonTimer;
import io.opensphere.core.video.ExtendedVideoContentHandler;
import io.opensphere.core.video.VideoContentHandler;
import io.opensphere.core.video.VideoDecoderException;

/**
 * Decodes video packets and sends them to {@link VideoContentHandler}s. When
 * displaying the image in a window the image may be resized before display. It
 * would be more efficient if we knew that size so that we could resample here.
 */
@ThreadSafe
public class VideoPacketDecoder implements Closeable
{
    /**
     * Converter used to produce {@link BufferedImage}s from
     * {@link IVideoPicture}s.
     */
    @GuardedBy("this")
    private final IConverter myConverter;

    /** The picture into which a frame is decoded. */
    @GuardedBy("this")
    private final IVideoPicture myPicture;

    /** Pool of {@link BufferedImage}s to use while decoding video. */
    private final MappedObjectPool<Void, BufferedImage> myPool;

    /** If a resampler is used this will be the resampled, decoded frame. */
    @GuardedBy("this")
    private final IVideoPicture myResampledPicture;

    /** When present, resize or reformat the video frames. */
    @GuardedBy("this")
    private final IVideoResampler myResampler;

    /**
     * When true, video decoding is not done, but resources are not cleaned up.
     */
    private volatile boolean mySuspended;

    /** Xuggler's workhorse. */
    @GuardedBy("this")
    private final IStreamCoder myVideoCoder;

    /** The thread being used for decoding. */
    private volatile Thread myDecodingThread;

    /**
     * Constructor.
     *
     * @param videoCoder The video coder.
     * @param outputWidth The output width in pixels.
     * @param outputHeight The output height in pixels.
     * @param imagePoolSize The number of images that can be created at once.
     */
    public VideoPacketDecoder(IStreamCoder videoCoder, int outputWidth, int outputHeight, int imagePoolSize)
    {
        Utilities.checkNull(videoCoder, "videoCoder");
        myVideoCoder = videoCoder;
        myPicture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());

        /* If the pixel types do not match the converter will also have a
         * resampler and that resampler will create a new IVideoPicture for each
         * frame. By resampling prior to using the converter, we can reuse the
         * IVideoPicture and cause the converter to skip resampling internally. */
        myResampler = getResampler(videoCoder, outputWidth, outputHeight);
        if (myResampler != null)
        {
            myResampledPicture = IVideoPicture.make(myResampler.getOutputPixelFormat(), myResampler.getOutputWidth(),
                    myResampler.getOutputHeight());
            myConverter = ConverterFactory.createConverter("XUGGLER-BGR-24", myResampledPicture);
        }
        else
        {
            myResampledPicture = null;
            myConverter = ConverterFactory.createConverter("XUGGLER-BGR-24", myPicture);
        }

        myPool = new MappedObjectPool<>(Void.class, null, imagePoolSize, imagePoolSize,
                CommonTimer.createProcrastinatingExecutor(10000, 30000));
    }

    @Override
    public synchronized void close()
    {
        Utilities.close(XugglerUtilities.getCloseableWrapper(myPicture), XugglerUtilities.getCloseableWrapper(myResampledPicture),
                XugglerUtilities.getClosableWrapper(myConverter));
    }

    /**
     * Decode a video packet and send the image to the
     * {@link VideoContentHandler}.
     *
     * @param packet The packet to be decoded.
     * @param handler The content handler.
     * @return true when an image is produced from this packet. Even if decoding
     *         is successful, an image may not be produced for some codec types.
     * @throws VideoDecoderException If there is an error.
     */
    public synchronized boolean decodePacket(IPacket packet, VideoContentHandler<? super ImageIOImage> handler)
        throws VideoDecoderException
    {
        myDecodingThread = Thread.currentThread();
        try
        {
            boolean imageProduced = false;
            int offset = 0;
            while (offset < packet.getSize() && !mySuspended)
            {
                int errorCount = 0;
                int bytesDecoded;
                do
                {
                    bytesDecoded = myVideoCoder.decodeVideo(myPicture, packet, offset);
                }
                while (bytesDecoded < 0 && ++errorCount < 3);

                if (bytesDecoded < 0)
                {
                    throw new VideoDecoderException("Unable to decode video packet.");
                }
                else
                {
                    offset += bytesDecoded;
                }
                if (myPicture.isComplete() && handler != null && !mySuspended)
                {
                    IVideoPicture picture = myPicture;
                    if (myResampler != null)
                    {
                        picture = myResampledPicture;
                        if (myResampler.resample(picture, myPicture) < 0)
                        {
                            throw new VideoDecoderException("could not resample video");
                        }
                    }
                    if (picture.getPixelType() != com.xuggle.xuggler.IPixelFormat.Type.BGR24)
                    {
                        throw new VideoDecoderException("could not decode video as BGR 24 bit data");
                    }

                    imageProduced |= produceImage(packet, handler);
                }
            }
            return imageProduced;
        }
        finally
        {
            myDecodingThread = null;
        }
    }

    /**
     * Suspend decoding, but do not clean up resources.
     *
     * @param suspend If decoding should be suspended or resumed.
     */
    public void suspendDecoding(boolean suspend)
    {
        mySuspended = suspend;

        // Interrupt the thread in case it's waiting on the image pool.
        Thread decodingThread = myDecodingThread;
        if (suspend && decodingThread != null)
        {
            decodingThread.interrupt();
        }
    }

    /**
     * Create an image, reusing a pooled image if possible.
     *
     * @param picture The picture for which an image is desired.
     * @return The newly created image.
     */
    private ImageIOImage createImage(final IVideoPicture picture)
    {
        Factory<? super Void, ? extends BufferedImage> factory = new Factory<Void, BufferedImage>()
        {
            @Override
            public BufferedImage create(Void key)
            {
                return myConverter.toImage(picture);
            }
        };

        Consumer<BufferedImage> consumer = new Consumer<BufferedImage>()
        {
            @Override
            public void accept(BufferedImage image)
            {
                myConverter.toImage(picture, image);
            }
        };

        BufferedImage image;
        try
        {
            image = myPool.take(null, factory, consumer);
        }
        catch (InterruptedException e)
        {
            image = null;
        }
        return image == null ? null : new ImageIOImage(image)
        {
            /** Serial version UID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void handleDispose(BufferedImage disposed)
            {
                myPool.surrender(null, disposed);
            }
        };
    }

    /**
     * If the pixel type for the coder is not BGR24 or the output dimensions do
     * not match the coder dimensions, return a resampler that will convert
     * convert the frames to the right format.
     *
     * @param videoCoder The video coder.
     * @param outputWidth The output width in pixels.
     * @param outputHeight The output height in pixels.
     * @return The resampler, or {@code null} if one is not necessary.
     */
    private IVideoResampler getResampler(IStreamCoder videoCoder, int outputWidth, int outputHeight)
    {
        IVideoResampler resampler;
        if (videoCoder.getPixelType() != com.xuggle.xuggler.IPixelFormat.Type.BGR24 || outputWidth != videoCoder.getWidth()
                || outputHeight != videoCoder.getHeight())
        {
            resampler = IVideoResampler.make(outputWidth, outputHeight, com.xuggle.xuggler.IPixelFormat.Type.BGR24,
                    videoCoder.getWidth(), videoCoder.getHeight(), videoCoder.getPixelType());
            if (resampler == null)
            {
                throw new IllegalStateException("Could not create resampler.");
            }
        }
        else
        {
            resampler = null;
        }
        return resampler;
    }

    /**
     * Produce an image and send it to the {@link VideoContentHandler}.
     *
     * @param packet The packet to be decoded.
     * @param handler The content handler.
     * @return true when an image is produced from this packet. Even if decoding
     *         is successful, an image may not be produced for some codec types.
     * @throws VideoDecoderException If there is an error.
     */
    private boolean produceImage(IPacket packet, VideoContentHandler<? super ImageIOImage> handler) throws VideoDecoderException
    {
        /* Use the DTS here despite the documentation, because ordering the
         * frames by the PTS results in jittery video. */
        long ptsMillis = XugglerUtilities.getDtsMillis(packet);

        // Create and handle the image
        IVideoPicture picture = myResampler == null ? myPicture : myResampledPicture;
        final ImageIOImage javaImage = createImage(picture);
        if (handler == null)
        {
            if (javaImage != null)
            {
                javaImage.dispose();
            }
        }
        else
        {
            handler.handleContent(javaImage, ptsMillis);
            if (handler instanceof ExtendedVideoContentHandler)
            {
                ((ExtendedVideoContentHandler<? super ImageIOImage>)handler).handleContentWithPosition(javaImage,
                        packet.getPosition());
            }
        }

        return javaImage != null;
    }
}
