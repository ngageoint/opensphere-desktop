package io.opensphere.auxiliary.video;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.io.XugglerIO;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.ExceptionCapturingCancellableInputStream;
import io.opensphere.core.util.io.ListOfBytesOutputStream;
import io.opensphere.core.video.AbstractVideoContentHandler;
import io.opensphere.core.video.ChunkException;
import io.opensphere.core.video.VideoContentHandler;
import io.opensphere.core.video.VideoDecoderException;

/**
 * Chunks a video stream into specified chunk sizes. The individual chunks will
 * contain the same video as the original stream but they will be in a different
 * format.
 */
public class TranscodingChunkProvider extends AbstractVideoContentHandler<ImageIOImage> implements VideoChunkProvider
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(TranscodingChunkProvider.class);

    /**
     * The approximate chunk size in milliseconds, the actual size will extend
     * past this size to the next frame which precedes a key frame or until the
     * end of the stream is reached.
     */
    private final long myApproxSizeMS;

    /** The locations of the key frames. */
    private final TLongList myChunkKeyFrames = new TLongArrayList();

    /** A re-usable stream for storing chunks of video data. */
    private final ListOfBytesOutputStream myChunkStream = new ListOfBytesOutputStream();

    /**
     * The object interested in new video chunks.
     */
    private VideoChunkConsumer myConsumer;

    /**
     * The category or title of the video.
     */
    private final String myFeedName;

    /**
     * The first presentation time for a video chunk.
     */
    private long myFirstPts;

    /**
     * The {@link IContainer} to read the video from.
     */
    private final IContainer myInputContainer;

    /**
     * The last known presentation time.
     */
    private long myLastPts;

    /**
     * The output encoder.
     */
    private IStreamCoder myOutCoder;

    /**
     * The output container.
     */
    private IContainer myOutContainer;

    /**
     * The previous video chunks end time.
     */
    private long myPreviousChunkEndTime;

    /** The stream which will be read to produce the video chunks. */
    private final CancellableInputStream myStream;

    /**
     * The start time of the stream.
     */
    private final long myStreamStart;

    /**
     * Constructs a new re-encoding chunker.
     *
     * @param inStream The video stream to chunk.
     * @param inputContainer The already constructed and opened
     *            {@link IContainer} to read the video from.
     * @param streamStart The start time of the video.
     * @param approxSizeMS The approximate size of the chunks.
     * @param feedName The feed name or title of the video.
     */
    public TranscodingChunkProvider(CancellableInputStream inStream, IContainer inputContainer, long streamStart,
            long approxSizeMS, String feedName)
    {
        myStream = inStream;
        myInputContainer = inputContainer;
        myApproxSizeMS = approxSizeMS <= 0 ? Long.MAX_VALUE : approxSizeMS;
        myFeedName = feedName;
        myPreviousChunkEndTime = streamStart;
        myStreamStart = streamStart;
    }

    @Override
    public long getApproxSizeMS()
    {
        return myApproxSizeMS;
    }

    @Override
    public IContainer getInputContainer()
    {
        return myInputContainer;
    }

    @Override
    public long getStreamStart()
    {
        return myStreamStart;
    }

    @Override
    public CancellableInputStream getVideoStream()
    {
        return myStream;
    }

    @Override
    public void handleContent(ImageIOImage ioImage, long ptsMS)
    {
        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Writing packet to chunk, pts: " + ptsMS);
        }
        BufferedImage image = ioImage.getAWTImage();
        try
        {
            if (myOutContainer == null)
            {
                setupOutputContainer(image.getWidth(), image.getHeight());
                myFirstPts = ptsMS;
            }

            IPacket packet = IPacket.make();
            try
            {
                IConverter converter = ConverterFactory.createConverter(image, myOutCoder.getPixelType());

                IVideoPicture picture = converter.toPicture(image, (ptsMS - myFirstPts) * 1000);
                picture.setQuality(1);

                if (myOutCoder.encodeVideo(packet, picture, 0) < 0)
                {
                    throw new ChunkException("Could not encode video for " + myFeedName);
                }

                if (packet.isComplete())
                {
                    myLastPts = ptsMS;
                    if (picture.isKeyFrame())
                    {
                        myChunkKeyFrames.add(picture.getTimeStamp());
                    }
                    int status = myOutContainer.writePacket(packet);
                    if (status < 0)
                    {
                        throw new ChunkException("Could not write packet to container for " + myFeedName);
                    }
                }
            }
            finally
            {
                packet.delete();
            }

            long chunkDuration = ptsMS - myFirstPts;
            if (chunkDuration >= myApproxSizeMS)
            {
                // Handle for bad video data, sometimes the pts is thrown way
                // off with certain videos.
                if (chunkDuration > 300000)
                {
                    chunkDuration = myApproxSizeMS;
                }

                long endTime = chunkDuration + myPreviousChunkEndTime;
                sendChunkToConsumer(endTime, false);
            }
        }
        catch (ChunkException e)
        {
            LOGGER.error(e.getMessage(), e);
        }
        finally
        {
            ioImage.dispose();
        }
    }

    @Override
    public boolean provideChunks(VideoChunkConsumer chunkConsumer, VideoContentHandler<ByteBuffer> contentHandler)
        throws ChunkException
    {
        myConsumer = chunkConsumer;
        boolean isLastChunk = true;
        try (XugglerVideoDecoder decoder = new XugglerVideoDecoder())
        {
            try
            {
                decoder.setInputStream(myStream, null, myInputContainer);
            }
            catch (VideoDecoderException e)
            {
                throw new ChunkException(e.getMessage(), e);
            }

            decoder.registerVideoContentHandler(this, null);

            if (contentHandler != null)
            {
                decoder.registerNonVideoContentHandler(contentHandler);
            }

            try
            {
                isLastChunk = decoder.decode();
            }
            catch (VideoDecoderException e)
            {
                isLastChunk = false;
                throw new ChunkException(e.getMessage(), e);
            }
        }
        catch (IOException e1)
        {
            isLastChunk = false;
            throw new ChunkException(e1.getMessage(), e1);
        }
        finally
        {
            if (isLastChunk && myStream instanceof ExceptionCapturingCancellableInputStream)
            {
                isLastChunk = ((ExceptionCapturingCancellableInputStream)myStream).getExceptions().isEmpty();
            }
            sendChunkToConsumer(myLastPts - myFirstPts + myPreviousChunkEndTime, isLastChunk);
        }

        return true;
    }

    /**
     * Sends the chunks to the consumer.
     *
     * @param endTime The end time of the chunk.
     * @param isLastChunk If this video chunk is the last one.
     */
    private void sendChunkToConsumer(long endTime, boolean isLastChunk)
    {
        if (myOutContainer != null)
        {
            myOutContainer.writeTrailer();
            myOutCoder.close();
            myOutCoder.delete();
            myOutContainer.flushPackets();
            myOutContainer.close();
            myOutContainer.delete();
            myOutContainer = null;

            long startTime = myPreviousChunkEndTime;

            if (isLastChunk)
            {
                myConsumer.consumeLastChunk(startTime, endTime, myPreviousChunkEndTime, myChunkStream, myChunkKeyFrames);
            }
            else
            {
                myConsumer.consumeVideoChunk(startTime, endTime, myChunkStream, myChunkKeyFrames);
            }

            myPreviousChunkEndTime = endTime;
            myChunkStream.reset();
            myChunkKeyFrames.clear();
        }
    }

    /**
     * Sets up the output container and encoder.
     *
     * @param width The width of the video.
     * @param height The height of the video.
     * @throws ChunkException If the output container could not be setup
     *             properly..
     */
    private void setupOutputContainer(int width, int height) throws ChunkException
    {
        IContainerFormat format = IContainerFormat.make();
        IRational timeBase = IRational.make(1, 60);
        try
        {
            format.setOutputFormat("mpeg", null, null);
            myOutContainer = IContainer.make(format);
            String outputStreamUrl = XugglerIO.map(myChunkStream);
            if (myOutContainer.open(outputStreamUrl, IContainer.Type.WRITE, format, true, false) < 0)
            {
                throw new ChunkException("Could not open output container for video chunk " + myFeedName);
            }

            IStream videoStream = myOutContainer.addNewStream(ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_MPEG2VIDEO));
            myOutCoder = videoStream.getStreamCoder();

            myOutCoder.setWidth(width);
            myOutCoder.setHeight(height);
            myOutCoder.setTimeBase(timeBase);
            myOutCoder.setPixelType(IPixelFormat.Type.YUV420P);
            myOutCoder.setBitRate(12000000);
            myOutCoder.setGlobalQuality(1);

            int error = myOutCoder.open(null, null);
            if (error < 0)
            {
                IError theError = IError.make(error);
                try
                {
                    throw new ChunkException("Could not open encoder " + theError);
                }
                finally
                {
                    theError.delete();
                }
            }

            myOutContainer.writeHeader();
        }
        finally
        {
            format.delete();
            timeBase.delete();
        }
    }
}
