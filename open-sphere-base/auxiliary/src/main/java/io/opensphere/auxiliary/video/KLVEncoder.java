package io.opensphere.auxiliary.video;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.xuggle.ferry.IBuffer;
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

import io.opensphere.core.video.KLVVideoEncoder;
import io.opensphere.core.video.VideoEncoderException;

/**
 * Encodes a video and metadata into a klv video. The klv video will be put into
 * the given {@link OutputStream}.
 */
public class KLVEncoder implements KLVVideoEncoder
{
    /**
     * The first presentation time for a video chunk.
     */
    private long myFirstPts;

    /**
     * The klv coder.
     */
    private IStreamCoder myKlvCoder;

    /**
     * The output encoder.
     */
    private IStreamCoder myOutCoder;

    /**
     * The output container.
     */
    private IContainer myOutContainer;

    /**
     * The outputstream to write out klv video to.
     */
    private OutputStream myOutputStream;

    /**
     * Completes the encoding process.
     */
    @Override
    public void close()
    {
        if (myOutContainer != null)
        {
            myOutContainer.writeTrailer();
            myOutCoder.close();
            myOutCoder.delete();
            myKlvCoder.close();
            myKlvCoder.delete();
            myOutContainer.flushPackets();
            myOutContainer.close();
            myOutContainer.delete();
            myOutContainer = null;
        }
    }

    /**
     * Encodes the metadata into the klv stream.
     *
     * @param metadata The metadata in MISB KLV format to add to the klv stream.
     * @param ptsMS The time fo the metadata.
     */
    @Override
    @SuppressWarnings("PMD.AvoidArrayLoops")
    public void encodeMetadata(ByteBuffer metadata, long ptsMS)
    {
        byte[] metadataBytes = metadata.array();
        byte[] packetBytes = new byte[metadataBytes.length * 2];
        for (int i = 0, j = 1; i < metadataBytes.length; i++, j += 2)
        {
            packetBytes[j] = metadataBytes[i];
        }

        IBuffer buffer = IBuffer.make(null, packetBytes, 0, packetBytes.length);
        IPacket packet = IPacket.make(buffer);
        try
        {
            packet.setTimeBase(myKlvCoder.getTimeBase());
            // TODO Since xuggler doesn't handle klv appropriately we must put
            // all metadata at the front of the video file so set the pts to
            // zero. Once our xuggler version handles klv appropriately
            // uncomment the below code out to pass in the correct time.
//            double secondsPerTimeBase = packet.getTimeBase().getDouble();
//            long timeBasePts = (long)((ptsMS - myFirstPts) / secondsPerTimeBase / Constants.MILLI_PER_UNIT);
            packet.setPts(0);
            packet.setStreamIndex(myKlvCoder.getStream().getIndex());
            packet.setComplete(true, packet.getSize());
            myOutContainer.writePacket(packet);
        }
        finally
        {
            buffer.delete();
            packet.delete();
        }
    }

    /**
     * Encodes the frame into the klv stream.
     *
     * @param image The image to encode.
     * @param ptsMS The presentation time of the image in milliseconds.
     * @throws VideoEncoderException Thrown if there were issues writing encoded
     *             video to the stream.
     */
    @Override
    public void encodeVideo(BufferedImage image, long ptsMS) throws VideoEncoderException
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
                throw new VideoEncoderException("Could not encode video", null);
            }

            if (packet.isComplete())
            {
                int status = myOutContainer.writePacket(packet);
                if (status < 0)
                {
                    throw new VideoEncoderException("Could not write packet to container", null);
                }
            }
        }
        finally
        {
            packet.delete();
        }
    }

    /**
     * Sets up the output container and encoder.
     *
     * @param width The width of the video.
     * @param height The height of the video.
     * @throws VideoEncoderException If the output container could not be setup
     *             properly.
     */
    private void setupOutputContainer(int width, int height) throws VideoEncoderException
    {
        IContainerFormat format = IContainerFormat.make();
        IRational timeBase = IRational.make(1, 60);
        try
        {
            format.setOutputFormat("mpegts", null, null);
            myOutContainer = IContainer.make(format);
            String outputStreamUrl = XugglerIO.map(myOutputStream);
            if (myOutContainer.open(outputStreamUrl, IContainer.Type.WRITE, format, true, false) < 0)
            {
                throw new VideoEncoderException("Could not open output container for klv video.", null);
            }

            IStream videoStream = myOutContainer.addNewStream(ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_MPEG2VIDEO));
            IStream klvStream = myOutContainer.addNewStream(ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_FFMETADATA));
            myOutCoder = videoStream.getStreamCoder();
            myKlvCoder = klvStream.getStreamCoder();

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
                    throw new VideoEncoderException("Could not open encoder " + theError, null);
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

    @Override
    public void init(OutputStream stream)
    {
        myOutputStream = stream;
    }
}
