package io.opensphere.auxiliary.video;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.io.XugglerIO;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.io.ExceptionCapturingCancellableInputStream;
import io.opensphere.core.util.io.ListOfBytesOutputStream;
import io.opensphere.core.video.ChunkException;
import io.opensphere.core.video.VideoContentHandler;

/**
 * Provides approximately five second chunks of video data for a video stream by
 * taking the existing video data and breaking it into 5 second chunks with
 * changing the video format.
 */
public class ReencodingChunkProvider implements VideoChunkProvider
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(ReencodingChunkProvider.class);

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
     * The container to read the video from.
     */
    private final IContainer myInputContainer;

    /** The stream which will be read to produce the video chunks. */
    private final CancellableInputStream myStream;

    /**
     * The start time of the stream (milliseconds since epoch). Using the start
     * time of the stream to determine a tag's time in combination with the
     * tag's time stamp (time since stream start) insures that we do not
     * experience time drift.
     */
    private final long myStreamStartTime;

    /**
     * Constructs a new re-encoding chunker.
     *
     * @param inStream The video stream to chunk.
     * @param inputContainer An already constructed and opened
     *            {@link IContainer} to read the video from.
     * @param streamStart The start time of the video.
     * @param approxSizeMS The approximate size of the chunks.
     */
    public ReencodingChunkProvider(CancellableInputStream inStream, IContainer inputContainer, long streamStart,
            long approxSizeMS)
    {
        myStream = inStream;
        myInputContainer = inputContainer;
        myStreamStartTime = streamStart;
        myApproxSizeMS = approxSizeMS <= 0 ? Long.MAX_VALUE : approxSizeMS;
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
        return myStreamStartTime;
    }

    @Override
    public CancellableInputStream getVideoStream()
    {
        return myStream;
    }

    @Override
    public boolean provideChunks(VideoChunkConsumer chunkConsumer, VideoContentHandler<ByteBuffer> contentHandler)
        throws ChunkException
    {
        boolean success = true;
        XugglerNativeUtilities.explodeXugglerNatives();

        IContainer outContainer = null;
        IPacket packet = null;
        long startTimeEpoch = myStreamStartTime;
        long timeInChunk = 0;
        long firstPacketMS = -1;
        long previousChunkLastEpoch = -1;
        try
        {
            outContainer = setupOutputContainer(myInputContainer);

            packet = IPacket.make();
            long previousPacketEpoch = myStreamStartTime;
            byte[] buf = null;
            while (outContainer != null && myInputContainer.readNextPacket(packet) >= 0 && !myStream.isCancelled())
            {
                ICodec.Type codecType = myInputContainer.getStream(packet.getStreamIndex()).getStreamCoder().getCodecType();
                boolean isVideo = codecType == ICodec.Type.CODEC_TYPE_VIDEO;
                boolean isMetadata = codecType == ICodec.Type.CODEC_TYPE_UNKNOWN;
                if (isVideo && packet.isComplete())
                {
                    /* There is only one stream being written, so the index will
                     * be 0. */
                    packet.setStreamIndex(0);

                    long ptsMS = XugglerUtilities.getPtsMillis(packet);

                    if (LOGGER.isTraceEnabled())
                    {
                        LOGGER.trace("Writing packet to chunk, pts: " + ptsMS + " dts: " + XugglerUtilities.getDtsMillis(packet));
                    }
                    if (firstPacketMS == -1)
                    {
                        firstPacketMS = ptsMS;
                    }
                    else
                    {
                        timeInChunk = ptsMS - firstPacketMS;
                    }

                    long ptsEpoch = startTimeEpoch + timeInChunk;
                    if (timeInChunk > myApproxSizeMS && packet.isKey() || timeInChunk > myApproxSizeMS * 3)
                    {
                        outContainer.writeTrailer();
                        myChunkStream.flush();

                        /* Shift the times for the chunk to include the duration
                         * of the last packet before this chunk and exclude the
                         * duration for this chunk's last packet. This will
                         * allow the queried chunk to find the correct display
                         * frame when the desired seek time is beyond the last
                         * packet's PTS but before the first one in the next
                         * chunk. */
                        long chunkStart = previousChunkLastEpoch == -1 ? startTimeEpoch : previousChunkLastEpoch;
                        chunkConsumer.consumeVideoChunk(chunkStart, previousPacketEpoch, myChunkStream, myChunkKeyFrames);

                        // Reset everything to prepare for the next chunk.
                        previousChunkLastEpoch = previousPacketEpoch;
                        startTimeEpoch = ptsEpoch;
                        myChunkKeyFrames.clear();
                        outContainer.close();
                        outContainer = setupOutputContainer(myInputContainer);
                        firstPacketMS = ptsMS;
                        timeInChunk = 0;
                    }

                    success = addPacketToChunk(outContainer, packet, ptsMS);
                    if (!success)
                    {
                        outContainer.writeTrailer();
                        break;
                    }
                    previousPacketEpoch = ptsEpoch;
                }
                else if (isMetadata && packet.isComplete() && contentHandler != null)
                {
                    buf = XugglerUtilities.handleMetadataPacket(packet, buf, New.list(contentHandler));
                }
            }
        }
        catch (IOException e)
        {
            if (!myStream.isCancelled())
            {
                myStream.cancel();
                throw new ChunkException("Error while segmenting video: " + e, e);
            }
        }
        finally
        {
            cleanResources(null, outContainer, packet);
        }

        if (myStream instanceof ExceptionCapturingCancellableInputStream)
        {
            success = ((ExceptionCapturingCancellableInputStream)myStream).getExceptions().isEmpty();
        }

        if (success)
        {
            /* Moved this out of a try finally because if this fails to chunkify
             * a video we want to reuse the input container in a fail over chunk
             * provider. */
            try
            {
                chunkConsumer.consumeLastChunk(startTimeEpoch, startTimeEpoch + timeInChunk, previousChunkLastEpoch,
                        myChunkStream, myChunkKeyFrames);
            }
            finally
            {
                cleanResources(myInputContainer, null, null);
            }
        }

        return success;
    }

    /**
     * Write the packet to the container and the index to the key frame list if
     * applicable.
     *
     * @param outContainer The output container which contains the chunk.
     * @param packet The packet to add to the chunk
     * @param ptsMS The time stamp of the packet converted to milliseconds. This
     *            time may be any value which is consistent with the difference
     *            from the first packet in the stream.
     * @return True if the packet was successfully added to the container, false
     *         otherwise.
     * @throws ChunkException If the packet cannot be written to the container.
     */
    private boolean addPacketToChunk(IContainer outContainer, IPacket packet, long ptsMS) throws ChunkException
    {
        boolean success = true;
        if (outContainer != null)
        {
            /* There is only one stream, so no interleaving is possible. */
            int status;
            if ((status = outContainer.writePacket(packet, false)) < 0)
            {
                IError err = IError.make(status);
                LOGGER.error("Failed to write video packet to chunk. " + err.toString());
                success = false;
            }

            /* If we are ending a chunk, do this after depositing the chunk so
             * that the new chunk will start with a key frame. */
            if (packet.isKey())
            {
                myChunkKeyFrames.add(ptsMS);
            }
        }

        return success;
    }

    /**
     * Cleanup the provided resources.
     *
     * @param inputContainer Input container.
     * @param outContainer Output container.
     * @param packet The packet which was used during chunking.
     */
    private void cleanResources(IContainer inputContainer, IContainer outContainer, IPacket packet)
    {
        try
        {
            if (inputContainer != null)
            {
                inputContainer.close();
            }
        }
        finally
        {
            try
            {
                if (outContainer != null)
                {
                    outContainer.close();
                }
            }
            finally
            {
                if (packet != null)
                {
                    packet.delete();
                }
            }
        }
    }

    /**
     * Setup an output container for generating video chunks which match the
     * format of the input container.
     *
     * @param inputContainer The input container which will provide the video
     *            data.
     * @return The output container.
     * @throws ChunkException If the stream does not contain video or there's an
     *             error writing to the output container.
     */
    private IContainer setupOutputContainer(IContainer inputContainer) throws ChunkException
    {
        IStreamCoder coder = null;
        for (int i = 0; i < inputContainer.getNumStreams(); ++i)
        {
            IStream stream = inputContainer.getStream(i);
            if (stream.getStreamCoder().getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
            {
                coder = stream.getStreamCoder();
            }
        }

        if (coder == null)
        {
            throw new ChunkException("Stream does not contain video.");
        }

        /* Whenever we have FLV video, put in in an FLV container even if the
         * original video is another type (like SWF). For H.264 video force it
         * to be in an MPEG container. Using an H.264 container will not cause
         * any errors, but no video frames can be produced from the resulting
         * chunks. */
        IContainerFormat outFormat = IContainerFormat.make();
        if ("flv".equalsIgnoreCase(coder.getCodec().getName()))
        {
            outFormat.setOutputFormat("flv", null, "video/x-flv");
        }
        else if ("h264".equalsIgnoreCase(coder.getCodec().getName()))
        {
            outFormat.setOutputFormat("mpeg", null, "video/mpeg");
        }
        else
        {
            /* If it is not FLV or H.264, try to initialize the output format to
             * match the input format. */
            IContainerFormat inFormat = inputContainer.getContainerFormat();
            outFormat.setOutputFormat(inFormat.getInputFormatShortName(), null, null);
        }

        IContainer outContainer = IContainer.make();
        outContainer.setFormat(outFormat);

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Starting to write to chunk");
        }
        myChunkStream.reset();
        String outputStreamUrl = XugglerIO.map(myChunkStream);
        if (outContainer.open(outputStreamUrl, IContainer.Type.WRITE, outFormat, true, false) < 0)
        {
            throw new ChunkException("Could not open output container for video chunk.");
        }

        IStream outStream = outContainer.addNewStream(coder.getCodec());
        IStreamCoder outCoder = outStream.getStreamCoder();
        /* The doc for getTimeBase() says that the returned object needs to be
         * released, but this will cause vm crash. */
        outCoder.setTimeBase(coder.getTimeBase());
        outCoder.setWidth(coder.getWidth());
        outCoder.setHeight(coder.getHeight());
        outCoder.setPixelType(coder.getPixelType());
        outCoder.setFlags(coder.getFlags());
        outCoder.setSampleFormat(coder.getSampleFormat());
        outCoder.setCodecTag(coder.getCodecTag());

        if (outContainer.writeHeader() < 0)
        {
            throw new ChunkException("Failed to write header for video chunk stream.");
        }

        return outContainer;
    }
}
