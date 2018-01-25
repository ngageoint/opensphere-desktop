package io.opensphere.auxiliary.video;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.io.XugglerIO;

import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.concurrent.ReportingScheduledExecutorService;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.video.AbstractVideoContentHandler;
import io.opensphere.core.video.FLVStreamTranscoder;
import io.opensphere.core.video.VideoContentHandler;
import io.opensphere.core.video.VideoDecoderException;
import io.opensphere.core.video.VideoErrorHandler;

/**
 * A class which can convert the video data in a stream into different video
 * type.
 */
public class XugglerFLVStreamTranscoder implements FLVStreamTranscoder
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(XugglerFLVStreamTranscoder.class);

    /** The decoder used to read the original video stream. */
    private XugglerVideoDecoder myDecoder;

    /**
     * The encoder is used to produce the FLV video stream which this transcoder
     * provides.
     */
    private XugglerVideoEncoder myEncoder;

    /**
     * The object to notify of any unrecoverable errors.
     */
    private VideoErrorHandler myErrorHandler;

    /** The stream which contains the transcoded video. */
    private PipedInputStream myIstream;

    /**
     * The output stream which feeds the transcoded input stream, the writer
     * provides the data to this stream.
     */
    private PipedOutputStream myOstream;

    /** When true this transcoder is currently transcoding data. */
    private boolean myStreaming;

    /** An executor for running the transcoding. */
    private final ReportingScheduledExecutorService myStreamProcessingExecutor;

    /** The original video stream which is to be transcoded. */
    private CancellableInputStream myVideoStream;

    /** The writer writes video packets into the output stream. */
    private IMediaWriter myWriter;

    /** Default constructor. */
    public XugglerFLVStreamTranscoder()
    {
        UncaughtExceptionHandler exHandler = new UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                close();
                if (myVideoStream != null)
                {
                    myVideoStream.cancel();
                }
            }
        };
        myStreamProcessingExecutor = new ReportingScheduledExecutorService(new ScheduledThreadPoolExecutor(0,
                new NamedThreadFactory("Xuggler Stream Transcoder"), SuppressableRejectedExecutionHandler.getInstance()),
                exHandler);
    }

    /** Clean up any resources possible. */
    public void close()
    {
        myStreaming = false;
        try
        {
            if (myVideoStream != null)
            {
                myVideoStream.cancel();
            }
            myVideoStream = null;
        }
        finally
        {
            try
            {
                /* Closing myEncoder will also close myWriter. */
                Utilities.close(myOstream, myIstream, myDecoder, myEncoder);
            }
            catch (RuntimeException e)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Failed to close Xuggler resources." + e, e);
                }
            }
        }
    }

    @Override
    public void setErrorHandler(VideoErrorHandler handler)
    {
        myErrorHandler = handler;
    }

    @Override
    public CancellableInputStream transcodeToFLV(final CancellableInputStream videoIn)
    {
        if (myStreaming)
        {
            throw new IllegalStateException("This transcoder is already transcoding video.");
        }
        XugglerNativeUtilities.explodeXugglerNatives();

        /* Transcode the video even if (especially if) it is already FLV video.
         * Video produced by the red5 code cannot be broken into chunks. Once
         * this issue is resolved, transcoding can be eliminated. */
        return doTranscodeToFLV(videoIn);
    }

    /**
     * Do the actual transcoding to FLV, this should only be called when the
     * source video is not already FLV.
     *
     * @param videoIn The source video to transcode.
     * @return A stream which supplies the transcoded video.
     */
    private CancellableInputStream doTranscodeToFLV(final CancellableInputStream videoIn)
    {
        myVideoStream = videoIn;
        try
        {
            myOstream = new PipedOutputStream();
            myIstream = new PipedInputStream(myOstream);

            final CancellableInputStream retStream = new CancellableInputStream(myIstream, new Runnable()
            {
                @Override
                public void run()
                {
                    videoIn.cancel();
                    close();
                }
            });

            myStreamProcessingExecutor.execute(new Runnable()
            {
                private final VideoContentHandler<? super ImageIOImage> myContentHandler = new AbstractVideoContentHandler<ImageIOImage>()
                {
                    @Override
                    public void handleContent(final ImageIOImage packet, long ptsMS)
                    {
                        if (packet == null)
                        {
                            return;
                        }
                        BufferedImage image = packet.getAWTImage();
                        /* Wait to create the writer until we get the first
                         * packet. This allows us to know the video width/height
                         * for the stream. */
                        if (myWriter == null)
                        {
                            myWriter = ToolFactory.makeWriter(XugglerIO.map(myOstream));
                            IContainerFormat containerFormat = IContainerFormat.make();
                            containerFormat.setOutputFormat("flv", null, "video/x-flv");
                            myWriter.getContainer().setFormat(containerFormat);
                            myWriter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_FLV1, image.getWidth(), image.getHeight());
                            myEncoder = new XugglerVideoEncoder();
                            myEncoder.open(myWriter);
                        }
                        if (myEncoder.isOpen())
                        {
                            myEncoder.encode(image, ptsMS);
                        }
                        packet.dispose();
                    }
                };

                @Override
                public void run()
                {
                    String errorMessage = null;
                    Exception videoException = null;
                    myStreaming = true;
                    try
                    {
                        myDecoder = new XugglerVideoDecoder();
                        myDecoder.registerVideoContentHandler(myContentHandler, (Vector2i)null);
                        myDecoder.setInputStream(myVideoStream, TimeInstant.get());
                        myDecoder.decode();
                    }
                    catch (VideoDecoderException e)
                    {
                        if (!myVideoStream.isCancelled())
                        {
                            errorMessage = "Failure occured while decoding video. " + e;
                            videoException = e;
                            LOGGER.error("Failure occured while decoding video. " + e, e);
                        }
                    }

                    /* We cannot read the stream anymore, perform clean up. */
                    retStream.cancel();

                    if (errorMessage != null && myErrorHandler != null)
                    {
                        myErrorHandler.error(errorMessage, videoException);
                    }
                }
            });

            return retStream;
        }
        catch (IOException e)
        {
            LOGGER.error("Streaming failure occurred while transcoding video" + e, e);
        }

        return null;
    }
}
