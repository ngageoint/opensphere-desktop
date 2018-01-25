package io.opensphere.osh.results.video;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import io.opensphere.core.Toolbox;
import io.opensphere.core.image.ImageIOImage;
import io.opensphere.core.model.time.TimeInstant;
import io.opensphere.core.model.time.TimeSpan;
import io.opensphere.core.util.collections.New;
import io.opensphere.core.util.io.CancellableInputStream;
import io.opensphere.core.util.lang.ThreadUtilities;
import io.opensphere.core.util.taskactivity.CancellableTaskActivity;
import io.opensphere.core.video.ExtendedVideoContentHandler;
import io.opensphere.core.video.VideoDecoder;
import io.opensphere.core.video.VideoDecoderException;
import io.opensphere.mantle.data.DataTypeInfo;
import io.opensphere.osh.util.OSHQuerier;

/** Video processor. */
public class VideoVideoProcessor extends AbstractVideoProcessor
{
    /** Logger reference. */
    private static final Logger LOGGER = Logger.getLogger(VideoVideoProcessor.class);

    /**
     * Constructor.
     *
     * @param toolbox The toolbox
     * @param querier The data registry querier
     */
    public VideoVideoProcessor(Toolbox toolbox, OSHQuerier querier)
    {
        super(toolbox, querier);
    }

    @Override
    public void processData(DataTypeInfo dataType, CancellableInputStream stream, CancellableTaskActivity ta,
            List<VideoFieldHandler> fieldHandlers)
        throws IOException
    {
        try (PipedOutputStream outStream = new PipedOutputStream())
        {
            InputStream inStream = new PipedInputStream(outStream);

            int fieldIndex = 0;
            final VideoData curVideoData = new VideoData();
            boolean eof = false;
            boolean first = true;
            VideoPositionTracker tracker = new VideoPositionTracker();
            while (!eof)
            {
                VideoFieldHandler handler = fieldHandlers.get(fieldIndex);
                if (handler != null)
                {
                    eof = handler.readField(stream, curVideoData);
                }

                fieldIndex = fieldIndex == fieldHandlers.size() - 1 ? 0 : fieldIndex + 1;

                if (curVideoData.hasData())
                {
                    if (first)
                    {
                        ThreadUtilities.runBackground(() -> decodeVideo(dataType, inStream, curVideoData.getTime(), tracker));
                        first = false;
                    }

                    tracker.addData(curVideoData.getData().length, curVideoData.getTime());

                    outStream.write(curVideoData.getData());
                    curVideoData.reset();
                }

                if (ta.isCancelled())
                {
                    stream.cancel();
                    break;
                }
            }
        }
    }

    /**
     * Decodes video.
     *
     * @param dataType the data type
     * @param inStream the input stream
     * @param startTime the video start time
     * @param tracker the video position tracker
     */
    private void decodeVideo(DataTypeInfo dataType, InputStream inStream, long startTime, VideoPositionTracker tracker)
    {
        Runnable emptyHandler = () ->
        {
        };
        try (CancellableInputStream cancellableStream = new CancellableInputStream(inStream, emptyHandler))
        {
            Iterator<VideoDecoder> decodersIterator = ServiceLoader.load(VideoDecoder.class).iterator();
            if (!decodersIterator.hasNext())
            {
                throw new IllegalStateException("Could not find a decoder for processing video.");
            }
            VideoDecoder videoDecoder = decodersIterator.next();
            videoDecoder.setInputStream(cancellableStream, TimeInstant.get(startTime));
            videoDecoder.registerVideoContentHandler(new OSHVideoContentHandler(dataType, tracker), null);
            videoDecoder.decode();
        }
        catch (IOException | VideoDecoderException e)
        {
            LOGGER.error(e, e);
        }
    }

    /** OSH VideoContentHandler. */
    private class OSHVideoContentHandler implements ExtendedVideoContentHandler<ImageIOImage>
    {
        /** The data type. */
        private final DataTypeInfo myDataType;

        /** The video position tracker. */
        private final VideoPositionTracker myTracker;

        /** The output stream for decoded images (reused). */
        private final ByteArrayOutputStream myImageStream = new ByteArrayOutputStream(1 << 17);

        /** List of queued frames. */
        private final List<VideoData> myQueuedData = New.linkedList();

        /**
         * Constructor.
         *
         * @param dataType The data type
         * @param tracker The video position tracker
         */
        public OSHVideoContentHandler(DataTypeInfo dataType, VideoPositionTracker tracker)
        {
            myDataType = dataType;
            myTracker = tracker;
        }

        @Override
        public void open()
        {
        }

        @Override
        public void close()
        {
        }

        @Override
        public void handleContent(ImageIOImage content, long ptsMS)
        {
        }

        @Override
        public void handleContentWithPosition(ImageIOImage image, long packetPosition)
        {
            // Process any queued data first
            if (!myQueuedData.isEmpty())
            {
                for (Iterator<VideoData> iter = myQueuedData.iterator(); iter.hasNext();)
                {
                    VideoData videoData = iter.next();
                    long aPacketPosition = videoData.getTime();
                    TimeSpan timeSpan = myTracker.getTimeSpan((int)aPacketPosition);
                    if (!timeSpan.isInstantaneous())
                    {
                        iter.remove();
                        videoData.setTime(timeSpan.getStart());
                        writeVideoData(myDataType, videoData, timeSpan.getEnd());
                    }
                }
            }

            TimeSpan timeSpan = myTracker.getTimeSpan((int)packetPosition);

            try
            {
                ImageIO.write(image.getAWTImage(), "jpg", myImageStream);
                byte[] imageBytes = myImageStream.toByteArray();
                myImageStream.reset();

                if (timeSpan.isInstantaneous())
                {
                    myQueuedData.add(new VideoData(packetPosition, imageBytes));
                }
                else
                {
                    writeVideoData(myDataType, new VideoData(timeSpan.getStart(), imageBytes), timeSpan.getEnd());
                }
            }
            catch (IOException e)
            {
                LOGGER.error(e, e);
            }

            image.dispose();
        }
    }
}
