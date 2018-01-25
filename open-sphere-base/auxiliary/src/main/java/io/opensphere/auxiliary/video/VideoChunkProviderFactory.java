package io.opensphere.auxiliary.video;

import org.apache.log4j.Logger;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.video.ChunkException;

/**
 * Builds the appropriate chunk provider based on the type of video that is
 * contained in the stream.
 */
public final class VideoChunkProviderFactory
{
    /**
     * Used to log messages.
     */
    private static final Logger LOGGER = Logger.getLogger(VideoChunkProviderFactory.class);

    /**
     * The instance of this class.
     */
    private static final VideoChunkProviderFactory ourInstance = new VideoChunkProviderFactory();

    /**
     * Gets the instance of this class.
     *
     * @return The instance of this class.
     */
    public static VideoChunkProviderFactory getInstance()
    {
        return ourInstance;
    }

    /**
     * Not constructible.
     */
    private VideoChunkProviderFactory()
    {
    }

    /**
     * Creates a provider to use in case the default provider failed to produce
     * chunks.
     *
     * @param failedProvider The provider that failed.
     * @param feedName The feed name of the video.
     * @return The fail over over provider, or null if the failed provider was
     *         the fail over provider.
     */
    public VideoChunkProvider createFailOverProvider(VideoChunkProvider failedProvider, String feedName)
    {
        VideoChunkProvider provider = null;
        if (!(failedProvider instanceof TranscodingChunkProvider))
        {
            provider = new TranscodingChunkProvider(failedProvider.getVideoStream(), failedProvider.getInputContainer(),
                    failedProvider.getStreamStart(), failedProvider.getApproxSizeMS(), feedName);
        }

        return provider;
    }

    /**
     * Builds the appropriate chunk provider based on the type of video that is
     * contained in the stream.
     *
     * @param videoStream The video stream.
     * @param streamStart The start timestamp of the video.
     * @param approxSizeMS The approximate size of chunks in milliseconds.
     * @param feedName The name of the video feed the user is accustomed to.
     * @return The newly create {@link VideoChunkProvider}.
     * @throws ChunkException If the specified video stream is invalid.
     */
    public VideoChunkProvider createProvider(CancellableInputStream videoStream, long streamStart, long approxSizeMS,
            String feedName) throws ChunkException
    {
        XugglerNativeUtilities.explodeXugglerNatives();
        IContainer inputContainer;
        inputContainer = IContainer.make();

        if (inputContainer.open(videoStream, null) < 0)
        {
            throw new ChunkException("Could not open video stream " + feedName);
        }

        IStreamCoder coder = null;
        for (int i = 0; i < inputContainer.getNumStreams(); ++i)
        {
            IStream stream = inputContainer.getStream(i);
            if (stream.getStreamCoder().getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO)
            {
                coder = stream.getStreamCoder();
                break;
            }
        }

        if (coder == null)
        {
            throw new ChunkException("Stream does not contain video.");
        }

        VideoChunkProvider chunkProvider = null;
        if ("h264".equalsIgnoreCase(coder.getCodec().getName()))
        {
            LOGGER.info("Transcoding video for " + feedName);
            chunkProvider = new TranscodingChunkProvider(videoStream, inputContainer, streamStart, approxSizeMS, feedName);
        }
        else
        {
            LOGGER.info("Re-encoding video for " + feedName);
            chunkProvider = new ReencodingChunkProvider(videoStream, inputContainer, streamStart, approxSizeMS);
        }

        return chunkProvider;
    }
}
