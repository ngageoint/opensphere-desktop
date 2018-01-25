package io.opensphere.controlpanels.recording;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.math.Vector2i;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.server.HttpServer;
import io.opensphere.core.server.ResponseValues;
import io.opensphere.core.server.ServerProvider;
import io.opensphere.core.util.Constants;
import io.opensphere.core.util.RateMeter;
import io.opensphere.core.util.RateMeter.Callback;
import io.opensphere.core.util.concurrent.ProcrastinatingExecutor;
import io.opensphere.core.util.concurrent.SuppressableRejectedExecutionHandler;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.NamedThreadFactory;
import io.opensphere.core.util.net.UrlUtilities;
import io.opensphere.core.video.AbstractVideoContentHandler;
import io.opensphere.core.video.VideoContentHandler;
import io.opensphere.core.video.VideoDecoder;
import io.opensphere.core.video.VideoDecoderException;

/**
 * Abstract base for playing videos. This class handles reading the video
 * streams and getting the individual frames to be displayed.
 */
public abstract class AbstractVideoPlayer
{
    /** Executor for playing the video. */
    private static final ScheduledExecutorService EXECUTOR = ProcrastinatingExecutor.protect(new ScheduledThreadPoolExecutor(1,
            new NamedThreadFactory("VideoPlayerExecutor"), SuppressableRejectedExecutionHandler.getInstance()));

    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(AbstractVideoPlayer.class);

    /** Executor for playing the video. */
    private final ProcrastinatingExecutor myPlayExecutor;

    /**
     * Handler for video packets.
     */
    private final VideoContentHandler<? super ImageIOImage> myVideoContentHandler = new AbstractVideoContentHandler<ImageIOImage>()
    {
        @Override
        public void handleContent(ImageIOImage image, long pts)
        {
            if (image != null)
            {
                initializeMedium(image.getWidth(), image.getHeight());

                updateMedium(image.getAWTImage());
                image.dispose();
            }
        }
    };

    /** Constructor. */
    public AbstractVideoPlayer()
    {
        myPlayExecutor = new ProcrastinatingExecutor(EXECUTOR, 1);
    }

    /**
     * Play a video.
     *
     * @param filename The file which contains the video to play.
     * @param toolbox The toolbox.
     */
    public void playVideo(final String filename, final Toolbox toolbox)
    {
        Runnable runner = new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        doPlayVideo(filename, toolbox);
                        closeMedium();
                        break;
                    }
                    catch (VideoPlayerException e)
                    {
                        LOGGER.error("Error playing video from file: " + filename + ": " + e, e);
                        break;
                    }
                    catch (IOException e)
                    {
                        LOGGER.error(e, e);
                        break;
                    }
                    catch (GeneralSecurityException e)
                    {
                        LOGGER.error(e, e);
                        break;
                    }
                    catch (URISyntaxException e)
                    {
                        LOGGER.error(e, e);
                        break;
                    }
                }
            }
        };

        myPlayExecutor.execute(runner);
    }

    /**
     * Play a video at the given URL.
     *
     * @param urlString The URL.
     * @param toolbox The toolbox.
     * @throws VideoPlayerException the video player exception
     * @throws GeneralSecurityException the general security exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws URISyntaxException If the URL could not be converted to a uri.
     */
    protected void doPlayVideo(final String urlString, final Toolbox toolbox)
        throws VideoPlayerException, GeneralSecurityException, IOException, URISyntaxException
    {
        URL url = new URL(urlString);
        if (UrlUtilities.isFile(url))
        {
            Path path = Paths.get(url.toURI());
            try (InputStream is = new FileInputStream(path.toFile()))
            {
                doDecodeVideo(is, false);
            }
        }
        else
        {
            ServerProvider<HttpServer> provider = toolbox.getServerProviderRegistry().getProvider(HttpServer.class);
            HttpServer server = provider.getServer(url);

            ResponseValues response = new ResponseValues();
            try (InputStream is = server.sendGet(url, response))
            {
                if (response.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    doDecodeVideo(is, true);
                }
                else
                {
                    LOGGER.error("Server returned response " + response.getResponseCode() + " " + response.getResponseMessage());
                }
            }
        }
    }

    /**
     * Get a wrapper on an input stream that reports bandwidth usage.
     *
     * @param is The input stream.
     * @return The wrapped input stream.
     */
    protected InputStream getBandwidthWrapper(final InputStream is)
    {
        final RateMeter meter = new RateMeter(1, new Callback()
        {
            @Override
            public void rateSampled(double instant, double average)
            {
                LOGGER.info(String.format(" (%.2f Mbps)",
                        Double.valueOf(instant * Constants.BITS_PER_BYTE / Constants.BYTES_PER_MEGABYTE)));
            }
        });
        return new InputStream()
        {
            @Override
            public int read() throws IOException
            {
                meter.increment();
                return is.read();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException
            {
                meter.increment(len);
                int read = is.read(b, off, len);
                return read;
            }
        };
    }

    /** Close the medium. */
    abstract void closeMedium();

    /**
     * Initialize the medium for rendering.
     *
     * @param width Video pixel width.
     * @param height Video pixel height.
     */
    abstract void initializeMedium(int width, int height);

    /**
     * Render this from to the medium.
     *
     * @param image The frame to render.
     */
    abstract void updateMedium(BufferedImage image);

    /**
     * Decode a video stream.
     *
     * @param stream The stream to decode.
     * @param monitorBandwidth When true, monitor and report bandwidth usage.
     * @throws VideoPlayerException when the stream cannot be decoded.
     */
    private void doDecodeVideo(InputStream stream, boolean monitorBandwidth) throws VideoPlayerException
    {
        Iterator<VideoDecoder> decodersIterator = ServiceLoader.load(VideoDecoder.class).iterator();
        if (decodersIterator.hasNext())
        {
            VideoDecoder decoder = decodersIterator.next();
            decoder.registerVideoContentHandler(myVideoContentHandler, (Vector2i)null);
            try
            {
                InputStream is = monitorBandwidth ? getBandwidthWrapper(stream) : stream;
                decoder.setInputStream(new CancellableInputStream(is, (Runnable)null), TimeInstant.get());
                decoder.decode();
            }
            catch (VideoDecoderException e)
            {
                throw new VideoPlayerException(e.getMessage(), e);
            }
        }
    }

    /** An error during video playback. */
    protected static class VideoPlayerException extends Exception
    {
        /** Serial version UID. */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param message The error message.
         */
        public VideoPlayerException(String message)
        {
            super(message);
        }

        /**
         * Constructor.
         *
         * @param message The error message.
         * @param e The wrapped exception.
         */
        public VideoPlayerException(String message, VideoDecoderException e)
        {
            super(message, e);
        }
    }
}
