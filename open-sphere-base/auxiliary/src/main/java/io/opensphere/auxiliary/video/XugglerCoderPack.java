package io.opensphere.auxiliary.video;

import java.io.Closeable;
import java.io.InputStream;

import javax.annotation.concurrent.NotThreadSafe;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IError;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;

import io.opensphere.core.util.Utilities;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.video.VideoDecoderException;

/**
 * A utility class which contains the unified set of things required to decode
 * video.
 */
@NotThreadSafe
public class XugglerCoderPack implements Closeable
{
    /**
     * The container which provides raw data. This conainer's video coder will
     * not be used if a video coder from the original container is available,
     * but the video packets will still be extracted from here.
     */
    private IContainer myContainer;

    /** The stream which provides raw data. */
    private CancellableInputStream myInputStream;

    /** The stream id of the metadata. */
    private int myMetadataStreamId;

    /**
     * This is the first container to be created for this coder pack. In order
     * to re-use the video coder with multiple video data sources this is
     * maintained.
     */
    private IContainer myOrigContainer;

    /** The video coder which can decode video from the stream. */
    private IStreamCoder myVideoCoder;

    /** The stream id of the video. */
    private int myVideoStreamId;

    @Override
    public void close()
    {
        Utilities.close(XugglerUtilities.getCloseableWrapper(myVideoCoder), XugglerUtilities.getCloseableWrapper(myContainer),
                XugglerUtilities.getCloseableWrapper(myOrigContainer));
        myVideoCoder = null;
        myContainer = null;
        myOrigContainer = null;
    }

    /**
     * Get the container.
     *
     * @return the container.
     */
    public IContainer getContainer()
    {
        return myContainer;
    }

    /**
     * Get the metadataStreamId.
     *
     * @return the metadataStreamId.
     */
    public int getMetadataStreamId()
    {
        return myMetadataStreamId;
    }

    /**
     * Get the videoCoder.
     *
     * @return the videoCoder.
     */
    public IStreamCoder getVideoCoder()
    {
        return myVideoCoder;
    }

    /**
     * Get the videoStreamId.
     *
     * @return the videoStreamId.
     */
    public int getVideoStreamId()
    {
        return myVideoStreamId;
    }

    /**
     * Initialize the set of items required for decoding the given stream.
     *
     * @param is The stream to be decoded.
     * @throws VideoDecoderException If there is an error.
     */
    public void init(CancellableInputStream is) throws VideoDecoderException
    {
        IContainer container = getContainer(is);

        init(is, container);
    }

    /**
     * Initializes the coder pack with an already opened input container.
     *
     * @param is The stream containing the video data.
     * @param inputContainer The already opened {@link IContainer} reading from
     *            the stream.
     * @throws VideoDecoderException If we could not open a decoder.
     */
    public void init(CancellableInputStream is, IContainer inputContainer) throws VideoDecoderException
    {
        myInputStream = is;
        myContainer = inputContainer;

        int numStreams = myContainer.getNumStreams();
        for (int streamIndex = 0; streamIndex < numStreams; ++streamIndex)
        {
            IStream stream = myContainer.getStream(streamIndex);
            IStreamCoder coder = stream.getStreamCoder();
            if (coder.getCodecType() == com.xuggle.xuggler.ICodec.Type.CODEC_TYPE_VIDEO)
            {
                myVideoStreamId = streamIndex;
                myVideoCoder = coder;
            }
            else if (coder.getCodecType() == com.xuggle.xuggler.ICodec.Type.CODEC_TYPE_UNKNOWN)
            {
                myMetadataStreamId = streamIndex;
            }
        }

        if (myVideoCoder == null)
        {
            throw new VideoDecoderException("could not find video stream in container");
        }
        if (myVideoCoder.open(null, null) < 0)
        {
            throw new VideoDecoderException("could not open video decoder for container");
        }
    }

    /**
     * Initialize the set of items required for decoding the given stream.
     *
     * @param file The file to be decoded.
     * @throws VideoDecoderException If there is an error.
     */
    public void init(String file) throws VideoDecoderException
    {
        IContainer container = getContainer(file);

        init(file, container);
    }

    /**
     * Initializes the coder pack with an already opened input container.
     *
     * @param is The stream containing the video data.
     * @param inputContainer The already opened {@link IContainer} reading from
     *            the stream.
     * @throws VideoDecoderException If we could not open a decoder.
     */
    public void init(String is, IContainer inputContainer) throws VideoDecoderException
    {
        myContainer = inputContainer;

        int numStreams = myContainer.getNumStreams();
        for (int streamIndex = 0; streamIndex < numStreams; ++streamIndex)
        {
            IStream stream = myContainer.getStream(streamIndex);
            IStreamCoder coder = stream.getStreamCoder();
            if (coder.getCodecType() == com.xuggle.xuggler.ICodec.Type.CODEC_TYPE_VIDEO)
            {
                myVideoStreamId = streamIndex;
                myVideoCoder = coder;
            }
            else if (coder.getCodecType() == com.xuggle.xuggler.ICodec.Type.CODEC_TYPE_UNKNOWN)
            {
                myMetadataStreamId = streamIndex;
            }
        }

        if (myVideoCoder == null)
        {
            throw new VideoDecoderException("could not find video stream in container");
        }
        if (myVideoCoder.open(null, null) < 0)
        {
            throw new VideoDecoderException("could not open video decoder for container");
        }
    }

    /**
     * Tell whether this pack is valid for use in decoding.
     *
     * @return true when this pack may be used.
     */
    public boolean isPackValid()
    {
        return (myInputStream == null || !myInputStream.isCancelled()) && myVideoCoder != null && myContainer != null
                && myContainer.isOpened();
    }

    /**
     * Use a new stream as the source of the video data. Note: it is required
     * that this stream match the previous stream. For example, the video stream
     * id must be the same.
     *
     * @param stream The stream which is the source of the video data.
     * @throws VideoDecoderException If there is an error.
     */
    public void replaceStream(CancellableInputStream stream) throws VideoDecoderException
    {
        if (myContainer != null)
        {
            if (myOrigContainer == null)
            {
                myOrigContainer = myContainer;
            }
            else
            {
                myContainer.close();
            }
        }
        myContainer = getContainer(stream);
    }

    /**
     * Get the Xuggler container for the input stream.
     *
     * @param is The input stream.
     * @return The container.
     * @throws VideoDecoderException If there is an error.
     */
    protected IContainer getContainer(final InputStream is) throws VideoDecoderException
    {
        IContainer container;
        container = IContainer.make();
        int status = container.open(is, null);
        if (status < 0)
        {
            IError err = IError.make(status);
            throw new VideoDecoderException(new StringBuilder().append("Could not open container: ").append(err).toString());
        }
        return container;
    }

    /**
     * Get the Xuggler container for the input stream.
     *
     * @param is The input stream.
     * @return The container.
     * @throws VideoDecoderException If there is an error.
     */
    protected IContainer getContainer(final String is) throws VideoDecoderException
    {
        IContainer container;
        container = IContainer.make();
        int status = container.open(is, IContainer.Type.READ, null);
        if (status < 0)
        {
            IError err = IError.make(status);
            throw new VideoDecoderException(new StringBuilder().append("Could not open container: ").append(err).toString());
        }
        return container;
    }
}
